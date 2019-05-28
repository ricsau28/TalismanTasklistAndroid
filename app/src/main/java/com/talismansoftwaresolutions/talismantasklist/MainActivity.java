package com.talismansoftwaresolutions.talismantasklist;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Pair;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements IJSONCallback{
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    TaskListManager taskListManager;
    ArrayList<TaskCLS> serverTaskList = new ArrayList<>();
    TextView txtHeading;
    TaskJSONParser jsonParser;
    UserCLS appUser;
    boolean userAuthenticated;
    HashMap<Integer, Integer> movedTasks = new HashMap<>();

    public static int tasksShowingMode;

    //=======================================
    // TODO: TESTING
    //=======================================
    boolean debugTakeSnapshot = true;

    //=======================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        txtHeading = findViewById(R.id.txt_heading);

        //FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        fab.setImageResource(R.drawable.ic_add_for_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewTask();
            }});

        taskListManager = TaskListManager.getInstance(this);

        try {
              //=== DELETE WHEN FINISHED! ================================
              //doOneTimeActions();
              //===========================================================

             initApp();

        } catch (Exception ex) {
            Util.writeToLog("initApp: " + ex.getMessage());
            Util.makeToast(this, ex.getMessage());
            //finish();
            return;
        }

        //initApp2(true);

        /*
        //tasksShowingMode = Constants.DOWNLOADED_TASKS;
        //populateListFromServer(false);

        tasksShowingMode = Constants.OPEN_TASKS;
        populateList(tasksShowingMode);

        //RecyclerView
        setUpRecyclerView();

        updateUI();
        */
    }// end onCreate

    private void doOneTimeActions() {
        // === TODO: DELETE WHEN FINISHED! ================================
        //DatabaseHelper dbh = DatabaseHelper.getInstance(this);
        //dbh.executeSQL("UPDATE tasks SET status = " + Constants.TASK_OPEN + ", to_delete =0");
        //dbh.executeSQL("UPDATE tasks SET to_delete = 0");
        //dbh.updateTaskOrder();
        //boolean flag = dbh.createViewOrderTable();


        //dbh.changeServerUserID(1);

        /*
        DatabaseCopier copier = new DatabaseCopier(this);
        try {
            copier.createDataBase();
        } catch (Exception ex) {
            Util.writeToLog("Couldn't create database!");
            return;
        }
        */



        //DatabaseHelper dbh = DatabaseHelper.getInstance(this);
        //dbh.clearTasksTable();
        //dbh.clearAllTables();
        //Util.makeToast(this, "Tables have been deleted");

        //dbh.dropTables();
        //dbh.createTables();
        //Util.makeToast(this, "Tables have been recreated");
        // ================================================================
    }// end of doOneTimeActions


    @Override
    public void onPause() {
        saveSwappedTaskPositionsToDB();
        super.onPause();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Credit: https://stackoverflow.com/questions/47150704/how-to-hide-menu-item-dynamically

        MenuItem menu1 = menu.findItem(R.id.action_delete_all_tasks);
        menu1.setVisible(false);
        MenuItem menu2 = menu.findItem(R.id.action_settings);
        menu2.setVisible(false);

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selectedMode = -1;
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        switch (id) {
            case R.id.action_completed_tasks:
                selectedMode = Constants.COMPLETED_TASKS;
                break;

            case R.id.action_open_tasks:
                selectedMode = Constants.OPEN_TASKS;
                break;

            case R.id.action_all_tasks:
                selectedMode = Constants.ALL_TASKS;
                break;

            case R.id.action_server_tasks:
                selectedMode = Constants.DOWNLOADED_TASKS;
                break;

            case R.id.action_deleted_tasks:
                selectedMode = Constants.DELETED_TASKS;
                break;

            case R.id.action_tests_server:
                startTestActivity();
                return true;

            case R.id.action_server_upload_tasks:
                uploadTasksToServer();
                return true;

            case R.id.action_delete_all_tasks:
                deleteLocalTasks();
                return true;

            case R.id.action_archived_tasks:
                selectedMode = Constants.ARCHIVED_TASKS;
                break;


            default:
                return super.onOptionsItemSelected(item);
        }

        if(selectedMode != tasksShowingMode)
            tasksShowingMode = selectedMode;

        if(tasksShowingMode != Constants.DOWNLOADED_TASKS)
            populateList(tasksShowingMode);
        else {
            boolean clearTasksTable = false;
            populateListFromServer(clearTasksTable);
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteLocalTasks() {
        DatabaseHelper dbh = DatabaseHelper.getInstance(this);
        dbh.clearTasksTable();
        taskListManager.clear();
        adapter.notifyDataSetChanged();
        updateUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        long newUserID = -1;

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                //User entered credentials
                DataHolder dh = DataHolder.getInstance();
                appUser = (UserCLS) dh.retrieve("user");

                //TODO ==== Remove line after testing =========
                //appUser.setUserID(1);
                //appUser.setUserName("pretty4423");
                //appUser.setUserPassword("password2345");
                //appUser.setUserEMail("pretty44@aol.com");
                appUser.setserverUserID(1);
                //=========================================

                if(appUser != null) {
                    //===== Add user to server
                    //TaskJSONParser.addUserToServer(appUser, this,  this, 1);  //TODO: Rectify

                    //Test
                    //TaskJSONParser.getUserIDFromToServer(appUser, this, this, 2);
                    // ============================================================================

                    //Add user to local db

                    DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
                    newUserID = dbHelper.addUser(appUser.getUserName(), appUser.getUserPassword(),
                                                 appUser.getserverUserID());
                    if(newUserID >= 0)
                        Util.makeToast(this, Long.toString(newUserID) + " successfully registered!");
                    else
                        Util.makeToast(this, "Error: could not register you.");

                }

            }
        }// end if (requestCode == 1)

        if(requestCode == 2) {
            if (resultCode == RESULT_OK) {
                DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
                DataHolder dataHolder = DataHolder.getInstance();
                TaskCLS newTask = (TaskCLS)dataHolder.retrieve("Task");

                try {
                    TaskCLS.setDefaultValues(newTask, appUser);
                    dbHelper.addTask(newTask);
                } catch (Exception ex) {
                    String msg = "Error adding new task to databse: " + newTask.getTaskName();
                    Util.makeToast(this, msg);
                    return;
                }

                //https://stackoverflow.com/questions/35653439/recycler-view-inconsistency-detected-invalid-view-holder-adapter-positionviewh/43933960
                // =======================================================
                // Error when attempting to insert an item as the first element in the recyclerview.
                // Let's try the following hack
                // ===============================
                if(!taskListManager.isEmpty()) {

                    /* Attempt 1 - this works
                    ArrayList<TaskCLS> taskListClone = (ArrayList<TaskCLS>) taskList.clone();
                    taskList.clear();
                    adapter.notifyDataSetChanged();


                    taskList.add(newTask);
                    taskList.addAll(taskListClone);

                    adapter.notifyDataSetChanged();
                    */

                    //Attempt 2
                    //ArrayList<TaskCLS> taskList = taskListManager.getTaskList();
                    //ArrayList<TaskCLS> taskListClone = (ArrayList<TaskCLS>) taskList.clone();
                    //taskListManager.clear();
                    //adapter.notifyDataSetChanged();


                    //taskList.add(newTask);
                    taskListManager.addTask(newTask);
                    //taskListManager.addAll(taskListClone);
                    //taskList.addAll(taskListClone);

                    adapter.notifyDataSetChanged(); // hmm...use this or the line below?
                    //adapter.notifyItemInserted(0);



                    //taskList.set(0, newTask);
                    //taskList.add(newTask);
                    //adapter.notifyDataSetChanged();
                    //adapter.notifyItemInserted(0);


                    LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    llm.scrollToPosition(0);
                    updateUI();

                    String msg = "Task added: " + newTask.getTaskName();
                    Util.makeToast(this, msg);
                } else {
                    //taskList.add(newTask);
                    taskListManager.addTask(newTask);
                    if(tasksShowingMode == Constants.OPEN_TASKS) {
                        adapter.notifyDataSetChanged();
                        updateUI();
                    }
                }
            }

            if (resultCode == RESULT_CANCELED) {
                //Do nothing (?)
            }

        }// end if (requestCode == 2)

    }// end onActivityResult


    public int getTasksShowingMode() {
        return tasksShowingMode;
    }

    public void startTestActivity() {

        Intent intent = new Intent(MainActivity.this, TestServerActivity.class);
        startActivity(intent);
    }

    public void performAuthenticationCheck() {

        Intent intent; //AuthenticationCLS.
        UserCLS user = AuthenticationCLS.isAuthenticated(this);

        if (user == null) {

            intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivityForResult(intent, 1);
        } else {
            this.appUser = user;
        }

    }// end



    private void initApp2() {

        //tasksShowingMode = Constants.DOWNLOADED_TASKS;
        //populateListFromServer(false);

        //RecyclerView
        setUpRecyclerView();

        tasksShowingMode = Constants.OPEN_TASKS;
        populateList(tasksShowingMode);

        updateUI();

    }

    private void initApp () throws Exception {

        //=== One time ===========
        // DatabaseHelper dbh = DatabaseHelper.getInstance(this);
        // dbh.executeSQL("ALTER TABLE users ADD COLUMN server_user_id INT DEFAULT -1");
        //=========================

        //1. Has user ever used app?
        performAuthenticationCheck();

        appUser = (DatabaseHelper.getInstance(this)).getAppUser();

        if(appUser == null) {

            throw new Exception("Couldn't get user info from internal database. Cannot continue.");
        }
        else {
            Util.makeToast(this, "Hello, " + appUser.getUserName() +
                            "\nYour user id: " + appUser.getUserID());
            initApp2();
        }

        //adapter = new TaskListAdapter(this, taskList);

        //performAuthenticationCheck();

        //jsonParser = new TaskJSONParser();

    }


    private void setHeading(String caption){
        txtHeading.setText(caption);
    }


    private int getTaskCount() {
        return taskListManager.getSize();
    }

    private void updateUI() {
        String status;

        int taskCount = getTaskCount();

        switch (tasksShowingMode) {
            case Constants.ALL_TASKS:
                status = "All Tasks (" + taskCount + ")";
                break;

            case Constants.OPEN_TASKS:
                status = "Open Tasks (" + taskCount + ")";
                break;

            case Constants.COMPLETED_TASKS:
                status = "Completed Tasks (" + taskCount + ")";
                break;

            case Constants.DELETED_TASKS:
                status = "Deleted Tasks (" + taskCount + ")";
                break;

            case Constants.DOWNLOADED_TASKS:
                status = "Downloaded Tasks (" + taskCount + ")";
                break;

            case Constants.ARCHIVED_TASKS:
                status = "Archived Tasks (" + taskCount + ")";
                break;

            default:
                status = "Local Tasks (" + taskCount + ")";
                break;
        }

        setHeading(status);
    }

    private void addNewTask() {
        DataHolder dataHolder = DataHolder.getInstance();
        dataHolder.clear();
        Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
        intent.putExtra("user_id", appUser.getUserID());
        startActivityForResult(intent, 2);
    }

    private void populateList(int taskStatus) {
        int userID;

        //addOneBogusTask();

        saveSwappedTaskPositionsToDB();


        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);

        if(dbHelper == null || appUser.getUserID() < 0)
            return;

        //this.taskList.clear();
        taskListManager.clear();

        //ArrayList<TaskCLS> taskListLocal = dbHelper.getTasks(appUser.getUserID(), Constants.ALL_TASKS);
        ArrayList<TaskCLS> taskListLocal;
        if(tasksShowingMode == Constants.DELETED_TASKS)
            taskListLocal = dbHelper.getDeletedTasks(appUser.getUserID());
        else
            taskListLocal = dbHelper.getTasks(appUser.getUserID(), tasksShowingMode);

        if(taskListLocal != null && taskListLocal.size() > 0)
            taskListManager.addAll(taskListLocal);

        adapter.notifyDataSetChanged();
        updateUI();
    }

    private void addOneBogusTask() {
        TaskCLS task = new TaskCLS("A third bogus task");
        task.setPriority(3);
        task.setStatus(1);
        task.setDueDate("6/19/2019");
        task.setUserID(1);
        taskListManager.addTask(task);
    }

    private void setUpRecyclerView() {

        adapter = new TaskListAdapter(this, taskListManager.getTaskList());
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            /*
            @Override public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.RIGHT  | ItemTouchHelper.DOWN;
                // movements drag return makeFlag( ItemTouchHelper.ACTION_STATE_DRAG , dragFlags);
                return dragFlags;
            }
            */


            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                //if (viewHolder instanceof TaskListAdapter.TaskViewHolder)
                //    return 0;

                //if(tasksShowingMode == Constants.TASK_DELETED || tasksShowingMode == Constants.TASK_COMPLETED)
                //    return 0;

                return super.getSwipeDirs(recyclerView, viewHolder);
            }


            @Override
            public boolean isLongPressDragEnabled() {
                if(tasksShowingMode == Constants.OPEN_TASKS)
                    return true;
                else
                    return false;
            }



            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder dragged, RecyclerView.ViewHolder target) {
                if(tasksShowingMode == Constants.OPEN_TASKS) {

                    int dragPosition = dragged.getAdapterPosition();
                    int targetPosition = target.getAdapterPosition();

                    saveSwappedTaskPosition(dragPosition, targetPosition);

                    taskListManager.swap(dragPosition, targetPosition);
                    adapter.notifyItemMoved(dragPosition, targetPosition);

                    return true;
                }

                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                int position = viewHolder.getAdapterPosition(); //getLayoutPosition

                TaskCLS task = taskListManager.getTask(position);

                //taskList.remove(position);
                taskListManager.removeTask(position);
                //taskListManager.removeTask(task);
                adapter.notifyItemRemoved(position);

                updateUI();

                if(direction == ItemTouchHelper.LEFT) {
                   if( markTaskAsDeleted(task, position) ) {
                       Util.makeToast(recyclerView.getContext(), "Deleted task " + task.getTaskName());
                   } else {
                       Util.makeToast(recyclerView.getContext(), "Error: Couldn't delete task " + task.getTaskName() + " in database");
                   }
                }

                if(direction == ItemTouchHelper.RIGHT) {
                    if(markTaskAsCompleted(task, position)){
                        Util.makeToast(recyclerView.getContext(), "Completed task " + task.getTaskName());
                    } else {
                        Util.makeToast(recyclerView.getContext(), "Error: Couldn't mark task as complete " + task.getTaskName() + " in database");
                    }
                }

            }
        }).attachToRecyclerView(recyclerView);

    }


    public void deleteTask(TaskCLS task) {
        if(task == null)
            return;

        if(task.getStatus() != Constants.TASK_DELETED) {
            taskListManager.deleteTask(task);
            adapter.notifyDataSetChanged();
            updateUI();
            Util.makeToast(this, "Deleted task");
        }
    }//end deleteTask


    public void unDeleteTask(TaskCLS task) {
        if(task == null)
            return;

        if(task.getStatus() == Constants.TASK_DELETED) {
            taskListManager.unDeleteTask(task);
            adapter.notifyDataSetChanged();
            updateUI();
            Util.makeToast(this, "Undeleted task");
        }
    }//end unDeleteTask


    public void unArchiveTask(TaskCLS task) {
        if(task == null)
            return;

        if(task.getStatus() == Constants.TASK_ARCHIVED) {
            taskListManager.changeArchiveStatus(task, Constants.TASK_OPEN);
            //adapter.notifyItemRemoved(taskPosition);
            adapter.notifyDataSetChanged();
            updateUI();
        }
    }


    public void setTaskToArchive(TaskCLS task) {
        if(task == null)
            return;

        if(task.getStatus() != Constants.TASK_DELETED) {
            taskListManager.changeArchiveStatus(task, Constants.TASK_ARCHIVED);
            //adapter.notifyItemRemoved(taskPosition);
            adapter.notifyDataSetChanged();
            updateUI();
        }
    }

    private void saveSwappedTaskPositionsToDB() {
        if(movedTasks.isEmpty())
            return;

        DatabaseHelper dbh = DatabaseHelper.getInstance(this);

        // using iterators
        Iterator<Map.Entry<Integer, Integer>> itr = movedTasks.entrySet().iterator();

        while(itr.hasNext())
        {
            Map.Entry<Integer, Integer> entry = itr.next();
            dbh.updateTaskViewOrder(entry.getKey(), entry.getValue());
            //System.out.println("Key = " + entry.getKey() +
            //        ", Value = " + entry.getValue());
        }

        movedTasks.clear();

    }// end saveSwappedTaskPositionsToDB

    private void saveSwappedTaskPosition(int oldPosition, int newPosition) {
        ArrayList<TaskCLS> taskList = TaskListManager.getInstance(this).getTaskList();

        TaskCLS taskAtOld = taskList.get(oldPosition);
        TaskCLS taskAtNew = taskList.get(newPosition);

        movedTasks.put(taskAtOld.getTaskID(), newPosition);
        movedTasks.put(taskAtNew.getTaskID(), oldPosition);


    }// end saveSwappedTaskPosition

    private boolean markTaskAsDeleted(TaskCLS task, int position) {
        DatabaseHelper dbh = DatabaseHelper.getInstance(this);

        if(dbh.updateTaskDeletionStatus(task.getTaskID(), Constants.TASK_DELETED)) {
            //task.setStatus(Constants.TASK_DELETED);
            //task.setDateModified(Util.getCurrentDateTime());
            taskListManager.deleteTask(position);
            //adapter.notifyItemRemoved(position);
            adapter.notifyDataSetChanged();
            return true;
        }
        else
            return false;
    }

    private boolean markTaskAsCompleted(TaskCLS task, int position) {
        DatabaseHelper dbh = DatabaseHelper.getInstance(this);

        if(dbh.updateTaskStatus(task.getTaskID(), Constants.TASK_COMPLETED)) {
            //task.setStatus(Constants.TASK_COMPLETED);
            //task.setDateModified(Util.getCurrentDateTime());
            taskListManager.markTaskAsCompleted(position);
            adapter.notifyItemRemoved(position);
            return true;
        }
        else
            return false;
    }

    private void populateListFromServer(boolean clearTasksTable) {
        if(clearTasksTable) {
            DatabaseHelper dbh = DatabaseHelper.getInstance(this);
            dbh.clearTasksTable();
        }

        //jsonParser = new TaskJSONParser();
        TaskJSONParser.getTasksFromServer(this, appUser.getUserID(), Constants.ALL_TASKS);
    }

    private void uploadTasksToServer() {
        /*
        if(taskList != null && taskList.size() > 0) {
            TaskJSONParser.uploadTasksToServer(taskList, this,1);
        }
        */

        if(!taskListManager.isEmpty()) {
            TaskJSONParser.uploadTasksToServer(taskListManager.getTaskList(), this,1);
        }
    }

    private void saveServerTasksLocally(ArrayList<TaskCLS> serverTaskList) {
        DatabaseHelper dbh;
        TaskCLS editedTask;
        String categoryName;
        String taskName;
        String separator = ":";
        String[] strings;
        int position = -1;
        int categoryID = -1;

        int debugCount =0;

        if(serverTaskList == null || serverTaskList.size() <= 0)
            return;

        dbh = DatabaseHelper.getInstance(this);

        for(TaskCLS task : serverTaskList){
            // ===================================================================
            // NB: Normally, call dbh.addTask. But my tasks have a colon which
            // separates the category from the task, e.g.:
            //   DEVEL: Unit test - "DEVEL" is the category followed by the task
            // ====================================================================

            taskName = task.getTaskName().trim();
            position = taskName.indexOf(separator);


            if(position != -1) {

                strings = taskName.split(separator);
                categoryName = strings[0].trim();
                taskName = strings[1].trim();
                categoryID = (int) dbh.addCategory(categoryName);

                task.setCategoryID(categoryID);
                task.setTaskName(taskName);

                dbh.addTask(task);

            }else {
                dbh.addTask(task);
            }


            //dbh.addTask(task);
            //debugCount += 1;
            //Util.writeToLog(" - added " + task.getTaskName() + " to the local database.");
        }

        Util.makeToast(this, "Finished downloading tasks from the web");
        //Util.makeToast(this, "Debug count: " + debugCount + " tasks saved to local database");

    }// end saveServerTasksLocally


    public void receivedTasksFromServerCallback(ArrayList<TaskCLS> serverTaskList){
        //Toast.makeText(this, String.format("%d tasks received from server." , count), Toast.LENGTH_SHORT).show();

        int count = taskListManager.getSize();
        String msg = String.format("%d tasks received from server." , count);
        //Util.makeToast(this, msg);

        Util.makeToast(this,  serverTaskList.size() + " parsed from JSON");


        saveServerTasksLocally(serverTaskList);

        //this.taskList.addAll(serverTaskList);
        taskListManager.addAll(serverTaskList);
        adapter.notifyDataSetChanged();

        updateUI();

    }//end receivedTasksFromServerCallback

    @Override
    public void receiveJSONArray(JSONArray response) {
        JSONArray jsonArray = null;
        JSONObject jsonObject = null;
        int user_id;
        String status;

        try {
            jsonArray = response;
            jsonObject = jsonArray.getJSONObject(0);
            user_id = jsonObject.getInt("user_id");
            jsonObject = jsonArray.getJSONObject(1);
            status = jsonObject.getString("status");

            Util.makeToast(this, "User ID: " + user_id + " Status: " + status);
        } catch (Exception ex) {
            Util.makeToast(this, "receiveJSONArray: error occurred");
        }
    }


    @Override
    public void receiveJSONInfo(JSONObject response, int requestCode) {
        int newServerUserID = -1;
        int newLocalUserID = -1;
        JSONObject jobj = null;

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);

        jobj = response;

        if(response == null) {
            Util.writeToLog("Error when adding new user to server");
            return;
        }

        Util.writeToLog("JSON string to be parsed: " + response.toString());

        if(requestCode == 1) {

            Util.makeToast(this, "Uploaded tasks to server");

            try {
                newServerUserID = response.getInt("Status");
                appUser.setserverUserID(newServerUserID);
            } catch (JSONException ex) {
                Util.writeToLog("receiveJSONInfo: " + ex.getMessage());
                return;
            }


            if (newServerUserID >= 0) {
                //Add user to local db

                newLocalUserID = dbHelper.addUser(appUser.getUserName(), appUser.getUserPassword(), newServerUserID);

                if (newLocalUserID >= 0) {
                    Util.makeToast(this, Long.toString(newServerUserID) + " successfully registered!");
                    appUser.setUserID(newLocalUserID);
                    //initApp2();
                } else {
                Util.makeToast(this, "Error: could not register you.");
                }
            }

        }

        if(requestCode == 2) {

            try {
                String msg = response.getString("status");
                Util.writeToLog(msg);
            } catch (JSONException ex) {
                Util.writeToLog("receiveJSONInfo: " + ex.getMessage());
            }


            try {
                newServerUserID = response.getInt("user_id");
            } catch (JSONException ex) {
                Util.writeToLog("receiveJSONInfo: " + ex.getMessage());
            }

            if (newServerUserID >= 0) {
                //Add user to local db
                dbHelper = DatabaseHelper.getInstance(this);
                newLocalUserID = dbHelper.addUser(appUser.getUserName(), appUser.getUserPassword(), newServerUserID);

                if (newLocalUserID >= 0) {
                    Util.makeToast(this, Long.toString(newServerUserID) + " successfully registered!");
                    //initApp2(false);
                }
            } else {
                Util.makeToast(this, "Error: could not register you.");
            }
        }

    }


}// end class
