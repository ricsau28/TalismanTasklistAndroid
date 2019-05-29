package com.talismansoftwaresolutions.talismantasklist;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.SyncStateContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String TASKS_TABLE = "tasks";
    public static final String USERS_TABLE = "users";
    public static final String CATEGORIES_TABLE = "categories";

    private static DatabaseHelper instance;

    private DatabaseHelper(Context context) {
        super(context, Constants.DATABASE, null, Constants.VERSION);
    }

    public static DatabaseHelper getInstance(Context ctx) {
        if(instance == null)
            instance = new DatabaseHelper(ctx);

        return  instance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        if(!createTables(db)) {
            Util.writeToLog("DatabaseHelper: Could not create tables");
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE);
        onCreate(db);
    }


    private SQLiteDatabase doGetWritableDatabase() {
        SQLiteDatabase db = null;

        try{
            db = this.getWritableDatabase();
        } catch (SQLiteException slex) {
            Util.writeToLog("DatabaseHelper.getWritableDatabase: " + slex.getMessage());
        }

        return db;
    }// end


    public void executeSQL(String sqlString){
        if(Util.stringIsNullOrEmpty(sqlString)) return;

        SQLiteDatabase db = null;

        try{
            db = this.getWritableDatabase();
            db.execSQL(sqlString);
        } catch (SQLiteException slex) {
            Util.writeToLog("DatabaseHelper.executeSQL: " + slex.getMessage());
        }

    }// end executeSQL

    public void updateTaskOrder() {
        String query;
        String sql;
        int count = -1;

        query = "SELECT COUNT(*) FROM tasks;";

        try {
            SQLiteDatabase db = doGetWritableDatabase();
            Cursor cur = db.rawQuery(query, null);
            if(cur.moveToFirst()) {
                count = cur.getInt(0);
            }
            cur.close();
            sql = "UPDATE tasks SET order_num=" + String.valueOf(count) + " WHERE order_num = 0";
            db.execSQL(sql);
        } catch (SQLiteException slex) {
            Util.writeToLog("updateTaskOrder: " + slex.getMessage());
        }

    }// end updateTaskOrder


    public boolean addTask(TaskCLS task) {
        boolean debugPoint = true;

        if(task.getTaskID() == 401) {
            debugPoint = true;
        }

        SQLiteDatabase db = doGetWritableDatabase();
        //String dueDate = Util.stringIsNullOrEmpty(task.getDueDate()) ? "" : task.getDueDate().trim();

        try {
        String sqlStr  = "INSERT INTO " + TASKS_TABLE +
                " (user_id, task_name, date_added, due_date, priority, status, to_delete) " +
                "VALUES ("+ task.getUserID() + ",' " +
                            task.getTaskName().trim()  + "', '" +
                            task.getDateAdded() + "', '" +
                            //dueDate + "', " +
                            task.getDueDate() + "', " +
                            task.getPriority() + ", " +
                            task.getStatus() + ", " +
                            task.getToDelete() + ")";

        Util.writeToLog("addData: Adding " + sqlStr + " to " + TASKS_TABLE);

        //try {
            db.execSQL(sqlStr);
        } catch (NullPointerException npex) {
            debugPoint = true;
            Util.writeToLog("Failed when attempting to add: " + task.getTaskName());
            Util.writeToLog(npex.getMessage());
            return false;

        } catch (SQLiteException sqlEx) {
            debugPoint = true;
            Util.writeToLog("Failed when attempting to add: " + task.getTaskName());
            Util.writeToLog(sqlEx.getMessage());
            return false;
        }

        return true;
    }// end addTask


    public void deleteTask(int id){
        SQLiteDatabase db = doGetWritableDatabase();

        //String query = "DELETE FROM " + TASKS_TABLE + " WHERE _id=" + String.valueOf(id);
        String query = "UPDATE " + TASKS_TABLE + " SET status = " + Constants.TASK_DELETED + ", " +
                       "to_delete = 1 " +
                       "WHERE _id=" + String.valueOf(id);

        Util.writeToLog("deleteName: query: " + query);
        Util.writeToLog("deleteName: Deleting taskID " + String.valueOf(id) + " from database.");

        db.execSQL(query);
    }


    public void updateTaskName(int id, String newName){
        SQLiteDatabase db = doGetWritableDatabase();

        String query = "UPDATE " + TASKS_TABLE + " SET task_name = '" + newName.trim() +
                "', modification_date = '" + Util.getCurrentDateTime() + "' WHERE _id=" + String.valueOf(id) ;

        Util.writeToLog("updateName: query: " + query);
        Util.writeToLog("updateName: Setting name to " + newName);

        //TODO: Also update modification date as well

        db.execSQL(query);
    }//end updateTask


    public boolean updateTaskDeletionStatus(int id, int newStatus){
        //TODO: decide on having a to_delete flag OR TASK_DELETED status...not both
        int to_delete = 0;
        boolean deleted = true;

        if(id < 0)
            return false;

        if(newStatus == Constants.TASK_DELETED)
            to_delete =  1;


        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TASKS_TABLE + " SET to_delete= " + to_delete + ", " +
                       "status = " + newStatus + " " +
                       "WHERE _id=" + String.valueOf(id) ;

        Util.writeToLog("updateTaskDeletionStatus: query: " + query);

        try {
            db.execSQL(query);
        }catch (SQLiteException slex) {
            Util.writeToLog("updateTaskDeletionStatus: error: " + slex.getMessage());
            deleted = false;
        }

        //TODO: Also update modification date as well

        return deleted;

    }// end updateTaskDeletionStatus


    public boolean updateTaskStatus(int id, int newStatus){
        boolean updated = true;
        String query = null;

        SQLiteDatabase db = this.getWritableDatabase();
        if(newStatus != Constants.TASK_DELETED) {
            query = "UPDATE " + TASKS_TABLE + " SET status= " + newStatus + ", " +
                    "modification_date='" + Util.getCurrentDateTime() + "', " +
                    "to_delete = 0 " +
                    "WHERE _id=" + String.valueOf(id);
        }

        Util.writeToLog("updateTaskStatus: query: " + query);

        try {
            db.execSQL(query);
        } catch (SQLiteException slex) {
            Util.writeToLog("updateTaskStatus: error: " + slex.getMessage());
            updated = false;
        }

        //TODO: Also update modification date as well

        return updated;

    }// end updateTaskStatus



    //NB: This version of createTables is called externally
    public boolean createTables() {
        boolean successFlag = true;

        SQLiteDatabase db = doGetWritableDatabase();

        String sqlCategories = createTaskCategoriesTableSQL();
        String sqlTasks = createTaskTableSQL();
        String sqlUsers = createUsersTableSQL();

        String [] queries = new String[]{sqlCategories, sqlUsers, sqlTasks};

        try {
            for(String query : queries)
                db.execSQL(query);

        } catch (SQLiteException slex) {
            successFlag = false;
            Util.writeToLog(Constants.TAG + " " + slex.getMessage());
        }

        return successFlag;
    }// end createTaskTable


    //NB: This version of createTables is called internally
    public boolean createTables(SQLiteDatabase db) {
        boolean successFlag = true;

        String sqlCategories = createTaskCategoriesTableSQL();
        String sqlTasks = createTaskTableSQL();
        String sqlUsers = createUsersTableSQL();

        String [] queries = new String[]{sqlCategories, sqlUsers, sqlTasks};

        try {
            for(String query : queries)
                db.execSQL(query);

        } catch (SQLiteException slex) {
            successFlag = false;
            Util.writeToLog(Constants.TAG + " " + slex.getMessage());
        }

        return successFlag;
    }// end createTaskTable


    public String createTaskCategoriesTableSQL() {
        return(
                "CREATE TABLE IF NOT EXISTS " + CATEGORIES_TABLE +
                        " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "category_name TEXT UNIQUE, " +
                        "date_added DATE DEFAULT (datetime('now','localtime')), " +
                        "modification_date DATE DEFAULT (datetime('now','localtime')), " +
                        "modified_by TEXT, " +
                        "to_delete INTEGER DEFAULT 0)"
        );
    }// end

    public String createTaskTableSQL() {
        return (
                "CREATE TABLE IF NOT EXISTS " + TASKS_TABLE +
                        " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "server_task_id INTEGER DEFAULT 0, " +
                        "server_user_id INTEGER DEFAULT 0, " +
                        "user_id INTEGER DEFAULT 0, " +
                        "category_id INTEGER DEFAULT 0, " +
                        "task_name TEXT NOT NULL UNIQUE, " +
                        "tags TEXT, " +
                        "due_date DATE, " +
                        "priority INTEGER DEFAULT 3, " +
                        "status INTEGER DEFAULT 0, " +
                        "date_added DATE DEFAULT (datetime('now','localtime')), " +
                        "modification_date DATE DEFAULT (datetime('now','localtime')), " +
                        "modified_by TEXT, " +
                        "date_synchronized DATE DEFAULT (datetime('now','localtime')), " +
                        "to_delete INTEGER DEFAULT 0)"
        );
    }


    public String createUsersTableSQL(){
        return (
                "CREATE TABLE IF NOT EXISTS " + USERS_TABLE +
                        " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_name TEXT NOT NULL UNIQUE, " +
                        "user_password TEXT NOT NULL, " +
                        "user_email TEXT, " +
                        "date_added DATE DEFAULT (datetime('now','localtime')), " +
                        "modification_date DATE DEFAULT (datetime('now','localtime')), " +
                        "modified_by TEXT, " +
                        "to_delete INTEGER DEFAULT 0)"
        );
    }// end createUsersTableSQL

    //TODO: To be used for testing only:
    public void changeServerUserID(int newServerID) {
        SQLiteDatabase db = doGetWritableDatabase();

        String sql = "UPDATE tasks SET server_user_id=" + newServerID;

        Util.writeToLog("DatabaseHelper.changeServerUserID: " + sql);

        try {
            db.execSQL(sql);
        } catch(SQLiteException slex) {
            Util.writeToLog("DatabaseHelper.changeServerUserID: " + slex.getMessage());
        }
    }


    public boolean updateTaskViewOrder(int taskID, int newPosition) {
        SQLiteDatabase db = doGetWritableDatabase();

        String sql = "UPDATE tasks SET order_num=" + newPosition + " WHERE _id=" + taskID;

        Util.writeToLog("DatabaseHelper.updateTaskViewOrder: " + sql);

        try {
            db.execSQL(sql);
        } catch(SQLiteException slex) {
            Util.writeToLog("DatabaseHelper.updateTaskViewOrder: " + slex.getMessage());
            return false;
        }

        return true;
    }// end updateTaskViewOrder


    //Returns the user of the app as reflected in the database
    public UserCLS getAppUser() {

        UserCLS user = null;
        boolean debugPoint;

        //There should only be one user, but...
        String query = "SELECT _id, user_name FROM users ORDER BY date_added ASC LIMIT 1";

        SQLiteDatabase db = doGetWritableDatabase();
        Util.writeToLog("DatabaseHelper.getUserByUsername: " + query);

        debugPoint = true;

        try {
            Cursor cur = db.rawQuery(query, null);
            if(cur.moveToFirst()) {
                user = new UserCLS();
                user.setUserID(cur.getInt(0));
                user.setUserName(cur.getString(1));
            }
        } catch (SQLiteException sqlEx) {
            Util.writeToLog("DatabaseHelper.getUserByUsername: " + sqlEx.getMessage());
        }

        return user;
    }

    public int getUserID(){
        int userID = -1;
        boolean debugPoint;

        String query = "SELECT _id FROM users LIMIT 1";

        SQLiteDatabase db = doGetWritableDatabase();
        Util.writeToLog("DatabaseHelper.getUserByUsername: " + query);

        debugPoint = true;

        try {
            Cursor cur = db.rawQuery(query, null);
            if(cur.moveToFirst()) {
                userID = cur.getInt(0);
            }
        } catch (SQLiteException sqlEx) {
            Util.writeToLog("DatabaseHelper.getUserByUsername: " + sqlEx.getMessage());

        }

        return userID;

    }//end getUserID


    public UserCLS getUserByUsername(String userName){
        UserCLS user = null;
        boolean debugPoint;

        if(Util.stringIsNullOrEmpty(userName))
            return null;

        userName = userName.trim();

        String sql = "SELECT _id, user_name, user_password FROM users WHERE user_name='" + userName + "'";

        SQLiteDatabase db = doGetWritableDatabase();
        Util.writeToLog("DatabaseHelper.getUserByUsername: " + sql);

        debugPoint = true;

        // === Remove later ====================
        //dropTables();
        //createTables(db);
        //=====================================

        try {
            Cursor cur = db.rawQuery(sql, null);
            if(cur.moveToFirst()) {
                user = new UserCLS();
                user.setUserID(cur.getInt(0));
                user.setUserName(cur.getString(1));
                user.setUserPassword(cur.getString(2));
            }
        } catch (SQLiteException sqlEx) {
            Util.writeToLog("DatabaseHelper.getUserByUsername: " + sqlEx.getMessage());
            return null;
        }

        return user;

    }// end getUserByUsername


    public int addCategory(String categoryName) {
        int newCategoryID = -1;
        String sql;

        SQLiteDatabase db = doGetWritableDatabase();

        if(db == null) return -1;

        if(Util.stringIsNullOrEmpty(categoryName))
            return -1;

        sql = "INSERT INTO " + CATEGORIES_TABLE + " (category_name) VALUES ('" +
                categoryName.trim() + "')";

        Util.writeToLog("DatabaseHelper.addCategory: " + sql);

        try{
            db.execSQL(sql);
            newCategoryID = (int)getLastAddedRowId();

        } catch (SQLiteException sqlEx) {
            Util.writeToLog("DatabaseHelper.addCategory: " + sqlEx.getMessage());
        }

        return newCategoryID;

    }// end addCategory


    public int addUser(String userName, String password, int serverUserID) {
        //TODO: Hash password, validate input, parameterize query

        int newLocalUserID = -1;
        String sql;

        SQLiteDatabase db = doGetWritableDatabase();

        // === Remove later ====================
        //dropTables();
        //createTables(db);
        //clearUsersTable();
        //=====================================

        if(db == null) return -1;

        if(Util.stringIsNullOrEmpty(userName) || Util.stringIsNullOrEmpty(password))
            return -1;

        /*
        if(serverUserID > 0)
            sql = "INSERT INTO " + USERS_TABLE + " (user_name, user_password, server_user_id) VALUES ('" +
                    userName.trim() + "', '" + password.trim() + "', " + serverUserID + ")";
        else
            sql = "INSERT INTO " + USERS_TABLE + " (user_name, user_password) VALUES ('" +
                    userName.trim() + "', '" + password.trim() + "')";
        */

        sql = "INSERT INTO " + USERS_TABLE + " (user_name, user_password) VALUES ('" +
                userName.trim() + "', '" + password.trim() + "')";


        Util.writeToLog("DatabaseHelper.adduser: " + sql);

        try{
            db.execSQL(sql);
            newLocalUserID = (int)getLastAddedRowId();

        } catch (SQLiteException sqlEx) {
            Util.writeToLog("DatabaseHelper.adduser: " + sqlEx.getMessage());

        }

        return newLocalUserID;

    }// end addUser


    /**
     * Return Last inserted row id(auto incremented row) (_id)
     * @return
     */
    public long getLastAddedRowId() {
        // *** Credit: https://stackoverflow.com/questions/5409751/get-generated-id-after-insert ***
        SQLiteDatabase db = doGetWritableDatabase();
        String queryLastRowInserted = "select last_insert_rowid()";

        final Cursor cursor = db.rawQuery(queryLastRowInserted, null);
        long _idLastInsertedRow = 0;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    _idLastInsertedRow = cursor.getInt(0);
                }
            } finally {
                cursor.close();
            }
        }

        return _idLastInsertedRow;

    }

    public int getUsersCount(){
        SQLiteDatabase db = doGetWritableDatabase();
        String sql = "SELECT COUNT(*), user_name FROM users " +
                "GROUP BY user_name";
        int userCount = -1;

        try{
            Cursor cur = db.rawQuery(sql, null);
            if(cur.moveToFirst())
                userCount = cur.getInt(0);
        } catch(SQLiteException slEx) {
            Util.writeToLog("getUsersCount: " + slEx.getMessage());
        }

        return userCount;
    }

    public void clearAllTables() {
        clearCategoriesTable();
        clearTasksTable();
        clearUsersTable();
    }


    private void clearCategoriesTable() {
        SQLiteDatabase db = doGetWritableDatabase();
        String sql = "DELETE FROM categories";

        try{
            Cursor cur = db.rawQuery(sql, null);
        } catch(SQLiteException slEx) {
            Util.writeToLog("clearCategoriesTable: " + slEx.getMessage());
        }
    }// end clearCategoriesTable

    public void clearTasksTable() {
        SQLiteDatabase db = doGetWritableDatabase();
        String sql = "DELETE FROM tasks";

        try{
            db.execSQL(sql);
        } catch(SQLiteException slEx) {
            Util.writeToLog("clearTasksTable: " + slEx.getMessage());
        }
    }// end clearTasksTable

    private void clearUsersTable() {
        SQLiteDatabase db = doGetWritableDatabase();
        String sql = "DELETE FROM users";

        try{
            Cursor cur = db.rawQuery(sql, null);
        } catch(SQLiteException slEx) {
            Util.writeToLog("clearUsersTable: " + slEx.getMessage());
        }
    }// end clearUsersTable



    public void dropTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CATEGORIES_TABLE);
    }


    private static boolean doesDatabaseExist(Context ctx) {
        return(ctx.getDatabasePath(Constants.DATABASE).exists());
    }


    public ArrayList<TaskCLS> getTasks(int userID, int taskShowingMode) {

        //============================
        // TODO: Remove later
        // ===========================
        //SQLiteDatabase db = doGetWritableDatabase();
        //db.execSQL("UPDATE tasks SET user_id=1");
        //============================

        int taskStatus = -1;


        switch(taskShowingMode){

            case Constants.OPEN_TASKS:
                taskStatus = Constants.TASK_OPEN;
                break;

            case Constants.COMPLETED_TASKS:
                taskStatus = Constants.TASK_COMPLETED;
                break;

            case Constants.DELETED_TASKS:
                taskStatus = Constants.TASK_DELETED;
                break;

            case Constants.ARCHIVED_TASKS:
                taskStatus = Constants.TASK_ARCHIVED;
                break;

            case Constants.ALL_TASKS:
                taskStatus = Constants.TASK_OPEN + Constants.TASK_COMPLETED;
                break;

            default:
                return null;

        }

        if(userID <= 0) return null;

        Cursor cursor = getTaskData(userID, taskStatus);

        if(cursor == null || ! cursor.moveToFirst()) {
            Util.writeToLog("DatabaseHelper.getTasks: Couldn't get cursor for task data");
            return null;
        }

        ArrayList<TaskCLS> taskList = new ArrayList<>();
        TaskCLS task = null;

        try {
            do {
                task = new TaskCLS();
                task.setUserID(userID);
                task.setTaskID(cursor.getInt(0));
                task.setServerTaskID(cursor.getInt(1));

                task.setServerUserID(cursor.getInt(2));

                //============================
                // TODO: Remove later after TESTING
                // ===========================
                task.setServerUserID(1);
                //SQLiteDatabase db = doGetWritableDatabase();
                //db.execSQL("UPDATE tasks SET user_id=1");
                //============================

                task.setCategoryID(cursor.getInt(3));

                task.setTaskName(cursor.getString(4).trim());
                task.setDueDate(cursor.getString(5));
                task.setPriority(cursor.getInt(6));
                task.setStatus(cursor.getInt(7));

                task.setDateAdded(cursor.getString(8));
                task.setDateModified(cursor.getString(9));

                task.setDateSynchronized(cursor.getString(10));

                taskList.add(task);
            } while (cursor.moveToNext());
        } catch (SQLiteException ex) {
            Util.writeToLog(ex.getMessage());
        }

        return taskList;
    }// end getTasks


    private Cursor getTaskData(int userID, int taskStatus){
        /*
        String query = "SELECT _id, server_task_id, server_user_id, category_id, task_name, " +
                       "due_date, priority, status, date_added, modification_date, date_synchronized " +
                       "FROM " + TASKS_TABLE + " WHERE user_id=" + userID + " AND (to_delete=0 OR to_delete IS NULL)";
        */

        String query;
        String baseQuery = "SELECT _id, server_task_id, server_user_id, category_id, task_name, " +
                "due_date, priority, status, date_added, modification_date, date_synchronized " +
                "FROM " + TASKS_TABLE + " WHERE user_id=" + userID; //" + " AND (to_delete=0 OR to_delete IS NULL)";
        String criteria = " AND (to_delete=0 OR to_delete IS NULL)";


        switch(taskStatus) {
            case Constants.TASK_OPEN + Constants.TASK_COMPLETED:
                /*Constants.ALL_TASKS:*/
                query = baseQuery + criteria;
                break;

            case Constants.TASK_COMPLETED:
            case Constants.TASK_OPEN:
            case Constants.TASK_ARCHIVED:
                query = baseQuery + criteria + " AND status = " + taskStatus;
                break;

            case Constants.TASK_DELETED:
                query = baseQuery + " AND to_delete = 1";
                break;

            default:
                return null;

        }

        query += " ORDER BY order_num ASC, priority DESC, modification_date DESC, date_added DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Util.writeToLog("getTaskData.query: " + query);

        Cursor data = db.rawQuery(query, null);

        return data;
    }// end getTaskData


    private Cursor getTaskData(int userID, String criteria){

        String query = "SELECT _id, task_name, due_date, priority, status, to_delete FROM " + TASKS_TABLE + " " +
                       "WHERE user_id=" + userID;

        if(!Util.stringIsNullOrEmpty(criteria))
            query += " AND " + criteria;

        query += " ORDER BY modification_date DESC";


        SQLiteDatabase db = this.getWritableDatabase();
        Util.writeToLog("getTaskData.query: " + query);

        Cursor data = null;

        try {
            data = db.rawQuery(query, null);
        } catch (SQLiteException slex) {
            Util.writeToLog("getTaskData: Error: " + slex.getMessage());
        }

        return data;

    }// end getTaskData


    public ArrayList<TaskCLS> getDeletedTasks(int userID) {
        String criteria = "to_delete=1";

        //============================
        // TODO: Remove later
        // ===========================
        //SQLiteDatabase db = doGetWritableDatabase();
        //db.execSQL("UPDATE tasks SET user_id=1");
        //============================

        if(userID <= 0) return null;

        Cursor cursor = getTaskData(userID, criteria);

        if(cursor == null || ! cursor.moveToFirst()) {
            Util.writeToLog("DatabaseHelper.getDeletedTasks: Couldn't get cursor for task data");
            return null;
        }

        ArrayList<TaskCLS> taskList = new ArrayList<>();
        TaskCLS task = null;

        try {
            do {
                task = new TaskCLS();
                task.setUserID(userID);
                task.setTaskID(cursor.getInt(0));
                task.setTaskName(cursor.getString(1).trim());
                task.setDueDate(cursor.getString(2));
                task.setPriority(cursor.getInt(3));
                task.setStatus(cursor.getInt(4));
                task.setToDelete(cursor.getInt(5));
                taskList.add(task);

            } while (cursor.moveToNext());
        } catch (SQLiteException ex) {
            Util.writeToLog(ex.getMessage());
        }

        return taskList;

    }// end getDeletedTasks


}// end class