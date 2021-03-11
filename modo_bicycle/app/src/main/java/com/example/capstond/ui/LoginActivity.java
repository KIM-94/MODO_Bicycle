package com.example.capstond.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.capstond.PostParams.signin_param;
import com.example.capstond.R;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText idText;
    private EditText passwordText;
    private Button loginButton;
    private TextView Sing_up;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        idText = (EditText)findViewById(R.id.idText);
        passwordText = (EditText)findViewById(R.id.passwordText);

        loginButton = (Button)findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginAPI();
            }
        });

        Sing_up = (TextView) findViewById(R.id.textView_signup);
        Sing_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginAPI() {
        final String user_id = idText.getText().toString();
        final String user_pw = passwordText.getText().toString();

        Response.Listener<String> resposneListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    if (success != null && success.equals("1")) {  // 회원가입 완료
                        Toast.makeText(getApplicationContext(),"로그인 성공!",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("userID", user_id);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"로그인 실패!",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(),"로그인 처리시 에러발생!", Toast.LENGTH_SHORT).show();
                return;
            }
        };

        // Volley 로 회원양식 웹으로 전송
        signin_param registerRequest = new signin_param(user_id,user_pw,resposneListener,errorListener);
        registerRequest.setShouldCache(false);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(registerRequest);
    }
}
