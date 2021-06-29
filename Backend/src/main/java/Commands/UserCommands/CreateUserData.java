package Commands.UserCommands;

import Interface.ConcreteCommand;

public class CreateUserData extends ConcreteCommand {
    @Override
     public void setParameters(){
        System.out.println("IN CREATE USER");
          type="create";
          model = "UserData";
          inputParams= new String[]{"userData"};
          collection  = "users";
    }

}
