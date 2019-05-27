package com.talismansoftwaresolutions.talismantasklist;

import org.json.JSONArray;
import org.json.JSONObject;

public interface IJSONCallback {
    void receiveJSONInfo(JSONObject jsonObject, int requestCode);
    void receiveJSONArray(JSONArray jsonArray);
}
