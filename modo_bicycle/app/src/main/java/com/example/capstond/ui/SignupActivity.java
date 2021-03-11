package com.example.capstond.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.capstond.PostParams.checkID_param;
import com.example.capstond.PostParams.signup_param;
import com.example.capstond.R;

import org.json.JSONException;
import org.json.JSONObject;

public class SignupActivity extends AppCompatActivity {
    private static String IP_ADDRESS = "193.122.111.182/capstonD2";

    private EditText idText;
    private EditText passwordText;
    private EditText passwordText2;
    private EditText nameText;
    private EditText birthdayText;
    private EditText emailText;
    private ImageView idCheckBtn;
    private Button buttonInsert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        idText = (EditText)findViewById(R.id.Sin_id);
        passwordText = (EditText)findViewById(R.id.Sin_password);
        passwordText2 = (EditText)findViewById(R.id.Sin_password2);
        nameText = (EditText)findViewById(R.id.Sin_name);
        birthdayText = (EditText)findViewById(R.id.Sin_birthday);
        emailText = (EditText)findViewById(R.id.Sin_email);

        buttonInsert = (Button)findViewById(R.id.sigBtn);
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sign_upAPI();
            }
        });

        idCheckBtn = (ImageView)findViewById(R.id.idCheckBtn);
        idCheckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check_idAPI();
            }
        });
    }

    private void sign_upAPI() {
        String userID = idText.getText().toString();
        String userPassword = passwordText.getText().toString();
        String userName = nameText.getText().toString();
        String userBirthday = birthdayText.getText().toString();
        String userEmail = emailText.getText().toString();

        Response.Listener<String> resposneListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d( "", "onMapClick :"+response);

                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    if (success != null && success.equals("1")) {  // 회원가입 완료
                        Toast.makeText(getApplicationContext(),"회원가입 성공!",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
//                        intent.putExtra("userID", userID);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"회원가입 실패!",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(),"회원가입 처리시 에러발생!", Toast.LENGTH_SHORT).show();
                return;
            }
        };

        // Volley 로 회원양식 웹으로 전송
        signup_param signupRequest = new signup_param(userID,userPassword,userName,userBirthday,userEmail,resposneListener,errorListener);
        signupRequest.setShouldCache(false);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(signupRequest);
    }

    private void check_idAPI() {
        String userID = idText.getText().toString();

        Response.Listener<String> resposneListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    if (success != null && success.equals("1")) {  // 회원가입 완료
                        Toast.makeText(getApplicationContext(),"사용가능한 아이디 입니다.",Toast.LENGTH_SHORT).show();
                        idCheckBtn.setImageResource(R.drawable.signup_check_on);
                        buttonInsert.setEnabled(true);
                        return;
                    }
                    else if (success != null && success.equals("-1")) {
                        Toast.makeText(getApplicationContext(),"이미 사용중 입니다.",Toast.LENGTH_SHORT).show();
                        idCheckBtn.setImageResource(R.drawable.signup_ic_check);
                        buttonInsert.setEnabled(false);
                        return;
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"아이디를 입력하세요.",Toast.LENGTH_SHORT).show();
                        idCheckBtn.setImageResource(R.drawable.signup_ic_check);
                        buttonInsert.setEnabled(false);
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
                Toast.makeText(getApplicationContext(),"중복확인 처리시 에러발생!", Toast.LENGTH_SHORT).show();
                return;
            }
        };

        // Volley 로 회원양식 웹으로 전송
        checkID_param checkIDRequest = new checkID_param(userID,resposneListener,errorListener);
        checkIDRequest.setShouldCache(false);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(checkIDRequest);
    }
}
