package com.example.capstond.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.capstond.PostParams.getMacAddress_param;
import com.example.capstond.PostParams.updateBicycleHistory_param;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ReservationRidingActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private static String IP_ADDRESS = "193.122.111.182/capstonD2";

    // #Bluetooth
    int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter myBluetoothAdapter;
    Intent btEnableIntent;
    public BluetoothSocket bluetoothSocket = null; // 블루투스 소켓
    private AlertDialog dialog;
    private OutputStream outputStream = null; // 블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; // 블루투스에 데이터를 입력하기 위한 입력 스트림
    private String ARDUINO_CONNECT = "connect";
    private String ARDUINO_LOCK = "lock";
    private int readBufferPosition; // 버퍼 내 문자 저장 위치
    private byte[] readBuffer; // 수신 된 문자열을 저장하기 위한 버퍼
    private Thread workerThread = null; // 문자열 수신에 사용되는 쓰레드

    private String macAddress;


    private String bicycleNumber;
    private String userID;

    private GpsTracker gpsTracker;
    private double start_latitude;
    private double start_longitude;
    private double finish_latitude;
    private double finish_longitude;

    private String start_time, finish_time;
    Date curDate = new Date();
    private long curDateTime = curDate.getTime();
    private long reqDateTime = curDate.getTime();


    private FusedLocationProviderClient mFusedLocationClient;

    private static final int REQUEST_CORE_LOCATION_PERMISSION = 1;

    Button btn1, btn2;
    TextView textViewTime, textViewKM, textViewKmH;

    private LocationService mService;
    private boolean isBind;
    private Boolean isRunning = true;
    private Thread timeThread = null;

    private LocationRequest locationRequest;
    private Location location;
    LatLng currentPosition;
    Location mCurrentLocatiion;

    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초

    private GoogleMap mMap;
    private Marker currentMarker = null;

    private Double distAdd;

    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소

    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;


    private ServiceConnection sconn = new ServiceConnection() {
        @Override //서비스가 실행될 때 호출
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.MyBinder myBinder = (LocationService.MyBinder) service;
            mService = myBinder.getService();

            isBind = true;
            Log.e("LOG", "onServiceConnected()");
        }

        @Override //서비스가 종료될 때 호출
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

        Intent intent1 = getIntent(); /*데이터 수신*/
        userID = intent1.getExtras().getString("userID");

        Intent intent = getIntent(); /*데이터 수신*/
        bicycleNumber = intent.getExtras().getString("bicycleNumber");

        getMacAddressAPI(bicycleNumber);


        if (ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    ReservationRidingActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CORE_LOCATION_PERMISSION
            );
        }

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothOnMethod(); //켜기 버튼과 연계

        btn1 = (Button)findViewById(R.id.btn_music_start);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("","start btn click");

                sendData(ARDUINO_CONNECT);

                // gps
                gpsTracker = new GpsTracker(ReservationRidingActivity.this);
                start_latitude = gpsTracker.getLatitude();
                start_longitude = gpsTracker.getLongitude();


                // TIME.
                long now = System.currentTimeMillis();  // 현재시간을 msec 으로 구한다
                Date date = new Date(now);  // 현재시간을 date 변수에 저장한다.
                SimpleDateFormat sdfNow1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                start_time = sdfNow1.format(date);  // nowDate 변수에 값을 저장한다.

                // DATETIME
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                //현재시간을 요청시간의 형태로 format 후 time 가져오기
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

                // 타이머
                timeThread = new Thread(new ReservationRidingActivity.timeThread());
                timeThread.start();

                LottieAnimationView animationView4 = findViewById(R.id.lottieAnimView_gps);
                setUpAnimation(animationView4);
            }
        });

        btn2 = (Button)findViewById(R.id.btn_music_stop);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("","stop btn click");
                sendData(ARDUINO_LOCK);
                resetConnection();

                stopLocationService();

                // 타이머
                isRunning = !isRunning;

                // gps
                gpsTracker = new GpsTracker(ReservationRidingActivity.this);

                finish_latitude = gpsTracker.getLatitude();
                finish_longitude = gpsTracker.getLongitude();


                // TIME
                long now = System.currentTimeMillis();  // 현재시간을 msec 으로 구한다.
                Date date = new Date(now);  // 현재시간을 date 변수에 저장한다.
                SimpleDateFormat sdfNow2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                finish_time = sdfNow2.format(date); // nowDate 변수에 값을 저장한다.

                // DATETIME
                Date curDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

                //현재시간을 요청시간의 형태로 format 후 time 가져오기
                try {
                    curDate = dateFormat.parse(dateFormat.format(curDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                curDateTime = curDate.getTime();

                long minute = (curDateTime - reqDateTime) / 60000;
                String Sminute = String.valueOf(minute);
                Sminute = Sminute+" 분";

                String st_Lati = String.valueOf(start_latitude);
                String st_Long = String.valueOf(start_longitude);
                String fi_Lati = String.valueOf(finish_latitude);
                String fi_Long = String.valueOf(finish_longitude);

//                // update bicycle history
//                InsertData task = new InsertData();
//                task.execute("http://" + IP_ADDRESS + "/insertHistory.php", userID, bicycleNumber, st_Lati, st_Long, fi_Lati, fi_Long, start_time, finish_time, Sminute);

                updateBicycleHistoryAPI(userID, bicycleNumber, st_Lati, st_Long, fi_Lati, fi_Long, start_time, finish_time, Sminute);

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

        // 반복횟수를 무한히 주고 싶을 땐 LottieDrawable.INFINITE or 원하는 횟수
        animationView.setRepeatCount(LottieDrawable.INFINITE);
        // 시작
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

    // 타이머
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            int mSec = msg.arg1 % 100;
            int sec = (msg.arg1) % 60;
            int min = (msg.arg1) / 60;
            int hour = (msg.arg1) / 3600;
            //1000이 1초 1000*60 은 1분 1000*60*10은 10분 1000*60*60은 한시간

            @SuppressLint("DefaultLocale") String result = String.format("%02d:%02d:%02d", hour, min, sec);

            textViewTime = (TextView)findViewById(R.id.textViewTime);
            textViewTime.setText(result);

            if (mService == null) {
                Toast.makeText(ReservationRidingActivity.this, "mService가 null이므로 불러 올 수 없습니다.", Toast.LENGTH_SHORT).show();
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
                while (isRunning) { //일시정지를 누르면 멈춤
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
                        return; // 인터럽트 받을 경우 return
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

        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            startLocationUpdates(); // 3. 위치 업데이트 시작
        }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT).show();
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        // 현재 오동작을 해서 주석처리

        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
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
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude()) + " 경도:" + String.valueOf(location.getLongitude());

                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);

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
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
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

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
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
        markerOptions.position(currentLatLng);  // 마커 위치 설정 (필수)
        markerOptions.visible(false);   // 마커 보이지 않게 설정

        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);
    }

    public void setDefaultLocation() {
        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";

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


    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
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



    // ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

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
                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
//                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT).show();
                }else {
//                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ReservationRidingActivity.this);
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
                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");
                        needRequest = true;
                        return;
                    }
                }
                break;
        }

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                //블루투스가 활성화 되었다.
                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되었습니다.", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                //블루투스 켜는것을 취소했다.
                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void getMacAddressAPI(String bicycleNumber) {

        final String TAG_JSON="webnautes";
        final String BICYCLE_MACADDRESS = "macAddress";

        Response.Listener<String> resposneListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d( "", "onMapClick :"+response);

//                    JSONObject jsonObject = new JSONObject(response);
//                    String success = jsonObject.getString("success");

                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

                    JSONObject item = jsonArray.getJSONObject(0);

                    macAddress = item.getString(BICYCLE_MACADDRESS);

                    connectDevice();

//                    if (success != null && success.equals("1")) {  // 회원가입 완료
//                        Toast.makeText(getApplicationContext(),"MacAddress 조회완료",Toast.LENGTH_SHORT).show();
////                        Intent intent = new Intent(ReservationRidingActivity.this, LoginActivity.class);
//////                        intent.putExtra("userID", userID);
////                        startActivity(intent);
////                        finish();
//
//                        JSONObject jsonObject = new JSONObject(mJsonString);
//                        JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
//
//                        JSONObject item = jsonArray.getJSONObject(0);
//
//                        macAddress = item.getString(BICYCLE_MACADDRESS);
//
//                        bluetoothPairing2();
//                    }
//                    else {
//                        Toast.makeText(getApplicationContext(),"MacAddress 조회실패",Toast.LENGTH_SHORT).show();
//                        return;
//                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"MacAddress 조회 처리시 에러발생", Toast.LENGTH_SHORT).show();
                return;
            }
        };

        // Volley 로 회원양식 웹으로 전송
        getMacAddress_param getMacAddressRequest = new getMacAddress_param(bicycleNumber,
                resposneListener, errorListener);
        getMacAddressRequest.setShouldCache(false);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(getMacAddressRequest);
    }



    private void updateBicycleHistoryAPI(String userID, String bicycleNumber,String rentalPlaceLatitude,String rentalPlaceLongitude,String returnPlaceLatitude,String returnPlaceLongitude, String rentalTime, String returnTime, String useTime) {

        Response.Listener<String> resposneListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d( "", "onMapClick :"+response);

                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    if (success != null && success.equals("1")) {  // 회원가입 완료
                        Toast.makeText(getApplicationContext(),"사용기록 업데이트 완료",Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(ReservationRidingActivity.this, LoginActivity.class);
////                        intent.putExtra("userID", userID);
//                        startActivity(intent);
//                        finish();
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"사용기록 업데이트 실패",Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"사용기록 업데이트 처리시 에러발생", Toast.LENGTH_SHORT).show();
                return;
            }
        };

        // Volley 로 회원양식 웹으로 전송
        updateBicycleHistory_param updatehistoryRequest = new updateBicycleHistory_param(userID, bicycleNumber, rentalPlaceLatitude, rentalPlaceLongitude, returnPlaceLatitude, returnPlaceLongitude, rentalTime, returnTime, useTime,
                resposneListener, errorListener);
        updatehistoryRequest.setShouldCache(false);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(updatehistoryRequest);
    }

//    private void showResult(){
//
//        String TAG_JSON="webnautes";
//        String BICYCLE_MACADDRESS = "macAddress";
//
//        try {
//            JSONObject jsonObject = new JSONObject(mJsonString);
//            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
//
//            JSONObject item = jsonArray.getJSONObject(0);
//
//            macAddress = item.getString(BICYCLE_MACADDRESS);
//
////            bluetoothPairing2();
//            connectDevice();
//        }
//        catch (JSONException e) {
//
//            Log.d(TAG, "showResult : ", e);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    // #Bluetooth
    private void bluetoothOnMethod(){
        /*
        bt_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
        */
        if(myBluetoothAdapter == null){
            //Device does not support Bluetooth
            //할게 없다 앱을 종료 한다. 블루투스앱인데 블루투스 지원 안하는데 뭘 하겠어.
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
        } else {
            //블루투스 되는 기기이다.
            //그렇다면 지금 현재 블루투스 기능이 켜져 있는지 체크 해야 한다.
            if(!myBluetoothAdapter.isEnabled()){ //false이면
                //지금 꺼져 있으니 켜야 한다.
                btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(btEnableIntent, REQUEST_ENABLE_BT);
                //위 결과값을 받아서 처리 한다.
            }
        }
    }

//    //startActivityForResult 실행 후 결과를 처리하는 부분
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_ENABLE_BT) {
//            if (resultCode == RESULT_OK) {
//                //블루투스가 활성화 되었다.
//                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되었습니다.", Toast.LENGTH_SHORT).show();
//            } else if (resultCode == RESULT_CANCELED) {
//                //블루투스 켜는것을 취소했다.
//                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되지 않았습니다.", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

//    private void bluetoothPairing2() throws IOException {
//
////        //어뎁터 선언 해준 뒤에 String에 디바이스 맥 주소를 넣어주세요
////        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
////        String Address = macAddress;
////
////        //어뎁터로 선언한 btAdapter로 블루투스 디바이스 주소를 넣어 줍니다.
////        BluetoothDevice device = btAdapter.getRemoteDevice(Address);
//
//        //커넥트 시도하면 됩니다.
////        connectDevice(device);
//        connectDevice();
//
//    }


    public void connectDevice() throws IOException {

        //어뎁터 선언 해준 뒤에 String에 디바이스 맥 주소를 넣어주세요
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        String Address = macAddress;

        //어뎁터로 선언한 btAdapter로 블루투스 디바이스 주소를 넣어 줍니다.
        BluetoothDevice device = btAdapter.getRemoteDevice(Address);

        AlertDialog.Builder builder = new AlertDialog.Builder(ReservationRidingActivity.this);

        // UUID 생성
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        // Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성
        try {
//            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);

            bluetoothSocket.connect();

            // 데이터 송,수신 스트림을 얻어옵니다.
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();

//            mTextViewResult.setText("상태 : " + "연결되었습니다.");

            dialog = builder.setMessage(macAddress + "에 연결되었습니다.").setNegativeButton("확인", null).create();

            // 데이터 수신 함수 호출
            receiveData();

        } catch (IOException e) {
            e.printStackTrace();
//            btAdapter.disable();
            bluetoothSocket.close();
            dialog = builder.setMessage(macAddress + "에 연결하지 못하였습니다.").setNegativeButton("확인", null).create();

//            mTextViewResult.setText("상태 : " + "페어링 실패하였습니다.");
        }

        dialog.show();

    }

    private void resetConnection() {
        if (inputStream != null) {
            try {inputStream.close();} catch (Exception e) {}
            inputStream = null;
        }

        if (outputStream != null) {
            try {outputStream.close();} catch (Exception e) {}
            outputStream = null;
        }

        if (bluetoothSocket != null) {
            try {bluetoothSocket.close();} catch (Exception e) {}
            bluetoothSocket = null;
        }
    }


    public void sendData(String text) {
        // 문자열에 개행문자("\n")를 추가해줍니다.
        text += "\n";
        try{
            // 데이터 송신
            outputStream.write(text.getBytes());
        }catch(Exception e) {
            e.printStackTrace();
        }
    }


    public void receiveData() {
//        mTextViewResult.setText("상태 : " + ".");

//        mTextViewRentalPlace.setText("adfgg");

        final Handler handler = new Handler();

        // 데이터를 수신하기 위한 버퍼를 생성

        readBufferPosition = 0;

        readBuffer = new byte[1024];



        // 데이터를 수신하기 위한 쓰레드 생성
        workerThread = new Thread(new Runnable() {

            @Override

            public void run() {

                while(Thread.currentThread().isInterrupted()) {

                    try {

                        // 데이터를 수신했는지 확인합니다.

                        final int byteAvailable = inputStream.available();

//                        mTextViewBicyclenumber.setText("상태 : " + byteAvailable);

                        // 데이터가 수신 된 경우

                        if(byteAvailable > 0) {
                            Log.d(TAG, "수신확인 ");
//                            mTextViewBicyclenumber.setText("상태 : " + byteAvailable);

                            // 입력 스트림에서 바이트 단위로 읽어 옵니다.

                            byte[] bytes = new byte[byteAvailable];

                            inputStream.read(bytes);

                            // 입력 스트림 바이트를 한 바이트씩 읽어 옵니다.

                            for(int i = 0; i < byteAvailable; i++) {

                                byte tempByte = bytes[i];

                                // 개행문자를 기준으로 받음(한줄)
                                if (tempByte == '\n') {

                                    // readBuffer 배열을 encodedBytes로 복사

                                    byte[] encodedBytes = new byte[readBufferPosition];

                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);

                                    // 인코딩 된 바이트 배열을 문자열로 변환

                                    final String text = new String(encodedBytes);

                                    readBufferPosition = 0;

//                                    mTextViewResult.setText("상태 : " + ".");

                                    handler.post(new Runnable() {

                                        @Override

                                        public void run() {

                                            Log.d("",text);

                                            // 텍스트 뷰에 출력
//                                            mTextViewResult.setText("상태 : " + ".");

                                            // 페어링 완료 신호
                                            if (text == "inputConnectOK") {
                                                Log.d("",text);
//                                                mTextViewResult.setText("상태 : " + ".");

//                                                gpsTracker = new GpsTracker(ScanSuccessActivity.this);
//
//                                                start_latitude = gpsTracker.getLatitude();
//                                                start_longitude = gpsTracker.getLongitude();

//                                                //TIME
//                                                // 현재시간을 msec 으로 구한다.
//                                                long now = System.currentTimeMillis();
//                                                // 현재시간을 date 변수에 저장한다.
//                                                Date date = new Date(now);
//                                                // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
//                                                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//                                                // nowDate 변수에 값을 저장한다.
//                                                String start_time = sdfNow.format(date);

//                                                mTextViewRentalPlace.setText("대여장소 : " + start_latitude + ", " + start_longitude);
//                                                mTextViewRentalTime.setText("대여시간 : " + start_time);
                                            }
                                            // 잠금동작 신호
                                            else if (text.equals("input Lock OK")) {
                                                Log.d("",text);
//                                                resetConnection();
//                                                try {
//                                                    bluetoothSocket.close();
//                                                } catch (IOException e) {
//                                                    e.printStackTrace();
//                                                }
//                                                gpsTracker = new GpsTracker(ScanSuccessActivity.this);
//
//                                                finish_latitude = gpsTracker.getLatitude();
//                                                finish_longitude = gpsTracker.getLongitude();

//                                                //TIME
//                                                // 현재시간을 msec 으로 구한다.
//                                                long now = System.currentTimeMillis();
//                                                // 현재시간을 date 변수에 저장한다.
//                                                Date date = new Date(now);
//                                                // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
//                                                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//                                                // nowDate 변수에 값을 저장한다.
//                                                String finish_time = sdfNow.format(date);

//                                                mTextViewReturnPlace.setText("반납장소 : " + finish_latitude + ", " + finish_longitude);
//                                                mTextViewReturnTime.setText("반납시간 : " + finish_time);
                                            }
                                        }

                                    });
                                } // 개행 문자가 아닐 경우

                                else {
                                    readBuffer[readBufferPosition++] = tempByte;
                                }
                            }
                        }
                    } catch (IOException e) {
                        Log.d(TAG, "수신실패 ");
                        e.printStackTrace();
                    }
                    try {
                        // 1초마다 받아옴
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        });
        workerThread.start();
    }

    //PHP
//    class InsertData extends AsyncTask<String, Void, String> {
//        ProgressDialog progressDialog;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//            progressDialog = ProgressDialog.show(ReservationRidingActivity.this,
//                    "Please Wait", null, true, true);
//        }
//
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//
//            progressDialog.dismiss();
//            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
//
//            Log.d(TAG, "POST response  - " + result);
//        }
//
//
//        @Override
//        protected String doInBackground(String... params) {
//            String serverURL = (String) params[0];
//            String postParameters = null;
//
//            String userID = (String) params[1];
//            String bicycleNumber = (String) params[2];
//            String rentalPlaceLatitude = (String) params[3];
//            String rentalPlaceLongitude = (String) params[4];
//            String returnPlaceLatitude = (String) params[5];
//            String returnPlaceLongitude = (String) params[6];
//            String rentalTime = (String) params[7];
//            String returnTime = (String) params[8];
//            String useTime = (String) params[9];
//
//            postParameters = "userID=" + userID
//                    + "&bicycleNumber=" + bicycleNumber
//                    + "&rentalPlaceLatitude=" + rentalPlaceLatitude
//                    + "&rentalPlaceLongitude=" + rentalPlaceLongitude
//                    + "&returnPlaceLatitude=" + returnPlaceLatitude
//                    + "&returnPlaceLongitude=" + returnPlaceLongitude
//                    + "&rentalTime=" + rentalTime
//                    + "&returnTime=" + returnTime
//                    + "&useTime=" + useTime;
//
//            try {
//
//                URL url = new URL(serverURL);
//                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//
//
//                httpURLConnection.setReadTimeout(5000);
//                httpURLConnection.setConnectTimeout(5000);
//                httpURLConnection.setRequestMethod("POST");
//                httpURLConnection.connect();
//
//
//                OutputStream outputStream = httpURLConnection.getOutputStream();
//                outputStream.write(postParameters.getBytes("UTF-8"));
//                outputStream.flush();
//                outputStream.close();
//
//
//                int responseStatusCode = httpURLConnection.getResponseCode();
//                Log.d(TAG, "POST response code - " + responseStatusCode);
//
//                InputStream inputStream;
//                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
//                    inputStream = httpURLConnection.getInputStream();
//                }
//                else{
//                    inputStream = httpURLConnection.getErrorStream();
//                }
//
//
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//
//                StringBuilder sb = new StringBuilder();
//                String line = null;
//
//                while((line = bufferedReader.readLine()) != null){
//                    sb.append(line);
//                }
//
//                bufferedReader.close();
//
//                return sb.toString();
//            } catch (Exception e) {
//
//                Log.d(TAG, "InsertData: Error ", e);
//
//                return new String("Error: " + e.getMessage());
//            }
//        }
//    }
}
