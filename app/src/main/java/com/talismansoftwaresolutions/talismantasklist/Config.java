package com.talismansoftwaresolutions.talismantasklist;

import android.util.Log;

public class Config {
    public static final String LOCAL_DB_NAME = "simpleTasks.db";

    public static final int DOMAIN_LOCAL = 0;
    public static final int DOMAIN_SANDBOX = 1;
    public static final int DOMAIN_PROD = 2;

    private static String[] domains = new String[]{"http://192.168.0.15", "sandbox-1804",
                                                   "http://www.talismansoftwaresolutions.com"};

    private static int CURRENT_DOMAIN = DOMAIN_PROD;

    public static String TASKS_ALL_URL          = domains[CURRENT_DOMAIN] +  "/api/v1/tasks/all";
    public static String TASKS_COMPLETED_URL    = domains[CURRENT_DOMAIN] +  "/api/v1/tasks/completed";
    public static String TASKS_OPEN_URL         = domains[CURRENT_DOMAIN] +  "/api/v1/tasks/open";
    public static String TASKS_UPDATE_URL       = domains[CURRENT_DOMAIN] +  "/api/v1/tasks/update";
    public static String LOGIN_URL              = domains[CURRENT_DOMAIN] +  "/api/v1/tasks";
    public static String TASKS_ADD_URL          = domains[CURRENT_DOMAIN] +  "/api/v1/tasks/add";
    public static String TASKS_UPLOAD_URL       = domains[CURRENT_DOMAIN] +  "/api/v1/tasks/upload";
    public static String TASKS_DELETE_URL       = domains[CURRENT_DOMAIN] +  "/api/v1/tasks/delete";
    public static String USERS_ADD_URL          = domains[CURRENT_DOMAIN] +  "/api/v1/users/add_user";
    public static String USERS_ID_URL           = domains[CURRENT_DOMAIN] +  "/api/v1/users/get_user_id";

    public static void setCurrentDomain(int domainChoice) {
        if(domainChoice < DOMAIN_LOCAL || domainChoice > DOMAIN_PROD)
            return;
        CURRENT_DOMAIN = domainChoice;
    }

    public static void printCurrentDomain() {
        Log.e("Config", domains[CURRENT_DOMAIN]);
    }
}