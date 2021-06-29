package NettyWebServer;

import Config.Config;
import Entities.MediaServerRequest;
import Entities.MediaServerResponse;
import MediaServer.MediaHandler;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.rabbitmq.client.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import org.json.JSONException;
import org.json.JSONObject;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class RequestHandler extends ChannelInboundHandlerAdapter {

    private HashMap<String, ChannelHandlerContext> uuid;
    private String RPC_QUEUE_REPLY_TO;
    private String RPC_QUEUE_SEND_TO;

    private Config config = Config.getInstance();
    private String serverHost = config.getServerQueueHost();
    private int serverPort = config.getServerQueuePort();
    private String serverUser = config.getServerQueueUserName();
    private String serverPass = config.getServerQueuePass();
    private Channel responseQueueChannel;
    private String responseConsumerTag;
    private Consumer responseConsumer;
    private String RESPONSE_QUEUE_NAME = "SERVER-RESPONSE";
    private final Logger LOGGER = Logger.getLogger(RequestHandler.class.getName()) ;
    private boolean isTesting = false;

  
    private Channel senderChannel;

    public RequestHandler(Channel channel, HashMap<String, ChannelHandlerContext> uuid, String RPC_QUEUE_REPLY_TO, String RPC_QUEUE_SEND_TO) {
        this.uuid = uuid;
        this.RPC_QUEUE_REPLY_TO = RPC_QUEUE_REPLY_TO;
        this.RPC_QUEUE_SEND_TO = RPC_QUEUE_SEND_TO;
    }

    public RequestHandler(Channel channel, HashMap<String, ChannelHandlerContext> uuid, String RPC_QUEUE_REPLY_TO, String RPC_QUEUE_SEND_TO,boolean isTesting) {
        this.uuid = uuid;
        this.RPC_QUEUE_REPLY_TO = RPC_QUEUE_REPLY_TO;
        this.RPC_QUEUE_SEND_TO = RPC_QUEUE_SEND_TO;
        this.isTesting = isTesting;
    }
    


    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) {

        if(!(o instanceof CompositeByteBuf) && !(o instanceof TextWebSocketFrame)&&!(o instanceof MediaServerRequest) ){
            channelHandlerContext.fireChannelRead(o);
            if(!isTesting) return;
        }
        ByteBuf buffer;
        JSONObject body;
        if(o instanceof TextWebSocketFrame) {
            buffer = (ByteBuf) (((TextWebSocketFrame)o).content());
            body = new JSONObject(buffer.toString(CharsetUtil.UTF_8));

        }
        else if(o instanceof MediaServerRequest) {
            body = ((MediaServerRequest)o).getRequest();
        }
        else {
            buffer = (ByteBuf) o;
            body = new JSONObject(buffer.toString(CharsetUtil.UTF_8));

        }
        String service = "";
        final JSONObject jsonRequest;
        String corrId = "";
        //try and catch
        try {

            if(o instanceof TextWebSocketFrame ){
                jsonRequest = new JSONObject();
                jsonRequest.put("Headers", new JSONObject());
                corrId = UUID.randomUUID().toString();
            }
            else {
                jsonRequest = (JSONObject) channelHandlerContext.channel().attr(AttributeKey.valueOf("REQUEST")).get();
                corrId = (String) channelHandlerContext.channel().attr(AttributeKey.valueOf("CORRID")).get();
            }
            jsonRequest.put("command", body.get("command"));
            service = (String) body.get("application");
            jsonRequest.put("application", service);
            jsonRequest.put("body", body);
            authenticate(channelHandlerContext, jsonRequest);
            if(o instanceof  MediaServerRequest){
                 MediaServerRequest msr= ((MediaServerRequest)o);
                 msr.setJsonRequest(jsonRequest.toString());
                 transmitMediaRequest(corrId,msr.getByteArray(),channelHandlerContext,service);
             }
             else{
                 transmitRequest(corrId,jsonRequest,channelHandlerContext,service);
                }
             if (o instanceof TextWebSocketFrame) {
                channelHandlerContext.fireChannelRead(o);
            }

        } catch (JSONException e ) {
            e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);
            String responseMessage = "NO JSON PROVIDED";
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.BAD_REQUEST,
                    copiedBuffer(responseMessage.getBytes()));
            channelHandlerContext.writeAndFlush(response);
        }
        System.out.println();
        System.out.println("CORRID: "+corrId);
        try {
            consumeResponse(channelHandlerContext, service + "-Response");
        }catch (IOException | TimeoutException e){
            e.printStackTrace();
        }
    }
    private void consumeResponse(ChannelHandlerContext ctx, String queue_name) throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(serverHost);
        connectionFactory.setPort(serverPort);
        connectionFactory.setUsername(serverUser);
        connectionFactory.setPassword(serverPass);
        Connection connection = null;
        Channel responseQueueChannel ;
        connection = connectionFactory.newConnection();
        responseQueueChannel = connection.createChannel();
        responseQueueChannel.queueDeclare(queue_name, true, false, false, null);
        responseQueueChannel.basicQos(5);
        LOGGER.log(Level.INFO," [x] Awaiting RPC RESPONSES on Queue : " + queue_name);
//        if(responseConsumerTag.length()>0)
//            responseQueueChannel.basicCancel(responseConsumerTag);
        Consumer responseConsumer = new DefaultConsumer(responseQueueChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                try {
                    //Using Reflection to convert a command String to its appropriate class
//                        Channel receiver = REQUEST_CHANNEL_MAP.get(RESPONSE_MAIN_QUEUE_NAME);

                    MediaServerResponse msr=MediaServerResponse.getObject(body);
                    if(msr!=null){   // If a download command
                        body=msr.getResponseJson().toString().getBytes("UTF-8");
                        String responseMsg = new String(body, StandardCharsets.UTF_8);

                        org.json.JSONObject responseJson = new org.json.JSONObject(responseMsg);
                        System.out.println(responseJson);
                        String status=responseJson.get("status").toString() ;
                        byte[] fileByteArray = msr.getFile();
                        File file=new File(msr.getFileName());

                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(file);
                            fos.write(fileByteArray);
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }


                        RandomAccessFile raf;

                        try {
                            raf = new RandomAccessFile(file, "r");
                        } catch (FileNotFoundException fnfe) {
                            return;
                        }

                        long fileLength = 0;
                        try {
                            fileLength = raf.length();
                        } catch (IOException ex) {
                            Logger.getLogger(MediaHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, mapToStatus(status));
                        org.json.JSONObject headers = (org.json.JSONObject) responseJson.get("Headers");
                        Iterator<String> keys = headers.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            String value = (String) headers.get(key);
                            response.headers().set(key, value);
                        }
                        HttpUtil.setContentLength(response, fileLength);
                        setContentTypeHeader(response,file);
                        ChannelHandlerContext ctx = NettyServerInitializer.getUuid().remove(properties.getCorrelationId());

                        // Write the initial line and the header.
                        ctx.write(response);
                        ChannelFuture sendFileFuture;
                        DefaultFileRegion defaultRegion = new DefaultFileRegion(raf.getChannel(), 0, fileLength);
                        sendFileFuture = ctx.write(defaultRegion);
                        // Write the end marker
                        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                        ctx.close();
                        // Write the content.

                        file.delete();
                    }
                    else{   // If a normal command's response
                        LOGGER.log(Level.INFO,"Responding to corrID: "+ properties.getCorrelationId() +  ", on Queue : " + queue_name);
                        LOGGER.log(Level.INFO,"Request    :   " + new String(body, "UTF-8"));
                        LOGGER.log(Level.INFO,"Application    :   " + queue_name);
                        String responseMsg = new String(body, StandardCharsets.UTF_8);

                        responseMsg = responseMsg.substring(responseMsg.indexOf("{"));

                        org.json.JSONObject responseJson = new org.json.JSONObject(responseMsg);
                        if(responseJson.getString("command").equals("UpdateChat")||responseJson.getString("command").equals("UploadMedia"))
                            return;
                        String status=responseJson.get("status").toString() ;

                        LOGGER.log(Level.INFO,"Application    :   " + responseJson + "12345678");
                        LOGGER.log(Level.INFO,"Application    :   " + responseMsg +  "12345678");

                        FullHttpResponse response = new DefaultFullHttpResponse(
                                HttpVersion.HTTP_1_1,
                                mapToStatus(status),
                                copiedBuffer(responseJson.get("response").toString().getBytes()));
                        org.json.JSONObject headers = (org.json.JSONObject) responseJson.get("Headers");
                        Iterator<String> keys = headers.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if(key.toLowerCase().contains("content")){
                                continue;
                            }
                            String value = (String) headers.get(key);
                            response.headers().set(key, value);
                        }
                        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
                        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                        response.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
                        //System.out.println(NettyServerInitializer.getUuid().remove(properties.getCorrelationId()));
                        System.out.println("Sending Response to User: "+response);


                        ctx.writeAndFlush(response);
                        ctx.close();
                      
                    }

                } catch (RuntimeException| IOException e) {
                    FullHttpResponse response = new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,
                            HttpResponseStatus.BAD_REQUEST,
                            copiedBuffer("ERROR".toString().getBytes()));

                    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
                    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

                    ctx.writeAndFlush(response);
                    ctx.close();
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    
                } finally {
                    synchronized (this) {
                        this.notify();
                    }
                    responseQueueChannel.basicCancel(consumerTag);
                }
            }
        };

        responseConsumerTag = responseQueueChannel.basicConsume(queue_name,true, responseConsumer);

    }
    private void transmitMediaRequest(String corrId, byte[] byteArray, ChannelHandlerContext ctx, String appName) {
        try {
            uuid.put(corrId,ctx);
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(RPC_QUEUE_REPLY_TO)
                    .build();
//            System.out.println("Sent   : " + jsonRequest.toString() + "to: " +appName+"-Request");
            LOGGER.log(Level.INFO,"Sent File to "+appName+"-Request");
//            System.out.println();

            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(serverHost);
            connectionFactory.setPort(serverPort);
            connectionFactory.setUsername(serverUser);
            connectionFactory.setPassword(serverPass);
            Connection connection = null;
            Channel channel ;
            try {
                connection = connectionFactory.newConnection();
                channel = connection.createChannel();

                channel.basicPublish("", appName + "-Request", props, byteArray);
            }catch(IOException | TimeoutException e) {
                e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);

                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }

        } catch (Exception e) {
            e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);
            LOGGER.log(Level.SEVERE,e.getMessage(),e);

        }
    }

    private void transmitRequest(String corrId, JSONObject jsonRequest, ChannelHandlerContext ctx,String appName){
        try {
            uuid.put(corrId,ctx);
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(RPC_QUEUE_REPLY_TO)
                    .build();
//            System.out.println("Sent   : " + jsonRequest.toString() + "to: " +appName+"-Request");
            LOGGER.log(Level.INFO,"Sent   : " + jsonRequest.toString() + "to: " +appName+"-Request");
//            System.out.println();

            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(serverHost);
            connectionFactory.setPort(serverPort);
            connectionFactory.setUsername(serverUser);
            connectionFactory.setPassword(serverPass);
            Connection connection = null;
            Channel channel ;
            try {
                connection = connectionFactory.newConnection();
                channel = connection.createChannel();
                channel.basicPublish("", appName + "-Request", props, jsonRequest.toString().getBytes());

            }catch(IOException | TimeoutException e) {

                e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);

                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }

        } catch (Exception e) {

            e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);
            LOGGER.log(Level.SEVERE,e.getMessage(),e);

        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        System.out.println("ALO ERORR");
        cause.printStackTrace();
        LOGGER.log(Level.SEVERE,cause.getMessage(),cause);
        ctx.writeAndFlush(new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                copiedBuffer(cause.getMessage().getBytes())
        ));
    }

    public void authenticate(ChannelHandlerContext channelHandlerContext, JSONObject jsonRequest) {
        if (jsonRequest.getString("command").equals("SignIn")
            || jsonRequest.getString("command").equals("SignUp")
            || jsonRequest.getString("command").equals("UpdateChat")
            || jsonRequest.getString("command").equals("UploadMedia")
            )
            return;
        try {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(jsonRequest.getJSONObject("Headers").getString("authorization"));
        } catch(JWTVerificationException | JSONException exception) {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("error", "Permission denied, you need to sign in");
            FullHttpResponse unauthorizedResponse = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.UNAUTHORIZED,
                    copiedBuffer(jsonResponse.toString().getBytes()));
            unauthorizedResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            unauthorizedResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, unauthorizedResponse.content().readableBytes());
            channelHandlerContext.writeAndFlush(unauthorizedResponse);
            channelHandlerContext.close();
        }
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
    private static HttpResponseStatus mapToStatus(String status){
        switch (status){
            case "_200":return HttpResponseStatus.OK;
            case "_404":return HttpResponseStatus.NOT_FOUND;
            case "_400":return HttpResponseStatus.BAD_REQUEST;
            case "_401":return HttpResponseStatus.UNAUTHORIZED;
            case "_500":return HttpResponseStatus.BAD_REQUEST;
            default:return HttpResponseStatus.ACCEPTED;
        }
    }
    private static void setContentTypeHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        mimeTypesMap.addMimeTypes("image png tif jpg jpeg bmp");
        mimeTypesMap.addMimeTypes("text/plain txt");
        mimeTypesMap.addMimeTypes("video/mp4 mp4");
        mimeTypesMap.addMimeTypes("application/pdf pdf");

        String mimeType = mimeTypesMap.getContentType(file);

        response.headers().set(CONTENT_TYPE, mimeType);
    }
}
