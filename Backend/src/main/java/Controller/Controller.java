package Controller;

import Config.Config;
import Interface.ServiceControl;
import Entities.ControlCommand;
import Entities.ControlMessage;
import Entities.ErrorLog;
import Entities.ServicesType;
import Logger.MyLogger;
import NettyWebServer.NettyServerInitializer;
import Services.ChatService;
import Services.UserToUserService;
import Services.UserService;
import Services.ModeratorService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.netty.buffer.Unpooled.copiedBuffer;

public class Controller implements Runnable{

    public static Channel channel;
    Config conf = Config.getInstance();
    private String server = conf.getControllerHost();
    private ServiceControl service;
    private int port;
    public static final Logger LOGGER = Logger.getLogger(Controller.class.getName()) ;

    //    public static void main(String[] args) {
//        Client c = new Client();
//        c.initService(ServicesType.post);
//        new Thread(() -> {
//            c.start();
//        }).start();
//        c.startService();
//    }
    public Controller(ServiceControl service, int port){
        this.service = service;
        this.port = port;
        MyLogger main_logger = new MyLogger();
        main_logger.initialize();
    }
    public void initDBs(){
//        for(String service : availableServices.keySet()){
//            availableServices.get(service).initDB();
//        }
    }
    public static void sendResponse(ChannelHandlerContext ctx,String responseMsg,boolean isError){
        JsonParser jsonParser = new JsonParser();
        JSONObject responseJ= new JSONObject();
        Controller.LOGGER.log(Level.INFO,responseMsg);
        responseJ.put("Message",responseMsg);
        JsonObject responseJson =(JsonObject) jsonParser.parse(responseJ.toString());
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                isError?HttpResponseStatus.BAD_REQUEST:HttpResponseStatus.OK,
                copiedBuffer(responseJson.get("Message").toString().getBytes()));

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response);
    }
//    public ServiceControl initService(ServicesType serviceName) {
//        ServiceControl service = null;
//        switch (serviceName) {
//            case user: {
//
//
//                int newId = instancesCounts.get(conf.getServicesMqUserQueue()) + 1;
//                service = new UserService(newId);
//                instancesCounts.replace(conf.getServicesMqUserQueue(), newId);
//                availableServices.get(conf.getServicesMqUserQueue()).putIfAbsent(newId + "", service);
//                System.out.println("INSTANCE "+newId+" OF SERVICE "+serviceName+" IS RUNNING");
//
//                break;
//            }
//            case user_to_user: {
//
////                this.serviceName = conf.getServicesMqUserToUserQueue();
//                int newId = instancesCounts.get(conf.getServicesMqUserToUserQueue()) + 1;
//                service = new UserToUserService(newId);
//                instancesCounts.replace(conf.getServicesMqUserToUserQueue(), newId);
//                availableServices.get(conf.getServicesMqUserToUserQueue()).putIfAbsent(newId + "", service);
//                System.out.println("INSTANCE "+newId+" OF SERVICE "+serviceName+" IS RUNNING");
//                break;
//            }
//            case moderator: {
//
////                this.serviceName = conf.getServicesMqModeratorQueue();
//                int newId = instancesCounts.get(conf.getServicesMqModeratorQueue()) + 1;
//                service = new ModeratorService(newId);
//                instancesCounts.replace(conf.getServicesMqModeratorQueue(), newId);
//                availableServices.get(conf.getServicesMqModeratorQueue()).putIfAbsent(newId + "", service);
//                System.out.println("INSTANCE "+newId+" OF SERVICE "+serviceName+" IS RUNNING");
//                break;
//            }
//            case chat:
////                this.serviceName = conf.getServicesMqChatQueue();
//                int newId = instancesCounts.get(conf.getServicesMqChatQueue()) + 1;
//                service = new ChatService(newId);
//                instancesCounts.replace(conf.getServicesMqChatQueue(), newId);
//                availableServices.get(conf.getServicesMqChatQueue()).putIfAbsent(newId + "", service);
//                System.out.println("INSTANCE "+newId+" OF SERVICE "+serviceName+" IS RUNNING");
//                break;
//            // TODO ADD SERVICE
//        }
//        return service;
//    }

    public void startServices() {
//        for(String service : availableServices.keySet()){
//            availableServices.get(service).start();
//        }
    }

    public void run() {

        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
//                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ControllerAdapterInitializer(service));
//            b.option(ChannelOption.SO_KEEPALIVE, true);
            Channel ch = b.bind(port).sync().channel();
            Controller.channel = ch;
            System.err.println("Controller is listening on http://127.0.0.1:" + port + '/');
//            ch.writeAndFlush(new ControlMessage(ControlCommand.initialize, serviceName));
            ch.closeFuture().sync();

//            Thread t = new Thread(() -> {
//                Scanner sc = new Scanner(System.in);
//                while (true){
//                    String line = sc.nextLine();
//                    ErrorLog l = new ErrorLog(LogLevel.ERROR, line);
//                    Client.channel.writeAndFlush(l);
//                }
//            });
//            t.start();

//            Controller.channel.writeAndFlush(new ControlMessage(ControlCommand.initialize, serviceName));
//            Controller.channel.closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Controller.channel.writeAndFlush(new ErrorLog(LogLevel.ERROR,errors.toString()));
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
//            System.exit(0);
        }
    }
}