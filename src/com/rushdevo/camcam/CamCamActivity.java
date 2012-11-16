package com.rushdevo.camcam;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
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

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class CamCamActivity extends ListActivity {
	    
    private ServerSocket mServerSocket;
	private RegistrationListener mRegistrationListener;
    private String mServiceName;
	private NsdManager mNsdManager;
	private DiscoveryListener mDiscoveryListener;
	private ResolveListener mResolveListener;
	private ArrayList<NsdServiceInfo> mDiscoveredServices;
	private ArrayAdapter<String> adapter;
	private ArrayList<String> serviceNames;
	public static String DOMAIN = "www.epiccamcam.com";
	//public static String DOMAIN = "10.0.1.28:3000";
	
	public static String DEVICE_ID = null;

	// lifecycle
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        initializeListeners();
    	initializeServiceList();
    	initializeDeviceRegistrationStatus();
        super.onCreate(savedInstanceState);
    }
	
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("CamCamActivity","calling registerService from onResume");
        registerService(nextPort());
        discoverServices();
    }

    @Override
    protected void onDestroy() {
        tearDown();
        super.onDestroy();
    }
    
    @Override
    protected void onPause() {
        tearDownService();
        super.onPause();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_cam_cam, menu);
        return true;
    }
    
    // initializers
    
    public void initializeListeners() {
        // initialize the registration listener
        initializeRegistrationListener();
        
        // initialize resolve listener
        initializeResolveListener();
        
        // initialize discovery listener
        initializeDiscoveryListener();
    }
    
    public void initializeServiceList() {
        setContentView(R.layout.activity_cam_cam);

    	// reset discovered services
    	mDiscoveredServices = new ArrayList<NsdServiceInfo>();
    	serviceNames = new ArrayList<String>();
    	// adapter
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, serviceNames);
        setListAdapter(adapter);
    }
    
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
    
    // listeners
    
    public void initializeRegistrationListener() {
        Log.d("CamCamActivity","initializing registration listener");
        mRegistrationListener = new NsdManager.RegistrationListener() {

			@Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                mServiceName = NsdServiceInfo.getServiceName();
                Log.d("CamCamActivity","service registered with service name: "+mServiceName);
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed!  Put debugging code here to determine why.
                Log.d("CamCamActivity","registration failed, code: "+errorCode);

            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered.  This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
                Log.d("CamCamActivity","service unregistered");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed.  Put debugging code here to determine why.
                Log.d("CamCamActivity","unregistration failed: "+errorCode);
            }
        };
    }
    
    public void registerService(int port) {
    	Log.d("CamCamActivity", "IN REGISTER SERVICE");
        // Create the NsdServiceInfo object, and populate it.
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.setServiceName("CamCam");
        serviceInfo.setServiceType("_http._tcp.");
        serviceInfo.setPort(port);
        
        Context context = getApplicationContext();
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        Log.d("CamCamActivity","attempting to register: "+serviceInfo.getServiceName());

    }
    
    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e("CamCamActivity", "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e("CamCamActivity", "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d("CamCamActivity", "Same IP.");
                    return;
                }
                NsdServiceInfo mService = serviceInfo;
                int port = mService.getPort();
                InetAddress host = mService.getHost();
            }
        };
    }
    
    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d("CamCamActivity", "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d("CamCamActivity", "Service discovery success" + service);
                mDiscoveredServices.add(service);
                serviceNames.add(service.getServiceName());
                
                for (NsdServiceInfo serviceInfo : mDiscoveredServices) {
                    Log.d("CamCamActivity","DISCOVERED SERVICE "+serviceInfo.getServiceName());
                }
                
                if (service.getServiceName().equals(mServiceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to.
                    Log.d("CamCamActivity", "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains("CamCam")){
                	Log.d("CamCamActivity", "CamCam found, attempting to resolve");
                    mNsdManager.resolveService(service, mResolveListener);
                } 
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e("CamCamActivity", "service lost" + service);
//                mDiscoveredServices.remove(mDiscoveredServices.indexOf(service));
//                serviceNames.remove(serviceNames.indexOf(service.getServiceName()));
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i("CamCamActivity", "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e("CamCamActivity", "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e("CamCamActivity", "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }
    
    public void discoverServices() {
    	mNsdManager.discoverServices("_http._tcp", NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }
    
    public int nextPort() {
    	// get next available port
        try {
			mServerSocket = new ServerSocket(0);
			return mServerSocket.getLocalPort();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return (Integer) null;
    }
    
    // nsd lifecycle
    
    public void tearDownService() {
        mNsdManager.unregisterService(mRegistrationListener);
    }
    
    public void tearDown() {
        mNsdManager.unregisterService(mRegistrationListener);
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
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
