package Entities;


import io.netty.channel.ChannelHandlerContext;
import org.json.JSONObject;

import java.io.*;

public class ServerRequest implements Serializable {

    private ChannelHandlerContext ctx;
    private String jsonRequest;


    public ServerRequest(ChannelHandlerContext ctx, String jsonRequest) {
        this.jsonRequest = jsonRequest;
        this.ctx=ctx;
    }

    public ServerRequest() {
    }




    public byte[] getByteArray(){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            byte[] objectBytes = bos.toByteArray();
            return objectBytes;
        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            try {
                bos.close();
            } catch (IOException ex) {
//                ex.printStackTrace();
            }
        }
        return null ;
    }
    public static ServerRequest getObject(byte[] objectBytes){
        ByteArrayInputStream bis = new ByteArrayInputStream(objectBytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            Object object = in.readObject();
            return (ServerRequest) object;

        }
        catch (Exception e) {

        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return null;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public JSONObject getJsonRequest() {
        return new JSONObject(jsonRequest);
    }

    public void setJsonRequest(String jsonRequest) {
        this.jsonRequest = jsonRequest;
    }
}

