package com.empatica.sample;

import android.app.Activity;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;


public class EmpaticaService extends IntentService implements EmpaDataDelegate, EmpaStatusDelegate {

    private static final int REQUEST_ENABLE_BT = 1;
    //private static final long STREAMING_TIME = 10000; // Stops streaming 10 seconds after connection
    private static final long STREAMING_TIME = 10000; // Stops streaming 1000 seconds after connection

    private static final String EMPATICA_API_KEY = "74da5531eacb41bb819a7643cfe88d06"; // TODO insert your API Key here

    private EmpaDeviceManager deviceManager;

    private TextView bvpLabel;
    private TextView edaLabel;
    private TextView ibiLabel;
    private TextView temperatureLabel;
    private TextView batteryLabel;
    private TextView statusLabel;
    private TextView deviceNameLabel;
    private RelativeLayout dataCnt;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * param to name the worker thread, important only for debugging.
     */
    public EmpaticaService() {
        super("EmpaticaService");
    }


    /*
    @Override
    protected void onPause() {
        super.onPause();
        deviceManager.stopScanning();
    }
    */

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(this, "Empatica Service Started", Toast.LENGTH_LONG).show();
                /*
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize vars that reference UI components
        statusLabel = (TextView) findViewById(R.id.status);
        dataCnt = (RelativeLayout) findViewById(R.id.dataArea);
        bvpLabel = (TextView) findViewById(R.id.bvp);
        edaLabel = (TextView) findViewById(R.id.eda);
        ibiLabel = (TextView) findViewById(R.id.ibi);
        temperatureLabel = (TextView) findViewById(R.id.temperature);
        batteryLabel = (TextView) findViewById(R.id.battery);
        deviceNameLabel = (TextView) findViewById(R.id.deviceName);
        */


        // Create a new EmpaDeviceManager. MainActivity is both its data and status delegate.
        deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);
        // Initialize the Device Manager using your API key. You need to have Internet access at this point.
        deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deviceManager.cleanUp();
        Toast.makeText(this, "Empatica Service Destroyed", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }



    @Override
    public void didDiscoverDevice(BluetoothDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
        // Check if the discovered device can be used with your API key. If allowed is always false,
        // the device is not linked with your API key. Please check your developer area at
        // https://www.empatica.com/connect/developer.php
        Log.e("msg", "HEJEHEJ");
        if (allowed) {
            // Stop scanning. The first allowed device will do.
            deviceManager.stopScanning();
            /*
            Intent spotifyIntent = new Intent(getBaseContext(), MainActivity.class);

            startActivity(spotifyIntent);
            */
            try {
                // Connect to the device
                deviceManager.connectDevice(bluetoothDevice);
            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                Toast.makeText(EmpaticaService.this, "Sorry, you can't connect to this device", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void didRequestEnableBluetooth() {
        // Request the user to enable Bluetooth
        //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    }
/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The user chose not to enable Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            // You should deal with this
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    */

    @Override
    public void didUpdateSensorStatus(EmpaSensorStatus status, EmpaSensorType type) {
        // No need to implement this right now
    }

    @Override
    public void didUpdateStatus(EmpaStatus status) {
        /* Update the UI
        //updateLabel(statusLabel, status.name());

        // The device manager is ready for use
        if (status == EmpaStatus.READY) {
            updateLabel(statusLabel, status.name() + " - Turn on your device");
            // Start scanning
            deviceManager.startScanning();
        // The device manager has established a connection
        } else if (status == EmpaStatus.CONNECTED) {
            // Stop streaming after STREAMING_TIME
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataCnt.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Disconnect device
                            deviceManager.disconnect();
                        }
                    }, STREAMING_TIME);
                }
            });
        // The device manager disconnected from a device
        } else if (status == EmpaStatus.DISCONNECTED) {
            updateLabel(deviceNameLabel, "");
        } */
    }

    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {
    }

    @Override
    public void didReceiveBatteryLevel(float battery, double timestamp) {
    }

    @Override
    public void didReceiveGSR(float gsr, double timestamp) {
    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        //updateLabel(ibiLabel, "" + ibi);
    }

    @Override
    public void didReceiveTemperature(float temp, double timestamp) {
        /*
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("temp", temp);
        sendBroadcast(intent);
        */

    }



}
