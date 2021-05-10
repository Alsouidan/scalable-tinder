package Commands.UserCommands;

import Interface.ConcreteCommand;

public class GetTargetInteraction extends  ConcreteCommand{
    @Override
    public void setParameters() {
        storedProcedure = "\"uspReadUserTargetInteractions\"";
        inputParams = new String[]{"interactionData.target_id","page","limit"};
        outputName = "interactions";
        useCache=true;
    }
}
