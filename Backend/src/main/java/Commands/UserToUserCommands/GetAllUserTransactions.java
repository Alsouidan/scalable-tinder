package Commands.UserCommands;

import Interface.ConcreteCommand;

public class GetAllUserTransactions extends ConcreteCommand {
    @Override
    public void setParameters() {
        storedProcedure = "\"uspReadAllUserTransactions\"";
        inputParams = new String[]{
                "transactionData.in_user_id",
                "page",
                "limit"
        };
        outputName = "transactions";
        useCache = true;
    }
}
