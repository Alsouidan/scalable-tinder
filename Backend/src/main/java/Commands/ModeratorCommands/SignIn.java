package Commands.ModeratorCommands;

import Entities.HttpResponseTypes;
import Interface.ConcreteCommand;
//import Models.User;
import at.favre.lib.crypto.bcrypt.BCrypt;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWT;
import org.json.JSONObject;

public class SignIn extends ConcreteCommand {

    @Override
    public void setParameters() {
        storedProcedure = "uspModLogin";
        inputParams = new String[] {"moderatorData.email"};
        outputName = "moderator";
    }

    @Override
    public void doCustomCommand() {
        if(responseJson.getJSONArray("moderator").getJSONObject(0).length() == 0) {
            System.out.println("HEREEEEEEEEEE 1");
            sendUnauthorized();
            return;
        }
        String hashedPassword = responseJson.getJSONArray("moderator").getJSONObject(0).getString("password");
        String plainTextPassword = (String) message.getParameter("moderatorData.password");
        BCrypt.Result result = BCrypt.verifyer().verify(plainTextPassword.toCharArray(), hashedPassword);
        if (result.verified) {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            String token = JWT.create()
                    .withClaim("moderatorId", responseJson.getJSONArray("moderator").getJSONObject(0).getInt("id"))
                    .withIssuer("auth0")
                    .sign(algorithm);
            responseJson = new JSONObject();
            responseJson.put("token", token);
            System.out.println("HEREEEEEEEEEE 3");    
        }
        else {
            System.out.println("HEREEEEEEEEEE 2");
            sendUnauthorized();
            return;
        }
    }

    public void sendUnauthorized() {
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("error", "Invalid Credentials");
        responseJson = errorResponse;
        status = HttpResponseTypes._401;
    }
}
