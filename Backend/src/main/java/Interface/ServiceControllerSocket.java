package Interface;

import Config.Config;
import Interface.ServiceControl;
import MediaServer.MediaServerInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceControllerSocket {

    Config c = Config.getInstance();
    private int port = c.getWebServerPort();
    private ServiceControl service;
    public final Logger LOGGER = Logger.getLogger(ServiceControllerSocket.class.getName()) ;
    public ServiceControllerSocket(int port, ServiceControl service){
        this.port = port;
        this.service = service;
    }
    public void start() throws InterruptedException {

        EventLoopGroup bossGroup = new NioEventLoopGroup(4);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
//                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new HttpObjectAggregator(1000000),service);
                        }});
//            b.option(ChannelOption.SO_KEEPALIVE, true);
            Channel ch = b.bind(port).sync().channel();

//            System.err.println("Web Server is listening on http://127.0.0.1:" + port + '/');
            LOGGER.log(Level.INFO,"ServiceServer is listening on http://127.0.0.1:" + port + '/');
            ch.closeFuture().sync();

        }
        catch (InterruptedException e) {
            System.out.println("ALO");
            e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    
//    public static void main(String[] args) {
//        int[] ports = new int[]{8080};
//        for(int port :ports){
//            NettyServer s = new NettyServer(port);
//            new Thread(() -> {
//                s.start();
//            }).start();
//            try {
//                Thread.sleep(200);
//            } catch (InterruptedException e) {
//                e.printStackTrace();s.LOGGER.log(Level.SEVERE,e.getMessage(),e);
//            }
//        }
//    }
}
