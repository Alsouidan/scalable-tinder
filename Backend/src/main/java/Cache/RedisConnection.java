package Cache;

import Config.Config;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.cluster.RedisClusterClient;
import com.lambdaworks.redis.cluster.api.StatefulRedisClusterConnection;
import com.lambdaworks.redis.cluster.api.sync.RedisAdvancedClusterCommands;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedisConnection {
     private Properties config = Config.getInstance().getRedisConfig();
     private int totalMasterNodes = Integer.parseInt(config.getProperty("totalMasterNodes"));
     private int expiryDuration = Integer.parseInt(config.getProperty("expiry_hours"));
     private String host =  config.getProperty("host");
     private static RedisAdvancedClusterCommands<String,String> redisCommands;
     private static RedisConnection redis;
     private final Logger LOGGER = Logger.getLogger(RedisConnection.class.getName()) ;
     public RedisConnection(){
         RedisClusterClient redisClient = RedisClusterClient.create(getNodesURI());
         StatefulRedisClusterConnection<String, String> connection = redisClient.connect();
         redisCommands = connection.sync();
         LOGGER.log(Level.INFO,"Connected to Redis Cluster");
         LOGGER.log(Level.INFO, connection.getPartitions().toString());
         redis = this;
     }
         
     private List<RedisURI> getNodesURI(){
      List<RedisURI> URIs = new ArrayList<RedisURI>();
      for(int i=0;i<totalMasterNodes;i++){
          int port = Integer.parseInt(config.getProperty("masterPort"+(i+1)));
          RedisURI masterURI = RedisURI.create(String.format("redis://%s:%s", host, port));
          RedisURI slaveURI = RedisURI.create(String.format("redis://%s:%s", host, port+2));
          URIs.add(masterURI);
          URIs.add(slaveURI);
      }
      return URIs;
     }

     public static RedisAdvancedClusterCommands<String,String> getRedisCommands(){
         return redisCommands;
     }

     public String getKey(String key){
         try {
             return redisCommands.get(key);
         }catch (Exception e){
             return null;
         }
     }

     public String setKey(String key,String value){
         try {
             redisCommands.set(key, value);
             Date oldDate = new Date();
             Date newDate = new Date(oldDate.getTime() + TimeUnit.HOURS.toMillis(expiryDuration));
             redisCommands.expireat(key, newDate);

             return value;
         }catch(Exception e){
             return null;
         }
     }
     public static RedisConnection getInstance(){
         return redis;
     }

}
