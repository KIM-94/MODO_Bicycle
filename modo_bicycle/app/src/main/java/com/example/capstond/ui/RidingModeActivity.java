package com.example.capstond.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.capstond.R;
import com.example.capstond.service.Constants;
import com.example.capstond.service.GpsTracker;
import com.example.capstond.service.LocationService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RidingModeActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private static String IP_ADDRESS = "193.122.111.182/capstonD2";

    // USER information
    private String bicycleNumber = "???????????????";
    private String userID;

    // GPS service
    private GpsTracker gpsTracker;
    private double start_latitude;
    private double start_longitude;
    private double finish_latitude;
    private double finish_longitude;

    // Location service
    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1???
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5???
    private LocationService mService;
    private boolean isBind;
    private Boolean isRunning = true;
    private Thread timeThread = null;
    private LocationRequest locationRequest;
    private Location location;
    LatLng currentPosition;
    Location mCurrentLocatiion;

    // GPS & Location
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int REQUEST_CORE_LOCATION_PERMISSION = 1;

    // Check
    private String start_time, finish_time;
    Date curDate = new Date();
    private long curDateTime = curDate.getTime();
    private long reqDateTime = curDate.getTime();
    private Double distAdd;


    // UI
    Button btn1, btn2;
    TextView textViewTime, textViewKM, textViewKmH;


    // Google Map
    private GoogleMap mMap;
    private Marker currentMarker = null;


    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // ?????? ?????????

    // onRequestPermissionsResult?????? ????????? ???????????? ActivityCompat.requestPermissions??? ????????? ????????? ????????? ???????????? ?????? ???????????????.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;


    private ServiceConnection sconn = new ServiceConnection() {
        @Override //???????????? ????????? ??? ??????
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.MyBinder myBinder = (LocationService.MyBinder) service;
            mService = myBinder.getService();

            isBind = true;
            Log.e("LOG", "onServiceConnected()");
        }

        @Override //???????????? ????????? ??? ??????
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            isBind = false;
            Log.e("LOG", "onServiceDisconnected()");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ridingmode);

        Intent intent1 = getIntent(); /*????????? ??????*/
        userID = intent1.getExtras().getString("userID");

        if (ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    RidingModeActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CORE_LOCATION_PERMISSION
            );
        }

        btn1 = (Button)findViewById(R.id.btn_music_start);
                btn1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("","start btn click");

                // gps
                gpsTracker = new GpsTracker(RidingModeActivity.this);
                start_latitude = gpsTracker.getLatitude();
                start_longitude = gpsTracker.getLongitude();


                // TIME.
                long now = System.currentTimeMillis();  // ??????????????? msec ?????? ?????????
                Date date = new Date(now);  // ??????????????? date ????????? ????????????.
                SimpleDateFormat sdfNow1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // ????????? ????????? ????????? ????????? ( yyyy/MM/dd ?????? ????????? ?????? ?????? )
                start_time = sdfNow1.format(date);  // nowDate ????????? ?????? ????????????.

                // DATETIME
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                //??????????????? ??????????????? ????????? format ??? time ????????????
                try {
                    date = dateFormat.parse(dateFormat.format(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                reqDateTime = date.getTime();

                distAdd = 0.0;
                startLocationService();

                findViewById(R.id.btn_music_stop).setVisibility(View.VISIBLE);
                findViewById(R.id.btn_music_start).setVisibility(View.GONE);

                // ?????????
                timeThread = new Thread(new RidingModeActivity.timeThread());
                timeThread.start();

                LottieAnimationView animationView4 = findViewById(R.id.lottieAnimView_gps);
                setUpAnimation(animationView4);
            }
        });

        findViewById(R.id.btn_music_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("","stop btn click");

                stopLocationService();

                // ?????????
                isRunning = !isRunning;

                // gps
                gpsTracker = new GpsTracker(RidingModeActivity.this);

                finish_latitude = gpsTracker.getLatitude();
                finish_longitude = gpsTracker.getLongitude();


                // TIME
                long now = System.currentTimeMillis();  // ??????????????? msec ?????? ?????????.
                Date date = new Date(now);  // ??????????????? date ????????? ????????????.
                SimpleDateFormat sdfNow2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // ????????? ????????? ????????? ????????? ( yyyy/MM/dd ?????? ????????? ?????? ?????? )
                finish_time = sdfNow2.format(date); // nowDate ????????? ?????? ????????????.

                // DATETIME
                Date curDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

                //??????????????? ??????????????? ????????? format ??? time ????????????
                try {
                    curDate = dateFormat.parse(dateFormat.format(curDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                curDateTime = curDate.getTime();

                long minute = (curDateTime - reqDateTime) / 60000;
                String Sminute = String.valueOf(minute);
                Sminute = Sminute+" ???";

                String st_Lati = String.valueOf(start_latitude);
                String st_Long = String.valueOf(start_longitude);
                String fi_Lati = String.valueOf(finish_latitude);
                String fi_Long = String.valueOf(finish_longitude);

                // update bicycle history
                InsertData task = new InsertData();
                task.execute("http://" + IP_ADDRESS + "/insertHistory.php", userID, bicycleNumber, st_Lati, st_Long, fi_Lati, fi_Long, start_time, finish_time, Sminute);

                finish();
            }
        });

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setUpAnimation(LottieAnimationView animationView) {

        // ??????????????? ????????? ?????? ?????? ??? LottieDrawable.INFINITE or ????????? ??????
        animationView.setRepeatCount(LottieDrawable.INFINITE);
        // ??????
        animationView.playAnimation();
    }

    private boolean isLocationServiceRunning() {
        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if(activityManager != null) {
            for(ActivityManager.RunningServiceInfo service :
                    activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if(LocationService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private void startLocationService() {
//        if(isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            bindService(new Intent(getApplicationContext(), LocationService.class), sconn, BIND_AUTO_CREATE);
            Toast.makeText(this,"Location service started", Toast.LENGTH_SHORT).show();
//        }
    }

    private void stopLocationService() {
//        if(isLocationServiceRunning()) {
            unbindService(sconn);
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this,"Location service stopped", Toast.LENGTH_SHORT).show();
//        }
    }

    // ?????????
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            int mSec = msg.arg1 % 100;
            int sec = (msg.arg1) % 60;
            int min = (msg.arg1) / 60;
            int hour = (msg.arg1) / 3600;
            //1000??? 1??? 1000*60 ??? 1??? 1000*60*10??? 10??? 1000*60*60??? ?????????

            @SuppressLint("DefaultLocale") String result = String.format("%02d:%02d:%02d", hour, min, sec);

            textViewTime = (TextView)findViewById(R.id.textViewTime);
            textViewTime.setText(result);

            if (mService == null) {
                Toast.makeText(RidingModeActivity.this, "mService??? null????????? ?????? ??? ??? ????????????.", Toast.LENGTH_SHORT).show();
            } else if (mService != null) {

                String get_dist = String.format("%.8f", mService.temp_dist);
                textViewKM = (TextView) findViewById(R.id.textViewKM);
                if(mService.temp_dist>10){
                    distAdd = Double.parseDouble(String.format("%.8f", distAdd)) + 0;
                }
                else{
                    distAdd = Double.parseDouble(String.format("%.8f", distAdd)) + Double.parseDouble(String.format("%.8f", mService.temp_dist));
                }
                textViewKM.setText(String.format("%.4f", mService.add_dist));
//                textViewKM.setText(String.format("%.4f", distAdd));
                Log.d("TestTAG", "Km"+String.format("%.8f", distAdd));

                double KmH = 3600.0 / (msg.arg1) * distAdd;
                textViewKmH = (TextView) findViewById(R.id.textViewKmH);
                textViewKmH.setText(String.format("%.2f", mService.temp_KmH));
                Log.d("TestTAG", "Km/h"+String.format("%.8f", KmH));
            }
        }
    };

    public class timeThread implements Runnable {
        @Override
        public void run() {
            int i = 0;

            while (true) {
                while (isRunning) { //??????????????? ????????? ??????
                    Message msg = new Message();
                    msg.arg1 = i++;
                    handler.sendMessage(msg);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                textViewKmH.setText("");
                                textViewKmH.setText("00:00:00:00");
                            }
                        });
                        return; // ???????????? ?????? ?????? return
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");

        mMap = googleMap;

        //????????? ????????? ?????? ??????????????? GPS ?????? ?????? ???????????? ???????????????
        //????????? ??????????????? ????????? ??????
        setDefaultLocation();

        //????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ???????????????.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            // 2. ?????? ???????????? ????????? ?????????
            // ( ??????????????? 6.0 ?????? ????????? ????????? ???????????? ???????????? ????????? ?????? ????????? ?????? ???????????????.)
            startLocationUpdates(); // 3. ?????? ???????????? ??????
        }else {  //2. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ???????????????. 2?????? ??????(3-1, 4-1)??? ????????????.
            // 3-1. ???????????? ????????? ????????? ??? ?????? ?????? ????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. ????????? ???????????? ?????? ?????????????????? ???????????? ????????? ????????? ???????????? ????????? ????????????.
                Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT).show();
            } else {
                // 4-1. ???????????? ????????? ????????? ??? ?????? ?????? ???????????? ????????? ????????? ?????? ?????????.
                // ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d( TAG, "onMapClick :");
            }
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "??????:" + String.valueOf(location.getLatitude()) + " ??????:" + String.valueOf(location.getLongitude());

                //?????? ????????? ?????? ???????????? ??????
                setCurrentLocation(location, markerTitle, markerSnippet);

                // ??????????????? ????????? ???????????? ????????????
                mCurrentLocatiion = location;
            }
        }
    };

    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED   ) {
                Log.d(TAG, "startLocationUpdates : ????????? ???????????? ??????");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            if (checkPermission())
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        if (checkPermission()) {
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            if (mMap!=null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFusedLocationClient != null) {
            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }


    public String getCurrentAddress(LatLng latlng) {

        //????????????... GPS??? ????????? ??????
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //???????????? ??????
            Toast.makeText(this, "???????????? ????????? ????????????", Toast.LENGTH_LONG).show();
            return "???????????? ????????? ????????????";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "????????? GPS ??????", Toast.LENGTH_LONG).show();
            return "????????? GPS ??????";
        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "?????? ?????????", Toast.LENGTH_LONG).show();
            return "?????? ?????????";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        if (currentMarker != null) currentMarker.remove();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);  // ?????? ?????? ?????? (??????)
        markerOptions.visible(false);   // ?????? ????????? ?????? ??????

        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);
    }

    public void setDefaultLocation() {
        //????????? ??????, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "???????????? ????????? ??? ??????";
        String markerSnippet = "?????? ???????????? GPS ?????? ?????? ???????????????";

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);
    }


    //??????????????? ????????? ????????? ????????? ?????? ????????????
    private boolean checkPermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            return true;
        }
        return false;
    }



    // ActivityCompat.requestPermissions??? ????????? ????????? ????????? ????????? ???????????? ??????????????????.
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // ?????? ????????? PERMISSIONS_REQUEST_CODE ??????, ????????? ????????? ???????????? ??????????????????
            boolean check_result = true;
            // ?????? ???????????? ??????????????? ???????????????.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if ( check_result ) {
                // ???????????? ??????????????? ?????? ??????????????? ???????????????.
                startLocationUpdates();
            }
            else {
                // ????????? ???????????? ????????? ?????? ????????? ??? ?????? ????????? ??????????????? ?????? ???????????????.2 ?????? ????????? ????????????
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
//                    // ???????????? ????????? ????????? ???????????? ?????? ?????? ???????????? ????????? ???????????? ?????? ????????? ??? ????????????.
                    Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT).show();
                }else {
//                    // "?????? ?????? ??????"??? ???????????? ???????????? ????????? ????????? ???????????? ??????(??? ??????)?????? ???????????? ???????????? ?????? ????????? ??? ????????????.
                    Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    //??????????????? GPS ???????????? ?????? ????????????
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(RidingModeActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n"
                + "?????? ????????? ???????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
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
                //???????????? GPS ?????? ???????????? ??????
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : GPS ????????? ?????????");
                        needRequest = true;
                        return;
                    }
                }
                break;
        }
    }

    //PHP
    class InsertData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(RidingModeActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();

            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {
            String serverURL = (String) params[0];
            String postParameters = null;

            String userID = (String) params[1];
            String bicycleNumber = (String) params[2];
            String rentalPlaceLatitude = (String) params[3];
            String rentalPlaceLongitude = (String) params[4];
            String returnPlaceLatitude = (String) params[5];
            String returnPlaceLongitude = (String) params[6];
            String rentalTime = (String) params[7];
            String returnTime = (String) params[8];
            String useTime = (String) params[9];

            postParameters = "userID=" + userID
                    + "&bicycleNumber=" + bicycleNumber
                    + "&rentalPlaceLatitude=" + rentalPlaceLatitude
                    + "&rentalPlaceLongitude=" + rentalPlaceLongitude
                    + "&returnPlaceLatitude=" + returnPlaceLatitude
                    + "&returnPlaceLongitude=" + returnPlaceLongitude
                    + "&rentalTime=" + rentalTime
                    + "&returnTime=" + returnTime
                    + "&useTime=" + useTime;

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString();
            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }
}
