package com.talismansoftwaresolutions.talismantasklist;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/* =========================================================================================================
    Credit: https://stackoverflow.com/questions/4878159/whats-the-best-way-to-share-data-between-activities
   =========================================================================================================
 */
public class DataHolder {
    //static Map<String, WeakReference<DataModel>> data = new HashMap<String, WeakReference<DataModel>>();

    private Map<String, Object> data = new HashMap<String, Object>();


    static DataHolder holder;

    private DataHolder() {}

    public static DataHolder getInstance() {
        if(holder == null)
            holder = new DataHolder();

        return holder;
    }

    public void save(String key, Object obj) {
        data.put(key, obj);
    }
    public void clear(){
        data.clear();
    }
    public Object retrieve(String key) {
        return(data.containsKey(key) ? data.get(key) : null);
    }
}