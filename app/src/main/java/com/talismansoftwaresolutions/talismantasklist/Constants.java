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
    public static final int ARCHIVED_TASKS   = 5;

    //Possible task statuses
    public static final int TASK_OPEN            = 115;
    public static final int TASK_COMPLETED       = 116;
    //public static final int TASK_NOT_COMPLETED = 117;
    public static final int TASK_DELETED         = 118;  //1
    public static final int TASK_UNDELETED       = 119;  //0
    public static final int TASK_ARCHIVED        = 120;

    //Version of this perpetual beta product :)
    public static final int VERSION = 1;

}