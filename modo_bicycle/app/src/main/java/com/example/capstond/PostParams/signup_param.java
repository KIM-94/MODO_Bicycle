package com.example.capstond.PostParams;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class signup_param extends StringRequest {
    private final static String URL = "http://193.122.111.182/capstonD2/insertPerson.php";
    private Map<String, String> map;

    public signup_param(String userID, String userPassword, String userName, String userBirthday, String userEmail, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST,URL, listener, errorListener);

        map = new HashMap<>();
        map.put("userID",userID);
        map.put("userPassword",userPassword);
        map.put("userName",userName);
        map.put("userBirthday",userBirthday);
        map.put("userEmail",userEmail);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}