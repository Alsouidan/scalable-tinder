package Commands.UserCommands;

import Interface.ConcreteCommand;


public class UpdateInteraction extends ConcreteCommand {
    @Override
    public void setParameters() {
        storedProcedure = "uspUpdateInteraction";
        inputParams = new String[]{"interactionData.id", "interactionData.source_user_id", "interactionData.target_user_id", "interactionData.type"};
        outputName = "interaction";
    }

}