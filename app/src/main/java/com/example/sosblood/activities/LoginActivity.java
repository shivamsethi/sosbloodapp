package com.example.sosblood.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.sosblood.R;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout phone_input_lay,password_input_lay;
    private TextInputEditText phone_edit,password_edit;
    private VideoView video_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setSupportActionBar((Toolbar)findViewById(R.id.login_toolbar_id));
        getSupportActionBar().setTitle("SOS Blood");

        phone_input_lay=(TextInputLayout)findViewById(R.id.phone_input_lay_id);
        password_input_lay=(TextInputLayout)findViewById(R.id.password_input_lay_id);
        phone_edit=(TextInputEditText)findViewById(R.id.phone_edittext_id);
        password_edit=(TextInputEditText)findViewById(R.id.password_edittext_id);


        phone_edit.setText("user");
        password_edit.setText("PasswordText");
    }


    public void actButtonLogin(View view) {
        String phone,password;
        phone=phone_edit.getText().toString();
        password=password_edit.getText().toString();
        if(phone.isEmpty())
            setErrorOnInputLayout(phone_input_lay,"Can't be empty");
        else
        {
            removeErrorFromInputLayout(phone_input_lay);
            if(password.isEmpty())
                setErrorOnInputLayout(password_input_lay,"Can't be empty");
            else
            {
                removeErrorFromInputLayout(password_input_lay);
                if(password.length()<8)
                    setErrorOnInputLayout(password_input_lay,"At least 8 characters required");
                else
                {
                    removeErrorFromInputLayout(password_input_lay);
                    attemptLogin(phone,password);
                }
            }
        }
    }


    private void setErrorOnInputLayout(TextInputLayout layout,String error)
    {
        layout.setErrorEnabled(true);
        layout.setError(error);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(layout.getEditText(), InputMethodManager.SHOW_IMPLICIT);

    }


    private void removeErrorFromInputLayout(TextInputLayout layout)
    {
        layout.setErrorEnabled(false);
    }


    private void attemptLogin(String phone, String password) {
        startActivity(new Intent(this,MainActivity.class));
    }

    public void actTextViewNewUser(View view) {
        startActivity(new Intent(this,SignUpActivity.class));
    }

    public void actTextViewForgetPassword(View view) {
        Toast.makeText(this, "Forgot password", Toast.LENGTH_SHORT).show();
    }
}