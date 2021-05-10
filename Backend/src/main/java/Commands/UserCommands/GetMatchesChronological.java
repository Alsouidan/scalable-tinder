package Commands.UserCommands;

import Interface.ConcreteCommand;


public class GetMatchesChronological extends ConcreteCommand{
    @Override
    public void setParameters() {
        storedProcedure = "\"uspSeeMatchesChronological\"";
        inputParams = new String[]{"userData.id"};
        outputName = "matches";
        useCache=true;
    }

}