package Commands.ModeratorCommands;

import Interface.ConcreteCommand;
import Models.BanData;
import Models.Message;
import com.google.gson.JsonObject;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GetAllBans extends ConcreteCommand {

    @Override
    protected void doCommand() {
        try {
            dbConn = PostgresInstance.getDataSource().getConnection();
            dbConn.setAutoCommit(true);
            Statement query = dbConn.createStatement();
            query.setPoolable(true);
            BanData in_banData = message.getBanData();
            set = query.executeQuery(String.format("SELECT * FROM \"uspReadAllBans\"('%s','%s')",
                    message.getPage(),
                    message.getLimit()
                    )
                    );
            List<BanData> out_banData_list = new ArrayList<BanData>();
            while(set.next()){
                BanData out_banData = new BanData();
                out_banData.setId(set.getInt("id"));
                out_banData.setModerator_id(set.getInt("moderator_id"));
                out_banData.setReason(set.getString("reason"));
                out_banData.setUser_id(set.getInt("user_id"));
                out_banData.setCreated_at((set.getTimestamp("created_at")));
                out_banData.setExpiry_date(set.getDate("expiry_date"));
                out_banData_list.add(out_banData);
            }

            JsonObject response = new JsonObject();
            System.out.println("BEFORE"+gson.toJson(out_banData_list));
            response.add("record", jsonParser.parse(gson.toJson(out_banData_list)));
            responseJson = response;
            System.out.println(response + "ALOOO");
        }catch (SQLException e) {
            e.printStackTrace();
//            CommandsHelp.handleError(map.get("app"), map.get("method"), e.getMessage(), map.get("correlation_id"), LOGGER);
            //Logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            PostgresInstance.disconnect(null, proc, dbConn);
        }
    }

    @Override
    public void setMessage(Message message) {

    }
}