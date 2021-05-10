package Commands.UserCommands;
import Interface.ConcreteCommand;

public class DeleteInteraction extends ConcreteCommand{

    @Override
    public void setParameters() {
        storedProcedure = "\"uspDeleteInteraction\"";
        inputParams = new String[]{"interactionData.id"};
        outputName = "interaction";
    }
}
