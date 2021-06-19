package NettyWebServer;

import Config.*;
import Chat.TextWebSocketFrameHandler;
import MediaServer.*;
import com.rabbitmq.client.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.json.JSONObject;


import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.netty.buffer.Unpooled.copiedBuffer;

public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private Config config = Config.getInstance();



    private static HashMap<String, ChannelHandlerContext> uuid = new HashMap<String, ChannelHandlerContext>();
    private Channel receiverChannel;
    private Channel senderChannel;

    private String loadBalancerHost = config.getLoadBalancerQueueHost();
    private int loadBalancerPort = config.getLoadBalancerQueuePort();
    private String loadBalancerUser = config.getLoadBalancerQueueUserName();                  
    private String loadBalancerPass = config.getLoadBalancerQueuePass();
    private String RPC_QUEUE_SEND_TO = config.getLoadBalancerQueueName();

    private String serverHost = config.getServerQueueHost();
    private int serverPort = config.getServerQueuePort();
    private String serverUser = config.getServerQueueUserName();
    private String serverPass = config.getServerQueuePass();
    private String RPC_QUEUE_REPLY_TO = config.getServerQueueName();
    private final Logger LOGGER = Logger.getLogger(NettyServerInitializer.class.getName()) ;

    public NettyServerInitializer(int port) {
//        establishLoadBalancerConnection();
//        establishServerConnection();
//        serverQueue();
    }
    public static HashMap<String, ChannelHandlerContext> getUuid() {
        return uuid;
    }

    @Override
    protected void initChannel(SocketChannel arg0) {

        CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin()
                .allowedRequestHeaders("X-Requested-With", "Content-Type", "Content-Length")
                .allowedRequestMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.OPTIONS,HttpMethod.HEAD)
                .build();
        EventExecutorGroup exec = new DefaultEventExecutorGroup(20);
        ChannelPipeline p = arg0.pipeline();
        p.addLast("decoder", new HttpRequestDecoder());
        p.addLast("encoder", new HttpResponseEncoder());
        p.addLast(new CorsHandler(corsConfig));
        p.addLast(new HttpObjectAggregator(10000000));

        p.addLast(new HTTPHandler());
        p.addLast(new MediaHandler());
        p.addLast(exec, new NettyWebServer.RequestHandler(senderChannel, uuid, RPC_QUEUE_REPLY_TO, RPC_QUEUE_SEND_TO));
        //pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        p.addLast(new WebSocketServerProtocolHandler("/chat/update"));
        p.addLast(new TextWebSocketFrameHandler());

    }

    private void establishLoadBalancerConnection() {
        ConnectionFactory loadBalancerFactory = new ConnectionFactory();
        loadBalancerFactory.setHost(loadBalancerHost);
        loadBalancerFactory.setPort(loadBalancerPort);
        loadBalancerFactory.setUsername(loadBalancerUser);
        loadBalancerFactory.setPassword(loadBalancerPass);
        Connection connection;
        try {
            connection = loadBalancerFactory.newConnection();
            senderChannel = connection.createChannel();
            senderChannel.queueDeclare(RPC_QUEUE_SEND_TO, false, false, false, null);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    private void establishServerConnection() {
        ConnectionFactory serverFactory = new ConnectionFactory();
        serverFactory.setHost(serverHost);
        serverFactory.setPort(serverPort);
        serverFactory.setUsername(serverUser);
        serverFactory.setPassword(serverPass);
        Connection connection;
        try {
            connection = serverFactory.newConnection();
            connection = serverFactory.newConnection();
            senderChannel = connection.createChannel();
            senderChannel.queueDeclare(RPC_QUEUE_SEND_TO, false, false, false, null);
            receiverChannel = connection.createChannel();
            receiverChannel.queueDeclare(RPC_QUEUE_REPLY_TO, false, false, false, null);
            receiverChannel.basicQos(4);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    private void serverQueue() {
        System.out.println(" [x] Awaiting RPC responses on Queue : " + RPC_QUEUE_REPLY_TO);
        try {
            Consumer consumer = new DefaultConsumer(receiverChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                    try {
                        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                                .Builder()
                                .correlationId(properties.getCorrelationId())
                                .build();
                        System.out.println("Responding to corrID: " + properties.getCorrelationId() +  ", on Queue : " + RPC_QUEUE_REPLY_TO);
                        String responseMsg = new String(body, "UTF-8");

                        JSONObject responseJson = new JSONObject(responseMsg);
                        FullHttpResponse response = new DefaultFullHttpResponse(
                                HttpVersion.HTTP_1_1,
                                HttpResponseStatus.OK,
                                copiedBuffer(responseJson.get("response").toString().getBytes()));

                        JSONObject headers = (JSONObject) responseJson.get("Headers");
                        System.out.println("Headers are "+headers);
                        Iterator<String> keys = headers.keys();

                        while (keys.hasNext()) {
                            String key = keys.next();
                            String value = (String) headers.get(key);
                            response.headers().set(key, value);
                        }

                        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
                        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

                        System.out.println("Response   :   " + responseJson.get("response"));
                        System.out.println();

                        ChannelHandlerContext ctxRec = uuid.remove(properties.getCorrelationId());
                        ctxRec.writeAndFlush(response);
                        ctxRec.close();
                    } catch (Exception e) {
                        e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        serverQueue();
                    }
                }
            };
            receiverChannel.basicConsume(RPC_QUEUE_REPLY_TO, true, consumer);
        } catch (Exception e) {
            e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);
//            serverQueue();
        }
    }
}
