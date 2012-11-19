package com.rushdevo.camcam;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;


public class CamCamActivity extends Activity {
	    
	//public static String DOMAIN = "www.epiccamcam.com";
	public static String DOMAIN = "10.0.1.28:3000"; // development
	public static String DEVICE_ID = null;
	public static String DEVICE_NAME = null;
	public static String USER_ID = "1"; // temporarily hard coded
	public static String GCM_PROJECT_ID = "945612395303";

	// lifecycle
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_cam);
        
        registerReceiver(registrationReceiver, new IntentFilter("ON_POST_EXECUTE"));
        initializeDeviceRegistrationStatus();
	}
	
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(registrationReceiver);
        super.onDestroy();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_cam_cam, menu);
        return true;
    }
    
    private final BroadcastReceiver registrationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	Log.d("BroadcastReceiver", "in registrationReceiver");
            JSONObject json = null;
			try {
				json = new JSONObject(intent.getStringExtra("result"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            // save id as deviceID
            String deviceID = null;
			try {
				deviceID = json.getString("id");
			} catch (JSONException e) {
				e.printStackTrace();
			}
            editor.putString("deviceID", deviceID);

            // save name as deviceName
            String deviceName = null;
			try {
				deviceName = json.getString("name");
			} catch (JSONException e) {
				e.printStackTrace();
			}
            editor.putString("deviceName", deviceName);
            
            editor.commit();
            Log.d("CamCamActivity", "SAVED NAME: "+prefs.getString("deviceName", null));
            Log.d("CamCamActivity", "SAVED ID: "+prefs.getString("deviceID", null));
            
            elementsForRegisteredDevice(deviceName);
        }
    };
    
    // initializers

    public void initializeDeviceRegistrationStatus() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        DEVICE_ID = prefs.getString("deviceID", null);
        if (DEVICE_ID != null) {
        	elementsForRegisteredDevice(prefs.getString("deviceName", null));
        } else {
        	elementsForUnregisteredDevice();
        }
    }
    
    // ui
    
    public void elementsForUnregisteredDevice() {
        // show ui elements to register device
        Button registerButton = (Button)findViewById(R.id.register_device_button);
        EditText nameField = (EditText)findViewById(R.id.device_name_field);
        registerButton.setVisibility(View.VISIBLE);
        nameField.setVisibility(View.VISIBLE);
        
        // hide text view displaying registered status and name
        TextView registeredDeviceText = (TextView)findViewById(R.id.registered_device_text);
        Button showFeedsButton = (Button)findViewById(R.id.show_feeds_button);
        registeredDeviceText.setText("");
        registeredDeviceText.setVisibility(View.GONE);
        showFeedsButton.setVisibility(View.GONE);
    }
    
    public void elementsForRegisteredDevice(String deviceName) {
        // hide ui elements to register device
        Button registerButton = (Button)findViewById(R.id.register_device_button);
        EditText nameField = (EditText)findViewById(R.id.device_name_field);
        registerButton.setVisibility(View.GONE);
        nameField.setVisibility(View.GONE);
        
        // show text view displaying registered status and name
        TextView registeredDeviceText = (TextView)findViewById(R.id.registered_device_text);
        Button showFeedsButton = (Button)findViewById(R.id.show_feeds_button);
        registeredDeviceText.setText("Device registered as '"+deviceName+"'");
        registeredDeviceText.setVisibility(View.VISIBLE);
        showFeedsButton.setVisibility(View.VISIBLE);
    }
    
 
   // device registration
    
    public void registerDevice(View v) {
    	EditText nameField = (EditText)findViewById(R.id.device_name_field);
    	DEVICE_NAME = nameField.getText().toString();
    	GCMRegistrar.checkDevice(this);
    	GCMRegistrar.checkManifest(this);
    	final String regId = GCMRegistrar.getRegistrationId(this);
    	if (regId.equals("")) {
    	  GCMRegistrar.register(this, GCM_PROJECT_ID);
    	} else {
    	  Log.v("CamCamActivity", "Already registered with GCM");
    	}
    }
    
    
    public void showFeeds(View v) {
    	Intent intent = new Intent(this, ShowFeedsActivity.class);
        startActivity(intent);      
        finish();
    }
}
