package com.locationtrigger;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;


public class GpsReadingService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, android.location.LocationListener, MqttCallback {


    private static int REQUEST_CODE_RECOVER_PLAY_SERVICES = 200;
    GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private String mLastUpdateTime;
    MqttClient client;
    public GpsReadingService() {


    }
    boolean isManualTrigger = true;
    /*
     Called befor service  onStart method is called.All Initialization part goes here
    */
    @Override
    public void onCreate() {

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


//            if (!mGoogleApiClient.isConnecting() &&
//                    !mGoogleApiClient.isConnected()) {
//
//            }

        buildGoogleApiClient();
        mGoogleApiClient.connect();

        //prepare connection request
        createLocationRequest();
        createLocationCallback();

        if (ConnectivityReceiver.isConnected()){
            connetToMosquittoServer();
            subscribeToMosquittoBrocker();
        }else {
            Toast.makeText(getApplicationContext(), "Check internet connectivity", Toast.LENGTH_SHORT).show();
        }



        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private boolean checkGooglePlayServices() {

        int checkGooglePlayServices = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (checkGooglePlayServices != ConnectionResult.SUCCESS) {

            return false;
        }

        return true;

    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }


    @Override
    public void onConnected(Bundle bundle) {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            isManualTrigger = false;
            if (ConnectivityReceiver.isConnected()){
                if (client.isConnected()) {

                    prepareMqttMessage(isManualTrigger,mLastLocation.getLatitude(), mLastLocation.getLongitude(),mLastUpdateTime);


                } else {
                    connetToMosquittoServer();


                    prepareMqttMessage(isManualTrigger,mLastLocation.getLatitude(), mLastLocation.getLongitude(),mLastUpdateTime);
                }

                isManualTrigger = true;
            }else {
                Toast.makeText(getApplicationContext(), "Check internet connectivity", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Latitude:" + mLastLocation.getLatitude() + ", Longitude:" + mLastLocation.getLongitude(), Toast.LENGTH_LONG).show();


            }




        }

        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    /* Second part*/


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }


    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationCallback, Looper.myLooper());


    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


    }

    @Override
    public void onLocationChanged(Location location) {
       // isManualTrigger = false;
        mLastLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());


//        if (ConnectivityReceiver.isConnected()){
//            if (client.isConnected()) {
//
//
//                prepareMqttMessage(isManualTrigger,mLastLocation.getLatitude(), mLastLocation.getLongitude(),mLastUpdateTime);
//
//
//            } else {
//                connetToMosquittoServer();
//
//
//                prepareMqttMessage(isManualTrigger,mLastLocation.getLatitude(), mLastLocation.getLongitude(),mLastUpdateTime);
//            }
//
//            isManualTrigger = true;
//        }else {
//            Toast.makeText(getApplicationContext(), "Check internet connectivity", Toast.LENGTH_SHORT).show();
//            AppLogger.e("Update : " + " Lat : " + mCurrentLocation.getLatitude() + "  Lon :" + mCurrentLocation.getLongitude() + " Time : " + mLastUpdateTime);
//
//
//        }




    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    @Override
    public void onProviderEnabled(String provider) {

    }


    @Override
    public void onProviderDisabled(String provider) {

    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationCallback);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //   stopLocationUpdates();

//        if (mGoogleApiClient != null) {
//            mGoogleApiClient.disconnect();
//        }


//        try {
//            if (client != null) {
//                client.disconnect();
//                client.close();
//            }
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }

    }


    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                isManualTrigger = false;
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());


                if (ConnectivityReceiver.isConnected()){


                    if (client.isConnected()) {


                        prepareMqttMessage(isManualTrigger,mLastLocation.getLatitude(), mLastLocation.getLongitude(),mLastUpdateTime);



                    } else {
                        connetToMosquittoServer();

                        prepareMqttMessage(isManualTrigger,mLastLocation.getLatitude(), mLastLocation.getLongitude(),mLastUpdateTime);

                    }
                    isManualTrigger = true;
                }else {
                   // Toast.makeText(getApplicationContext(), "Check internet connectivity", Toast.LENGTH_SHORT).show();
                    AppLogger.e("Check internet connectivity");
                    AppLogger.e("Update : " + " Lat : " + mCurrentLocation.getLatitude() + "  Lon :" + mCurrentLocation.getLongitude() + " Time : " + mLastUpdateTime);
                }


            }

        };


    }

    private void publishMessage(String s) {

        try {
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(s.getBytes());
            client.publish("topic/location", mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    private void connetToMosquittoServer() {


        if (!ConnectivityReceiver.isConnected()){
            Toast.makeText(this, "Check internet connectivity", Toast.LENGTH_SHORT).show();
            return;

        }

        AppLogger.e("Connecting to Mosquitto");


        try {
            client = new MqttClient("tcp://m14.cloudmqtt.com:17446", MqttClient.generateClientId(), new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("ggzxvdno");
            options.setPassword("xxkf8DWwM0Qf".toCharArray());
            client.setCallback(this);
            client.connect(options);

            AppLogger.e("Connected Successfully");

        } catch (MqttException e) {
            e.printStackTrace();
            AppLogger.e("MqttException connection getMessage: " + e.getMessage());
            AppLogger.e("MqttException connection getReasonCode: " + e.getReasonCode());
            AppLogger.e("MqttException connection getReasonCode: " + e.getCause());
            AppLogger.e("MqttException connection getStackTrace: " + e.getStackTrace());
        }

        if (client.isConnected()) {
            AppLogger.e("Connected Successfully");
        } else {
            AppLogger.e("Connection failed");
        }

    }

    private void subscribeToMosquittoBrocker() {
        String topic = "topic/location";
        try {
            client.subscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void connectionLost(Throwable cause) {
        AppLogger.e("connectionLost....");
    }


    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        final String payload = new String(message.getPayload());

        JSONObject jsonObject = new JSONObject(payload);

        String type =  jsonObject.optString("TYPE");

        if(type.equalsIgnoreCase("continuous_trigger")){
            AppLogger.e("messageArrived..." + payload);


        }else if (type.equalsIgnoreCase("manual_trigger")){
            AppLogger.e("messageArrived..." + payload);

            if(isManualTrigger){
                prepareMqttMessage(isManualTrigger,mLastLocation.getLatitude(), mLastLocation.getLongitude(),mLastUpdateTime);
            }




        }





    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
            AppLogger.e("deliveryComplete..." + token.isComplete());
    }



    public void prepareMqttMessage(boolean isManuallyTrigger, double latitude, double longitude, String mLastUpdateTime){

        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("TYPE", isManuallyTrigger?"manual_trigger":"continuous_trigger");
            jsonObject.put("LATITUDE",latitude);
            jsonObject.put("LONGITUDE",longitude);
            jsonObject.put("TIME", mLastUpdateTime);

            publishMessage(jsonObject.toString());


        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(isManuallyTrigger){
            isManualTrigger = false;
        }



    }


}
