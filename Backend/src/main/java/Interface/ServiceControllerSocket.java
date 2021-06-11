package Interface;

import Config.Config;
import Interface.ServiceControl;
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

        EventLoopGroup group = new NioEventLoopGroup();

        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.localAddress(new InetSocketAddress("localhost", port));

            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(service);
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            System.out.println("SOCKET OF SERVICE:"+service.getClass().getName()+" IS RUNNING ON PORT:"+port);
            channelFuture.channel().closeFuture().sync();
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            group.shutdownGracefully().sync();
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
