package NettyWebServer;

import Config.Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import Database.PostgreSQL;
import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NettyServer {

    Config c = Config.getInstance();
    private int port = c.getWebServerPort();
    public final Logger LOGGER = Logger.getLogger(NettyServer.class.getName()) ;
    public NettyServer(int port){
        this.port = port;
    }
    public void start() {

        EventLoopGroup bossGroup = new NioEventLoopGroup(4);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
//                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new NettyServerInitializer(port));
//            b.option(ChannelOption.SO_KEEPALIVE, true);
            Channel ch = b.bind(port).sync().channel();


//            System.err.println("Web Server is listening on http://127.0.0.1:" + port + '/');
            LOGGER.log(Level.INFO,"Web Server is listening on http://127.0.0.1:" + port + '/');
            Boolean isPopulate = c.getPostgresqlPopulate();
            if(isPopulate){
                PostgreSQL db = new PostgreSQL();
                db.populateDB();
            }
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

    public static void main(String[] args) {
        int[] ports = new int[]{8080};
        for(int port :ports){
            NettyServer s = new NettyServer(port);
            new Thread(() -> {
                s.start();
            }).start();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();s.LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        }
      
    }
}
