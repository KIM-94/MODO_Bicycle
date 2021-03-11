package com.example.capstond.PostParams;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class checkID_param extends StringRequest {
    private final static String URL = "http://193.122.111.182/capstonD2/check.php";
    private Map<String, String> map;

    public checkID_param(String userID, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST,URL, listener, errorListener);

        map = new HashMap<>();
        map.put("userID",userID);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}