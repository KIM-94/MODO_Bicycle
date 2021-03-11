package com.example.capstond.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.capstond.PostParams.bicycleState_param;
import com.example.capstond.R;
import com.example.capstond.service.GpsTracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;



public class ReservationActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {
    private int apiRequestCount;

    public static boolean startFlagForCoronaApi;

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

    //GPS
    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private double latitude;
    private double longitude;

    //google map
    private GoogleMap mMap;
    private Button button1;

    private String userID;

    public ArrayList<HashMap<String, String>> mArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        Intent intent1 = getIntent(); /*데이터 수신*/
        userID = intent1.getExtras().getString("userID");

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        button1 = (Button)findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(ReservationActivity.this);
                integrator.setCaptureActivity(CustomScannerActivity.class);
                integrator.initiateScan();
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mArrayList = new ArrayList<>();
        getbicyclelistAPI();

        apiRequestCount = 0;
        final Handler temp_handler = new Handler();
        temp_handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (apiRequestCount < 100) {
                    if (startFlagForCoronaApi) {
                        apiRequestCount++;
                        temp_handler.postDelayed(this, 100);
                    } else {
                        //api 호출이 완료 되었을 떄
                        drawMarker();
                    }
                } else {
                    //api 호출이 10초 이상 경괴했을 때
                    Toast.makeText(getApplicationContext(), "호출에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                }
            }


        }, 100);

        mMap.setOnMarkerClickListener(ReservationActivity.this);

        Log.d("tag", "onMap - ");
        gpsTracker = new GpsTracker(ReservationActivity.this);
        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();
        LatLng Clocation = new LatLng(latitude, longitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(Clocation));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Clocation,15));
        mMap.setMyLocationEnabled(true);    // 현재위치 파란점
        mMap.setOnMapClickListener(this);   // 구글맵 클릭 될 때 터치이벤트 설정
    }

    public boolean onMarkerClick(Marker marker) {
        ConstraintLayout stateView = (ConstraintLayout)findViewById(R.id.rectangle_6);
        stateView.setVisibility(View.VISIBLE);
        TextView bicycleName = (TextView)findViewById(R.id.textView);
        bicycleName.setText(marker.getTitle());

        LatLng Clocation = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Clocation));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Clocation,15));
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        ConstraintLayout stateView = (ConstraintLayout)findViewById(R.id.rectangle_6);
        stateView.setVisibility(View.INVISIBLE);
    }

    private void drawMarker() {

        HashMap<String,String> hashMap1 = new HashMap<>();
        MarkerOptions markerOptions = new MarkerOptions();
        for(int i=0; i<mArrayList.size(); i++){
            hashMap1 = mArrayList.get(i);
            String Lats = String.format("%.6f", Double.parseDouble(hashMap1.get(TAG_rentalPlaceLatitude)));
            String Longs = String.format("%.6f", Double.parseDouble(hashMap1.get(TAG_rentalPlaceLongitude)));
            double Lat1, Long1;
            Lat1 = Double.parseDouble(Lats);
            Long1 = Double.parseDouble(Longs);
            markerOptions.position(new LatLng(Lat1, Long1));
            markerOptions.title(hashMap1.get(TAG_bicycleNumber));
            markerOptions.snippet(hashMap1.get(TAG_bicycleState));
            mMap.addMarker(markerOptions);
        }
        return;
    }

    private void getbicyclelistAPI() {
        String bicycleState = "available";

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

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put(TAG_bicycleNumber, bicyclenumber);
                        hashMap.put(TAG_rentalPlaceLatitude, rentalPlaceLatitude);
                        hashMap.put(TAG_rentalPlaceLongitude, rentalPlaceLongitude);
                        hashMap.put(TAG_bicycleState, bicycleState);

                        mArrayList.add(hashMap);
                    }
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
        bicycleState_param bicycleStateRequest = new bicycleState_param(bicycleState,resposneListener,errorListener);
        bicycleStateRequest.setShouldCache(false);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(bicycleStateRequest);
    }

    //  GPS
    //  ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if ( check_result ) {
                //위치 값을 가져올 수 있음
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(ReservationActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(ReservationActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(ReservationActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(ReservationActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)

            // 3.  위치 값을 가져올 수 있음
        } else {
            //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(ReservationActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(ReservationActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(ReservationActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);

            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(ReservationActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ReservationActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }

        Log.d("onActivityResult", "onActivityResult: .");
        if (resultCode == Activity.RESULT_OK) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            String re = scanResult.getContents();
            String message = re;
            Log.d("onActivityResult", "onActivityResult: ." + re);
            Toast.makeText(this, re, Toast.LENGTH_LONG).show();

            Intent intent2 = new Intent(ReservationActivity.this, ReservationRidingActivity.class);
            intent2.putExtra("userID",userID);
            intent2.putExtra("bicycleNumber", re);
            startActivity(intent2);
            finish();
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}
