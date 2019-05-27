package com.talismansoftwaresolutions.talismantasklist;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

// ============================================================================================
//   Credits - some code adapted from: https://www.mkyong.com/java/java-how-to-get-current-date-time-date-and-calender/
// ============================================================================================

public class Util {


    public static String getCurrentDateTime(){
        DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        Date date = new Date();
        //System.out.println(sdf.format(date));
        return sdf.format(date);
    }

    public static void makeToast(Context ctx, String msg){
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    public static void writeToLog(String msg) {
        Log.d(Constants.TAG, msg);
    }// end writeToLog

    public static boolean stringIsNullOrEmpty(String str) {
        return(str == null || str.trim().length() <= 0);
    }
}