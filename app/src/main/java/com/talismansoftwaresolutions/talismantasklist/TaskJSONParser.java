package com.talismansoftwaresolutions.talismantasklist;


/* ======================================================================================================
    Adapted from: https://stackoverflow.com/questions/17810044/android-create-json-array-and-json-object
    See also: https://codinginflow.com/tutorials/android/volley/simple-get-request, and
              https://demonuts.com/android-json-parsing-using-volley-and-display-with-recyclerview/
   ======================================================================================================
*/

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonIOException;
//import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class TaskJSONParser {
    static final String TAG = "TaskJSONParser";

    static final int ADD_TASK = 1;
    static final int EDIT_TASK = 2;



    //static String TASKS_UPDATE_URL= "http://www.talismansoftwaresolutions.com/api/v1/tasks/update/";
    //static String TASKS_ADD_URL= "http://www.talismansoftwaresolutions.com/api/v1/tasks/add";
    //TODO: Make class a singleton?

    private static String getURLFromStatusType(int taskStatusType) {
        String url;

        switch(taskStatusType) {
            case Constants.OPEN_TASKS:
                url = Config.TASKS_OPEN_URL;
                break;
            case Constants.COMPLETED_TASKS:
                url = Config.TASKS_COMPLETED_URL;
                break;
            case Constants.ALL_TASKS:
                url = Config.TASKS_ALL_URL;
                break;
            default:
                return null;
        }

        return url;
    }


    public static void getTasksFromServer(final MainActivity mainActivity, int userID, int taskStatusType)
    {

        final ArrayList<TaskCLS> taskList = new ArrayList<>();

        String url = getURLFromStatusType(taskStatusType);

        if(Util.stringIsNullOrEmpty(url))
            return;

        //showSimpleProgressDialog(this, "Loading...","Fetching tasks",false);

        // ============================================
        // TODO Remove after testing
        // url = "http://192.168.0.15/api/v1/tasks/all";
        // =============================================

        url = url + "/" + userID;

        Util.writeToLog("url: " + url);

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, ">>" + response);
                        try {

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);

                                TaskCLS task = new TaskCLS(jsonObject.getString("task_name").trim());
                                task.setUserID(jsonObject.getInt("user_id"));
                                task.setTaskID(jsonObject.getInt("task_id"));
                                task.setDueDate(jsonObject.getString("due_date").trim());
                                task.setPriority(jsonObject.getInt("priority"));
                                task.setStatus(jsonObject.getInt("status"));

                                if(jsonObject.getBoolean("to_delete"))
                                    task.setToDelete(1);
                                else
                                    task.setToDelete(0);

                                taskList.add(task);
                            }

                            mainActivity.receivedTasksFromServerCallback(taskList);


                        } catch (JSONException e) {
                            Log.e("jsonParse", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        Log.e("jsonParse", e.getMessage());
                        e.printStackTrace();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(mainActivity);
        requestQueue.add(req);



    }// the end of doJsonParse


    public static JSONObject CreateNewJSONUser(UserCLS user)
    {
        JSONObject jsonObject = new JSONObject();

        //TODO: Remove after testing
        //user.setUserName("TEST_0323");
        //user.setUserPassword("TEST_123");
        //

        try
        {
            jsonObject.put("user_name", user.getUserName());
            jsonObject.put("email", user.getUserEMail());
            jsonObject.put("password", user.getUserPassword());

            Util.writeToLog("User " + user.toString());


        }catch (JSONException ex) {
            Log.e("JsonParser", ex.getMessage());
            ex.printStackTrace();
        }

        return jsonObject;
    }// END


    public static void getUserIDFromToServer(UserCLS user, Context ctx, final IJSONCallback callback,
                                             final int requestCode)
    {
        //TODO: Have authenticator be a listener of some sort??

        String url = Config.USERS_ID_URL;


        //TODO validate user entries
        JSONObject jsonObject = TaskJSONParser.CreateNewJSONUser(user);

        JsonObjectRequest req = new JsonObjectRequest(url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            openProfile(response.toString(1));
                            //Log.v("Response:%n %s", response.toString(4));
                            Util.writeToLog(response.toString());
                            //authenticator.jSONResponseCallback("JSON response: " + response.toString());
                            callback.receiveJSONInfo(response, requestCode);

                        } catch (JSONException e) {
                            Util.writeToLog(e.getMessage());
                            //authenticator.jSONResponseCallback("JSON exception: " + e.getMessage());
                            callback.receiveJSONInfo(null, requestCode);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                Util.writeToLog("Error: " + e.getMessage());
                //authenticator.jSONResponseCallback("Volley error: " + e.getMessage());
                callback.receiveJSONInfo(null, requestCode);
            }
        });

        Util.writeToLog("Request: " + req.toString());
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(req);

    }// end update addUserToServer


    /*
    public static void addUserToServer(UserCLS user, Context ctx, final IJSONCallback callback final MainActivity ma)
    {
        //TODO: Have authenticator be a listener of some sort??

        String url = Config.USERS_ADD_URL;

        //TODO validate user entries
        JSONObject jsonObject = TaskJSONParser.CreateNewJSONUser(user);

        JsonArrayRequest req = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        callback.receiveJSONArray(response);
                    }
                 },
                 new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { callback.receiveJSONArray(null); }
        });

        Util.writeToLog("Request " + req.toString());

        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(req);

    }// end update addUserToServer
    */



    public static void addUserToServer(UserCLS user, Context ctx, final IJSONCallback callback,
                                       final int requestCode)
    {
        //TODO: Have authenticator be a listener of some sort??

        String url = Config.USERS_ADD_URL;

        //TODO validate user entries
        JSONObject jsonObject = TaskJSONParser.CreateNewJSONUser(user);

        final JsonObjectRequest req = new JsonObjectRequest(url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            openProfile(response.toString(1));
                            //Log.v("Response:%n %s", response.toString(4));
                            Util.writeToLog(response.toString());
                            //authenticator.jSONResponseCallback("JSON response: " + response.toString());
                            callback.receiveJSONInfo(response, requestCode);

                        } catch (JSONException e) {
                            Util.writeToLog(e.getMessage());
                            //authenticator.jSONResponseCallback("JSON exception: " + e.getMessage());
                            callback.receiveJSONInfo(null, requestCode);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                Util.writeToLog("Error: " + e.getMessage());
                //authenticator.jSONResponseCallback("Volley error: " + e.getMessage());
                callback.receiveJSONInfo(null, requestCode);
            }
        });

        Util.writeToLog("Request " + req.toString());

        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(req);


    }// end update addUserToServer



    public static JSONObject CreateNewJSONTask(String taskDescription)
    {
        TaskCLS task = new TaskCLS(taskDescription);

        //TODO: create a method which creates a default json object
        //      which has sane defaults for user_id, status, priority etc.

        JSONObject jsonObject = new JSONObject();
        try
        {
            //jsonObject.put("task_id", Integer.toString(task.getTaskID()));
            jsonObject.put("user_id", Integer.toString(1));
            jsonObject.put("status", Integer.toString(0));
            jsonObject.put("priority", Integer.toString(3));
            jsonObject.put("task_name", task.getTaskName());
            //jsonObject.put("due_date", "5/12/2019");


        }catch (JSONException ex) {
            Log.e("JsonParser", ex.getMessage());
            ex.printStackTrace();
        }

        return jsonObject;
    }



    public static JSONObject CreateJSONTask(TaskCLS task)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            // ==========================
            // TODO: Remove after testing
            //  --   blank for now  --
            // ==========================
            jsonObject.put("task_id", Integer.toString(task.getTaskID()));
            jsonObject.put("server_task_id", Integer.toString(task.getServerTaskID()));
            jsonObject.put("user_id", Integer.toString(task.getUserID()));


            jsonObject.put("local_user_id", Integer.toString(task.getUserID()));
            jsonObject.put("local_task_id", Integer.toString(task.getTaskID()));

            // ==========================
            // TODO: Remove after testing
               jsonObject.put("server_user_id", 1);
            // ==========================

            //jsonObject.put("server_user_id", Integer.toString(task.getServerUserID()));

            jsonObject.put("status", Integer.toString(task.getStatus()));
            jsonObject.put("priority", Integer.toString(task.getPriority()));
            jsonObject.put("task_name", task.getTaskName());
            jsonObject.put("due_date", task.getDueDate());
            jsonObject.put("date_added", task.getDateAdded());
            jsonObject.put("date_modified", task.getDateModified());
            jsonObject.put("date_synchronized", task.getDateSynchronized());

            if(task.getToDelete() == 1)
                jsonObject.put("to_delete", true);
            else
                jsonObject.put("to_delete", false);


        }catch (JSONException ex) {
            Log.e("JsonParser", ex.getMessage());
            ex.printStackTrace();
        }

        return jsonObject;
    }

    public static void deleteTaskOnServer(TaskCLS task, Context ctx)
    {
        String url = Config.TASKS_DELETE_URL;

        JSONObject jsonObject = TaskJSONParser.CreateJSONTask(task);

/*        username = editTextUsername.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();

        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(KEY_USERNAME, username);
        params.put(KEY_PASSWORD, password);*/

        JsonObjectRequest req = new JsonObjectRequest(url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            openProfile(response.toString(1));
                            //Log.v("Response:%n %s", response.toString(4));
                            Log.e(TAG, response.toString());

                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error: ", error.getMessage());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(req);

    }// end update addTaskToServer



    public static void deleteTasksOnServer(ArrayList<TaskCLS> taskList, Context ctx)
    {
        String url = Config.TASKS_DELETE_URL;

        if(taskList == null || taskList.size() <= 0) return;

        int nTaskCount = taskList.size();

        for(int i = 0; i < nTaskCount; i++) {
            //TaskCLS taskInfo = taskInfoList.get(i);
            TaskCLS task = taskList.get(i);
            TaskJSONParser.deleteTaskOnServer(task, ctx);
        }

        return;

    }// end deleteTasksOnServer

    //TODO: Left off here
    public static void uploadTasksToServer(ArrayList<TaskCLS> taskList, final IJSONCallback callbackObj,
                                           final int requestCode)
    {
        String url = Config.TASKS_UPLOAD_URL;
        int debugTaskCount = -1;

        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();

        //NB: Be careful here as we're *not* uploading tasks mark for deletion
        for(TaskCLS task : taskList) {
            if(task.getStatus() == Constants.TASK_DELETED)
                continue;

            debugTaskCount++;

            JSONObject jObj = CreateJSONTask(task);
            jsonArray.put(jObj);
        }

        try {
            jsonObject.put("tasks", jsonArray);
            Util.writeToLog("TaskJSONParser.addTasksToServer: finished adding tasks to jsonArray");
        } catch (JSONException ex) {
            Util.writeToLog("TaskJSONParser.addTasksToServer: " + ex.getMessage());
        }


        final JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,
                url, jsonObject, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Util.writeToLog(response.toString());
                callbackObj.receiveJSONInfo(response, requestCode);

                /*
                try {
                    JSONArray arrData = response.getJSONArray("Status");
                    JSONObject userObj = arrData.getJSONObject(0);
                    JSONObject errorObj = arrData.getJSONObject(1);
                    String strResponse = "User id: " + userObj.getInt("user_id") +
                            " Status: " + errorObj.getString("status");
                    //txtResponse.setText(strResponse);
                    callbackObj.receiveJSONInfo(response, requestCode);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText((MainActivity)callbackObj,"Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                */


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText((MainActivity)callbackObj, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue((MainActivity)callbackObj);
        requestQueue.add(req);

    } //end uploadTasksToServer





    public static void addTaskToServer(String newTaskDescription, View vw, int taskMode)
    {
        String url = Config.TASKS_ADD_URL;

        //TODO validate editedTask
        JSONObject jsonObject = TaskJSONParser.CreateNewJSONTask(newTaskDescription);

/*        username = editTextUsername.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();

        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(KEY_USERNAME, username);
        params.put(KEY_PASSWORD, password);*/

        JsonObjectRequest req = new JsonObjectRequest(url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            openProfile(response.toString(1));
                            //Log.v("Response:%n %s", response.toString(4));
                            Log.e(TAG, response.toString());

                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error: ", error.getMessage());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(vw.getContext());
        requestQueue.add(req);

    }// end update addTaskToServer


    public static void updateEditedTaskOnServer(TaskCLS editedTask, View vw)
    {
        String taskIDstr = null;
        String url = null;

        if(editedTask == null) {
            return;
        }
        else {
            taskIDstr = Integer.toString(editedTask.getTaskID());
            url = Config.TASKS_UPDATE_URL + taskIDstr;
        }

        //TODO validate editedTask
        JSONObject jsonObject = TaskJSONParser.CreateJSONTask(editedTask);

/*        username = editTextUsername.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();

        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(KEY_USERNAME, username);
        params.put(KEY_PASSWORD, password);*/

        JsonObjectRequest req = new JsonObjectRequest(url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            openProfile(response.toString(1));
                            //Log.v("Response:%n %s", response.toString(4));
                            Log.e(TAG, response.toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error: ", error.getMessage());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(vw.getContext());
        requestQueue.add(req);

    }// end update EditedTask

    static void openProfile(String message) {
        Log.d(TAG, message);
    };

}
