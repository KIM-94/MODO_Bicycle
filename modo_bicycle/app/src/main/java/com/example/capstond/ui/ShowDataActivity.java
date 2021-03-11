package com.example.capstond.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capstond.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ShowDataActivity extends AppCompatActivity {
    private static String IP_ADDRESS = "193.122.111.182/capstonD2";
    private static String TAG = "phpquerytest";


    private static final String TAG_JSON="webnautes";
    private static final String TAG_userID = "userID";
    private static final String TAG_bicycleNumber = "bicycleNumber";
    private static final String TAG_rentalTime ="rentalTime";
    private static final String TAG_returnTime ="returnTime";
    private static final String TAG_rentalPlaceLatitude ="rentalPlaceLatitude";
    private static final String TAG_rentalPlaceLongitude ="rentalPlaceLongitude";
    private static final String TAG_returnPlaceLatitude ="returnPlaceLatitude";
    private static final String TAG_returnPlaceLongitude ="returnPlaceLongitude";
    private static final String TAG_distanceKM ="distanceKM";
    private static final String TAG_useTime ="useTime";

    private String userID;

    ArrayList<HashMap<String, String>> mArrayList;
    ListView mListViewList;
    String mJsonString;

    private TextView listSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data);

        Intent intent1 = getIntent(); /*데이터 수신*/
        userID = intent1.getExtras().getString("userID");


        mListViewList = (ListView) findViewById(R.id.listView_main_list);
        mArrayList = new ArrayList<>();

        listSize = (TextView)findViewById(R.id.textItemsize);

        GetData task = new GetData();
        task.execute("http://" + IP_ADDRESS + "/getjsonHistory.php", userID);
    }

    // userID에 해당하는 사용자의 자전거 이용기록 조회
    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ShowDataActivity.this,
                    "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            //mTextViewResult.setText(result);
            Log.d(TAG, "response - " + result);

            if (result == null){

            }
            else {
                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String postParameters = "userID=" + params[1];

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

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
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();

            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }
        }
    }

    // 수신받은 userID의 자전거 이용기록 listview에 디스플레이
    private void showResult(){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=jsonArray.length()-1;0<=i;i--){

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

                String date = rentaltime.split(" ")[0];
                String timeA = rentaltime.split(" ")[1].split(":")[0]+":"+rentaltime.split(" ")[1].split(":")[1];
                String timeB = returntime.split(" ")[1].split(":")[0]+":"+returntime.split(" ")[1].split(":")[1];
                String time = "총 "+usetime+" | "+timeA+" - "+timeB;
                String pointA = rentalPlaceLatitude+" "+rentalPlaceLongitude;
                String pointB = returnPlaceLatitude+" "+returnPlaceLongitude;

                Log.d(TAG, "date "+date);
                Log.d(TAG, "time "+time);
                Log.d(TAG, "pointA "+pointA);
                Log.d(TAG, "pointB "+pointB);

                // 킬로미터(Kilo Meter) 단위
                double distanceKiloMeter = distance(Double.parseDouble(rentalPlaceLatitude), Double.parseDouble(rentalPlaceLongitude), Double.parseDouble(returnPlaceLatitude), Double.parseDouble(returnPlaceLongitude), "kilometer");
                distanceKiloMeter = Math.round(distanceKiloMeter*100)/100.0;
                String distanceKM = Double.toString(distanceKiloMeter)+"Km";

                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put(TAG_userID, userid);
                hashMap.put(TAG_bicycleNumber, bicyclenumber);
                hashMap.put(TAG_rentalTime, rentaltime);
                hashMap.put(TAG_returnTime, returntime);
                hashMap.put(TAG_rentalPlaceLatitude, rentalPlaceLatitude);
                hashMap.put(TAG_rentalPlaceLongitude, rentalPlaceLongitude);
                hashMap.put(TAG_returnPlaceLatitude, returnPlaceLatitude);
                hashMap.put(TAG_returnPlaceLongitude, returnPlaceLongitude);
                hashMap.put(TAG_distanceKM, distanceKM);
                hashMap.put(TAG_useTime, usetime);

                hashMap.put("date", date);
                hashMap.put("pointA", pointA);
                hashMap.put("pointB", pointB);
                hashMap.put("time", time);

                mArrayList.add(hashMap);
            }

            String TAG_listSize = Integer.toString(jsonArray.length());
            listSize.setText(TAG_listSize + "건");

            ListAdapter adapter = new SimpleAdapter(
                    ShowDataActivity.this, mArrayList, R.layout.item_list3,
                    new String[]{"date", "pointA", "pointB", "time", TAG_distanceKM},
                    new int[]{R.id.textViewDate, R.id.textViewA, R.id.textViewB, R.id.textViewTime, R.id.textViewDist}
            );

            mListViewList.setAdapter(adapter);
            mListViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    HashMap<String, String> map  = mArrayList.get(position);
                    Intent intent = new Intent(ShowDataActivity.this, ShowDetailDataActivity.class);
                    intent.putExtra("map", map);
                    startActivity(intent);
                }
            });

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }

    }

    // 좌표값을 입력해 지번주소 생성
    public String getCurrentAddress(String lati, String longti) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(Double.parseDouble(lati), Double.parseDouble(longti), 1);
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

    // haversine 공식을 이용해 두 좌표간 거리를 계산
    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (unit == "kilometer") {
            dist = dist * 1.609344;
        } else if(unit == "meter"){
            dist = dist * 1609.344;
        }
        return (dist);
    }

    // This function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}
