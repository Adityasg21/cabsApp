package com.example.cabsapp;

import android.app.Application;

import com.parse.Parse;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("tqGrtxNQrrIBUDV1Hf2UZw1zKziV4wHsU86mYthY")
                .clientKey("OBkYWee4gQON7UdOnIVs5zzyOWlYYbyIudwqtJpw")
                .server("https://parseapi.back4app.com/")
                .build());
    }
}
