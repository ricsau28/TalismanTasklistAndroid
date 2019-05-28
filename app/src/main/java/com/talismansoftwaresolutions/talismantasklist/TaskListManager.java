package com.talismansoftwaresolutions.talismantasklist;

import android.content.Context;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class TaskListManager {
    ArrayList<TaskCLS> taskList;
    static TaskListManager instance;
    Context context;

    private TaskListManager(Context c) {
        taskList = new ArrayList<>();
        context = c;
    }

    public static TaskListManager getInstance(Context c){
        if (instance == null)
            instance = new TaskListManager(c);

        return instance;
    }

    class sortByDateAdded implements Comparator<TaskCLS> {
        public int compare(TaskCLS a, TaskCLS b) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date dateFirst;
            Date dateSecond;

            try {
                dateFirst = sdf.parse(a.getDateAdded());
                dateSecond = sdf.parse(b.getDateAdded());
            }catch (java.text.ParseException ex) {
                Util.writeToLog(ex.getLocalizedMessage());
                return 0;
            }

            //TODO: Is there a version of compareTo to return longs??
            return (int) (dateFirst.getTime())/(1000 * 60) % 60 - (int)(dateSecond.getTime())/(1000 * 60) % 60;
        }
    }


    class sortByOrderNum implements Comparator<TaskCLS> {
        public int compare(TaskCLS a, TaskCLS b) {
            return b.getOrderNum() - a.getOrderNum();
        }
    }


    public void clear(){
        this.taskList.clear();
    }

    public boolean isEmpty() {
        return (this.taskList.isEmpty());
    }

    public ArrayList<TaskCLS> getTaskList(){
        return this.taskList;
    }

    private TaskCLS getTaskIndex(ArrayList<TaskCLS> taskList, Integer taskID) {
        TaskCLS task = null;

        for(int i = 0; i < taskList.size(); i++) {
            task = (TaskCLS) taskList.get(i);
            if(task.getTaskID() == taskID) {
                break;
            }
        }

        return task;
    }


    public void setTaskList(ArrayList<TaskCLS> newTaskList) {
        this.taskList = newTaskList;

        Collections.sort(taskList, new sortByOrderNum());

    }// setTaskList



    public int getSize() {
        return this.taskList.size();
    }

    public TaskCLS getTask(int position) {
        if(position >= 0) {
            return this.taskList.get(position);
        }

        return null;
    }


    private int findTaskPosition(TaskCLS taskToFind) {
        //TODO: store tasks in hashtable by task name
        String taskName = taskToFind.getTaskName();

        for(int i = 0; i < taskList.size(); i++) {
            if(taskName.equals( (taskList.get(i)).getTaskName()) ) {
                return i;
            }
        }
        return -1;
    }// end findTaskPosition


    public void removeTask(TaskCLS task) {
        int position = findTaskPosition(task);
        taskList.remove(position);
    }// end removeTask


    public void removeTask(int position){
        if(position >= 0) {
            this.taskList.remove(position);
        }
    }// end removeTask


    //TODO: Scrap current implentation of matching task ids to view order
    //      It's not working. There are duplicates populating the recyclerview
    //      LEFT OFF HERE, 5/26/2019
    public void addAll(ArrayList<TaskCLS> newList) {
        if(newList.isEmpty())
            return;

        this.taskList.addAll(newList);

        Collections.sort(taskList, new sortByOrderNum());
    }


    public void addTask(TaskCLS newTask) {
        int size;
        int newSize;

        ArrayList<TaskCLS> taskListClone = (ArrayList<TaskCLS>) taskList.clone();
        this.taskList.clear();
        this.taskList.add(newTask);
        this.taskList.addAll(taskListClone);

    }

    public void markTaskAsCompleted(int position) {
        TaskCLS task = taskList.get(position);
        task.setStatus(Constants.TASK_COMPLETED);
        task.setDateModified(Util.getCurrentDateTime());
        removeTask(position);
    }

    public void deleteTask(int position) {
        TaskCLS task = taskList.get(position);
        task.setStatus(Constants.TASK_DELETED);
        //task.setDateModified(Util.getCurrentDateTime());
        removeTask(position);
    }

    public boolean deleteTask(TaskCLS task) {
        if(task.getStatus() == Constants.TASK_DELETED)
            return false;

        DatabaseHelper dbh = DatabaseHelper.getInstance(context);

        if(dbh.updateTaskDeletionStatus(task.getTaskID(), Constants.TASK_DELETED)) {
            task.setStatus(Constants.TASK_DELETED);
            removeTask(task);
            return true;
        } else {
            return false;
        }

    }// end setTaskAsArchived



    public boolean unDeleteTask(TaskCLS task) {
        if(task.getStatus() != Constants.TASK_DELETED)
            return false;

        DatabaseHelper dbh = DatabaseHelper.getInstance(context);

        if(dbh.updateTaskDeletionStatus(task.getTaskID(), Constants.TASK_OPEN)) {
            task.setStatus(Constants.TASK_OPEN);
            removeTask(task);
            return true;
        } else {
            return false;
        }

    }// end setTaskAsArchived

    public boolean changeArchiveStatus(TaskCLS task, int archiveStatus) {

        if(archiveStatus != Constants.TASK_ARCHIVED && archiveStatus != Constants.TASK_OPEN)
            return false;

        DatabaseHelper dbh = DatabaseHelper.getInstance(context);

        if(dbh.updateTaskStatus(task.getTaskID(), archiveStatus)) {
            task.setStatus(archiveStatus);
            removeTask(task);
            return true;
        } else {
            return false;
        }

    }// end setTaskAsArchived


    public void swap(int oldPosition, int newPosition) {

        //TaskCLS taskAtOld = taskList.get(oldPosition);
        //TaskCLS taskAtNew = taskList.get(newPosition);

        /*
        DatabaseHelper dbh = DatabaseHelper.getInstance(context);

        //TODO: Move the database updates to onPause? onStop?
        dbh.updateTaskViewOrder(taskAtOld.getTaskID(), newPosition);
        dbh.updateTaskViewOrder(taskAtNew.getTaskID(), oldPosition);
        */

        Collections.swap(this.taskList, oldPosition, newPosition);
    }

}
