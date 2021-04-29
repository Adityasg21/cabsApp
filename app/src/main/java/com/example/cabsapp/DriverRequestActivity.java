package com.example.cabsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class DriverRequestActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private LocationManager locationManager;
    private Button btnGetRequest;
    private LocationListener locationListener;
    private ListView listView;
    private ArrayList<String> nearByDriverRequest;
    private ArrayAdapter adapter;
    private ArrayList<Double> passengersLatitude;
    private ArrayList<Double> passengerLongitude;
    private ArrayList<String> requestCarUsernames;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request);
        btnGetRequest = findViewById(R.id.btnUpadteList);
        btnGetRequest.setOnClickListener(this);
        listView = findViewById(R.id.ReqListView);

        nearByDriverRequest = new ArrayList<>();
        passengerLongitude=new ArrayList<>();
        passengersLatitude=new ArrayList<>();
        requestCarUsernames=new ArrayList<>();

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nearByDriverRequest);
        listView.setAdapter(adapter);
        nearByDriverRequest.clear();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT<23 || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
           locationListener=new LocationListener() {
               @Override
               public void onLocationChanged(@NonNull Location location) {

                   locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
               }
           };
        }
        listView.setOnItemClickListener(DriverRequestActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.driverLogoutItem) {
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        finish();
                    }
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {




        if (Build.VERSION.SDK_INT < 23) {

            Location currentDriverLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateRequestListView(currentDriverLocation );
        }else if(Build.VERSION.SDK_INT>=23){

            if(ContextCompat.checkSelfPermission(DriverRequestActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(DriverRequestActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1000);
            }else{
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location currentDriverLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView(currentDriverLocation );
            }
        }

    }

    private void updateRequestListView(Location location) {

        if(location!=null){
            saveDriverLocationToParse(location);

            final ParseGeoPoint driverCurrentLocation= new ParseGeoPoint(location.getLatitude(),location.getLongitude());
            final ParseQuery<ParseObject> requestCarQuery=ParseQuery.getQuery("RequestCar");
            requestCarQuery.whereNear("passengerLocation",driverCurrentLocation);
            requestCarQuery.whereDoesNotExist("driverOfME");
            requestCarQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if( e==null){
                        if(objects.size()>0 ){

                            if(nearByDriverRequest.size()>0){
                                nearByDriverRequest.clear();
                            }
                            if(passengerLongitude.size()>0)
                                passengerLongitude.clear();
                            if (passengersLatitude.size()>0)
                                passengerLongitude.clear();
                            if(requestCarUsernames.size()>0)
                                requestCarUsernames.clear();


                        for(ParseObject nearRequest:objects){
                            ParseGeoPoint pLocation= (ParseGeoPoint) nearRequest.get("passengerLocation");
                            Double milesDistanceToPassenger = driverCurrentLocation.distanceInMilesTo(pLocation);
                             float roundedDistanceValue=Math.round(milesDistanceToPassenger*10)/10;
                             nearByDriverRequest.add("there are "+roundedDistanceValue+" miles to " + nearRequest.get("username"));
                             passengersLatitude.add(pLocation.getLatitude());
                             passengerLongitude.add(pLocation.getLongitude());
                            requestCarUsernames.add(nearRequest.get("username")+"");

                        }
                    }else{
                            Toast.makeText(DriverRequestActivity.this,"no request",Toast.LENGTH_LONG).show();
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });

        }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1000 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){

            if(ContextCompat.checkSelfPermission(DriverRequestActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                initializeLocationListener();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
               Location currentDriverLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView( currentDriverLocation);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            Location cdLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(cdLocation!=null){
            Intent intent=new Intent(this,ViewLocationMapActivity.class);
            intent.putExtra("dLatitude",cdLocation.getLatitude());
            intent.putExtra("dLongitude",cdLocation.getLongitude());
            intent.putExtra("pLatitude",passengersLatitude.get(position));
            intent.putExtra("pLongitude",passengerLongitude.get(position));
            intent.putExtra("rUsername",requestCarUsernames.get(position));
            startActivity(intent);}
        }

    }
    private void initializeLocationListener(){
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            }
        };
    }
    private void saveDriverLocationToParse(Location location) {
        ParseUser driver = ParseUser.getCurrentUser();
        ParseGeoPoint driverLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        driver.put("driverLocation", driverLocation);
        driver.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null){
                    Toast.makeText(DriverRequestActivity.this,"Location saved",Toast.LENGTH_LONG).show();
                }

            }
        });

    }
}