package Commands.UserCommands;

import Interface.ConcreteCommand;

public class GetAllInteraction extends ConcreteCommand{

    @Override
    public void setParameters() {
        storedProcedure = "\"uspReadInteractions\"";
        inputParams = new String[]{"page","limit"};
        outputName = "interactions";
        useCache = true;
    }
}
