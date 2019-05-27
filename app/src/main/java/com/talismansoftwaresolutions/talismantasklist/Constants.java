package com.talismansoftwaresolutions.talismantasklist;

public class Constants {

    public static String TAG = "talismantasklist";

    public static final String DATABASE = "simpleTasks.db";  //TODO: Change db

    //Task showing modes
    public static final int OPEN_TASKS       = 0;
    public static final int COMPLETED_TASKS  = 1;
    public static final int DOWNLOADED_TASKS = 2;
    public static final int DELETED_TASKS    = 3;
    public static final int ALL_TASKS        = 4;

    //Possible task statuses
    public static final int TASK_OPEN            = 5;
    public static final int TASK_COMPLETED       = 6;
    //public static final int TASK_NOT_COMPLETED = 5;
    public static final int TASK_DELETED         = 7;  //1
    public static final int TASK_UNDELETED       = 8;  //0

    //Version of this perpetual beta product :)
    public static final int VERSION = 1;

}