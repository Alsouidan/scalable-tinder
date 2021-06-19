package Services;

//import Cache.RedisConf;
import Controller.Controller;
//import Database.ArangoInstance;
//import Interface.ControlService;
import Database.ArangoInstance;

import Database.PostgreSQL;
import Interface.ServiceControl;
import Entities.ErrorLog;
import io.netty.handler.logging.LogLevel;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModeratorService extends ServiceControl {
    public ModeratorService(int ID) {
        super(ID);
    }
    private final Logger LOGGER = Logger.getLogger(ModeratorService.class.getName()) ;

//    private RedisConf redisConf;

    @Override
    public void init() {
        RPC_QUEUE_NAME = conf.getServicesMqModeratorQueue();
////            redisConf  = new RedisConf();
//        } catch (IOException e) {
//            StringWriter errors = new StringWriter();
//            e.printStackTrace(new PrintWriter(errors));
//            Controller.channel.writeAndFlush(new ErrorLog(LogLevel.ERROR, errors.toString()));
//            e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);
//        }
//        liveObjectService = redisConf.getService();
    }

    @Override
    public void initDB() {
        try {
            arangoInstance=new ArangoInstance(15);
            postgresDB= new PostgreSQL();
            postgresDB.initSource();
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
//            Controller.channel.writeAndFlush(new ErrorLog(LogLevel.ERROR, errors.toString()));
            e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    @Override
    public boolean setMaxDBConnections(String connections){
        arangoInstance.setMaxDBConnections(Integer.parseInt(connections));
        return postgresDB.setDbMaxConnections(connections+"");
//        ChatArangoInstance.setMaxDBConnections(maxDBConnections);
    }


    public static void main(String[] args) {
        new ModeratorService(12000).start();
    }
}
