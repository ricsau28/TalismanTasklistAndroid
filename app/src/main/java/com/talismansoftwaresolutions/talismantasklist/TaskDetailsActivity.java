package com.talismansoftwaresolutions.talismantasklist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TaskDetailsActivity extends AppCompatActivity {
    EditText txtTaskView;
    Button btnSave;
    Button btnCancel;
    Button btnDelete;

    DataHolder dataHolder;
    TaskCLS taskObj;
    TaskListAdapter.TaskViewHolder taskHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        txtTaskView = findViewById(R.id.txtNewTaskDetails);

        btnSave = findViewById(R.id.btnSaveNewTask);
        btnCancel = findViewById(R.id.btnCancelNewTask);
        btnDelete = findViewById(R.id.btnDelete);
        iniButtonHandlers();

        dataHolder = DataHolder.getInstance();

        taskObj = (TaskCLS)dataHolder.retrieve("Task");
        taskHolder = (TaskListAdapter.TaskViewHolder) dataHolder.retrieve("ViewHolder");

        showTaskDetails();
    }

    private void iniButtonHandlers() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTask();
            }
        });

    }

    private void showTaskDetails() {
        txtTaskView.setText(taskObj.getTaskName());
    }

    private void deleteTask() {
        //taskHolder.itemRemoved();
        String taskName = txtTaskView.getText().toString().trim();
        TaskController taskController = TaskController.getInstance(this);
        taskController.deleteTask(taskName);
        finish();
    }

    private void saveTask() {
        String newText = txtTaskView.getText().toString().trim();
        if(newText != null && !newText.equals(taskObj.getTaskName())) {
            taskObj.setTaskName(newText);
            dataHolder.save("Task", taskObj);
            taskHolder.dataChanged(true);
        }

        finish();
    }
}
