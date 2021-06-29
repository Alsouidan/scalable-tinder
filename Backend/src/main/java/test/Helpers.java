package test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

public class Helpers {
    TestServer server;
    EmbeddedChannel channel;
    String token;


    public Helpers(){
        server = new TestServer();
        channel = server.getChannel();
        Algorithm algorithm = Algorithm.HMAC256("secret");
        token = JWT.create()
                .withClaim("moderatorId", 16)
                .withIssuer("auth0")
                .sign(algorithm);
    }

    public TestServer getTestServer(){
        return server;
    }

    public EmbeddedChannel getEmbbeddedChannel(){
        return channel;
    }

    @Test
    @DisplayName("Request Sent")
    public void requestSent(FullHttpRequest request){
        System.out.println("INISSENT IS: ");
        Boolean isSent = channel.writeInbound(request);
        System.out.println("ISSENT IS: "+isSent);
        assertTrue(isSent);
    }

    @Test
    @DisplayName("Response Received")
    public void responseReceived(){
        int messageSize = channel.outboundMessages().size();
        assertNotEquals(messageSize,0);
    }

    public void sendRequest(String commandName, String application, JSONObject body){
        body.put("command",commandName);
        body.put("application",application);
        ByteBuf bbuf = Unpooled.copiedBuffer( body.toString(), StandardCharsets.UTF_8);
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.POST, "http://127.0.0.1:8020",bbuf);
        request.headers().add("Content-Type","application/json");
        request.headers().add("authorization",token);
        requestSent(request);
        await().atMost(5, TimeUnit.SECONDS).until(threadsFinished());
        responseReceived();

    }

    private Callable<Boolean> threadsFinished() {
        return new Callable<Boolean>() {
            
            public Boolean call(){
                return channel.outboundMessages().size() >0;
            }
        };
    }

    public void sendRequest(String commandName, String application, JSONObject body,boolean sendToken){
        body.put("command",commandName);
        body.put("application",application);
        ByteBuf bbuf = Unpooled.copiedBuffer( body.toString(), StandardCharsets.UTF_8);
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.POST, "http://127.0.0.1:8020",bbuf);
        request.headers().add("Content-Type","application/json");
        requestSent(request);
        responseReceived();

    }
    @Test
    @DisplayName("Reading and comparing HTTP status")
    private JSONObject readResponse(int status){
        FullHttpResponse httpResponse = channel.readOutbound();
        HttpResponseStatus received = httpResponse.status();
        String httpResponseContent = httpResponse.content().toString(Charset.defaultCharset());
        System.out.println(httpResponseContent + "CONTENT");
        assertEquals(received.code(),status);
        return new JSONObject(httpResponseContent);
    }

    @Test
    @DisplayName("Compare Responses")
    public void compareResponses(JSONObject response,JSONObject expected, boolean strictMatch){
         Iterator<String> expectedKeys = expected.keys();
         int expectedKeyLength = 0;
         int receivedKeyLength = 0;
         while(expectedKeys.hasNext()){
             expectedKeyLength +=1;
             String expectedKey = expectedKeys.next();
             System.out.println("Comparing: "+ expectedKey);
             System.out.println(response + "KEYYY");
             assertTrue(response.has(expectedKey));
             Object expectedValue = expected.get(expectedKey);
             Object receivedValue = response.get(expectedKey);
             assertTrue(expectedValue.equals((receivedValue)));

         }
        if(strictMatch) {
            Iterator<String> receivedKeys = response.keys();
            while(receivedKeys.hasNext()){
                receivedKeys.next();
                receivedKeyLength +=1;
            }
            assertEquals(expectedKeyLength,receivedKeyLength);
        }
    }


    public JSONObject testCommand(String commandName,String application,String outputName,JSONObject body,JSONObject expected, boolean strictMatch,int status ){
           sendRequest(commandName,application,body);
           JSONObject response = readResponse(status);
           System.out.println(response + "RESPONSE");
           compareResponses(response.getJSONArray(outputName).getJSONObject(0),expected, strictMatch);
           return response;

    }
    public JSONObject testCommandNoReponse(String commandName,String application,String outputName,JSONObject body,JSONObject expected, boolean strictMatch,int status ){
        sendRequest(commandName,application,body);
        JSONObject response = readResponse(status);
//        System.out.println(response + "RESPONSE");
//        compareResponses(response.getJSONArray(outputName).getJSONObject(0),expected, strictMatch);
        return response;

    }
    public JSONObject testObjectCommand(String commandName,String application,String outputName,JSONObject body,JSONObject expected, boolean strictMatch,int status ){
        sendRequest(commandName,application,body);
        JSONObject response = readResponse(status);
        System.out.println(response + "RESPONSE");
        compareResponses(response,expected, strictMatch);
        return response;

    }

    public JSONObject testObjectCommandWithOutput(String commandName,String application,String outputName, JSONObject body,JSONObject expected, boolean strictMatch,int status ){
        sendRequest(commandName,application,body);
        JSONObject response = readResponse(status).getJSONObject(outputName);
        compareResponses(response,expected, strictMatch);
        return response;

    }
    @Test
    public JSONArray testListCommand(String commandName,String application,String outputName,JSONObject body,JSONObject expected, boolean strictMatch,int status ){
        sendRequest(commandName,application,body);
        int limit = body.getInt("limit");
        JSONObject response = readResponse(status);
        JSONArray responseArray = response.getJSONArray(outputName);
        System.out.println(response + "RESPONSE");
        assertTrue(responseArray.length()<=limit);
        compareResponses(responseArray.getJSONObject(0),expected, strictMatch);
        return responseArray;

    }

    @Test
    public void forAllArrayHolds(JSONArray array, JSONObject properties){
        Iterator<String> expectedKeys;
        for(int i =0;i<array.length();i++){
           expectedKeys =  properties.keys();
          while (expectedKeys.hasNext()) {
              String key = expectedKeys.next();
             Object received = array.getJSONObject(i).get(key);
             Object expected = properties.get(key);
             assertTrue(received.equals(expected));
          }
      }
    }


}
