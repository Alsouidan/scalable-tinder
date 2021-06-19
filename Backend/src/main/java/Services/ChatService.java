package Services;

//import Database.ChatArangoInstance;
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

public class ChatService extends ServiceControl{

    public ChatService(int ID) {
        super(ID);
    }
    private final Logger LOGGER = Logger.getLogger(ChatService.class.getName()) ;

    @Override
    public void init() {
        RPC_QUEUE_NAME = conf.getServicesMqChatQueue();
        System.out.println(RPC_QUEUE_NAME);
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

    public static void main(String[] args) {
        new ChatService(12000).start();
    }
}
