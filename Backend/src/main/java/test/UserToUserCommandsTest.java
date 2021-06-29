package test;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserToUserCommandsTest {
    Helpers testHelpers = new Helpers();

    @Test
    @DisplayName("Create Block")
    public void testCreateBlockCommand() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject blockData = new JSONObject();
        blockData.put("source_user_id", 3);
        blockData.put("target_user_id", 16);
        blockData.put("created_at", new Date().toLocaleString());
        body.put("blockData", blockData);
        testHelpers.testObjectCommand("CreateBlock", "UserToUser", "id", body, expected, false, 200);

    }

    @Test
    @DisplayName("Creates a Block with an invalid source id")
    public void testCreateBlockCommandSourceError() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject blockData = new JSONObject();
        blockData.put("source_user_id", 200);
        blockData.put("target_user_id", 16);
        blockData.put("created_at", new Date().toLocaleString());
        body.put("blockData", blockData);
        testHelpers.testCommandNoReponse("CreateBlock", "UserToUser", "error", body, expected, false, 400);

    }

    @Test
    @DisplayName("Creates a Block with an invalid target id")
    public void testCreateBlockCommandTargetError() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject blockData = new JSONObject();
        blockData.put("source_user_id", 3);
        blockData.put("target_user_id", 200);
        blockData.put("created_at", new Date().toLocaleString());
        body.put("blockData", blockData);
        testHelpers.testCommandNoReponse("CreateBlock", "UserToUser", "error", body, expected, false, 400);

    }

    @Test
    @DisplayName("Get existing source blocks")
    public void GetSourceBlockCommand() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject blockData = new JSONObject();
        blockData.put("in_source_user", 3);
        body.put("blockData", blockData);
        body.put("page", 0);
        body.put("limit", 3);
        expected.put("source_user_id", 3);
        JSONArray array = testHelpers.testListCommand("GetSourceBlocks", "UserToUser", "blocks", body, new JSONObject(), false, 200);
        testHelpers.forAllArrayHolds(array, expected);
    }

    @Test
    @DisplayName("Get  non-existing source blocks")
    public void GetSourceBlockCommandError() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject blockData = new JSONObject();
        blockData.put("in_source_user", 13);
        body.put("blockData", blockData);
        body.put("page", 0);
        body.put("limit", 3);
        expected.put("source_user_id", 13);
        testHelpers.testCommandNoReponse("GetSourceBlocks", "UserToUser", "blocks", body, new JSONObject(), false, 404);

    }

    @Test
    @DisplayName("Get existing Target blocks")
    public void GetTargetBlockCommand() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject blockData = new JSONObject();
        blockData.put("in_target_user", 18);
        body.put("blockData", blockData);
        body.put("page", 0);
        body.put("limit", 3);
        expected.put("target_user_id", 18);
        JSONArray array = testHelpers.testListCommand("GetTargetBlocks", "UserToUser", "blocks", body, new JSONObject(), false, 200);
        testHelpers.forAllArrayHolds(array, expected);
    }

    @Test
    @DisplayName("Get  non-existing target blocks")
    public void GetTargetBlockCommandError() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject blockData = new JSONObject();
        blockData.put("in_target_user", 13);
        body.put("blockData", blockData);
        body.put("page", 0);
        body.put("limit", 3);
        expected.put("target_user_id", 13);
        testHelpers.testCommandNoReponse("GetTargetBlocks", "UserToUser", "blocks", body, new JSONObject(), false, 404);

    }

    @Test
    @DisplayName("Get All  blocks")
    public void GetAllBlockCommand() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        body.put("page", 0);
        body.put("limit", 3);
        testHelpers.testListCommand("GetAllBlocks", "UserToUser", "blocks", body, expected, false, 200);
    }

    @Test
    @DisplayName("Delete an existing Block")
    public void DeleteBlockCommand() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject blockData = new JSONObject();
        blockData.put("in_source", 19);
        blockData.put("in_target", 18);
        body.put("blockData", blockData);
        testHelpers.testCommandNoReponse("DeleteBlock", "UserToUser", "block", body, expected, false, 200);
    }

    @Test
    @DisplayName("Delete non existing Block")
    public void DeleteBlockCommandError() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject blockData = new JSONObject();
        blockData.put("in_source", 200);
        blockData.put("in_target", 18);
        body.put("blockData", blockData);
        testHelpers.testCommand("DeleteBlock", "UserToUser", "block", body, expected, false, 404);
    }

    @Test
    @DisplayName("Create Interaction")
    public void CreateInteractionCommand() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject interactionData = new JSONObject();
        interactionData.put("source_user_id", 19);
        interactionData.put("target_user_id", 18);
        interactionData.put("type", "like");
        body.put("interactionData", interactionData);
        testHelpers.testCommand("CreateInteraction", "UserToUser", "interaction", body, expected, false, 200);
    }

    @Test
    @DisplayName("Get All  interactions")
    public void GetAllInteractionsCommand() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        body.put("page", 0);
        body.put("limit", 3);
        testHelpers.testListCommand("GetAllInteractions", "UserToUser", "interactions", body, expected, false, 200);
    }

    @Test
    @DisplayName("Get interaction")
    public void GetInteractionsCommand() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject interactionData = new JSONObject();
        interactionData.put("id", 3);
        body.put("interactionData", interactionData);
        expected.put("id", 3);
        testHelpers.testCommand("GetInteraction", "UserToUser", "interaction", body, expected, false, 200);

    }

    @Test
    @DisplayName("Get non-existent interaction")
    public void GetInteractionsCommandError() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject interactionData = new JSONObject();
        interactionData.put("id", 200);
        body.put("interactionData", interactionData);
        expected.put("id", 200);
        testHelpers.testCommandNoReponse("GetInteraction", "UserToUser", "interaction", body, expected, false, 404);

    }

    @Test
    @DisplayName("Get existing source interactions")
    public void GetSourceInteractionsCommand() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject interactionData = new JSONObject();
        interactionData.put("source_id", 16);
        body.put("interactionData", interactionData);
        body.put("page", 0);
        body.put("limit", 3);
        expected.put("source_user_id", 16);
        JSONArray array = testHelpers.testListCommand("GetSourceInteractions", "UserToUser", "interactions", body, new JSONObject(), false, 200);
        testHelpers.forAllArrayHolds(array, expected);
    }
    @Test
    @DisplayName("Get non existing source interactions")
    public void GetSourceInteractionsCommandError(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject interactionData = new JSONObject();
        interactionData.put("source_id", 200);
        body.put("interactionData", interactionData);
        body.put("page", 0);
        body.put("limit", 3);
        expected.put("source_user_id", 200);
        testHelpers.testCommandNoReponse("GetSourceInteractions", "UserToUser", "interactions", body, new JSONObject(), false, 404);

    }
    @Test
    @DisplayName("Get existing target interactions")
    public void GetTargetInteractionsCommand() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject interactionData = new JSONObject();
        interactionData.put("target_id", 16);
        body.put("interactionData", interactionData);
        body.put("page", 0);
        body.put("limit", 3);
        expected.put("target_user_id", 16);
        JSONArray array = testHelpers.testListCommand("GetTargetInteractions", "UserToUser", "interactions", body, new JSONObject(), false, 200);
        testHelpers.forAllArrayHolds(array, expected);
    }
    @Test
    @DisplayName("Get non existing target interactions")
    public void GetTargetInteractionsCommandError(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject interactionData = new JSONObject();
        interactionData.put("target_id", 200);
        body.put("interactionData", interactionData);
        body.put("page", 0);
        body.put("limit", 3);
        expected.put("target_user_id", 200);
        testHelpers.testCommandNoReponse("GetTargetInteractions", "UserToUser", "interactions", body, new JSONObject(), false, 404);

    }

    @Test
    @DisplayName("Updating existing interaction")
    public void UpdateInteractionCommand(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject interactionData = new JSONObject();
        interactionData.put("type","dislike");
        interactionData.put("id",3);
        body.put("interactionData",interactionData);
        expected.put("type","dislike");
        testHelpers.testCommand("UpdateInteraction","UserToUser","interaction",body,expected,false,200);

    }
    @Test
    @DisplayName("Updating non existing interaction")
    public void UpdateInteractionCommandError(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject interactionData = new JSONObject();
        interactionData.put("type","dislike");
        interactionData.put("id",100);
        body.put("interactionData",interactionData);
        expected.put("type","dislike");
        testHelpers.testCommandNoReponse("UpdateInteraction","UserToUser","interaction",body,expected,false,404);

    }

    @Test
    @DisplayName("Delete an existing interaction")
    public void DeleteInteractionCommand() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject interactionData = new JSONObject();
        interactionData.put("id", 1);
        body.put("interactionData", interactionData);
        testHelpers.testCommand("DeleteInteraction","UserToUser","interaction",body,expected,false,200);

    }

    @Test
    @DisplayName("Delete non existing interaction")
    public void DeleteInteractionCommandError() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject interactionData = new JSONObject();
        interactionData.put("id", 100);
        body.put("interactionData", interactionData);
        testHelpers.testCommandNoReponse("DeleteInteraction","UserToUser","interaction",body,expected,false,404);

    }

    @Test
    @DisplayName("Create Report")
    public void CreateReportCommand() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject reportData = new JSONObject();
        reportData.put("source_user_id", 19);
        reportData.put("target_user_id", 18);
        reportData.put("reason", "Hate Speech");
        reportData.put("created_at",new Date());
        body.put("reportData", reportData);
        testHelpers.testCommand("CreateReport", "UserToUser", "report", body, expected, false, 200);
    }

    @Test
    @DisplayName("Get Reported")
    public void GetReportCommand() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        body.put("page", 0);
        body.put("limit", 3);
        testHelpers.testListCommand("GetAllReports", "UserToUser", "reports", body, expected, false, 200);

    }

    @Test
    @DisplayName("Updating existing Report")
    public void UpdateReportCommand(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject reportData = new JSONObject();
        reportData.put("reason","Spamming");
        reportData.put("id",2);
        body.put("reportData",reportData);
        expected.put("reason","Spamming");
        testHelpers.testCommand("UpdateReport","UserToUser","report",body,expected,false,200);

    }
    @Test
    @DisplayName("Updating non existing Report")
    public void UpdateReportCommandError(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject reportData = new JSONObject();
        reportData.put("reason","Spamming");
        reportData.put("id",100);
        body.put("reportData",reportData);
        expected.put("reason","Spamming");
        testHelpers.testCommandNoReponse("UpdateReport","UserToUser","report",body,expected,false,404);

    }

    @Test
    @DisplayName("Delete an existing Report")
    public void DeleteReportCommand() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject reportData = new JSONObject();
        reportData.put("id", 1);
        body.put("reportData", reportData);
        testHelpers.testCommand("DeleteReport","UserToUser","report",body,expected,false,200);

    }
    @Test
    @DisplayName("Delete a non existing Report")
    public void DeleteReportCommandError() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject reportData = new JSONObject();
        reportData.put("id", 1000);
        body.put("reportData", reportData);
        testHelpers.testCommandNoReponse("DeleteReport","UserToUser","report",body,expected,false,404);

    }

    @Test
    @DisplayName("Get Matches")
    public void GetMatchesCommand() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject userData = new JSONObject();
        userData.put("id", 15);
        body.put("userData", userData);
        testHelpers.testCommand("GetMatches", "UserToUser", "matches", body, expected, false, 200);

    }
    @Test
    @DisplayName("Get Matches of non existent user")
    public void GetMatchesCommandError() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject userData = new JSONObject();
        userData.put("id", 100);
        body.put("userData", userData);
        testHelpers.testCommandNoReponse("GetMatches", "UserToUser", "matches", body, expected, false, 404);

    }

    @Test
    @DisplayName("Create Profile View")
    public void CreateProfileViewCommand() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject profileViewsData = new JSONObject();
        profileViewsData.put("_key","100");
        profileViewsData.put("viewedUser", "2");
        profileViewsData.put("viewers","[]");
        body.put("profileViewsData", profileViewsData);
        testHelpers.testObjectCommand("CreateProfileView", "UserToUser", "id", body, expected, false, 200);
    }

    

    @Test
    @DisplayName("Delete existing profile view")
    public void testDeleteProfileViewCommand(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject profileViewsData = new JSONObject();
        profileViewsData.put("id","16");
        body.put("profileViewsData",profileViewsData);
        JSONObject profileView = testHelpers.testObjectCommand("DeleteProfileViews","UserToUser","id",body,expected,false,200);
        assertTrue(profileView.getJSONObject("record").getString("key").equals("16"));
    }
    @Test
    @DisplayName("Delete non existing profile view")
    public void testDeleteProfileViewCommandError(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject profileViewsData = new JSONObject();
        profileViewsData.put("id","1134");
        body.put("profileViewsData",profileViewsData);
        testHelpers.testCommandNoReponse("DeleteProfileViews","UserToUser","id",body,expected,false,404);
    }

    @Test
    @DisplayName("Get existing profile view")
    public void GetProfileViewsCommand(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject profileViewsData = new JSONObject();
        profileViewsData.put("id","15");
        body.put("profileViewsData",profileViewsData);
        JSONObject profileViews = testHelpers.testObjectCommand("GetProfileViews","UserToUser","id",body,expected,false,200);
        assertTrue(profileViews.getJSONObject("record").getString("_key").equals("15"));
    }

    @Test
    @DisplayName("Get non-existing profile view")
    public void GetProfileViewsCommandError(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject profileViewsData = new JSONObject();
        profileViewsData.put("id","163");
        body.put("profileViewsData",profileViewsData);
        testHelpers.testCommandNoReponse("GetProfileViews","UserToUser","id",body,expected,false,404);
    }

    @Test
    @DisplayName("Get All Profile Views")
    public void GetAllProfileViewsCommand(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        body.put("page",0);
        body.put("limit",4);
        testHelpers.testListCommand("GetAllProfileViews","UserToUser","profileViews",body,expected,false,200);
    }

    @Test
    @DisplayName("Update existing profile view")
    public void UpdateProfileViewsCommand(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject profileViewsData = new JSONObject();
        profileViewsData.put("id","16");
        profileViewsData.put("viewers","[\"23\",\"43\"]");
        body.put("profileViewsData",profileViewsData);
        testHelpers.testObjectCommand("GetProfileViews","UserToUser","id",body,expected,false,200);
    }
    @Test
    @DisplayName("Update non existing profile view")
    public void UpdateProfileViewsCommandError(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject profileViewsData = new JSONObject();
        profileViewsData.put("id","1623");
        profileViewsData.put("viewers","[\"23\",\"43\"]");
        body.put("profileViewsData",profileViewsData);
        testHelpers.testCommandNoReponse("GetProfileViews","UserToUser","id",body,expected,false,404);
    }
}
