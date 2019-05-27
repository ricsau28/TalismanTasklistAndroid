package com.talismansoftwaresolutions.talismantasklist;

// Credit(s): https://codinginflow.com/tutorials/android/textinputlayout


import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout textInputEmail;
    private TextInputLayout textInputUsername;
    private TextInputLayout textInputPassword;
    private Button btnPassword;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = DatabaseHelper.getInstance(this);

        textInputEmail = findViewById(R.id.text_input_email);
        textInputUsername = findViewById(R.id.text_input_username);
        textInputPassword = findViewById(R.id.text_input_password);
        btnPassword = findViewById(R.id.btnPassword);

        btnPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmInput(v);
            }
        });
    }

    private boolean validateEmail() {
        String emailInput = textInputEmail.getEditText().getText().toString().trim();

        if (emailInput.isEmpty()) {
            textInputEmail.setError("Field can't be empty");
            return false;
        } else {
            textInputEmail.setError(null);
            return true;
        }
    }

    private boolean validateUsername() {
        String usernameInput = textInputUsername.getEditText().getText().toString().trim();

        if (usernameInput.isEmpty()) {
            textInputUsername.setError("Field can't be empty");
            return false;
        } else if (usernameInput.length() > 15) {
            textInputUsername.setError("Username too long");
            return false;
        } else {
            textInputUsername.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = textInputPassword.getEditText().getText().toString().trim();

        if (passwordInput.isEmpty()) {
            textInputPassword.setError("Field can't be empty");
            return false;
        } else {
            textInputPassword.setError(null);
            return true;
        }
    }

    public void confirmInput(View v) {
        if (!validateEmail() | !validateUsername() | !validatePassword()) {
            return;
        }

        String input = "Email: " + textInputEmail.getEditText().getText().toString();
        input += "\n";
        input += "Username: " + textInputUsername.getEditText().getText().toString();
        input += "\n";
        input += "Password: " + textInputPassword.getEditText().getText().toString();

        //Toast.makeText(this, input, Toast.LENGTH_SHORT).show();
        //Util.makeToast(this, input);

        String userName = textInputUsername.getEditText().getText().toString();

        if(true /*!isUserRegistered(userName)*/) {
            //Util.makeToast(this, userName + " is not registered!");

            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            //intent.putExtra("userName", userName);
            startActivityForResult(intent, 1);
        }
    }


    private boolean isUserRegistered(String userName) {
        UserCLS user = dbHelper.getUserByUsername(userName);
        return(user != null);
    }// end isUserRegistered




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //User was registered
                DataHolder dh = DataHolder.getInstance();
                UserCLS user = (UserCLS) dh.retrieve("user");
                if(user != null) {
                    textInputEmail.getEditText().setText(user.getUserEMail());
                    textInputUsername.getEditText().setText(user.getUserName());
                    textInputPassword.getEditText().setText(user.getUserPassword());
                }

            }
        }

    }// end onActivityResult
}