package com.example.capstond.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.capstond.service.GpsTracker;
import com.example.capstond.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


// 어플리케이션의 메뉴들이 모여있는 메인 페이지 입니다.
// 사용자의 위치정보 기반으로 날씨를 확인할 수 있습니다.
// 주변의 자전거 위치를 파악하고 자전거를 예약할 수 있는 '예약하기'
// 로그인된 사용자가 그동안 이용내역을 확인할 수 있는 '이용기록'
// 자전거에 부착할 수 있는 QR코드를 생성할 수 있는 '자전거등록'
// 사용자의 운동 기록용으로 활용할 수 있는 '라이딩모드'
public class MainActivity extends AppCompatActivity {
    private static String TAG = "phpquerytest";

    private static final String TAG_Weater_1 ="lon";
    private static final String TAG_Weater_2 ="lat";
    private static final String TAG_Weater_3 ="id";
    private static final String TAG_Weater_4 ="main";
    private static final String TAG_Weater_5 ="description";
    private static final String TAG_Weater_6 ="icon";
    private static final String TAG_Weater_7 ="temp";
    private static final String TAG_Weater_8 ="feels_like";
    private static final String TAG_Weater_9 ="temp_min";
    private static final String TAG_Weater_10 ="temp_max";
    private static final String TAG_Weater_11 ="pressure";
    private static final String TAG_Weater_12 ="humidity";
    private static final String TAG_Weater_13 ="speed";
    private static final String TAG_Weater_14 ="name";
    private static final String TAG_Weater_15 ="TAG_Weater_";
    private static final String TAG_Weater_16 ="TAG_Weater_";
    private static final String TAG_Weater_17 ="TAG_Weater_";
    private static final String TAG_Weater_18 ="TAG_Weater_";
    private static final String TAG_Weater_19 ="TAG_Weater_";
    private static final String TAG_Weater_20 ="TAG_Weater_";
    private static final String TAG_Weater_21 ="TAG_Weater_";
    private static final String TAG_Weater_22 ="TAG_Weater_";
    private static final String TAG_Weater_23 ="TAG_Weater_";
    private static final String TAG_Weater_24 ="TAG_Weater_";
    private static final String TAG_Weater_25 ="TAG_Weater_";
    private static final String TAG_Weater_26 ="TAG_Weater_";

    private RequestQueue queue;
    private String apiKEY = "openweathermap api KEY";

    private GpsTracker gpsTracker;

    ConstraintLayout main_button0, main_button1, main_button2, main_button3, main_button4;
    Button QuickQRBtn;
    TextView mainText;

    private String userID;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 날씨 조회
        weatherAPI();


        // lottie 설정
        LottieAnimationView animationView1 = findViewById(R.id.lottieAnimView1);
        setUpAnimation(animationView1);

        LottieAnimationView animationView2 = findViewById(R.id.lottieAnimView2);
        setUpAnimation(animationView2);

        LottieAnimationView animationView3 = findViewById(R.id.lottieAnimView3);
        setUpAnimation(animationView3);

        LottieAnimationView animationView4 = findViewById(R.id.lottieAnimView4);
        setUpAnimation(animationView4);


        Intent intent1 = getIntent(); /*데이터 수신*/
        userID = intent1.getExtras().getString("userID");


        mainText = (TextView)findViewById(R.id.textViewMain);
        mainText.setText(userID+"님 안녕하세요.");


        final LottieAnimationView lottieRefresh = (LottieAnimationView)findViewById(R.id.lottieAnimViewRe);
        main_button0 = (ConstraintLayout)findViewById(R.id.main_button0);
        main_button0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lottieRefresh.setSpeed(1);
                lottieRefresh.playAnimation();
                weatherAPI();
            }
        });

        main_button1 = (ConstraintLayout)findViewById(R.id.main_button1);
        main_button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReservationActivity.class);
                intent.putExtra("userID",userID);
                startActivity(intent);
            }
        });

        main_button2 = (ConstraintLayout)findViewById(R.id.main_button2);
        main_button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShowDataActivity.class);
                intent.putExtra("userID",userID);
                startActivity(intent);
            }
        });

        main_button3 = (ConstraintLayout)findViewById(R.id.main_button3);
        main_button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreatBicycleActivity.class);
                intent.putExtra("userID",userID);
                startActivity(intent);
            }
        });

        main_button4 = (ConstraintLayout)findViewById(R.id.main_button4);
        main_button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, RidingModeActivity.class);
                Intent intent = new Intent(MainActivity.this, RidingModeActivity.class);
                intent.putExtra("userID",userID);
                startActivity(intent);
            }
        });

        QuickQRBtn = (Button)findViewById(R.id.QuickQRBtn);
        QuickQRBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setCaptureActivity(CustomScannerActivity.class);
                integrator.initiateScan();
            }
        });

    }

    // QR 스캐너 촬영을 통해 수신받은 데이터
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d("onActivityResult", "onActivityResult: .");
        if (resultCode == Activity.RESULT_OK) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            String re = scanResult.getContents();
            String message = re;
            Log.d("onActivityResult", "onActivityResult: ." + re);
            Toast.makeText(this, re, Toast.LENGTH_LONG).show();

            Intent intent2 = new Intent(MainActivity.this, ReservationRidingActivity.class);
            intent2.putExtra("userID",userID);
            intent2.putExtra("bicycleNumber", re);
            startActivity(intent2);
        }
    }

    private void setUpAnimation(LottieAnimationView animationView) {
        // 반복횟수를 무한히 주고 싶을 땐 LottieDrawable.INFINITE or 원하는 횟수
        animationView.setRepeatCount(LottieDrawable.INFINITE);
        // 시작
        animationView.playAnimation();
    }

    private void setUpSunny_WetherAnimation(LottieAnimationView animationView, String weather) {
        try {
            weather = weather+".json";
            this.getAssets().open(weather); // assets 디렉터리에 파일이 있는지 확인
        } catch (Exception e) {
            weather = "weather_sunny.json";
            e.printStackTrace();
        } finally {

        }
        animationView.setAnimation(weather);
        animationView.playAnimation();
        // 시작
        animationView.playAnimation();
    }

    private void weatherAPI() {
        // gps
        gpsTracker = new GpsTracker(MainActivity.this);

        queue = Volley.newRequestQueue(this);
        String url = "http://api.openweathermap.org/data/2.5/weather?lat="+gpsTracker.getLatitude()+"&lon="+gpsTracker.getLongitude()+"&appid="+apiKEY;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // JSONObject로 변경
                try {
                    JSONObject jObject = new JSONObject(response);
                    Log.d(TAG, "weather response - " + response);

                    Log.d(TAG, "main response - " + jObject.getJSONArray("weather").getJSONObject(0).getString("main"));
                    Log.d(TAG, "description response - " + jObject.getJSONArray("weather").getJSONObject(0).getString("description"));
                    Log.d(TAG, "temp response - " + jObject.getJSONObject("main").getString("temp"));
                    Log.d(TAG, "humidity response - " + jObject.getJSONObject("main").getString("humidity"));
                    Log.d(TAG, "speed response - " + jObject.getJSONObject("wind").getString("speed"));
                    Log.d(TAG, "name response - " + jObject.getString("name"));

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put(TAG_Weater_4, jObject.getJSONArray("weather").getJSONObject(0).getString("main"));
                    hashMap.put(TAG_Weater_5, jObject.getJSONArray("weather").getJSONObject(0).getString("description"));
                    hashMap.put(TAG_Weater_7, Double.toString(Double.parseDouble(jObject.getJSONObject("main").getString("temp"))));
                    hashMap.put(TAG_Weater_12, Double.toString(Double.parseDouble(jObject.getJSONObject("main").getString("humidity"))));
                    hashMap.put(TAG_Weater_13, Double.toString(Double.parseDouble(jObject.getJSONObject("wind").getString("speed"))));
                    hashMap.put(TAG_Weater_14, jObject.getString("name"));

                    String get_main = jObject.getJSONArray("weather").getJSONObject(0).getString("main");
                    String get_description = jObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    String get_temp = Double.toString(Math.round(Double.parseDouble(jObject.getJSONObject("main").getString("temp")) - 273 ));
                    String get_location = jObject.getString("name");

                    // lottie 설정
                    LottieAnimationView animationView0 = findViewById(R.id.lottieAnimView0);
                    setUpSunny_WetherAnimation(animationView0, get_description);

                    TextView textViewWeater1 = (TextView)findViewById(R.id.textViewWeater1);
                    textViewWeater1.setText(get_temp + "℃" + " / " + get_description);

                    TextView textViewWeater2 = (TextView)findViewById(R.id.textViewWeater2);
                    textViewWeater2.setText("현재 위치 - "+get_location);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return;
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        stringRequest.setTag(TAG);
        queue.add(stringRequest);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }
}
