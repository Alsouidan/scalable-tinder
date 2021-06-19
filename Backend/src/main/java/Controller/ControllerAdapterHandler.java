package Controller;

import Interface.ServiceControl;
import Entities.ControlMessage;
import Entities.ErrorLog;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.netty.buffer.Unpooled.copiedBuffer;

public class ControllerAdapterHandler extends ChannelInboundHandlerAdapter {

    private ServiceControl service ;
    private final Logger LOGGER = Logger.getLogger(ControllerAdapterHandler.class.getName()) ;

    public ControllerAdapterHandler(ServiceControl service) {
        this.service= service;
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

            String param = (String) body.get("param");
            String path = (String) body.get("path");
            jsonRequest.put("application", service_s);
            jsonRequest.put("body", body);
            String responseMessage = controlService(channelHandlerContext,service,(String)(body.get("command")),param,path);

//            Controller.sendResponse(channelHandlerContext,responseMessage,false);

        } catch (JSONException e) {
            e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);
            String responseMessage = "NO CORRECT JSON PROVIDED";
            Controller.sendResponse(channelHandlerContext,responseMessage,false);
        }
        
    }

    private String controlService(ChannelHandlerContext ctx,ServiceControl service,String command,String param,String path){
        String responseMessage = "";
        boolean result = false;
        try {
            switch (command) {
                case "set_max_db_connections_count":
                    result = service.setMaxDBConnections(param);
                    responseMessage = "MAX DB CONNECTIONS SET";
                    break;
                case "set_max_thread_count":
                    result = service.setMaxThreadsSize(Integer.parseInt(param));
                    responseMessage = "MAX THREAD COUNT SET";
                    break;
                case "continue":
                    result = service.resume();
                    responseMessage = "SERVICE RESUMED";
                    break;
                case "freeze":
                    result = service.freeze();

                    responseMessage = "SERVICE FROZE";
                    break;
                case "add_command":
                    result = service.add_command(param, path);
                    responseMessage = "COMMAND ADDED";
                    break;
                case "delete_command":
                    result = service.delete_command(param);
                    responseMessage = "COMMAND DELETED";
                    break;
                case "set_error_reporting_level":
                    result = service.set_log_level(param);
                    responseMessage = "LOG LEVEL UPDATED";
                    break;
                case "update_command":
                    result = service.update_command(param, path);
                    responseMessage = "COMMAND UPDATED";
                    break;
                case "update_class":
                    result = service.update_class(param, path);
                    responseMessage = "Class UPDATED";
                    break;

                default: {
                    responseMessage = "Unknown Command";

                    break;
                }
            }
            Controller.sendResponse(ctx,result?responseMessage:"ERROR",!result);

        }catch(Exception e){
            e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Controller.sendResponse(ctx,"ERROR",true);
        }

        return responseMessage +" Instance ID: "+service.ID;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext arg0) {

    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext arg0) {

    }

}