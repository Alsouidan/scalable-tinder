package test;

import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChatCommandsTest {
    
    Helpers testHelpers = new Helpers();

    @Test
    @DisplayName("CreateChat for unmatched users")
    public void testCreateErrorCommand(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject chatData = new JSONObject();
        chatData.put("userAId",13);
        chatData.put("userBId",1);
        expected.put("error","Cannot create chat between unmatched users");
        body.put("chatData",chatData);
        testHelpers.testObjectCommand("CreateChat","Chat","error",body,expected,true,400);

    }

    @Test
    @DisplayName("CreateChat for matched users")
    public void testCreateChatCommand(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject chatData = new JSONObject();
        chatData.put("userAId",11);
        chatData.put("userBId",15);
        body.put("chatData",chatData);
        testHelpers.testObjectCommand("CreateChat","Chat","id",body,expected,false,200);

    }

    @Test
    @DisplayName("Delete existing chat")
    public void testDeleteChatCommand(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject chatData = new JSONObject();
        chatData.put("id","17");
        body.put("chatData",chatData);
        JSONObject chat = testHelpers.testObjectCommand("DeleteChat","Chat","id",body,expected,false,200);
        assertTrue(chat.getJSONObject("chat").getString("key").equals("17"));
    }
    @Test
    @DisplayName("Delete non existing chat")
    public void testDeleteCommandError(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject chatData = new JSONObject();
        chatData.put("id","100");
        body.put("chatData",chatData);
        testHelpers.testObjectCommand("DeleteChat","Chat","id",body,expected,false,404);
    }

    @Test
    @DisplayName("Get existing chat")
    public void testGetChatCommand(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject chatData = new JSONObject();
        chatData.put("id","1");
        body.put("chatData",chatData);
        JSONObject chat = testHelpers.testObjectCommand("GetChat","Chat","id",body,expected,false,200);
        assertTrue(chat.getJSONObject("chat").getString("_key").equals("1"));
    }
    @Test
    @DisplayName("Get non existing chat")
    public void testGetChatCommandError(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject chatData = new JSONObject();
        chatData.put("id","100");
        body.put("chatData",chatData);
        testHelpers.testObjectCommand("GetChat","Chat","id",body,expected,false,404);
    }
}



