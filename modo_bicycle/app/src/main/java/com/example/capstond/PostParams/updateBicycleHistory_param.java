package com.example.capstond.PostParams;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class updateBicycleHistory_param extends StringRequest {
    private final static String URL = "http://193.122.111.182/capstonD2/insertHistory.php";
    private Map<String, String> map;

    public updateBicycleHistory_param(String userID, String bicycleNumber,String rentalPlaceLatitude,String rentalPlaceLongitude,String returnPlaceLatitude,String returnPlaceLongitude, String rentalTime, String returnTime, String useTime,
                                      Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST,URL, listener, errorListener);

        map = new HashMap<>();
        map.put("userID",userID);
        map.put("bicycleNumber",bicycleNumber);
        map.put("rentalPlaceLatitude",rentalPlaceLatitude);
        map.put("rentalPlaceLongitude",rentalPlaceLongitude);
        map.put("returnPlaceLatitude",returnPlaceLatitude);
        map.put("returnPlaceLongitude",returnPlaceLongitude);
        map.put("rentalTime",rentalTime);
        map.put("returnTime",returnTime);
        map.put("useTime",useTime);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}