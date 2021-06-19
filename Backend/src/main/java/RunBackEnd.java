//import ClientService.Client;
//import Controller.Controller;
import Cache.RedisConnection;
import Config.Config;
import Interface.ServiceBuilder;
import Interface.ServiceControl;
import MessageQueue.ServicesMQ;
import Entities.ServicesType;
import NettyWebServer.NettyServer;
import Controller.Controller;

import java.util.logging.Level;

public class RunBackEnd {

    private static ServicesType[] services;
    private static int[] ports;


    public static void main(String[] args) throws InterruptedException {
            ports = new int[]{8020,8021};
            run("server");
            run("mQinstance");
            services = new ServicesType[]{ServicesType.user,ServicesType.moderator, ServicesType.chat,ServicesType.user_to_user};
            run("controller");
    }
    private static int getInitialInstanceNum(ServicesType serviceName){
        Config config = Config.getInstance();
        switch (serviceName){
            case user:
                return config.getUserServiceNumInstances();
            case chat:
                return config.getChatServiceNumInstances();
            case moderator:
                return config.getModeratorServiceNumInstances();
            case user_to_user:
                return config.getUserToUserServiceNumInstances();
        }
        return 0;
    }
    private static void run(String instance) throws InterruptedException {
        Thread t = new Thread(() -> {
            switch (instance.toLowerCase()){
                case "server":
                    for(int port :ports){
                        NettyServer s = new NettyServer(port);
                        new Thread(() -> {
                            s.start();
                        }).start();
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();s.LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        }
                    }
                    break;
                case "mqinstance":
                    ServicesMQ mQinstance = new ServicesMQ();
                    break;
                case "controller":
                    ServiceBuilder builder = new ServiceBuilder();
                    int base_port = 12000;
                    int count = 0;
                    for(ServicesType type:services) {
                        for(int i =0;i<getInitialInstanceNum(type);i++) {
                            ServiceControl s =   builder.build(type,base_port+count) ;
                            count ++;
                            new Thread(() -> {
                                s.start();
                            }).start();
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();s.LOGGER.log(Level.SEVERE,e.getMessage(),e);
                            }
                        }
                    }
//                    c.initDBs();
//                    c.startServices();

                    break;
            }
        });
        t.start();
        Thread.sleep(200);
    }
}
