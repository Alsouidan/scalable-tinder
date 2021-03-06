package NettyWebServer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;


import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HTTPHandler extends ChannelInboundHandlerAdapter {
    private HttpRequest request;
    private final Logger LOGGER = Logger.getLogger(HTTPHandler.class.getName()) ;

    public void channelReadComplete(ChannelHandlerContext ctx) {

        ctx.flush();
        ctx.fireChannelReadComplete();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        JSONObject fullRequest = new JSONObject();
        if(msg instanceof HttpServletRequest){
            HttpServletRequest req=(HttpServletRequest) msg;
        }
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;
            if (request.uri().contains("/chat/update")) {
                ctx.fireChannelRead(((FullHttpRequest)request).retain());
                return;
            }
            if (HttpHeaders.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }
            fullRequest.put("Version",request.protocolVersion());
            fullRequest.put("Hostname",request.headers().get(HttpHeaderNames.HOST, "unknown"));
            fullRequest.put("Uri",request.uri());

            HttpHeaders headers = request.headers();
            JSONObject headerJson = new JSONObject();
            if (!headers.isEmpty()) {
                for (Map.Entry<String, String> h: headers) {
                    CharSequence key = h.getKey();
                    CharSequence value = h.getValue();
                    headerJson.put(key.toString(), value.toString());
                }
            }

            fullRequest.put("Headers", headerJson);

            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
            Map<String, List<String>> params = queryStringDecoder.parameters();
            JSONObject paramJson = new JSONObject();
            if (!params.isEmpty()) {
                for (Map.Entry<String, List<String>> p: params.entrySet()) {
                    String key = p.getKey();
                    List<String> vals = p.getValue();
                    for (String val : vals) {
                        paramJson.put(key, val);
                    }
                }
            }
            fullRequest.put("Parameters",paramJson);
            String requestId = UUID.randomUUID().toString();
            
            ctx.channel().attr(AttributeKey.valueOf("REQUEST")).set(fullRequest);
            ctx.channel().attr(AttributeKey.valueOf("CORRID")).set(requestId);

        }

        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf content = httpContent.content();
            if(msg.toString().contains("HEAD") || request.uri().contains("/file/upload")) {
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        OK,
                        copiedBuffer("ACK".getBytes()));
                ctx.writeAndFlush(response);
                if (request.uri().contains("/file/upload")) {
                    ctx.fireChannelRead(((FullHttpRequest)request).retain());
//                    ctx.fireChannelRead(content);
                }
            }else{
                    ctx.fireChannelRead(content);
            }
        }
        if (msg instanceof LastHttpContent) {
//            LastHttpContent trailer = (LastHttpContent) msg;
            HttpObject trailer = (HttpObject) msg;
//            if (!writeResponse(trailer, ctx)) {
//                // If keep-alive is off, close the connection once the content is fully written.
//                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
//            }
        }
    }


    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, currentObj.decoderResult().isSuccess()? OK : BAD_REQUEST,
                Unpooled.copiedBuffer("Not KeepAlive", CharsetUtil.UTF_8));

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        // Write the response.
        ctx.write(response);

        return keepAlive;
    }


    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                CONTINUE);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        cause.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, OK,
                Unpooled.copiedBuffer("Not KeepAlive", CharsetUtil.UTF_8));
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        // Add keep alive header as per:
        // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        ctx.write(response);
//        ctx.close();
    }
    public static String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            // As of https://en.wikipedia.org/wiki/X-Forwarded-For
            // The general format of the field is: X-Forwarded-For: client, proxy1, proxy2 ...
            // we only want the client
            return new StringTokenizer(xForwardedForHeader, ",").nextToken().trim();
        }
    }
}

