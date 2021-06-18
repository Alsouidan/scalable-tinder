package Interface;

//import Cache.UserCacheController;
//import ClientService.Client;
import Cache.RedisConnection;
import Database.ArangoInstance;
import Database.PostgreSQL;
import Entities.HttpResponseTypes;
import Entities.MediaServerRequest;
import Entities.MediaServerResponse;
import Entities.ServerRequest;
import MediaServer.MinioInstance;
import Models.Message;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

//import Database.ChatArangoInstance;
//import Models.ErrorLog;

public abstract class ConcreteCommand extends Command {


//    protected RLiveObjectService RLiveObjectService;
    protected ArangoInstance ArangoInstance;
    protected PostgreSQL PostgresInstance;
    protected MinioInstance MinioInstance;
    protected  RedisConnection redis;
//    protected ChatArangoInstance ChatArangoInstance;
//    protected UserCacheController UserCacheController;
    protected Message message;
    protected JSONObject jsonBodyObject;
    protected JSONObject responseJson = new JSONObject();
    protected Gson gson = new Gson();
    protected JsonParser jsonParser;
    protected String customQuery;
    public  String  storedProcedure;
    protected String[] inputParams;
    protected String[] outputParams;
    protected String outputName = "record";
    protected String type;
    protected String model;
    protected String collection;
    protected Boolean useCache=false;
    protected MediaServerRequest mediaServerRequest;
    protected Object filterParams;
    protected  byte [] file;

    private final Logger LOGGER = Logger.getLogger(ConcreteCommand.class.getName()) ;
    protected Object sortParams;
    protected HttpResponseTypes status;


    @Override
    protected void execute() {
        try {
            TreeMap<String, Object> parameters = data;
            ArangoInstance = (ArangoInstance)
                    parameters.get("ArangoInstance");
            PostgresInstance = (PostgreSQL) parameters.get("PostgresInstance");
            MinioInstance=(MinioInstance) parameters.get("MinioInstance");
            redis = (RedisConnection) parameters.get("redis");
            ChannelHandlerContext ctx = (ChannelHandlerContext) parameters.get("ctx");
            ArangoInstance.setRedisConnection(redis);
            LOGGER.log(Level.INFO,"ARANGO is "+ArangoInstance);
//            UserCacheController = (UserCacheController)
//                    parameters.get("UserCacheController");
//            ChatArangoInstance = (ChatArangoInstance)
//                    parameters.get("ChatArangoInstance");

            Channel channel = (Channel) parameters.get("channel");
            AMQP.BasicProperties replyProps = (AMQP.BasicProperties) parameters.get("replyProps");
            jsonParser = new JsonParser();
            message = new Message();
            mediaServerRequest=((MediaServerRequest)parameters.get("mediaServerRequest"));
            if(mediaServerRequest!=null){
                message.setParameters(mediaServerRequest.getRequest());
                jsonBodyObject=new JSONObject(mediaServerRequest.getJsonRequest().toString());
                }
            else {
                String jsonString = (String) parameters.get("body");
                jsonBodyObject = new JSONObject(jsonString);
                message.setParameters(new JSONObject(jsonBodyObject.get("body").toString()));
            }
            filterParams = message.getParameter("filter") ==null? new JSONObject(): message.getParameter("filter");
            sortParams = message.getParameter("sort") ==null? new JSONObject(): message.getParameter("sort");
            status = doCommand();
            doCustomCommand();
            jsonBodyObject.put("response", responseJson);
            jsonBodyObject.put("status",status);
            if(responseJson.has("isFile")&&(boolean)responseJson.get("isFile")==true){
                MediaServerResponse msr=new MediaServerResponse(file,jsonBodyObject.toString(),(String)message.getParameter("fileName"));
                channel.basicPublish("", replyProps.getReplyTo(), replyProps, msr.getByteArray());
            }
            else
                channel.basicPublish("", replyProps.getReplyTo(), replyProps, new ServerRequest(ctx,jsonBodyObject.toString()).getByteArray());;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
//            StringWriter errors = new StringWriter();
//            e.printStackTrace(new PrintWriter(errors));
//            Client.channel.writeAndFlush(new ErrorLog(LogLevel.ERROR, errors.toString()));
        }
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    private String generateSQLQuery(){
        if(customQuery!= null) return customQuery;
        String query = String.format("SELECT * FROM %s(", storedProcedure);
        for(int i =0;i<inputParams.length;i++){
            // TODO: Required Fields & validations
            Object parameter = message.getParameter(inputParams[i]);
            String newParameterString = parameter != null ? "'%s',":"%s,";
            query+= String.format(newParameterString, parameter);
        }
        query = query.substring(0,query.length()-1) + ")";
        return query;
    }
    private JSONArray convertToJSONArray(ResultSet resultSet)
            throws Exception {
        JSONArray jsonArray = new JSONArray();
        while (resultSet.next()) {
            JSONObject obj = new JSONObject();
            int totalRows = resultSet.getMetaData().getColumnCount();
            for (int i = 0; i < totalRows; i++) {
                obj.put(resultSet.getMetaData().getColumnLabel(i + 1)
                        .toLowerCase(), resultSet.getObject(i + 1));

            }
            jsonArray.put(obj);
        }
        return jsonArray;
    }
    private String getSQLCommandId() {
        return String.format("%s%s",storedProcedure,message.getParameterValues(inputParams));
    }

    private HttpResponseTypes sqlStatus(JSONArray data){
        if (!(storedProcedure==null) && (storedProcedure.contains("GetAll"))) return HttpResponseTypes._200;
        if(data.length()== 0) return HttpResponseTypes._404;
        if(data.getJSONObject(0).length() ==0 ) return HttpResponseTypes._404;
        return HttpResponseTypes._200;
    }
    protected HttpResponseTypes handleSQLCommand() {
        if(storedProcedure==null && customQuery==null) return null;
        try{
            String id = getSQLCommandId();
            if(useCache) {
                String value = redis.getKey(id);
                if(value != null){
                    responseJson.put(outputName,new JSONArray(value));
                    LOGGER.log(Level.INFO,"Command: "+ this.getClass().getName()+" Executed Successfully");
                    return HttpResponseTypes._200;
                }
            }
        dbConn = PostgresInstance.getDataSource().getConnection();
        dbConn.setAutoCommit(true);
        Statement query = dbConn.createStatement();
        query.setPoolable(true);
        String SQLQuery = customQuery !=null ? customQuery : generateSQLQuery();
        set = query.executeQuery(SQLQuery);
        JSONObject response = new JSONObject();
        JSONArray data = convertToJSONArray(set);
        response.put(outputName,data );
        redis.setKey(id, data.toString());
        responseJson = response;
        LOGGER.log(Level.INFO,"Command: "+ this.getClass().getName()+" Executed Successfully");
        return sqlStatus(data);
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return HttpResponseTypes._500;
        } catch (Exception e){
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return HttpResponseTypes._500;
        }
        finally {
            PostgresInstance.disconnect(null, proc, dbConn);
        }

    }
    protected  HttpResponseTypes  handleNoSQLCommand(){
        try {
            if (collection ==  null) return null;
            ArrayList<Object> parameters = new ArrayList<>();
            for(int i =0;i<inputParams.length;i++) {
                // TODO: Required Fields & validations
               parameters.add(message.getParameter(inputParams[i]));
            }

            responseJson = new JSONObject();
            JSONObject dbResponse = null;
            JSONArray dbArrayResponse = null;
            switch(type){
                case "create":
                    String key = ArangoInstance.insert(collection, parameters.get(0));
                    responseJson.put("id",key);
                    return HttpResponseTypes._200;
                case "delete":
                     dbResponse = ArangoInstance.delete(collection, parameters.get(0));
                     break;
                //case "update":
                case "find":
                    dbResponse = ArangoInstance.find(collection, parameters.get(0), model);
                    break;
                case "update":
                    dbResponse  = ArangoInstance.update(collection,parameters.get(0),parameters.get(1));
                    break;
                case "findAll":
                    dbArrayResponse = ArangoInstance.findAll(collection,parameters.get(0),parameters.get(1),model, filterParams);
                    break;
                case "generateRecom":
                    dbArrayResponse = ArangoInstance.generateRecom(collection,parameters.get(0),parameters.get(1),model, filterParams,parameters.get(2),parameters.get(3),parameters.get(4),parameters.get(5),parameters.get(6),parameters.get(7));
            }
            LOGGER.log(Level.INFO,"Command: "+ this.getClass().getName()+" Executed Successfully");
            responseJson.put(outputName, dbResponse==null?dbArrayResponse:dbResponse);
            if(dbResponse==null && dbArrayResponse==null) return HttpResponseTypes._404;
            return HttpResponseTypes._200;
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return HttpResponseTypes._500;
        }
    }
    protected HttpResponseTypes doCommand() {
        setParameters();
        HttpResponseTypes sqlStatus =  handleSQLCommand();
        HttpResponseTypes NoSqlStatus = handleNoSQLCommand();
        return sqlStatus == null? NoSqlStatus:sqlStatus;



    };

    public void setParameters(){
        
    };

    public void doCustomCommand(){
        
    }
}
