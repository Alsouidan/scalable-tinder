package Controller;

import Interface.ServiceControl;
import Entities.ControlMessage;
import Entities.ErrorLog;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.netty.buffer.Unpooled.copiedBuffer;

public class ControllerAdapterHandler extends ChannelInboundHandlerAdapter {

    private HashMap<String, HashMap<String,Integer>> availableServices ;
    private final Logger LOGGER = Logger.getLogger(ControllerAdapterHandler.class.getName()) ;

    public ControllerAdapterHandler(HashMap<String, HashMap<String,Integer>> availableServices) {
        this.availableServices= availableServices;
    }

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object arg1) {
        ByteBuf buffer = (ByteBuf) arg1;

        //try and catch
        try {
            JSONObject body = new JSONObject(buffer.toString(CharsetUtil.UTF_8));

            final JSONObject jsonRequest = (JSONObject) channelHandlerContext.channel().attr(AttributeKey.valueOf("REQUEST")).get();
            final String corrId = (String) channelHandlerContext.channel().attr(AttributeKey.valueOf("CORRID")).get();
            jsonRequest.put("command", body.get("command"));
            String service_s = (String) body.get("application");
            String instance = (String) body.get("instance_num");
            String param = (String) body.get("param");
            String path = (String) body.get("path");
            jsonRequest.put("application", service_s);
            jsonRequest.put("body", body);
            int port = availableServices.get(service_s).get(instance);
            new Thread(() -> {
                try {
                    controlService(channelHandlerContext,port,(String)(body.get("command")),param,path);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }).start();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            Controller.sendResponse(channelHandlerContext,responseMessage,false);

        } catch (JSONException e) {
            e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);
            String responseMessage = "NO CORRECT JSON PROVIDED";
            Controller.sendResponse(channelHandlerContext,responseMessage,false);
        }
        
    }

    private void controlService(ChannelHandlerContext ctx,int port,String command,String param,String path) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap clientBootstrap = new Bootstrap();

            clientBootstrap.group(group);
            clientBootstrap.channel(NioSocketChannel.class);
            System.out.println("PORT TO SEND TO:"+port);
            clientBootstrap.remoteAddress(new InetSocketAddress("localhost", port));
            clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new StringDecoder(),new StringEncoder(),new ChannelInboundHandlerAdapter(){
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            Controller.sendResponse(ctx,(String)msg,false);
                        }
                    });
                }
            });
            ChannelFuture channelFuture = clientBootstrap.connect("localhost",port).sync();
            Channel c = channelFuture.channel();
            System.out.println("CONNECTED TO SERVICE");
            c.writeAndFlush(new ControlMessage(command,param,path));
            System.out.print(c.isActive());
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully().sync();
        }


    }

    @Override
    public void channelReadComplete(ChannelHandlerContext arg0) {

    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext arg0) {

    }

}