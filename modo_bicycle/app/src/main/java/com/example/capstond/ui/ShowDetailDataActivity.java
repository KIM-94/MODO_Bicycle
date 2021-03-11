package com.example.capstond.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capstond.R;
import com.example.capstond.db.SQLiteHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShowDetailDataActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static boolean startFlagForCoronaApi;

    private int apiRequestCount;
    public ArrayList<HashMap<String, String>> mArrayList;

    private String a, b;

    // chart
    BarChart barChart;

    private LineChart mChart;

    private static final String TAG_Time = "Time";
    private static final String TAG_LastTime = "LastTime";
    private static final String TAG_DistDif = "DistDif";
    private static final String TAG_CheckTime = "CheckTime";
    public static final String TAG_Lati = "Lati";
    public static final String TAG_Longti = "Longti";

    // google map
    private GoogleMap mMap;
    private String bicycleNumber;

    private static final String TAG_bicycleNumber = "bicycleNumber";
    private static final String TAG_rentalTime ="rentalTime";
    private static final String TAG_returnTime ="returnTime";
    private static final String TAG_useTime ="useTime";
    private static final String TAG_rentalPlaceLatitude ="rentalPlaceLatitude";
    private static final String TAG_rentalPlaceLongitude ="rentalPlaceLongitude";
    private static final String TAG_returnPlaceLatitude ="returnPlaceLatitude";
    private static final String TAG_returnPlaceLongitude ="returnPlaceLongitude";
    private static final String TAG_distanceKM ="distanceKM";

    private double rentalPlaceLatitude, rentalPlaceLongitude, returnPlaceLatitude, returnPlaceLongitude;

    TextView tvBicyclenumber, tvTime1, tvTime2, tvDist, tvAveDist, textViewTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_detail_data);

        Intent intent = getIntent();
        HashMap<String, String> hashMap = (HashMap<String, String>)intent.getSerializableExtra("map");

        tvTime1 = (TextView)findViewById(R.id.TextViewTime1SD);
        tvTime2 = (TextView)findViewById(R.id.TextViewTime2SD);
        textViewTime = (TextView)findViewById(R.id.textViewTime);
        tvDist = (TextView)findViewById(R.id.textViewKM);
        tvAveDist = (TextView)findViewById(R.id.textViewKmH);

        String start_time = hashMap.get(TAG_rentalTime);
        String finish_time = hashMap.get(TAG_returnTime);
        String Sminute = hashMap.get(TAG_useTime);

        // 이용날짜와 시간을 표시
        String get_datetime = start_time.split(" ")[0]; // 2021/03/06
        String get_AtoBtime = start_time.split(" ")[1] + " - " + finish_time.split(" ")[1] + " ("+Sminute+")";  //11:58:17 - 12:20:31 (22분)
        tvTime1.setText(get_datetime);
        tvTime2.setText(get_AtoBtime);

        // 대여시작시간과 반납시간의 시간차를 계산
        int useTime = ( Integer.parseInt(finish_time.split(" ")[1].split(":")[0])*3600
                    + Integer.parseInt(finish_time.split(" ")[1].split(":")[1])*60
                    + Integer.parseInt(finish_time.split(" ")[1].split(":")[2]) )
                -     ( Integer.parseInt(start_time.split(" ")[1].split(":")[0])*3600
                    + Integer.parseInt(start_time.split(" ")[1].split(":")[1])*60
                    + Integer.parseInt(start_time.split(" ")[1].split(":")[2]) );

        String get_usetime = String.format("%02d",useTime/3600) + ":" + String.format("%02d",useTime/60) + ":" + String.format("%02d",useTime%60);  //11:58:17 - 12:20:31 (22분)
        textViewTime.setText(get_usetime);

        // sqlite init
        final SQLiteHelper dbHelper = new SQLiteHelper(getApplicationContext(), "MODObicycle.db", null, 1);

        a = start_time;
        b = finish_time;

        // SELECT * FROM imageTb WHERE Time BETWEEN rentalTime AND returnTime;
        ArrayList<HashMap<String, String>> mArrayList2;
        mArrayList2 = dbHelper.getAllContacts(start_time, finish_time);


        try {
            rentalPlaceLatitude = Double.parseDouble(mArrayList2.get(0).get(TAG_Lati));
            rentalPlaceLongitude = Double.parseDouble(mArrayList2.get(0).get(TAG_Longti));
            returnPlaceLatitude = Double.parseDouble(mArrayList2.get(mArrayList2.size() - 1).get(TAG_Lati));
            returnPlaceLongitude = Double.parseDouble(mArrayList2.get(mArrayList2.size() - 1).get(TAG_Longti));
        } catch (Exception e){
            rentalPlaceLatitude = Double.parseDouble(hashMap.get(TAG_rentalPlaceLatitude));
            rentalPlaceLongitude = Double.parseDouble(hashMap.get(TAG_rentalPlaceLongitude));
            returnPlaceLatitude = Double.parseDouble(hashMap.get(TAG_returnPlaceLatitude));
            returnPlaceLongitude = Double.parseDouble(hashMap.get(TAG_returnPlaceLongitude));
        }

        int i=0;
        double aveDist=0;

        ArrayList<BarEntry> dataVals = new ArrayList<>();
        List<Entry> humidataValsLine = new ArrayList<>();
        HashMap<String,String> hashMap1 = new HashMap<>();
        for(i=1; i<mArrayList2.size(); i++){
            double dist, KmH;

            hashMap1 = mArrayList2.get(i);

            String[] stTime = mArrayList2.get(i-1).get(TAG_Time).split(" ")[1].split(":");
            long long_stTime = Long.parseLong(stTime[0])*3600 + Long.parseLong(stTime[1])*60 + Long.parseLong(stTime[2]);
            String[] fnTime = mArrayList2.get(i).get(TAG_Time).split(" ")[1].split(":");
            long long_fnTime = Long.parseLong(fnTime[0])*3600 + Long.parseLong(fnTime[1])*60 + Long.parseLong(fnTime[2]);

            // 두 좌표간 시간 간격
            long sec = long_fnTime - long_stTime;

            // 두 좌표간 이동거리
            if(hashMap1.get(TAG_DistDif).equals("NaN")){
                dist = 0.0;
            }
            else {
                dist = Double.parseDouble(hashMap1.get(TAG_DistDif));
            }

            // 두 좌표간 이동거리 및 이동속도
            aveDist = (dist + aveDist);
            KmH = (3600/sec) * dist;

            dataVals.add(new BarEntry(i, (float)KmH));
            humidataValsLine.add(new Entry(i,(float)KmH));
        }

        // 총 이동거리
        String get_dist = String.format("%.2f", aveDist);
        tvDist.setText(""+get_dist);

        // 평균 이동속도
        String get_KmH = String.format("%.2f", ( 3600.0 / useTime ) * aveDist);
        tvAveDist.setText(""+get_KmH);


        // linec chart 생성
        LineDataSet lineDataSet = new LineDataSet(humidataValsLine, "tempdataValsLine");
        lineDataSet.setLineWidth(1);
        lineDataSet.setCircleRadius(0);
        lineDataSet.setCircleColor(Color.parseColor("#DE9393"));
        lineDataSet.setCircleColor(Color.RED);
        lineDataSet.setColor(Color.parseColor("#DE9393"));
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setDrawHighlightIndicators(false);
        lineDataSet.setDrawValues(false);

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(lineDataSet);

        LineData data = new LineData(dataSets);
        mChart = (LineChart)findViewById(R.id.mChart);
        mChart.setData(data);
        mChart.getAxisRight().setDrawGridLines(false);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);

        YAxis tempyLAxis = mChart.getAxisLeft();
        tempyLAxis.setAxisMinValue(-10);
        tempyLAxis.setAxisMaxValue(50);


        // bar chart 생성
        BarDataSet bardataset = new BarDataSet(dataVals, "Cells");
        bardataset.setHighLightAlpha(10);
        bardataset.setColor(Color.RED);

        BarData data3 = new BarData(bardataset);
        barChart = (BarChart)findViewById(R.id.barchart1);
        barChart.setData(data3);
        barChart.getBarData().getBarWidth();
        barChart.getXAxis().setDrawGridLines(false);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync((OnMapReadyCallback) this);
    }



    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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
                        final SQLiteHelper dbHelper = new SQLiteHelper(getApplicationContext(), "MODObicycle.db", null, 1);
                        mArrayList = dbHelper.getAllContacts(a, b);
                        drawMarker(mArrayList);
                    }
                } else {
                    //api 호출이 10초 이상 경괴했을 때
                    Toast.makeText(getApplicationContext(), "호출에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                }

            }


        }, 100);

        // Add a marker in Sydney and move the camera
        LatLng retalPlace = new LatLng(rentalPlaceLatitude, rentalPlaceLongitude);
        LatLng returnPlace = new LatLng(returnPlaceLatitude, returnPlaceLongitude);

        MarkerOptions markerOptions1 = new MarkerOptions();
        markerOptions1.position(retalPlace);
        markerOptions1.title(bicycleNumber);
        markerOptions1.snippet("대여위치");
        mMap.addMarker(markerOptions1);

        MarkerOptions markerOptions2 = new MarkerOptions();
        markerOptions2.position(returnPlace);
        markerOptions2.title(bicycleNumber);
        markerOptions2.snippet("반납위치");
        mMap.addMarker(markerOptions2);

        double centerlat;
        double centerlong;
        if(rentalPlaceLatitude>=returnPlaceLatitude){
            centerlat = rentalPlaceLatitude - Math.abs(rentalPlaceLatitude - returnPlaceLatitude)/2;
        }
        else{
            centerlat = rentalPlaceLatitude + Math.abs(rentalPlaceLatitude - returnPlaceLatitude)/2;
        }

        if(rentalPlaceLongitude>=returnPlaceLongitude){
            centerlong = rentalPlaceLongitude - Math.abs(rentalPlaceLongitude - returnPlaceLongitude)/2;
        }
        else{
            centerlong = rentalPlaceLongitude + Math.abs(rentalPlaceLongitude - returnPlaceLongitude)/2;
        }

        LatLng centerPlace = new LatLng(centerlat, centerlong);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(centerPlace));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(centerPlace,15));
    }

    private void drawMarker(ArrayList<HashMap<String, String>> mArrayList1) {
        PolylineOptions line = new PolylineOptions()
                .color(Color.RED)
                .width(5);

        HashMap<String,String> hashMap1 = new HashMap<>();
        MarkerOptions markerOptions = new MarkerOptions();
        for(int i=0; i<mArrayList1.size(); i++){
            hashMap1 = mArrayList1.get(i);

            String Lats = String.format("%.6f", Double.parseDouble(hashMap1.get(TAG_Lati)));
            String Longs = String.format("%.6f", Double.parseDouble(hashMap1.get(TAG_Longti)));

            double Lat1, Long1;
            Lat1 = Double.parseDouble(Lats);
            Long1 = Double.parseDouble(Longs);

            line.add(new LatLng(Lat1, Long1));
        }
        mMap.addPolyline(line);
        return;
    }

}
