package Commands.UserCommands;

import Interface.ConcreteCommand;

public class CheckIsMatched extends ConcreteCommand{
    @Override
    public void setParameters() {
        storedProcedure = "\"uspCheckIfUsersMatched\"";
        inputParams = new String[]{"interactionData.source_user_id",
                "interactionData.target_user_id"};
        outputName = "matches";
        useCache=true;
    }

}
