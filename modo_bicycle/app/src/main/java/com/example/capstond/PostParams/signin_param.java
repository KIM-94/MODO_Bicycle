package com.example.capstond.PostParams;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class signin_param extends StringRequest {
    private final static String URL = "http://193.122.111.182/capstonD2/login_ok.php";
    private Map<String, String> map;

    public signin_param(String userID, String userPassword, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Request.Method.POST,URL, listener, errorListener);

        map = new HashMap<>();
        map.put("userID",userID);
        map.put("userPassword",userPassword);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String utf8String = new String(response.data, "UTF-8");
            return Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            // log error
            return Response.error(new ParseError(e));
        } catch (Exception e) {
            // log error
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return Collections.emptyMap();
    }
}
