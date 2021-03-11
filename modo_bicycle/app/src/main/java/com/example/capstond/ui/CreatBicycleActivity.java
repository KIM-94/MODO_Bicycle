package com.example.capstond.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.capstond.PostParams.userBicycle_param;
import com.example.capstond.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CreatBicycleActivity extends AppCompatActivity {

    private static final String TAG_JSON="webnautes";
    private static final String TAG_userID = "userID";
    private static final String TAG_bicycleNumber = "bicycleNumber";
    private static final String TAG_rentalTime ="rentalTime";
    private static final String TAG_returnTime ="returnTime";
    private static final String TAG_rentalPlaceLatitude ="rentalPlaceLatitude";
    private static final String TAG_rentalPlaceLongitude ="rentalPlaceLongtitude";
    private static final String TAG_bicycleState ="bicycleState";
    private static final String TAG_returnPlaceLatitude ="returnPlaceLatitude";
    private static final String TAG_returnPlaceLongitude ="returnPlaceLongitude";
    private static final String TAG_distanceKM ="distanceKM";
    private static final String TAG_useTime ="useTime";
    private static final String TAG_macAddress = "macAddress";
    public ArrayList<HashMap<String, String>> mArrayList;
    ListView mListViewList;

    private String userID;
    String mJsonString;

    private Button createQRBtn, button_1, button_2;
    private EditText macAddress;
    private TextView listSize;
    private ConstraintLayout editLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creat_qrcode);

        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        Intent intent1 = getIntent(); /*데이터 수신*/
        userID = intent1.getExtras().getString("userID");

        mArrayList = new ArrayList<>();
        mListViewList = (ListView) findViewById(R.id.listView_main_list);
        getmybicyclelist();

        macAddress = (EditText) findViewById(R.id.macaddressEditText);

        createQRBtn = (Button) findViewById(R.id.creatButton);
        createQRBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                editLayout = (ConstraintLayout)findViewById(R.id.editLayout);
                editLayout.setVisibility(View.VISIBLE);
            }
        });

        button_1 = (Button) findViewById(R.id.button_1);
        button_1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                imm.hideSoftInputFromWindow(macAddress.getWindowToken(), 0);
                macAddress.setText("");
                editLayout = (ConstraintLayout)findViewById(R.id.editLayout);
                editLayout.setVisibility(View.GONE);
            }
        });

        button_2 = (Button) findViewById(R.id.button_2);
        button_2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(CreatBicycleActivity.this, CreatQR.class);
                intent.putExtra("MAC_ADDRESS",macAddress.getText().toString());
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });
    }

    private void getmybicyclelist() {
        String bicycleState = userID;

        Response.Listener<String> resposneListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d("tag", "response - " + response);

                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

                    for(int i=0;i<jsonArray.length();i++) {

                        JSONObject item = jsonArray.getJSONObject(i);

                        String bicyclenumber = item.getString(TAG_bicycleNumber);
                        String rentalPlaceLatitude = item.getString(TAG_rentalPlaceLatitude);
                        String rentalPlaceLongitude = item.getString(TAG_rentalPlaceLongitude);
                        String bicycleState = item.getString(TAG_bicycleState);
                        String macAddress = item.getString(TAG_macAddress);
                        String address = getCurrentAddress(Double.parseDouble(rentalPlaceLatitude),Double.parseDouble(rentalPlaceLongitude));
                        Log.d("tag", "response - " + address);

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put(TAG_bicycleNumber, bicyclenumber);
                        hashMap.put(TAG_rentalPlaceLatitude, rentalPlaceLatitude);
                        hashMap.put(TAG_rentalPlaceLongitude, rentalPlaceLongitude);
                        hashMap.put(TAG_bicycleState, bicycleState);
                        hashMap.put(TAG_macAddress, macAddress);
                        hashMap.put("address", address);
                        hashMap.put("text1", bicyclenumber+" ("+macAddress+")");
                        if (bicycleState.equals("available")){
                            hashMap.put("text2", "대기중"+" - "+address);
                        } else if(bicycleState.equals(("unavailable"))){
                            hashMap.put("text2", "사용중"+" - "+address);
                        }

                        mArrayList.add(hashMap);
                    }

                    String TAG_listSize = Integer.toString(jsonArray.length());
                    listSize = (TextView)findViewById(R.id.textItemsize) ;
                    listSize.setText(TAG_listSize + "건");

                    ListAdapter adapter = new SimpleAdapter(
                            CreatBicycleActivity.this, mArrayList, R.layout.item_list2,
                            new String[]{"text1", "text2"},
                            new int[]{R.id.textViewbicycleID, R.id.textViewbicycleState}
                    );

                    mListViewList.setAdapter(adapter);
                    mListViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            HashMap<String, String> map  = mArrayList.get(position);
                            Intent intent = new Intent(CreatBicycleActivity.this, ShowUserBicycleActivity.class);
                            intent.putExtra("map", map);
                            intent.putExtra("userID",userID);
                            startActivity(intent);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"조회 처리시 에러발생!", Toast.LENGTH_SHORT).show();
            }
        };

        // Volley 로 회원양식 웹으로 전송
        userBicycle_param bicycleStateRequest = new userBicycle_param(userID,resposneListener,errorListener);
        bicycleStateRequest.setShouldCache(false);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(bicycleStateRequest);
    }

    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }

}
