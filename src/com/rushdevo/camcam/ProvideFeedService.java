package com.rushdevo.camcam;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class ProvideFeedService extends Service {
	private String TAG = "ProvideFeedService";
	
	public ProvideFeedService() {
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Received start id " + startId + ": " + intent);
        new SendDataTask().execute();
        return START_STICKY;
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
    public final class SendDataTask extends AsyncTask<String, Boolean, String> {
		
		@Override
		protected String doInBackground(String... myParams) {
			Log.d(TAG,"attempting to send data over socket");
			InetAddress address = null;
	        Socket socket;
	        try {
				address = InetAddress.getByName(CamCamActivity.HOST);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
	        try {
	        	// this should open and connect socket
				//socket = new Socket(address, intent.getIntExtra("port", 3001));
	        	socket = new Socket(address, 2000);
	        	OutputStream out = socket.getOutputStream();       
	        	PrintWriter output = new PrintWriter(out);         

	        	output.println("Hello from Android");              
	        	output.flush();
	        	output.close();
	        	out.flush();
	        	out.close();
	        	socket.close();                                    
			} catch (IOException e) {
				e.printStackTrace();
			}
	        return "";
		}
		
		@Override
		protected void onPostExecute(String result) {
		
		}
    }
	
	
	
}
