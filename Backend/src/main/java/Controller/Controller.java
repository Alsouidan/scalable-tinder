package Controller;

import Config.Config;
import Interface.ServiceControl;
import Entities.ControlCommand;
import Entities.ControlMessage;
import Entities.ErrorLog;
import Entities.ServicesType;
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

import static io.netty.buffer.Unpooled.copiedBuffer;

public class Controller {

    public static Channel channel;
    Config conf = Config.getInstance();
    private String server = conf.getControllerHost();
    private int port = conf.getControllerPort();
    private HashMap<String, HashMap<String,ServiceControl>>  availableServices = new HashMap<>();
    private HashMap<String,Integer> instancesCounts = new HashMap<String, Integer>();
//    public static void main(String[] args) {
//        Client c = new Client();
//        c.initService(ServicesType.post);
//        new Thread(() -> {
//            c.start();
//        }).start();
//        c.startService();
//    }
    public Controller(){
        availableServices.putIfAbsent(conf.getServicesMqUserQueue(),new HashMap<>());
        availableServices.putIfAbsent(conf.getServicesMqModeratorQueue(),new HashMap<>());
        availableServices.putIfAbsent(conf.getServicesMqUserToUserQueue(),new HashMap<>());
        availableServices.putIfAbsent(conf.getServicesMqChatQueue(),new HashMap<>());
        instancesCounts.putIfAbsent(conf.getServicesMqUserQueue(),0);
        instancesCounts.putIfAbsent(conf.getServicesMqUserToUserQueue(),0);
        instancesCounts.putIfAbsent(conf.getServicesMqModeratorQueue(),0);
        instancesCounts.putIfAbsent(conf.getServicesMqChatQueue(),0);
    }
    public void initDBs(){
//        for(String service : availableServices.keySet()){
//            availableServices.get(service).initDB();
//        }
    }
    public static void sendResponse(ChannelHandlerContext ctx,String responseMsg){
        JsonParser jsonParser = new JsonParser();
        JSONObject responseJ= new JSONObject();
        responseJ.put("Message",responseMsg);
        JsonObject responseJson =(JsonObject) jsonParser.parse(responseJ.toString());
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                copiedBuffer(responseJson.get("Message").toString().getBytes()));

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response);
    }
    public ServiceControl initService(ServicesType serviceName) {
        ServiceControl service = null;
        switch (serviceName) {
            case user: {

                
                int newId = instancesCounts.get(conf.getServicesMqUserQueue()) + 1;
                service = new UserService(newId);
                instancesCounts.replace(conf.getServicesMqUserQueue(), newId);
                availableServices.get(conf.getServicesMqUserQueue()).putIfAbsent(newId + "", service);
                System.out.println("INSTANCE "+newId+" OF SERVICE "+serviceName+" IS RUNNING");

                break;
            }
            case user_to_user: {

//                this.serviceName = conf.getServicesMqUserToUserQueue();
                int newId = instancesCounts.get(conf.getServicesMqUserToUserQueue()) + 1;
                service = new UserToUserService(newId);
                instancesCounts.replace(conf.getServicesMqUserToUserQueue(), newId);
                availableServices.get(conf.getServicesMqUserToUserQueue()).putIfAbsent(newId + "", service);
                System.out.println("INSTANCE "+newId+" OF SERVICE "+serviceName+" IS RUNNING");
                break;
            }
            case moderator: {

//                this.serviceName = conf.getServicesMqModeratorQueue();
                int newId = instancesCounts.get(conf.getServicesMqModeratorQueue()) + 1;
                service = new ModeratorService(newId);
                instancesCounts.replace(conf.getServicesMqModeratorQueue(), newId);
                availableServices.get(conf.getServicesMqModeratorQueue()).putIfAbsent(newId + "", service);
                System.out.println("INSTANCE "+newId+" OF SERVICE "+serviceName+" IS RUNNING");
                break;
            }
            case chat:
//                this.serviceName = conf.getServicesMqChatQueue();
                int newId = instancesCounts.get(conf.getServicesMqChatQueue()) + 1;
                service = new ChatService(newId);
                instancesCounts.replace(conf.getServicesMqChatQueue(), newId);
                availableServices.get(conf.getServicesMqChatQueue()).putIfAbsent(newId + "", service);
                System.out.println("INSTANCE "+newId+" OF SERVICE "+serviceName+" IS RUNNING");
                break;
            // TODO ADD SERVICE
        }
        return service;
    }

    public void startServices() {
//        for(String service : availableServices.keySet()){
//            availableServices.get(service).start();
//        }
    }

    public void start() {

        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
//                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ControllerAdapterInitializer(availableServices));
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
            e.printStackTrace();
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