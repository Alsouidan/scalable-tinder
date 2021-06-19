package Services;


//import Cache.UserCacheController;
import Controller.ControllerAdapterHandler;
import Database.ArangoInstance;
import Database.PostgreSQL;
import Interface.ServiceControl;
import MediaServer.MinioInstance;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserToUserService extends ServiceControl {

    public UserToUserService(int ID) {
        super(ID);
    }
    private final Logger LOGGER = Logger.getLogger(UserToUserService.class.getName()) ;

    @Override
    public void init() {
        RPC_QUEUE_NAME = conf.getServicesMqUserToUserQueue();

    }

    @Override
    public void initDB() {
        try {
            minioInstance =new MinioInstance();
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

    public static void main(String[] argv) {
        new UserToUserService(12000).start();
    }

}



