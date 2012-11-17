package com.rushdevo.camcam;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class CamCamActivity extends Activity {
	    
	//public static String DOMAIN = "www.epiccamcam.com";
	public static String DOMAIN = "10.0.1.28:3000";
	
	public static String DEVICE_ID = null;

	// lifecycle
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_cam);
        initializeDeviceRegistrationStatus();
	}
	
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
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
    
    // initializers

    public void initializeDeviceRegistrationStatus() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        DEVICE_ID = prefs.getString("deviceID", null);
        if (DEVICE_ID != null) {
        	hideRegistrationElements(prefs.getString("deviceName", null));
        } else {
        	showRegistrationElements();
        }
    }
    
    // ui
    
    public void showRegistrationElements() {
        // show ui elements to register device
        Button registerButton = (Button)findViewById(R.id.register_device_button);
        EditText nameField = (EditText)findViewById(R.id.device_name_field);
        registerButton.setVisibility(View.VISIBLE);
        nameField.setVisibility(View.VISIBLE);
        
        // hide text view displaying registered status and name
        TextView registeredDeviceText = (TextView)findViewById(R.id.registered_device_text);
        Button pushFeedButton = (Button)findViewById(R.id.push_feed_button);
        registeredDeviceText.setText("");
        registeredDeviceText.setVisibility(View.GONE);
        pushFeedButton.setVisibility(View.GONE);
    }
    
    public void hideRegistrationElements(String deviceName) {
        // hide ui elements to register device
        Button registerButton = (Button)findViewById(R.id.register_device_button);
        EditText nameField = (EditText)findViewById(R.id.device_name_field);
        registerButton.setVisibility(View.GONE);
        nameField.setVisibility(View.GONE);
        
        // show text view displaying registered status and name
        TextView registeredDeviceText = (TextView)findViewById(R.id.registered_device_text);
        Button pushFeedButton = (Button)findViewById(R.id.push_feed_button);
        registeredDeviceText.setText("Device registered as '"+deviceName+"'");
        registeredDeviceText.setVisibility(View.VISIBLE);
        pushFeedButton.setVisibility(View.VISIBLE);
    }
    
 
   // device registration
    
    public void registerDevice(View v) {
    	Log.d("CamCamActivity","IN REGISTER DEVICE");
    	new RegisterDeviceTask().execute();	
    }
    
    public final class RegisterDeviceTask extends AsyncTask<String, Boolean, String> {
    	@Override
        protected String doInBackground(String...myParams) {
    		EditText nameField = (EditText)findViewById(R.id.device_name_field);
    		String deviceName = nameField.getText().toString();
            String result = "";
            try {
                HttpClient client = new DefaultHttpClient();  
                String postURL = "http://"+DOMAIN+"/devices.json";
                HttpPost post = new HttpPost(postURL); 
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("device[name]", deviceName));
                params.add(new BasicNameValuePair("device[user_id]", "1"));
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params,HTTP.UTF_8);
                post.setEntity(ent);
                HttpResponse responsePOST = client.execute(post);  
                HttpEntity resEntity = responsePOST.getEntity();  
                if (resEntity != null) {
                	result = EntityUtils.toString(resEntity);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }            
            Log.i("RESPONSE",result);
            return result;
        }
    	
    	@Override
        protected void onPostExecute(String result) {
            publishProgress(false);
            JSONObject json = null;
			try {
				json = new JSONObject(result);
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
            
            // hide ui elements to register device
            Button registerButton = (Button)findViewById(R.id.register_device_button);
            EditText nameField = (EditText)findViewById(R.id.device_name_field);
            registerButton.setVisibility(View.GONE);
            nameField.setVisibility(View.GONE);
            
            // show text view displaying registered status and name
            TextView registeredDeviceText = (TextView)findViewById(R.id.registered_device_text);
            Button pushFeedButton = (Button)findViewById(R.id.push_feed_button);
            registeredDeviceText.setText("Device registered as '"+deviceName+"'");
            registeredDeviceText.setVisibility(View.VISIBLE);
            pushFeedButton.setVisibility(View.VISIBLE);

    	}
    }
    
    // push video feed
    public void pushFeed(View view) {
    	Intent intent = new Intent(this, ProvideFeedActivity.class);
        startActivity(intent);      
        finish();
    }
}
