package Commands.UserCommands;

import Interface.ConcreteCommand;

public class CreateInteraction extends ConcreteCommand {

    @Override
    public void setParameters() {
        storedProcedure = "\"uspCreateInteraction\"";
        inputParams = new String[]{"interactionData.source_user_id",
                                "interactionData.target_user_id",
                                "interactionData.type"};
        outputName = "interaction";
    }
}
