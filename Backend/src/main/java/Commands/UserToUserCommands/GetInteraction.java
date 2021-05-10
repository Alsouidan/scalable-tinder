package Commands.UserToUserCommands;

import Interface.ConcreteCommand;

public class GetInteraction extends ConcreteCommand{

    @Override
    public void setParameters() {
        storedProcedure = "\"uspReadInteraction\"";
        inputParams = new String[]{"interactionData.id"};
        outputName = "interaction";
        useCache=true;
    }
}
