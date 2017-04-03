package com.mobilesecurity.scullyallisonle.locationservicesapp;

import android.*;
import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.places.Places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class LSApp_Main extends AppCompatActivity implements OnConnectionFailedListener, View.OnClickListener {

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lsapp__main);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        //get my permissions
        getPermissions();

        Button actionButton = (Button) findViewById(R.id.lookupButton);
        actionButton.setOnClickListener(this);

    }

    protected void getPermissions(){
        int permissionCheck = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);

        if(permissionCheck != 0) {
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 1);
        }

        permissionCheck = ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION);

        if(permissionCheck != 0) {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_COARSE_LOCATION}, 1);
        }

        permissionCheck = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION);

        if(permissionCheck != 0) {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
        }


    }

     protected String getLocationData(double lon, double lat, int rad, String type) throws IOException {
        //get json from google api
        //decalre some vars
        HttpURLConnection conn = null;
        URL url = null;
        lon = -33.8670522;
        lat = 151.1957362;
        String key = "AIzaSyC7TOLAiUPdoNcSHyy7ZOjh2v4f4YMXROU";



        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            url = new URL("https://maps.googleapis.com/maps/api/place/search/json?location="+lon+","+lat+"&radius="+rad+"&types="+type+"&key="+key);

            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();

            if(responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    builder.append(inputLine).append("\n");
                }

                reader.close();

                return builder.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


         return "Didn't connect";
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }


    @Override
    public void onClick(View view) {
        String raw;
        JSONObject json;
        JSONArray resultsArr;
        String results;
        JSONObject resultObj;

        EditText inputs;
        String type;
        int rad;

        if(view.getId() == R.id.lookupButton){

            //get phone gps data


            //retrieve text data
            inputs = (EditText) findViewById(R.id.typeText);
            type = inputs.getText().toString();

            inputs = (EditText) findViewById(R.id.radText);
            rad = Integer.parseInt(inputs.getText().toString());


            try {

                raw = getLocationData(0.0,0.0,rad,type);

                try {

                    json = new JSONObject(raw);
                    resultsArr = json.getJSONArray("results");

                    StringBuilder builder = new StringBuilder();

                    //handle results
                    for(int result = 0; result < resultsArr.length(); result++){
                        resultObj =  resultsArr.getJSONObject(result);
                        builder.append(resultObj.get("name")).append(", ")
                                .append(resultObj.getJSONObject("geometry").getJSONObject("location").get("lat") ).append(", ")
                                .append(resultObj.getJSONObject("geometry").getJSONObject("location").get("lng") ).append(", ")
                                .append(result+1).append('\n');
                    }

                    //print results to txt
                    File f = new File(Environment.getExternalStorageDirectory(), "ls_services_log.txt");
                    if(!f.exists()){
                        f.createNewFile();
                    }

                    FileOutputStream writer = new FileOutputStream(f);

                    writer.write(builder.toString().getBytes());

                    writer.close();



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
