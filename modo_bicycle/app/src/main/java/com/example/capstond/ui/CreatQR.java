package com.example.capstond.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capstond.R;
import com.example.capstond.service.GpsTracker;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CreatQR extends AppCompatActivity {

    String imgName = "QR_";    // 이미지 이름

    private static String IP_ADDRESS = "193.122.111.182/capstonD2";
//    private static String IP_ADDRESS = "ksw2102.iptime.org";
    private static String TAG = "phptest";

    private TextView bicycleIDTextView;
    private TextView bicycleNumberTextView;
    private TextView macAddressTextView;
    private TextView bicycleStateTextView;
    private TextView bicycleUsageTextView;
    private TextView textView6;
    private TextView latitudeTextview, longtitudeTextview;

    public int setCheck;
    private int checkSet;
    private int macCheck;

    private String MACADDRESS, userID;

    //QR_code
    private ImageView iv;
    private String text;
    private String QR_number;

    // GPS
    private GpsTracker gpsTracker;

    private double finish_latitude;
    private double finish_longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creat_qr);

        Intent intent = getIntent(); /*데이터 수신*/
        userID = intent.getExtras().getString("userID");
        MACADDRESS = intent.getExtras().getString("MAC_ADDRESS");
        MACADDRESS = MACADDRESS.toUpperCase();


        bicycleIDTextView = (TextView)findViewById(R.id.textView);
        bicycleNumberTextView = (TextView)findViewById(R.id.textView2);
        macAddressTextView = (TextView)findViewById(R.id.textView3);
        bicycleStateTextView = (TextView)findViewById(R.id.textView4);
        bicycleUsageTextView = (TextView)findViewById(R.id.textView5);
        textView6 = (TextView)findViewById(R.id.textView6);
        latitudeTextview = (TextView)findViewById(R.id.textView7);
        longtitudeTextview = (TextView)findViewById(R.id.textView8);;
        iv = (ImageView)findViewById(R.id.qrcode);

        bicycleStateTextView.setVisibility(View.GONE);
        bicycleUsageTextView.setVisibility(View.GONE);
        textView6.setVisibility(View.GONE);

        setCheck = 3;
        InsertData task2 = new InsertData();
        task2.execute("http://" + IP_ADDRESS + "/checkMAC.php", MACADDRESS);
    }

    public void saveBitmapToJpeg(Bitmap bitmap, String QR_number) {   // 선택한 이미지 내부 저장소에 저장
        File tempFile = new File(getCacheDir(), imgName+QR_number+".png");    // 파일 경로와 이름 넣기
        try {
            tempFile.createNewFile();   // 자동으로 빈 파일을 생성하기
            FileOutputStream out = new FileOutputStream(tempFile);  // 파일을 쓸 수 있는 스트림을 준비하기
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);   // compress 함수를 사용해 스트림에 비트맵을 저장하기
            out.close();    // 스트림 닫아주기
            Toast.makeText(getApplicationContext(), "파일 저장 성공", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "파일 저장 실패", Toast.LENGTH_SHORT).show();
        }
    }

    class InsertData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(CreatQR.this,
                    "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            textView6.setText(result);

            if(result.equals(" 사용가능한 MAC주소입니다.")){
                textView6.setText("TRUR");
                Log.d(TAG, "POST response  - " + "TRUE");
                GetData task1 = new GetData();
                task1.execute("http://" + IP_ADDRESS + "/getjsonBicycle.php", "");
            }
            else if(result.equals("새로운 자전거를 추가했습니다.")){
                textView6.setText("자전거추가성공");
                Log.d(TAG, "POST response  - " + "자전거추가성공");

                iv = (ImageView)findViewById(R.id.qrcode);
                text = QR_number;

                // 생성된 QR코드 비트맵 이미지를 로컬 저장소에 저장
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                try{
                    BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE,200,200);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    iv.setImageBitmap(bitmap);
                    saveBitmapToJpeg(bitmap, QR_number);    // 내부 저장소에 저장
                }catch (Exception e){}
            }
            else{
                Log.d(TAG, "POST response  - " + "FALSE");
            }
            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {
            String serverURL = (String) params[0];
            String postParameters = null;

            if(setCheck==1) {
                setCheck = 0;

                String userID = (String) params[1];
                String userPassword = (String) params[2];
                String userName = (String) params[3];
                String userBirthday = (String) params[4];
                String userEmail = (String) params[5];
                postParameters = "userID=" + userID + "&userPassword=" + userPassword + "&userName=" + userName + "&userBirthday=" + userBirthday + "&userEmail=" + userEmail;
            }

            else if(setCheck==2) {
                setCheck = 0;
                String userID = (String) params[1];
                postParameters = "userID=" + userID;
            }

            else if(setCheck==3) {
                setCheck = 0;
                String macaddress = (String) params[1];
                postParameters = "macAddress=" + macaddress;
            }

            if(setCheck==4) {
                setCheck = 0;

                String bicycleID = (String) params[1];
                String bicycleNumber = (String) params[2];
                String macAddress = (String) params[3];
                String bicycleState = (String) params[4];
                String bicycleUsage = (String) params[5];
                String latitude1 = (String) params[6];
                String longtitude1 = (String) params[7];
                QR_number = bicycleNumber;
                postParameters = "bicycleID=" + bicycleID + "&bicycleNumber=" + bicycleNumber + "&macAddress=" + macAddress + "&bicycleState=" + bicycleState + "&bicycleUsage=" + bicycleUsage + "&rentalPlaceLatitude=" + latitude1 + "&rentalPlaceLongtitude=" + longtitude1 + "&userID=" + userID;
            }

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


    private class GetData extends AsyncTask<String, Void, String>{

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(CreatQR.this,
                    "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            checkSet = 1;

            int num = Integer.parseInt(result)+1;

            String text1 = "bicycle:";
            String text2 = String.valueOf(num);

            String bicycleID = text2;
            String bicycleNumber = text1.concat(text2);
            String macAddress = MACADDRESS;
            String bicycleState = "available";
            String bicycleUsage = "00";


            //mTextViewResult.setText(bicycleNumber);
            Log.d(TAG, "response - " + bicycleNumber);

            if (result == null){

                //mTextViewResult.setText(errorString);
            }
            else {
                //mJsonString = result;
                //mTextViewResult.setText(bicycleNumber);

                // gps
                gpsTracker = new GpsTracker(CreatQR.this);


                finish_latitude = gpsTracker.getLatitude();
                finish_longitude = gpsTracker.getLongitude();

                String latitude = Double.toString(finish_latitude);
                String longtitude = Double.toString(finish_longitude);

                bicycleIDTextView.setText("bicycleID : "+bicycleID);
                bicycleNumberTextView.setText("bicycleNumber : "+bicycleNumber);
                macAddressTextView.setText("macAddress : "+macAddress);
                bicycleStateTextView.setText("bicycleState : "+bicycleState);
                bicycleUsageTextView.setText("bicycleUsage : "+bicycleUsage);
                latitudeTextview.setText("latitude : " + latitude);
                longtitudeTextview.setText("latitude : " + longtitude);

                setCheck = 4;
                InsertData task2 = new InsertData();
                task2.execute("http://" + IP_ADDRESS + "/inserbicycle.php", bicycleID, bicycleNumber, macAddress, bicycleState, bicycleUsage, latitude, longtitude, userID);

                //showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String postParameters = params[1];


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

                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }
}
