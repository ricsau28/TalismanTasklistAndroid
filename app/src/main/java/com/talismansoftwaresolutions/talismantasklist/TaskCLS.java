package com.talismansoftwaresolutions.talismantasklist;

import android.net.ParseException;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Date;

public class TaskCLS {

    private int taskID;
    private int serverTaskID;
    private int userID;
    private int serverUserID;
    private String taskName;
    private String dateAdded;
    private String dateModified;
    private String dueDate;
    private String dateSynchronized;
    private int priority;
    private int status;
    private int categoryID;
    private int toDelete;
    private int orderNum;

    public TaskCLS() {}
    public TaskCLS(String task) { setTaskName(task); }

    public String getTaskName() { return taskName; }
    public void setTaskName(String _taskName) {taskName = _taskName;}


    public int getUserID(){ return userID; }
    public void setUserID(int userID){ this.userID = userID; }


    public int getServerUserID(){ return serverUserID; }
    public void setServerUserID(int serverUserID){ this.serverUserID = serverUserID; }

    public int getTaskID(){ return taskID; }
    public void setTaskID(int taskID) { this.taskID = taskID; }

    public int getServerTaskID(){ return serverTaskID; }
    public void setServerTaskID(int serverTaskID) { this.serverTaskID = serverTaskID; }

    public String getDueDate(){ return dueDate; }
    public void setDueDate(String _newDate) { this.dueDate = _newDate; }

    public String getDateSynchronized(){ return dateSynchronized; }
    public void setDateSynchronized(String _dateSynchronized) { this.dateSynchronized = _dateSynchronized; }


    public String getDateAdded(){ return dateAdded; }
    public void setDateAdded(String _newDate) { this.dateAdded = _newDate; }

    public String getDateModified(){ return dateModified; }
    public void setDateModified(String _newDate) { this.dateModified = _newDate; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public int getStatus() { return status;}
    public void setStatus(int status) { this.status = status;}

    public int getCategoryID() { return categoryID; }
    public void setCategoryID(int categoryID) { this.categoryID = categoryID;}

    public int getToDelete() { return toDelete; }
    public void setToDelete(int toDelete) { this.toDelete = toDelete;}

    //Adapted from: https://www.geeksforgeeks.org/equals-hashcode-methods-java/
    public boolean equals (Object obj) {
        if(this == obj)
            return true;

        if(obj == null || obj.getClass() != this.getClass())
            return false;

        TaskCLS task = (TaskCLS)obj;
        //return(task.taskID == this.taskID && task.taskName == this.taskName);
        return( task.taskID == this.taskID ||  task.taskName.equals(this.taskName));
    }

    public int hashCode() {
        return this.taskID;
    }

    public void update(TaskCLS editedTask) {
        this.setTaskName(editedTask.getTaskName());
    }

    public int getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(int orderNum) {
        this.orderNum = orderNum;
    }

    public static void setDefaultValues(TaskCLS newTask, UserCLS appUser) {
        newTask.setUserID(appUser.getUserID());
        newTask.setServerUserID(appUser.getserverUserID());
        newTask.setPriority(3);
        newTask.setStatus(Constants.TASK_OPEN);
        newTask.setDateAdded(Util.getCurrentDateTime());
        newTask.setOrderNum(0);
        newTask.setToDelete(0);
    }

}