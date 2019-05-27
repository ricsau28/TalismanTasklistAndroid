package com.talismansoftwaresolutions.talismantasklist;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RegistrationActivity extends AppCompatActivity {
    TextInputLayout txtUsername;
    TextInputLayout txtPassword;
    TextInputLayout txtEmail;
    Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        txtUsername = findViewById(R.id.text_input_username);
        txtPassword = findViewById(R.id.text_input_password);
        txtEmail = findViewById(R.id.text_input_email);
        btnConfirm = findViewById(R.id.btnConfirm);

        //Get value passed
        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        txtUsername.getEditText().setText(userName);

        //Set onClickListener
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DataHolder dh = DataHolder.getInstance();
                dh.clear();

                UserCLS user = new UserCLS();
                user.setUserName(txtUsername.getEditText().getText().toString());
                user.setUserPassword(txtPassword.getEditText().getText().toString());
                user.setUserEMail(txtEmail.getEditText().getText().toString());

                dh.save("user", user);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("result", user.getUserName());
                setResult(RESULT_OK, resultIntent);
                finish();

            }
        });
    }
}