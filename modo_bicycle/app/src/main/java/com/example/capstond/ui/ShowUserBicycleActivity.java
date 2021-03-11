package com.example.capstond.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.capstond.PostParams.bicycleHistory_param;
import com.example.capstond.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class ShowUserBicycleActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    private GoogleMap mMap;

    private static final String TAG_JSON="webnautes";
    private static final String TAG_userID = "userID";
    private static final String TAG_bicycleNumber = "bicycleNumber";
    private static final String TAG_bicycleState ="bicycleState";
    private static final String TAG_rentalTime ="rentalTime";
    private static final String TAG_returnTime ="returnTime";
    private static final String TAG_rentalPlaceLatitude ="rentalPlaceLatitude";
    private static final String TAG_rentalPlaceLongitude ="rentalPlaceLongitude";
    private static final String TAG_returnPlaceLatitude ="returnPlaceLatitude";
    private static final String TAG_returnPlaceLongitude ="returnPlaceLongitude";
    private static final String TAG_distanceKM ="distanceKM";
    private static final String TAG_useTime ="useTime";
    private static final String TAG_useDistance ="useDistance";


    ArrayList<HashMap<String, String>> mArrayList;
    ListView mListViewList;

    private TextView listSize, textViewbicyclenumber, textViewuserid;
    private Button buttonshowqr, button_2, buttonshare;
    private ImageView imageView2;
    private ConstraintLayout qrlayout, constraintLayoutmap;
    private LinearLayout linearLayoutlist;

    private String bicycleNumber, rentalPlaceLatitude, rentalPlaceLongitude, bicycleState, address;
    String imgName = "QR_";    // 이미지 이름



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user_bicycle);

        Intent intent1 = getIntent(); /*데이터 수신*/
        final String userID = intent1.getExtras().getString("userID");

        Intent intent = getIntent();
        HashMap<String, String> hashMap = (HashMap<String, String>)intent.getSerializableExtra("map");
        bicycleNumber = hashMap.get("bicycleNumber");
        bicycleState = hashMap.get("bicycleState");
        rentalPlaceLatitude = hashMap.get("rentalPlaceLatitude");
        rentalPlaceLongitude = hashMap.get("rentalPlaceLongtitude");
        address = hashMap.get("address");

        mListViewList = (ListView) findViewById(R.id.listView_main_list);

        //mArrayList.clear();
        mArrayList = new ArrayList<>();

        listSize = (TextView)findViewById(R.id.textItemsize);

        getmybicyclelist(bicycleNumber);

        constraintLayoutmap = (ConstraintLayout)findViewById(R.id.constraintLayoutmap);
        linearLayoutlist = (LinearLayout) findViewById(R.id.linearLayoutlist);

        imageView2 = (ImageView)findViewById(R.id.qrcode);
        qrlayout = (ConstraintLayout)findViewById(R.id.qrlayout);
        button_2 = (Button)findViewById(R.id.button_2);
        buttonshare = (Button)findViewById(R.id.buttonshare);
        textViewbicyclenumber = (TextView)findViewById(R.id.textViewbicyclenumber);
        textViewuserid = (TextView)findViewById(R.id.textViewuserid);

        buttonshowqr = (Button)findViewById(R.id.buttonshowqr);
        buttonshowqr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    qrlayout.setVisibility(View.VISIBLE);
                    String imgpath = getCacheDir() + "/" + imgName+bicycleNumber+".png";   // 내부 저장소에 저장되어 있는 이미지 경로
                    Bitmap bm = BitmapFactory.decodeFile(imgpath);
                    imageView2.setImageBitmap(bm);   // 내부 저장소에 저장된 이미지를 이미지뷰에 셋
                    textViewbicyclenumber.setText(bicycleNumber);
                    textViewuserid.setText(userID);
                    Toast.makeText(getApplicationContext(), "파일 로드 성공", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "파일 로드 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qrlayout.setVisibility(View.GONE);
            }
        });

//        buttonshare.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//                String imgpath = getCacheDir() + "/" + imgName+bicycleNumber+".png";   // 내부 저장소에 저장되어 있는 이미지 경로
//                Bitmap bm = BitmapFactory.decodeFile(imgpath);
//                sharingIntent.setType("image/png");
//                sharingIntent.putExtra(Intent.EXTRA_STREAM, bm);
//                startActivity(Intent.createChooser(sharingIntent, "Share image using")); // 변경가능
//            }
//        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getmybicyclelist(String bicycleNumber) {
//        String bicycleState = userID;

        Response.Listener<String> resposneListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d("tag", "response - " + response);

                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);


                    for(int i=jsonArray.length()-1;0<=i;i--) {

                        JSONObject item = jsonArray.getJSONObject(i);

                        String userid = item.getString(TAG_userID);
                        String bicyclenumber = item.getString(TAG_bicycleNumber);
                        String rentaltime = item.getString(TAG_rentalTime);
                        String returntime = item.getString(TAG_returnTime);
                        String rentalPlaceLatitude = item.getString(TAG_rentalPlaceLatitude);
                        String rentalPlaceLongitude = item.getString(TAG_rentalPlaceLongitude);
                        String returnPlaceLatitude = item.getString(TAG_returnPlaceLatitude);
                        String returnPlaceLongitude = item.getString(TAG_returnPlaceLongitude);
                        String usetime = item.getString(TAG_useTime);
                        String useDistance = item.getString(TAG_useDistance);

                        String date = rentaltime.split(" ")[0];
                        String timeA = rentaltime.split(" ")[1].split(":")[0]+":"+rentaltime.split(" ")[1].split(":")[1];
                        String timeB = returntime.split(" ")[1].split(":")[0]+":"+returntime.split(" ")[1].split(":")[1];
                        String time = "총 "+usetime+" | "+timeA+" - "+timeB;
                        String pointA = rentalPlaceLatitude+" "+rentalPlaceLongitude;
                        String pointB = returnPlaceLatitude+" "+returnPlaceLongitude;

//                getCurrentAddress(rentalPlaceLatitude, rentalPlaceLongitude);

                        HashMap<String,String> hashMap = new HashMap<>();
                        hashMap.put(TAG_userID, userid);
                        hashMap.put(TAG_bicycleNumber, bicyclenumber);
                        hashMap.put(TAG_rentalTime, rentaltime);
                        hashMap.put(TAG_returnTime, returntime);
                        hashMap.put(TAG_rentalPlaceLatitude, rentalPlaceLatitude);
                        hashMap.put(TAG_rentalPlaceLongitude, rentalPlaceLongitude);
                        hashMap.put(TAG_returnPlaceLatitude, returnPlaceLatitude);
                        hashMap.put(TAG_returnPlaceLongitude, returnPlaceLongitude);
                        hashMap.put(TAG_distanceKM, useDistance);
                        hashMap.put(TAG_useTime, usetime);
                        hashMap.put(TAG_useDistance, useDistance);

                        hashMap.put("date", date);
                        hashMap.put("pointA", pointA);
                        hashMap.put("pointB", pointB);
                        hashMap.put("time", time);

                        mArrayList.add(hashMap);
                    }

                    String TAG_listSize = Integer.toString(jsonArray.length());
                    listSize.setText(TAG_listSize + "건");


                    ListAdapter adapter = new SimpleAdapter(
                            ShowUserBicycleActivity.this, mArrayList, R.layout.item_list3,
                            new String[]{"date", "pointA", "pointB", "time", TAG_distanceKM},
                            new int[]{R.id.textViewDate, R.id.textViewA, R.id.textViewB, R.id.textViewTime, R.id.textViewDist}
                    );

                    mListViewList.setAdapter(adapter);

                    mListViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            HashMap<String, String> map  = mArrayList.get(position);
                            Intent intent = new Intent(ShowUserBicycleActivity.this, ShowDetailDataActivity.class);
                            intent.putExtra("map", map);
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
        bicycleHistory_param bicycleStateRequest = new bicycleHistory_param(bicycleNumber,resposneListener,errorListener);
        bicycleStateRequest.setShouldCache(false);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(bicycleStateRequest);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mArrayList = new ArrayList<>();

        drawMarker();

        Log.d("tag", "onMap - ");
        Log.d("", "lat "+rentalPlaceLatitude+" long "+rentalPlaceLongitude);
        LatLng Clocation = new LatLng(Double.parseDouble(rentalPlaceLatitude), Double.parseDouble(rentalPlaceLongitude));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(Clocation));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Clocation,15));
    }

    private void drawMarker() {
        MarkerOptions markerOptions = new MarkerOptions();
        double Lat1, Long1;
        Lat1 = Double.parseDouble(rentalPlaceLatitude);
        Long1 = Double.parseDouble(rentalPlaceLongitude);
        markerOptions.position(new LatLng(Lat1, Long1));
        markerOptions.title(bicycleNumber+ " - "+bicycleState);
        markerOptions.snippet(address);
        mMap.addMarker(markerOptions);

        return;
    }

}
