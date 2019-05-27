package com.talismansoftwaresolutions.talismantasklist;

import android.content.Context;

public class AuthenticationCLS {
    private static AuthenticationCLS instance;

    private AuthenticationCLS() {}

    public static AuthenticationCLS getInstance() {
        if(instance == null)
            instance = new AuthenticationCLS();
        return instance;
    }

    // =======================================================================
    // For now, just check to see if our database is there and
    // a user has been saved to it :(
    // Perhaps a setting can be used to allow user to login upon each use
    // =======================================================================
    public static UserCLS isAuthenticated(Context ctx) {

        //TODO (eventually): 1. Check local, 2. Check server 3. Show registration activity
        //NB: Is there a proper way to authenticate in Android?

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(ctx);

        return( dbHelper.getAppUser() );
    }
}