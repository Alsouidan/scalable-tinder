package Controller;

import Chat.TextWebSocketFrameHandler;
import Interface.ServiceControl;
import NettyWebServer.HTTPHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.HashMap;


public class ControllerAdapterInitializer extends ChannelInitializer<SocketChannel> {

    private ServiceControl service;
    public ControllerAdapterInitializer(ServiceControl service) {
        this.service= service;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin()
                .allowedRequestHeaders("X-Requested-With", "Content-Type", "Content-Length")
                .allowedRequestMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.OPTIONS)
                .build();
        ChannelPipeline p = channel.pipeline();
        p.addLast("decoder", new HttpRequestDecoder());
        p.addLast("encoder", new HttpResponseEncoder());
        p.addLast(new CorsHandler(corsConfig));
        p.addLast(new HttpObjectAggregator(65536));
        p.addLast(new HTTPHandler());
        p.addLast("handler", new ControllerAdapterHandler(service));
        p.addLast(new WebSocketServerProtocolHandler("/chat/update"));
        p.addLast(new TextWebSocketFrameHandler());
    }

}