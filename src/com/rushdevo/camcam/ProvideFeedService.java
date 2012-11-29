package com.rushdevo.camcam;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class ProvideFeedService extends Service {
	private String TAG = "ProvideFeedService";
	private String filePath;
		
	public ProvideFeedService() {
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Received start id " + startId + ": " + intent);
        filePath = intent.getStringExtra("filePath");
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
	        try {
				address = InetAddress.getByName(CamCamActivity.HOST);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
	        try {
	        	Socket socket = new Socket(address, 2000);

	        	OutputStream out = socket.getOutputStream();       
	        	DataOutputStream dataOut = new DataOutputStream(out);
	        	
	        	FileInputStream bufferStream=new FileInputStream(filePath);
	            byte buffer[]=new byte[2000];
	            bufferStream.read(buffer,0,20);
	            
	        	dataOut.write(buffer, 0, 20);
	        	dataOut.flush();
	        	dataOut.close();
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
