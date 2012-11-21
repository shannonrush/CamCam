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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;


public class GCMIntentService extends GCMBaseIntentService {
	
	public Context CONTEXT;
	
	// methods run in the intent service's thread and hence are free to make network calls without the risk of blocking the UI thread.
	
	public GCMIntentService() {
	}

	@Override
	protected void onError(Context context, String errorID) {
		Log.d("GCMIntentService", "GCM ERROR ID: "+errorID);
	}

	protected boolean onRecoverableError(Context context, String errorID) {
		Log.d("GCMIntentService", "GCM RECOVERABLE ERROR ID: "+errorID);
		return false;
	}
	
	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.d("GCMIntentService", "in onMessage");
//		String requested = intent.getStringExtra("requested");
    	Intent feedIntent = new Intent(this, ProvideFeedActivity.class);
    	feedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(feedIntent);
	}

	@Override
	protected void onRegistered(Context context, String gcmID) {
		Log.d("GCMIntentService", "Registered with GCM new ID: "+gcmID);
		CONTEXT = context;
		new RegisterDeviceTask().execute(gcmID);
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	public final class RegisterDeviceTask extends AsyncTask<String, Boolean, String> {
    	@Override
        protected String doInBackground(String... myParams) {
    		String gcmID = myParams[0];
            String result = "";
            try {
                HttpClient client = new DefaultHttpClient();  
                String postURL = "http://"+CamCamActivity.DOMAIN+"/devices.json";
                HttpPost post = new HttpPost(postURL); 
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("device[name]", CamCamActivity.DEVICE_NAME));
                params.add(new BasicNameValuePair("device[user_id]", "1"));
                params.add(new BasicNameValuePair("device[gcm_id]", gcmID));
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params,HTTP.UTF_8);
                post.setEntity(ent);
                HttpResponse responsePOST = client.execute(post);  
                HttpEntity entity = responsePOST.getEntity();  
                if (entity != null) 
                	result = EntityUtils.toString(entity);
            } catch (Exception e) {
                e.printStackTrace();
            }            
            Log.i("RESPONSE",result);
            return result;
        }
    	
    	@Override
        protected void onPostExecute(String result) {
    		Intent intent = new Intent("ON_POST_EXECUTE");
    		intent.putExtra("result", result);
            CONTEXT.sendBroadcast(intent);
    	}
    }
	
}
