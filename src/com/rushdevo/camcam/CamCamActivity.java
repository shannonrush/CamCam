package com.rushdevo.camcam;

import java.io.IOException;
import java.net.ServerSocket;

import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;


public class CamCamActivity extends Activity {
	    
    ServerSocket mServerSocket;
	private RegistrationListener mRegistrationListener;
    private String mServiceName;
	private NsdManager mNsdManager;
	private DiscoveryListener mDiscoveryListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_cam);
        
        // initialize the registration listener
        initializeRegistrationListener();
        
        // register the CamCam service on Network Service Discovery (NSD)
        registerService(nextPort());
        
        // initialize discovery listener
        initializeDiscoveryListener();
        
        discoverServices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_cam_cam, menu);
        return true;
    }
    
    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

			@Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                mServiceName = NsdServiceInfo.getServiceName();
                Log.d("CamCamActivity","in onServiceRegistered with service name: "+mServiceName);
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
                if (service.getServiceName().equals(mServiceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to.
                    Log.d("CamCamActivity", "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains("CamCam")){
                	Log.d("CamCamActivity", "CamCam found, attempting to resolve");
//                    mNsdManager.resolveService(service, mResolveListener);
                } 
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e("CamCamActivity", "service lost" + service);
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
    
     
}
