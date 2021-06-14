package Config;






import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {

    private static Config instance = new Config();
    private final Logger LOGGER = Logger.getLogger(Config.class.getName()) ;

    private final Properties arangoConfig = new Properties();
    private final Properties minioConfig = new Properties();
    private final Properties controllerConfig = new Properties();
    private final Properties loadBalancerConfig = new Properties();
    private final Properties mediaServerConfig = new Properties();
    private final Properties servicesMQConfig = new Properties();
    private final Properties serviceConfig = new Properties();
    private final Properties NettyServerConfig = new Properties();
    private final Properties postgresqlConfig = new Properties();
    private final Properties redisConfig = new Properties();
    private final Properties loggerConfig = new Properties();
    private final Properties firebaseConfig = new Properties();

    private final String arangoPath = "src/main/resources/arango.conf";
    private final String minioPath = "src/main/resources/minio.conf";
    private final String controllerPath = "src/main/resources/controller.conf";
    private final String loadBalancerPath = "src/main/resources/load.balancer.conf";
    private final String mediaServerPath = "src/main/resources/media.server.conf";
    private final String servicesMQPath = "src/main/resources/mq.instance.conf";
    private final String servicePath = "src/main/resources/service.conf";
    private final String nettyServerPath = "src/main/resources/web.server.conf";
    private final String postgresqlPath = "src/main/resources/postgresql.conf";
    //private final String redisPath = "src/main/resources/redisEnv.conf";
    private final String redisPath = "src/main/resources/redisEnv.conf";
    private final String loggerPath = "src/main/resources/logger.conf";
//
    private final String fireBasePath = "src/main/resources/firebase.conf";
    private final String arangoUserName="root";
    private final String arangoPass="";
    private String root_path = "";


    public Properties getArangoConfig() {
        return arangoConfig;
    }

    public String getArangoPath() {
        return arangoPath;
    }

    public Properties getRedisConfig() {
        return redisConfig;
    }

    public Properties getFirebaseConfig() {
        return firebaseConfig;
    }





    private Config() {
        if(System.getProperty("user.dir").contains("Backend")){
            root_path=System.getProperty("user.dir");
        }else{
            root_path = System.getProperty("user.dir")+"/Backend";
        }
        loadConfig(arangoConfig, arangoPath);
        loadConfig(minioConfig,minioPath)   ;
        loadConfig(controllerConfig, controllerPath);
        loadConfig(loadBalancerConfig, loadBalancerPath);
        loadConfig(mediaServerConfig, mediaServerPath);
        loadConfig(servicesMQConfig, servicesMQPath);
        loadConfig(serviceConfig, servicePath);
        loadConfig(NettyServerConfig, nettyServerPath);
        loadConfig(postgresqlConfig,postgresqlPath);
        loadConfig(redisConfig, redisPath);
        loadConfig(loggerConfig,loggerPath);
        loadConfig(firebaseConfig,fireBasePath);
        
//        readSystemVariables(loadBalancerConfig,"load_balancer_rabbitmq_host");
    }

    private void loadConfig(Properties config, String path){
        try {
            FileInputStream file = new FileInputStream(root_path+"/"+path);
            config.load(file);
            file.close();
        } catch (IOException e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
//            Controller.logger.error(errors);
            e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    public static void readSystemVariables(Properties conf, String param){
        System.out.println(System.getenv(param));
        if(System.getenv(param) != null)
            conf.setProperty(param, System.getenv(param));
    }

    public static Config getInstance(){
        return instance;
    }

    public void setProperty(ConfigTypes config, String key, String val){
        Properties props = null;
        String path = null;
        switch (config){
            case Arango:
                props = arangoConfig;
                path = arangoPath;
                break;
            case Controller:
                props = controllerConfig;
                path = controllerPath;
                break;
            case LoadBalancer:
                props = loadBalancerConfig;
                path = loadBalancerPath;
                break;
            case MediaServer:
                props = mediaServerConfig;
                path = mediaServerPath;
                break;
            case MqInstance:
                props = servicesMQConfig;
                path = servicesMQPath;
                break;
            case PostSql:
//                props = postSqlConf;
//                path = postSqlPath;
                break;
            case Service:
                props = serviceConfig;
                path = servicePath;
                break;
            case WebServer:
                props = NettyServerConfig;
                path = nettyServerPath;
                break;
            case Minio:
                props=minioConfig;
                path=minioPath;
        }
        props.setProperty(key,val);
        writeConfig(props, path);
    }

    private void writeConfig(Properties config, String path){
        OutputStream out;
        try {
            out = new FileOutputStream(path, false);
            config.store(out, "");
            out.close();
        } catch (IOException e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
//            Controller.logger.error(errors);
            e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }


    //Web WebServer Configs

    public int getWebServerPort() {
        return Integer.parseInt(NettyServerConfig.getProperty("server_port"));
    }

    public String getServerQueueHost() {
        return NettyServerConfig.getProperty("server_rabbitmq_host");
    }

    public int getServerQueuePort() {
        return Integer.parseInt(NettyServerConfig.getProperty("server_rabbitmq_port"));
    }

    public String getServerQueueUserName() {
        return NettyServerConfig.getProperty("server_rabbitmq_username");
    }

    public String getServerQueuePass() {
        return NettyServerConfig.getProperty("server_rabbitmq_password");
    }

    public String getServerQueueName() {
        return NettyServerConfig.getProperty("server_rabbitmq_queue");
    }


    //LoadBalancer Configs

    public String getLoadBalancerQueueHost() {
        return loadBalancerConfig.getProperty("load_balancer_rabbitmq_host");
    }

    public int getLoadBalancerQueuePort() {
        return Integer.parseInt(loadBalancerConfig.getProperty("load_balancer_rabbitmq_port"));
    }

    public String getLoadBalancerQueueUserName() {
        return loadBalancerConfig.getProperty("load_balancer_rabbitmq_username");
    }

    public String getLoadBalancerQueuePass() {
        return loadBalancerConfig.getProperty("load_balancer_rabbitmq_password");
    }

    public String getLoadBalancerQueueName() {
        return loadBalancerConfig.getProperty("load_balancer_rabbitmq_queue");
    }

    public String getLoadBalancerUserQueue() {
        return loadBalancerConfig.getProperty("load_balancer_rabbitmq_user_queue");
    }
    public String getLoadBalancerUserToUserQueue() {
        return loadBalancerConfig.getProperty("load_balancer_rabbitmq_usertouser_queue");
    }

    public String getLoadBalancerModeratorQueue() {
        return loadBalancerConfig.getProperty("load_balancer_rabbitmq_moderator_queue");
    }

    public String getLoadBalancerChatQueue() {
        return loadBalancerConfig.getProperty("load_balancer_rabbitmq_chat_queue");
    }


    //MediaServer Configs

    public String getMediaServerPath() {
        return mediaServerConfig.getProperty("media_server_file_path");
    }

    public int getMediaServerPort() {
        return Integer.parseInt(mediaServerConfig.getProperty("media_server_port"));
    }

    public int getMediaServerThreads() {
        return Integer.parseInt(mediaServerConfig.getProperty("media_server_threads"));
    }


    //MqInstance Configs

    public String getServicesMqQueueHost() {
        return servicesMQConfig.getProperty("mq_instance_rabbitmq_host");
    }

    public int getServicesMqQueuePort() {
        return Integer.parseInt(servicesMQConfig.getProperty("mq_instance_rabbitmq_port"));
    }

    public String getServicesMqQueueUserName() {
        return servicesMQConfig.getProperty("mq_instance_rabbitmq_username");
    }

    public String getServicesMqQueuePass() {
        return servicesMQConfig.getProperty("mq_instance_rabbitmq_password");
    }

    public String getServicesMqUserQueue() {
        return servicesMQConfig.getProperty("mq_instance_rabbitmq_user_queue");
    }

    public String getServicesMqUserToUserQueue() {
        return servicesMQConfig.getProperty("mq_instance_rabbitmq_usertouser_queue");
    }
    public String getServicesMqModeratorQueue() {
        return servicesMQConfig.getProperty("mq_instance_rabbitmq_moderator_queue");
    }

    public String getServicesMqChatQueue() {
        return servicesMQConfig.getProperty("mq_instance_rabbitmq_chat_queue");
    }


    //Controller Configs

    public String getControllerHost() {
        return controllerConfig.getProperty("controller_host");
    }

    public int getControllerPort() {
        return Integer.parseInt(controllerConfig.getProperty("controller_port"));
    }


    //Service Configs

   public int getServiceMaxThreads() {
        return Integer.parseInt(serviceConfig.getProperty("service_max_thread"));
    }

    public int getServiceMaxDbConnections() {
        return Integer.parseInt(serviceConfig.getProperty("service_max_db"));
    }
    public int getUserServiceNumInstances(){return Integer.parseInt(serviceConfig.getProperty("user_service_num_instances"));}
    public int getModeratorServiceNumInstances(){return Integer.parseInt(serviceConfig.getProperty("moderator_service_num_instances"));}
    public int getUserToUserServiceNumInstances(){return Integer.parseInt(serviceConfig.getProperty("user_to_user_service_num_instances"));}
    public int getChatServiceNumInstances(){return Integer.parseInt(serviceConfig.getProperty("chat_service_num_instances"));}

    //Arango Configs

    public String getArangoUserName() {
        return arangoConfig.getProperty("arango_username");
    }

    public String getArangoHost() {
        return arangoConfig.getProperty("arango_host");
    }

    public int getArangoPort() {
        return Integer.parseInt(arangoConfig.getProperty("arango_port"));
    }

    public String getArangoQueuePass() {
        return arangoConfig.getProperty("arango_password");
    }

    public String getArangoDbName() {
        return arangoConfig.getProperty("arango_db_name");
    }

// Minio Configs

    public String getMinioAccessKey(){return minioConfig.getProperty("minio_access_key");}
    public String getMinioSecretKey(){return minioConfig.getProperty("minio_secret_key");}
    public String getMinioBucketName(){return minioConfig.getProperty("minio_bucket_name");}
    public String getMinioPort(){return minioConfig.getProperty("minio_port");}
    //Postgresql Configs

    public String getPostgresqlUserName() {return postgresqlConfig.getProperty("postgresql_username");}
    public String getPostgresqlPassword() {return postgresqlConfig.getProperty("postgresql_password");}

    public String getPostgresqlHost() {return postgresqlConfig.getProperty("postgresql_host");}

    public String getPostgresqlPort() {return postgresqlConfig.getProperty("postgresql_port");}
    public String getPostgresqlDBName() {return postgresqlConfig.getProperty("postgresql_db_name");}
    public String getPostgresqlMaxConn() {return postgresqlConfig.getProperty("postgresql_max_db");}

    public String getPostgresqlInitConn() {return postgresqlConfig.getProperty("postgresql_init_db");}

    //  Logger Configs
    public String getLoggerPath() {return root_path+"/"+loggerConfig.getProperty("logger_path");}

    public String getLoggerPropsPath() {return root_path+"/"+loggerConfig.getProperty("logger_props_path");}



}
