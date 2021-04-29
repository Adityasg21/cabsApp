package com.example.cabsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.nio.file.attribute.BasicFileAttributeView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnSignUPLogin,btnOneTimeLogin;
    private RadioButton driverRadioButton,passengerRadioButton ;
    private EditText edtUsername,edtPassword,edtDriverOrPassenger;

    @Override
    public void onClick(View v) {

        if (edtDriverOrPassenger.getText().toString().equals("Driver") || edtDriverOrPassenger.getText().toString().equals("Passenger")) {

            if(ParseUser.getCurrentUser()==null){
                ParseAnonymousUtils.logIn(new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {

                        if(user!=null && e==null){
                            Toast.makeText(MainActivity.this,"we have an anonymous user",Toast.LENGTH_LONG).show();

                            user.put("as",edtDriverOrPassenger.getText().toString());
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    transitionToPassengerActivity();
                                    transitionToDriverActivity();
                                }
                            });
                        }
                    }
                });
            }

        }else{
            Toast.makeText(MainActivity.this, "Are u Driver or Passenger", Toast.LENGTH_LONG).show();
        }

    }

    enum State{
        SIGNUP,LOGIN
    }
    private State state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseInstallation.getCurrentInstallation().saveInBackground();

        if(ParseUser.getCurrentUser()!=null){
            transitionToPassengerActivity();
            transitionToDriverActivity();
        }
        state=State.SIGNUP;
        btnSignUPLogin=findViewById(R.id.btnSignUp);
        btnOneTimeLogin=findViewById(R.id.btnOneTimeLogin);
        edtUsername=findViewById(R.id.edtUsername);
        edtPassword=findViewById(R.id.edtPassword);
        edtDriverOrPassenger=findViewById(R.id.edtDorP);
        driverRadioButton=findViewById(R.id.rdbDriver);
        passengerRadioButton=findViewById(R.id.rdbPassenger);

        btnOneTimeLogin.setOnClickListener(this);

        btnSignUPLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state==State.SIGNUP){

                    if(driverRadioButton.isChecked()==false&&passengerRadioButton.isChecked()==false){
                        Toast.makeText(MainActivity.this,"Are u Driver or Passenger",Toast.LENGTH_LONG).show();
                        return;
                    }

                    ParseUser appUser=new ParseUser();
                    appUser.setUsername(edtUsername.getText().toString());
                    appUser.setPassword(edtPassword.getText().toString());
                    if(driverRadioButton.isChecked())
                        appUser.put("as","Driver");
                    else if(passengerRadioButton.isChecked()){
                        appUser.put("as","Passenger");
                    }
                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e==null){
                                Toast.makeText(MainActivity.this, "Signed up", Toast.LENGTH_LONG).show();
                                transitionToPassengerActivity();
                                transitionToDriverActivity();
                            }
                        }
                    });

                }else if(state==State.LOGIN){
                    ParseUser.logInInBackground(edtUsername.getText().toString()
                            , edtPassword.getText().toString(), new LogInCallback() {
                                @Override
                                public void done(ParseUser user, ParseException e) {
                                    if(user!=null && e==null){
                                        Toast.makeText(MainActivity.this, "user logged in", Toast.LENGTH_SHORT).show();
                                        transitionToPassengerActivity();
                                        transitionToDriverActivity();
                                    }
                                }
                            });



                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signup_activity,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.loginItem:
                if(state==State.SIGNUP){
                    state=State.LOGIN;
                    item.setTitle("Sign UP");
                    btnSignUPLogin.setText("Log in");
                }else if(state==State.LOGIN){
                    state=State.SIGNUP;
                    item.setTitle("log IN");
                    btnSignUPLogin.setText("Sign UP");

            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void transitionToPassengerActivity(){
        if(ParseUser.getCurrentUser()!=null){
            if(ParseUser.getCurrentUser().get("as").equals("Passenger")){

                Intent intent=new Intent(MainActivity.this,PassengersActivity.class);
                startActivity(intent);

            }
        }
    }


    private void transitionToDriverActivity(){

        if(ParseUser.getCurrentUser()!=null){
            if(ParseUser.getCurrentUser().get("as").equals("Driver")){
                Intent intent=new Intent(MainActivity.this,DriverRequestActivity.class);
                startActivity(intent);
            }
        }

    }
}