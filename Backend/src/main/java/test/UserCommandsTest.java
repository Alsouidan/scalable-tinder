package test;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserCommandsTest {
    Helpers testHelpers = new Helpers();
    @Test
    @DisplayName("test CreateTransaction")
    public void testCreateTransaction(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject transactionData = new JSONObject();
        transactionData.put("user_id",1);
        transactionData.put("amount",500);
        body.put("transactionData",transactionData);
        expected.put("amount",500);
        expected.put("user_id",1);
        testHelpers.testCommand("CreateTransaction","User","transaction",body,expected,false,200);
    }

    @Test
    @DisplayName("CreateTransaction with invalid user id")
    public void createTransactionError(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject transactionData = new JSONObject();
        transactionData.put("user_id",100);
        transactionData.put("amount",500);
        body.put("transactionData",transactionData);
        testHelpers.testCommandNoReponse("CreateTransaction","User","error",body,expected,false,400);
    }

    @Test
    @DisplayName("Creating user Data")
    public void createUserData(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject userData = new JSONObject();
        String bio = "HI There";
        userData.put("bio",bio);
        body.put("userData",userData);
        testHelpers.testObjectCommand("CreateUserData","User","id",body,expected,false,200);

    }

    @Test
    @DisplayName("Delete existing Transaction ")
    public void deleteTransaction(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject transactionData = new JSONObject();
        transactionData.put("in_id",1);
        body.put("transactionData",transactionData);
        testHelpers.testCommand("DeleteTransaction","User","transaction",body,expected,false,200);
    }

    @Test
    @DisplayName("Delete non-existing Transaction ")
    public void deleteTransactionError(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject transactionData = new JSONObject();
        transactionData.put("in_id",1000);
        body.put("transactionData",transactionData);
        testHelpers.testCommandNoReponse("DeleteTransaction","User","transaction",body,expected,false,404);
    }


    @Test
    @DisplayName("Delete existing user")
    public void deleteUser(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject userData = new JSONObject();
        userData.put("id","5");
        body.put("userData",userData);
        JSONObject user = testHelpers.testObjectCommand("DeleteUserData","User","record",body,expected,false,200);
        assertTrue(user.getJSONObject("record").getString("key").equals("5"));

    }

    @Test
    @DisplayName("Delete non-existing user")
    public void deleteUserError(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject userData = new JSONObject();
        userData.put("id",500000);
        body.put("userData",userData);
        testHelpers.testCommandNoReponse("DeleteUserData","User","user",body,expected,false,400);
    }

    @Test
    @DisplayName("GetAllTransactions Test")
    public void getAllTransactions() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        body.put("page",0);
        body.put("limit",10);
        testHelpers.testListCommand("GetAllTransactions","User","transactions",body,expected,false,200);

    }
    @Test
    @DisplayName("GetAllUserNotifications Test")
    public void getAllUserNotifications() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        body.put("page",0);
        body.put("limit",10);
        JSONObject notificationData = new JSONObject();
        notificationData.put("userID",5);
        body.put("notification", notificationData) ;
        expected.put("userID",5);
        JSONArray array = testHelpers.testListCommand("GetAllUserNotifications","User","notifications",body,new JSONObject(),false,200);
        testHelpers.forAllArrayHolds(array,expected);
    }

    @Test
    @DisplayName("GetAllUserTransactions Test")
    public void getAllUserTransactions() {
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        body.put("page",0);
        body.put("limit",10);
        JSONObject transactionData = new JSONObject();
        transactionData.put("in_user_id",15);
        body.put("transactionData", transactionData) ;
        expected.put("user_id",15);
        JSONArray array = testHelpers.testListCommand("GetAllUserTransactions","User","transactions",body,new JSONObject(),false,200);
        testHelpers.forAllArrayHolds(array,expected);
    }

    @Test
    @DisplayName("Get existing transaction")
    public void GetTransactionCommand(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject transactionData = new JSONObject();
        transactionData.put("in_id",2);
        body.put("transactionData",transactionData);
        expected.put("id",2) ;
        testHelpers.testCommand("GetTransaction","User","transaction",body,expected,false,200);

    }


    @Test
    @DisplayName("Get existing UserData")
    public void getUserData(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject userData = new JSONObject();
        userData.put("id","1");
        body.put("userData",userData);
        expected.put("_key","1") ;
        testHelpers.testObjectCommandWithOutput("GetUserData","User","record",body,expected,false,200);

    }


    @Test
    @DisplayName("GetUserDataPaginated")
    public void getUserDataPaginated(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        body.put("page",0);
        body.put("limit",10);
        testHelpers.testListCommand("GetUserDataPaginated","User","users",body,expected,false,200);

    }

    @Test
    @DisplayName("SignIn with wrong password Test")
    public void signIn(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject userData = new JSONObject();
        userData.put("email","ariannagrande@gmail.com");
        userData.put("password","123456789");
        body.put("userData", userData);
        expected.put("error","Invalid Credentials");
        testHelpers.testObjectCommand("SignIn","User","error",body,expected,false,401);

    }

    @Test
    @DisplayName("SignIn with correct password Test")
    public void signInCorrect(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject userData = new JSONObject();
        userData.put("email","ariannagrande@gmail.com");
       userData.put("password","12345678");
        body.put("userData", userData);
        testHelpers.testObjectCommand("SignIn","User","token",body,expected,false,200);

    }


    @Test
    @DisplayName("Sign Up")
    public void signUp(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject userData = new JSONObject();
        userData.put("email","ariannagrande@gmail.com");
        userData.put("password","12345678");
        userData.put("firstName","Hi");
        userData.put("lastName","There");
        body.put("userData", userData);
        testHelpers.testObjectCommand("SignIn","User","token",body,expected,false,200);

    }

    @Test
    @DisplayName("Updating existing transaction")
    public void updateTransaction(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject transactionData = new JSONObject();
        transactionData.put("in_id",2);
        transactionData.put("in_amout",510);
        body.put("transactionData",transactionData);
        expected.put("in_amout",510);
        expected.put("in_id",2);
        testHelpers.testCommand("UpdateTransaction","User","transaction",body,expected,false,200);

    }

    @Test
    @DisplayName("Updating non-existing transaction")
    public void updateTransactionError(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject transactionData = new JSONObject();
        transactionData.put("in_id",1100);
        transactionData.put("in_amout",510);
        body.put("transactionData",transactionData);
        testHelpers.testCommandNoReponse("UpdateTransaction","User","transaction",body,expected,false,404);

    }


    @Test
    @DisplayName("Updating UserData")
    public void updateUserData(){
        JSONObject body = new JSONObject();
        JSONObject expected = new JSONObject();
        JSONObject userData = new JSONObject();
        String bio = "HI There UPDATED";
        userData.put("bio",bio);
        userData.put("_key","1");
        body.put("userData",userData);
        testHelpers.testObjectCommand("UpdateUserData","User","record",body,expected,false,200);

    }





}
