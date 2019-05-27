package com.talismansoftwaresolutions.talismantasklist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddTaskActivity extends AppCompatActivity {
    EditText txtView;
    Button btnSave;
    Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        txtView = findViewById(R.id.txtNewTaskDetails);
        btnSave = findViewById(R.id.btnSaveNewTask);
        //btnCancel = findViewById(R.id.btnCancelNewTask);

        initButtonsOnClicked();
    }

    private void initButtonsOnClicked() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                int user_id = intent.getIntExtra("user_id", 0);
                String newTask = txtView.getText().toString();

                TaskCLS task = new TaskCLS(newTask);
                task.setUserID(user_id);

                DataHolder dh = DataHolder.getInstance();
                dh.clear();
                dh.save("Task", task);

                returnResult(false);

            }
        });

        /*

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnResult(true);
            }
        });
        */
    }


    private void returnResult(boolean cancel) {

        Intent resultIntent = new Intent();
        //resultIntent.putExtra("result", result);

        if(cancel)
            setResult(RESULT_CANCELED, resultIntent);
        else
            setResult(RESULT_OK, resultIntent);

        finish();
    }
}
