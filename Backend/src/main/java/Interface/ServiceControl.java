package Interface;

//import Cache.RedisConf;
//import Cache.UserCacheController;
//import ClientService.Client;

import Cache.RedisConnection;
import Config.Config;
import Config.ConfigTypes;
import Controller.Controller;
import Database.ArangoInstance;
import Database.PostgreSQL;
import Entities.ErrorLog;
import Entities.MediaServerRequest;
import Entities.MediaServerResponse;
import MediaServer.MediaHandler;
import MediaServer.MinioInstance;
import NettyWebServer.NettyServerInitializer;
import com.rabbitmq.client.*;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.activation.MimetypesFileTypeMap;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public abstract class ServiceControl {    // This class is responsible for Managing Each Service (Application) Fully (Queue (Reuqest/Response), Controller etc.)

    public int ID;
    protected Config conf = Config.getInstance();
    protected int maxDBConnections = conf.getServiceMaxDbConnections();
    protected String RPC_QUEUE_NAME; //set by init
    //    RedisConf redisConf ;
//    protected RLiveObjectService liveObjectService; // For Post Only
    protected ArangoInstance arangoInstance; // For Post Only
    protected MinioInstance minioInstance;
    protected PostgreSQL postgresDB;
    private final String RESPONSE_EXTENSION = "-Response";
    private final String REQUEST_EXTENSION = "-Request";
    //    protected ChatArangoInstance ChatArangoInstance;
//    protected UserCacheController userCacheController; // For UserModel Only
    private int threadsNo = conf.getServiceMaxThreads();
    private final ThreadPoolExecutor executor; //Executes the requests of this service
    private final String host = conf.getServerQueueHost();    // Define the Queue containing the requests of this service
    private final int port = conf.getServerQueuePort();
    private final String user = conf.getServerQueueUserName();
    private final String pass = conf.getServerQueuePass();
    private Channel requestQueueChannel; //The Channel containing the Queue of this service
    private Channel responseQueueChannel;
    private String requestConsumerTag;
    private String responseConsumerTag;
    private Consumer requestConsumer;
    private Consumer responseConsumer;
    private String REQUEST_QUEUE_NAME;
    private String RESPONSE_QUEUE_NAME;
    private Class last_com;
    public final Logger LOGGER = Logger.getLogger(ServiceControl.class.getName()) ;
    protected RedisConnection redis;

    public ServiceControl(int ID) {
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadsNo);
        init();
        //initDB();
        REQUEST_QUEUE_NAME = RPC_QUEUE_NAME + REQUEST_EXTENSION;
        RESPONSE_QUEUE_NAME = RPC_QUEUE_NAME + RESPONSE_EXTENSION;
        this.ID = ID;
        redis = new RedisConnection();
    }

    public MinioInstance getMinioInstance() {
        return minioInstance;
    }

    public void setMinioInstance(MinioInstance minioInstance) {
        this.minioInstance = minioInstance;
    }

    public abstract void init();

    public abstract void initDB();

    public void start() {
        consumeFromRequestQueue();
        consumeFromResponseQueue();
    }

    
    private static HttpResponseStatus mapToStatus(String status){
        switch (status){
            case "_200":return HttpResponseStatus.OK;
            case "_404":return HttpResponseStatus.NOT_FOUND;
            case "_400":return HttpResponseStatus.BAD_REQUEST;
            case "_401":return HttpResponseStatus.UNAUTHORIZED;
            case "_500":return HttpResponseStatus.BAD_REQUEST;
            default:return HttpResponseStatus.ACCEPTED;
        }
    }

    private void consumeFromResponseQueue() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(user);
        factory.setPassword(pass);
        Connection connection = null;

        try {
            connection = factory.newConnection();
            responseQueueChannel = connection.createChannel();
            responseQueueChannel.queueDeclare(RESPONSE_QUEUE_NAME, true, false, false, null);
            responseQueueChannel.basicQos(threadsNo);
            LOGGER.log(Level.INFO," [x] Awaiting RPC RESPONSES on Queue : " + RESPONSE_QUEUE_NAME);
            responseConsumer = new DefaultConsumer(responseQueueChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                    try {
                        //Using Reflection to convert a command String to its appropriate class
//                        Channel receiver = REQUEST_CHANNEL_MAP.get(RESPONSE_MAIN_QUEUE_NAME);

                         MediaServerResponse msr=MediaServerResponse.getObject(body);
                         if(msr!=null){   // If a download command
                           body=msr.getResponseJson().toString().getBytes("UTF-8");
                             String responseMsg = new String(body, StandardCharsets.UTF_8);

                             org.json.JSONObject responseJson = new org.json.JSONObject(responseMsg);
                             System.out.println(responseJson);
                             String status=responseJson.get("status").toString() ;
                             byte[] fileByteArray = msr.getFile();
                             File file=new File(msr.getFileName());

                             FileOutputStream fos = null;
                             try {
                                 fos = new FileOutputStream(file);
                                 fos.write(fileByteArray);
                             }
                             catch(Exception e){
                                 e.printStackTrace();
                             }


                             RandomAccessFile raf;

                             try {
                                 raf = new RandomAccessFile(file, "r");
                             } catch (FileNotFoundException fnfe) {
                                 return;
                             }

                             long fileLength = 0;
                             try {
                                 fileLength = raf.length();
                             } catch (IOException ex) {
                                 Logger.getLogger(MediaHandler.class.getName()).log(Level.SEVERE, null, ex);
                             }

                             HttpResponse response = new DefaultHttpResponse(HTTP_1_1, mapToStatus(status));
                             org.json.JSONObject headers = (org.json.JSONObject) responseJson.get("Headers");
                             Iterator<String> keys = headers.keys();
                             while (keys.hasNext()) {
                                 String key = keys.next();
                                 String value = (String) headers.get(key);
                                 response.headers().set(key, value);
                             }
                             HttpUtil.setContentLength(response, fileLength);
                             setContentTypeHeader(response,file);
                             ChannelHandlerContext ctx = NettyServerInitializer.getUuid().remove(properties.getCorrelationId());

                             // Write the initial line and the header.
                             ctx.write(response);
                             ChannelFuture sendFileFuture;
                             DefaultFileRegion defaultRegion = new DefaultFileRegion(raf.getChannel(), 0, fileLength);
                             sendFileFuture = ctx.write(defaultRegion);
                             // Write the end marker
                             ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                             ctx.close();
                             // Write the content.

                             file.delete();
                         }
                         else{   // If a normal command's response
                        LOGGER.log(Level.INFO,"Responding to corrID: "+ properties.getCorrelationId() +  ", on Queue : " + RESPONSE_QUEUE_NAME);
                        LOGGER.log(Level.INFO,"Request    :   " + new String(body, "UTF-8"));
                        LOGGER.log(Level.INFO,"Application    :   " + RPC_QUEUE_NAME);
                        LOGGER.log(Level.INFO,"INSTANCE NUM   :   " + ID);
                        String responseMsg = new String(body, StandardCharsets.UTF_8);
                        org.json.JSONObject responseJson = new org.json.JSONObject(responseMsg);
                        if(responseJson.getString("command").equals("UpdateChat")||responseJson.getString("command").equals("UploadMedia"))
                            return;
                        String status=responseJson.get("status").toString() ;
                        FullHttpResponse response = new DefaultFullHttpResponse(
                                HttpVersion.HTTP_1_1,
                                mapToStatus(status),
                                copiedBuffer(responseJson.get("response").toString().getBytes()));
                        org.json.JSONObject headers = (org.json.JSONObject) responseJson.get("Headers");
                        Iterator<String> keys = headers.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if(key.toLowerCase().contains("content")){
                                continue;
                            }
                            String value = (String) headers.get(key);
                            response.headers().set(key, value);
                        }
                        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
                        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                        response.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
                        //System.out.println(NettyServerInitializer.getUuid().remove(properties.getCorrelationId()));
                        ChannelHandlerContext ctxRec = NettyServerInitializer.getUuid().remove(properties.getCorrelationId());
                        ctxRec.writeAndFlush(response);
                        ctxRec.close();
                         }

                    } catch (RuntimeException| IOException e) {
                        FullHttpResponse response = new DefaultFullHttpResponse(
                                HttpVersion.HTTP_1_1,
                                HttpResponseStatus.BAD_REQUEST,
                                copiedBuffer("ERROR".toString().getBytes()));

                        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
                        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

                        ChannelHandlerContext ctxRec = NettyServerInitializer.getUuid().remove(properties.getCorrelationId());
                        ctxRec.writeAndFlush(response);
                        ctxRec.close();
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        consumeFromResponseQueue();
                    } finally {
                        synchronized (this) {
                            this.notify();
                        }
                    }
                }
            };
            responseConsumerTag = responseQueueChannel.basicConsume(RESPONSE_QUEUE_NAME, true, responseConsumer);
            // Wait and be prepared to consume the message from RPC client.
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
//            consumeFromQueue(RPC_QUEUE_NAME,QUEUE_TO);
        }
    }

    private void consumeFromRequestQueue() {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(user);
        factory.setPassword(pass);
        Connection connection = null;

        try {
            connection = factory.newConnection();
            requestQueueChannel = connection.createChannel();
            requestQueueChannel.queueDeclare(REQUEST_QUEUE_NAME, true, false, false, null);
            requestQueueChannel.basicQos(threadsNo);
//            redisConf = new RedisConf();
//            liveObjectService = redisConf.getService();
//
//            Controller.channel.writeAndFlush(new ErrorLog(LogLevel.INFO, " [x] Awaiting RPC requests on Queue : " + RPC_QUEUE_NAME));
            LOGGER.log(Level.INFO," [x] Awaiting RPC requests on Queue : " + REQUEST_QUEUE_NAME);
       
            requestConsumer = new DefaultConsumer(requestQueueChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(properties.getCorrelationId())
                            .replyTo(RESPONSE_QUEUE_NAME)
                            .build();
//                    Controller.channel.writeAndFlush(new ErrorLog(LogLevel.INFO, "Responding to corrID: " + properties.getCorrelationId() + ", on Queue : " + RPC_QUEUE_NAME));
                    LOGGER.log(Level.INFO,"Responding to corrID: " + properties.getCorrelationId() + ", on Queue : " + REQUEST_QUEUE_NAME);
                    LOGGER.log(Level.INFO,"INSTANCE NUM   :   " + ID);
                    try {
                        String message;
                        //Using Reflection to convert a command String to its appropriate class
                        MediaServerRequest mediaServerRequest =MediaServerRequest.getObject(body);
                        if(mediaServerRequest !=null){
                            message= mediaServerRequest.getJsonRequest().toString();
                        }
                        else{
                            message = new String(body, StandardCharsets.UTF_8);
                        }

                        JSONParser parser = new JSONParser();
                        JSONObject command = (JSONObject) parser.parse(message);
                        String className = (String) command.get("command");
                        LOGGER.log(Level.INFO,"className:"+className);
                        last_com = Class.forName("Commands."+RPC_QUEUE_NAME + "Commands." + className);
                        //
//                        ClassLoader parentLoader = com.getClassLoader();
//                        File dir= new File("/home/vm/Desktop/scalable-tinder/Backend/target/classes");
//                        URLClassLoader loader1 = new URLClassLoader(
//                                new URL[] { dir.toURL()}, parentLoader);
//                        Class com2 = loader1.loadClass("Commands."+RPC_QUEUE_NAME+"Commands."+className);

                        Command cmd = (Command) last_com.newInstance();
                        TreeMap<String, Object> init = new TreeMap<>();
                        init.put("channel", requestQueueChannel);
                        init.put("properties", properties);
                        init.put("replyProps", replyProps);
                        init.put("envelope", envelope);
                        init.put("PostgresInstance", postgresDB);
                        init.put("body", message);
                        init.put("mediaServerRequest",mediaServerRequest);
//                        init.put("RLiveObjectService", liveObjectService);
                        init.put("ArangoInstance", arangoInstance);
                        init.put("MinioInstance", minioInstance);
                        init.put("redis", redis);
//                        init.put("ChatArangoInstance", ChatArangoInstance);
//                        init.put("UserCacheController", userCacheController);
                        cmd.init(init);
                        executor.submit(cmd);
                    } catch (RuntimeException | ParseException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                        FullHttpResponse response = new DefaultFullHttpResponse(
                                HttpVersion.HTTP_1_1,
                                HttpResponseStatus.BAD_REQUEST,
                                copiedBuffer("ERROR".toString().getBytes()));

                        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
                        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                        ChannelHandlerContext ctxRec = NettyServerInitializer.getUuid().remove(properties.getCorrelationId());
                        ctxRec.writeAndFlush(response);
                        ctxRec.close();
                        StringWriter errors = new StringWriter();
                        e.printStackTrace(new PrintWriter(errors));
                        Controller.channel.writeAndFlush(new ErrorLog(LogLevel.ERROR, errors.toString()));
                        start();
                    } finally {
                        synchronized (this) {
                            this.notify();
                        }
                    }
                }
            };
            requestConsumerTag = requestQueueChannel.basicConsume(REQUEST_QUEUE_NAME, true, requestConsumer);


        } catch (IOException | TimeoutException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Controller.channel.writeAndFlush(new ErrorLog(LogLevel.ERROR, errors.toString()));
//            start();
        }
    }
    public abstract boolean setMaxDBConnections(String connections);

    public boolean setMaxThreadsSize(int threads) {
        threadsNo = threads;
        executor.setMaximumPoolSize(threads);
        conf.setProperty(ConfigTypes.Service, "service.max.thread", String.valueOf(threads));
        return true;
    }

    public boolean resume() {
        try {
            requestConsumerTag = requestQueueChannel.basicConsume(REQUEST_QUEUE_NAME, false, requestConsumer);
            responseConsumerTag =responseQueueChannel.basicConsume(RESPONSE_QUEUE_NAME, false, responseConsumer);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Controller.channel.writeAndFlush(new ErrorLog(LogLevel.ERROR, errors.toString()));
            return false;
        }
//        Controller.channel.writeAndFlush(new ErrorLog(LogLevel.INFO, "Service Resumed"));
    }

    public boolean freeze() {
        try {
            requestQueueChannel.basicCancel(requestConsumerTag);
            responseQueueChannel.basicCancel(responseConsumerTag);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            return false;
//            Controller.channel.writeAndFlush(new ErrorLog(LogLevel.ERROR, errors.toString()));
        }
//        Controller.channel.writeAndFlush(new ErrorLog(LogLevel.INFO, "Service Freezed"));
    }

    //TODO CHECK IF FILE EXISTS FIRST IF THERE THEN LOG AN ERROR
    public boolean add_command(String commandName, String source_code) {
        String full_java_path = "";
        String full_target_path = "";
        String class_path = "";
        if(commandName.contains(".")){
            class_path = commandName;
        }  else{
            class_path = "Commands."+RPC_QUEUE_NAME+"Commands."+commandName;
        }
        System.out.println(class_path);
        try {
            String root_path = new File(".").getCanonicalPath()+"/Backend";

            full_target_path = root_path+"/target/classes";
            full_java_path = root_path;
            full_java_path +="/src/main/java";
            String[] splitted_2 =   class_path.split("\\.");
            System.out.println(Arrays.toString(splitted_2));
            for(String s:splitted_2){
                full_target_path+= "/"+s;
                full_java_path += "/"+s;

            }
            System.out.println(full_java_path);
            System.out.println(full_target_path);
            if(!new File(full_java_path).getParentFile().exists()||!new File(full_target_path).getParentFile().exists()){
                LOGGER.log(Level.SEVERE,"DIRECTORY DOESN'T EXISTS");
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return false;
        }

        FileWriter fileWriter;
        try {

            // Save source in .java file.
            
            File root = new File(full_java_path); // On Windows running on C:\, this is C:\java.
            File sourceFile = new File(full_target_path+".java");
            File javaFile = new File(full_java_path+".java");
            if (sourceFile.exists()) {
                LOGGER.log(Level.SEVERE,commandName + " Already Exists please use update");
//                Controller.channel.writeAndFlush(new ErrorLog(LogLevel.ERROR, commandName + " Already exists please use update"));
                return false;
            }
            sourceFile.getParentFile().mkdirs();
            Files.write(sourceFile.toPath(), source_code.getBytes(StandardCharsets.UTF_8));
            javaFile.getParentFile().mkdirs();
            Files.write(javaFile.toPath(), source_code.getBytes(StandardCharsets.UTF_8));

            // Compile source file.
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            
            compiler.run(null, null, null, sourceFile.getPath());

            
            boolean deleted = Files.deleteIfExists(sourceFile.toPath());
            return true;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Controller.channel.writeAndFlush(new ErrorLog(LogLevel.ERROR, errors.toString()));
            return false;
        }

    }
    public boolean update_class(String className,String sourceCode){
        return update_command(className,sourceCode);
    }

    public boolean delete_command(String commandName) {
        String full_java_path = "";
        String full_target_path = "";
        String class_path = "";
        if(commandName.contains(".")){
            class_path = commandName;
        }  else{
            class_path = "Commands."+RPC_QUEUE_NAME+"Commands."+commandName;
        }
        System.out.println(class_path);
        try {
            String root_path = new File(".").getCanonicalPath()+"/Backend";

            full_target_path = root_path+"/target/classes";
            full_java_path = root_path;
            full_java_path +="/src/main/java";
            String[] splitted_2 =   class_path.split("\\.");
            System.out.println(Arrays.toString(splitted_2));
            for(String s:splitted_2){
                full_target_path+= "/"+s;
                full_java_path += "/"+s;

            }
            System.out.println(full_java_path);
            System.out.println(full_target_path);
            if(!new File(full_java_path).getParentFile().exists()||!new File(full_target_path).getParentFile().exists()){
                LOGGER.log(Level.SEVERE,"DIRECTORY DOESN'T EXISTS");
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return false;
        }
        try {
            if(!Files.deleteIfExists(Paths.get( full_target_path+ ".class"))){
                LOGGER.log(Level.SEVERE,"No such file/directory exists");
                
                return false;
            }
            if(!Files.deleteIfExists(Paths.get( full_java_path+ ".java"))){
                LOGGER.log(Level.SEVERE,"No such file/directory exists");

                return false;
            }
            last_com=null;
            System.gc();
            System.runFinalization();
        } catch (NoSuchFileException e) {
            LOGGER.log(Level.SEVERE,"No such file/directory exists");
            return false;
        } catch (DirectoryNotEmptyException e) {
            LOGGER.log(Level.SEVERE,"Directory is not empty.");
            return false;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,"Invalid permissions.");
            return false;
        } 
        LOGGER.log(Level.INFO, "Deletion successful.");
        return true;
    }

    public boolean update_command(String commandName, String filePath) {
        if(delete_command(commandName))
            return add_command(commandName, filePath);
        return false;
    }

    public void setArangoInstance(ArangoInstance arangoInstance) {
        this.arangoInstance = arangoInstance;
    }


    public void createNoSQLDB() {

        arangoInstance.initializeDB();
    }

    public void dropNoSQLDB() {

        arangoInstance.dropDB();
    }
    public static Level mapToLevel(String level){
        switch (level.toLowerCase(Locale.ROOT)){
            case "error","severe":return Level.SEVERE;
            case "info":return Level.INFO;
            case "warning": return Level.WARNING;
            case "config": return Level.CONFIG;
            default: return  Level.ALL;
        }
    }
    public boolean set_log_level(String level){
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(mapToLevel(level));
        return true;
    }
//
//
//    public void seedPostDB(){
//        ArrayList<String> catid = new ArrayList<String>();
//        ArrayList<String> postid = new ArrayList<String>();
//        for(int i=0;i<2;i++){
//            CategoryDBObject cat = new CategoryDBObject("category"+i,new ArrayList<>());
//            arangoInstance.insertNewCategory(cat);
//            catid.add(cat.getId());
//        }
//        for(int i=0;i<10;i++){
//            PostDBObject post= new PostDBObject("Kefa7y"+i,new ArrayList<>(), new ArrayList<>(), "45b1f6ff-cc8a-43e7-bb6d-6f96d9b9f3a1.jpeg");
//            arangoInstance.insertNewPost(post);
//            arangoInstance.addNewPostToCategory(catid.get(i%2),post.getId());
//            postid.add(post.getId());
//        }
//        Client.channel.writeAndFlush(new ErrorLog(LogLevel.INFO,"Database seeded: Post"));
//
//    }

    private static void setContentTypeHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        mimeTypesMap.addMimeTypes("image png tif jpg jpeg bmp");
        mimeTypesMap.addMimeTypes("text/plain txt");
        mimeTypesMap.addMimeTypes("video/mp4 mp4");
        mimeTypesMap.addMimeTypes("application/pdf pdf");

        String mimeType = mimeTypesMap.getContentType(file);

        response.headers().set(CONTENT_TYPE, mimeType);
    }
}
