package com.talismansoftwaresolutions.talismantasklist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TestServerActivity extends AppCompatActivity implements IJSONCallback{
    EditText txtUserName;
    EditText txtUserID;
    EditText txtEmail;
    EditText txtPassword;

    EditText txtResults;

    Button btnGetUserID;
    Button btnAddUser;

    TestServerActivity current;
    UserCLS user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_server);

        txtUserName = findViewById(R.id.txtUserName);
        txtUserID = findViewById(R.id.txtUserID);
        txtPassword = findViewById(R.id.txtUserPassword);
        txtEmail = findViewById(R.id.txtEmail);
        txtResults = findViewById(R.id.txtResults);

        btnGetUserID = findViewById(R.id.btnGetID);
        btnAddUser = findViewById(R.id.btnAddUser);

        user = new UserCLS();
        current = this;

        initButtons();
    }

    public void populateUser() {
        user.setUserName(txtUserName.getEditableText().toString());
        user.setUserPassword(txtPassword.getEditableText().toString());
        user.setUserEMail(txtEmail.getEditableText().toString());
    }

    public void initButtons() {
        btnGetUserID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                populateUser();
                TaskJSONParser.getUserIDFromToServer(user, current, current, 1);

            }
        });

        btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                populateUser();
                TaskJSONParser.addUserToServer(user, current, current, 1);

            }
        });



    }// end method

    @Override
    public void receiveJSONInfo(JSONObject response, int requestCode) {
        int newServerUserID = -1;
        int newLocalUserID = -1;


        //DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);

        JSONObject jobj = response;

        if(response == null) {
            Util.writeToLog("Error when adding new user to server");
            return;
        }

        Util.writeToLog("JSON string to be parsed: " + response.toString());

        if(requestCode == 1) {

            try {
                JSONArray arrData = response.getJSONArray("Status");
                JSONObject userObj = arrData.getJSONObject(0);
                JSONObject errorObj = arrData.getJSONObject(1);
                String strResponse = "User id: " + userObj.getInt("user_id") +
                        " Status: " + errorObj.getString("status");
                txtResults.setText(strResponse);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            /*
            if (newServerUserID >= 0) {
                //Add user to local db

                newLocalUserID = dbHelper.addUser(appUser.getUserName(), appUser.getUserPassword(), newServerUserID);

                if (newLocalUserID >= 0) {
                    Util.makeToast(this, Long.toString(newServerUserID) + " successfully registered!");
                    appUser.setUserID(newLocalUserID);
                    //initApp2();
                } else {
                    Util.makeToast(this, "Error: could not register you.");
                }
            }
            */
        }



    }

    @Override
    public void receiveJSONArray(JSONArray jsonArray) {

    }
}
