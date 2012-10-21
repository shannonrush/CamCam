package com.rushdevo.camcam;

import java.io.IOException;
import java.net.ServerSocket;

import android.app.Activity;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;


public class CamCamActivity extends Activity {
	    
    ServerSocket mServerSocket;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_cam);
        
        // register the CamCam service on Network Service Discovery (NSD)
        registerService(nextPort());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_cam_cam, menu);
        return true;
    }
    
    public void registerService(int port) {
    	Log.d("CamCamActivity", "PORT: "+port);
        // Create the NsdServiceInfo object, and populate it.
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.setServiceName("CamCam");
        serviceInfo.setServiceType("_http._tcp.");
        serviceInfo.setPort(port);
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
