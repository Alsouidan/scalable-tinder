package test;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ModeratorCommandsTest {
    Helpers testHelpers = new Helpers();

    @Test
    @DisplayName("CreateBan Test")
    public void testCreateCommand(){
            JSONObject body = new JSONObject();
            JSONObject expected = new JSONObject();
             String reason = "Ban reason";
             int userID = 6;
             String expiryDate="9/9/2022";
             JSONObject banData = new JSONObject();
             banData.put("reason",reason);
             banData.put("user_id",userID);
             banData.put("expiry_date", expiryDate);
             banData.put("moderator_id",1);
             body.put("banData",banData);
             expected.put("reason",reason);
             expected.put("user_id",userID);
             expected.put("moderator_id",1);
             testHelpers.testCommand("CreateBan","Moderator","ban",body,expected,false,200);
    }

    @Test
    @DisplayName("CreateInterest Test")
    public void testCreateInterest(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject interestInfo = new JSONObject();
        String name = "Test Interest";
        interestInfo.put("name",name);
        body.put("interestinfo",interestInfo);
        expected.put("name",name);
        testHelpers.testCommand("CreateInterest","Moderator","interest",body,expected,false,200);

    }


    @Test
    @DisplayName("DeleteBan Test")
    public void deleteBan(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject banData = new JSONObject();
        banData.put("id",1);
        expected.put("uspdeleteban",1);
        body.put("banData",banData);
        testHelpers.testCommand("DeleteBan","Moderator","ban",body,expected,false,200);
    }

    @Test
    @DisplayName("DeleteInterest Test")
    public void deleteInterest() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject interestInfo = new JSONObject();
        interestInfo.put("id",16);
        expected.put("uspdeleteinterest",16);
        body.put("interestinfo",interestInfo);
        testHelpers.testCommand("DeleteInterest","Moderator","interest",body,expected,false,200);
    }

    @Test
    @DisplayName("GetAllBans Test")
    public void getAllBans() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        body.put("page",0);
        body.put("limit",10);
        testHelpers.testListCommand("GetAllBans","Moderator","bans",body,expected,false,200);

    }

    @Test
    @DisplayName("GetAllInterests Test")
    public void getAllInterests() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        body.put("page",0);
        body.put("limit",10);
        testHelpers.testListCommand("GetAllInterests","Moderator","interests",body,expected,false,200);

    }

    @Test
    @DisplayName("GetAllNotifications Test")
    public void getAllNotifications() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        body.put("page",0);
        body.put("limit",12);
        testHelpers.testListCommand("GetAllNotifications","Moderator","notifications",body,expected,false,200);

    }

    @Test
    @DisplayName("GetBan Test")
    public void getBan(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject banData = new JSONObject();
        banData.put("id",2);
        expected.put("id",2);
        expected.put("moderator_id",1);
        expected.put("reason","racist bio_2");
        body.put("banData",banData);
        testHelpers.testCommand("GetBan","Moderator","ban",body,expected,false,200);
    }

    @Test
    @DisplayName("GetModeratorBans Test")
    public void getModeratorBans(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject banData = new JSONObject();
        body.put("page",0);
        body.put("limit",10);
        banData.put("id",1);
        body.put("banData",banData);
        JSONArray array = testHelpers.testListCommand("GetModeratorBans", "Moderator","moderatorBans",body,expected,false,200);
        JSONObject properties = new JSONObject();
        properties.put("moderator_id",1);
        testHelpers.forAllArrayHolds(array,properties);
    }

    @Test
    @DisplayName("GetModeratorUserBans Test")
    public void getModeratorUserBans(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject banData = new JSONObject();
        body.put("page",0);
        body.put("limit",10);
        banData.put("id",1);
        banData.put("user_id",12);
        body.put("banData",banData);
        JSONArray array = testHelpers.testListCommand("GetModeratorUserBans", "Moderator","moderatorUserBans",body,expected,false,200);
        JSONObject properties = new JSONObject();
        properties.put("moderator_id",1);
        properties.put("usr_id",12);
        testHelpers.forAllArrayHolds(array,properties);
    }

    @Test
    @DisplayName("GetUserBans Test")
    public void getUserBans(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject banData = new JSONObject();
        body.put("page",0);
        body.put("limit",10);
        banData.put("user_id",12);
        body.put("banData",banData);
        JSONArray array = testHelpers.testListCommand("GetUserBans", "Moderator","userBans",body,expected,false,200);
        JSONObject properties = new JSONObject();
        properties.put("usr_id",12);
        testHelpers.forAllArrayHolds(array,properties);
    }

    @Test
    @DisplayName("SignIn with wrong password Test")
    public void signIn(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject moderatorData = new JSONObject();
        moderatorData.put("email","hussein.badr@gmail.com");
        moderatorData.put("password","123456789");
        body.put("moderatorData", moderatorData);
        expected.put("error","Invalid Credentials");
        testHelpers.testObjectCommand("SignIn","Moderator","error",body,expected,false,401);

    }

    @Test
    @DisplayName("SignIn with correct password Test")
    public void signInCorrect(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject moderatorData = new JSONObject();
        moderatorData.put("email","hussein.badr@gmail.com");
        moderatorData.put("password","12345678");
        body.put("moderatorData", moderatorData);
        testHelpers.testObjectCommand("SignIn","Moderator","token",body,expected,false,200);

    }

    @Test
    @DisplayName("Sign up Test")
    public void signUp(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject moderatorData = new JSONObject();
        moderatorData.put("email","test@gmail.com");
        moderatorData.put("password","12345678");
        body.put("moderatorData", moderatorData);
        expected.put("email","test@gmail.com") ;
        testHelpers.testCommand("SignUp","Moderator","moderator",body,expected,false,200);
    }

//    @Test
//    @DisplayName("Update Ban Test")
//    public void updateBan(){
//        JSONObject body = new JSONObject();
//        JSONObject expected = new JSONObject();
//        JSONObject banData = new JSONObject();
//        String reason = "updated reason";
//        banData.put("id",3);
//        banData.put ("reason",reason);
//        expected.put("id",3);
//        expected.put("reason",reason);
//        body.put("banData",banData);
//        testHelpers.testCommand("UpdateBan","Moderator","ban",body,expected,false,200);
//    }

    @Test
    @DisplayName("UpdateInterest Test")
    public void updateInterest(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject interestinfo = new JSONObject();
        String name = "updatedName";
        interestinfo.put("id",15);
        interestinfo.put ("name",name);
        expected.put("id",15);
        expected.put("name",name);
        body.put("interestinfo",interestinfo);
        testHelpers.testCommand("UpdateInterest","Moderator","interest",body,expected,false,200);
    }
}
