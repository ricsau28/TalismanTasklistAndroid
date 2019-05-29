package com.talismansoftwaresolutions.talismantasklist;

import android.content.Context;

public class TaskController {
    Context ctx;
    static TaskController instance;

    public static TaskController getInstance(Context context){
        if(instance == null)
            instance = new TaskController(context);

        return instance;
    }

    private TaskController(Context context) {
        this.ctx = context;
    }

    protected void changeTaskStatus(String taskName, int newStatus) {

        if(Util.stringIsNullOrEmpty(taskName))
            return;

        DatabaseHelper dbh = DatabaseHelper.getInstance(ctx);
        TaskListManager mgr = TaskListManager.getInstance(ctx);

        //int taskID = mgr.markTaskAsCompleted(taskName);
        int taskID = mgr.removeTask(taskName);//task.getTaskID();

        boolean success;
        if(newStatus == Constants.TASK_DELETED)
            success = dbh.updateTaskDeletionStatus(taskID, newStatus);
        else
            success = dbh.updateTaskStatus(taskID, newStatus);

        ((MainActivity)ctx).changeTaskStatus(taskName, success, newStatus);

    }

    //Our API

    public void archiveTask(String taskName) {
        changeTaskStatus(taskName, Constants.TASK_ARCHIVED);
    }

    public void unArchiveTask(String taskName) {
        changeTaskStatus(taskName, Constants.TASK_OPEN);
    }

    public void markTaskAsIncomplete(String taskName) {
       changeTaskStatus(taskName, Constants.TASK_OPEN);
    }

    public void markTaskAsCompleted(String taskName) {
        changeTaskStatus(taskName, Constants.TASK_COMPLETED);
    }

    public void unDeleteTask(String taskName) {
        changeTaskStatus(taskName, Constants.TASK_OPEN);

    }
    //Delete task from db and UI
    public void deleteTask(String taskName) {
        changeTaskStatus(taskName, Constants.TASK_DELETED);
    }//end deleteTask
}
