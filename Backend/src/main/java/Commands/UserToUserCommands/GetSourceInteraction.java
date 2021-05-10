package Commands.UserCommands;

import Interface.ConcreteCommand;

public class GetSourceInteraction extends ConcreteCommand{

    @Override
    public void setParameters() {
        storedProcedure = "\"uspReadUserSourceInteractions\"";
        inputParams = new String[]{"interactionData.source_id","page","limit"};
        outputName = "interactions";
        useCache=true;
    }
}
