package Interface;

import Entities.ServicesType;
import Services.ChatService;
import Services.ModeratorService;
import Services.UserService;
import Services.UserToUserService;

public class ServiceBuilder {
    public ServiceBuilder(){

    }
    public ServiceControl build(ServicesType type,int ID) {
        switch (type){
            case user -> {
                return new UserService(ID);
            }
            case chat -> {
                return  new ChatService(ID);
            }
            case moderator -> {
                return new ModeratorService(ID);
            }
            case user_to_user -> {
                return new UserToUserService(ID);
            }
            default -> {
                return null;
            }
        }
    }
}
