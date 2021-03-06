package com.rushdevo.camcam;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;



public class ProvideFeedActivity extends Activity implements SurfaceHolder.Callback {
	private String TAG = "ProvideFeedActivity";
	
	private MediaRecorder mediaRecorder;
    private SurfaceHolder surfaceHolder;
    private String filePath = null;
    
    int retries = 0;

    
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d("ProvideFeedActivity", "in provide feed activity");
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

        mediaRecorder = new MediaRecorder();
        initRecorder();
        setContentView(R.layout.activity_provide_feed);

        SurfaceView cameraView = (SurfaceView) findViewById(R.id.surface_camera);
        surfaceHolder = cameraView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void initRecorder() {
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    	mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
    	mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
    	mediaRecorder.setOrientationHint(90);
    	mediaRecorder.setVideoSize(1280, 720);
    	mediaRecorder.setVideoFrameRate(30);
//    	filePath = getOutputFilePath();
//        mediaRecorder.setOutputFile(filePath);
    }
    
    public final class StreamViaSocketTask extends AsyncTask<String,Boolean,String> {
    	@Override
    	protected String doInBackground(String...params) {
    		InetAddress address = null;
    		Socket socket = null;
            try {
    			address = InetAddress.getByName(CamCamActivity.HOST);
    		} catch (UnknownHostException e) {
    			e.printStackTrace();
    		}
            try {
            	socket = new Socket(address, 2000);
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
            ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(socket);
            FileDescriptor fd = pfd.getFileDescriptor();
            mediaRecorder.setOutputFile(fd);
        	prepareRecorder();
            return "";
    	}
    }
    
    private void prepareRecorder() {
    	mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
        	Log.e("ProvideFeedActivity", e.toString());
        } catch (IOException e) {
        	Log.e("prepareRecorder_IOException", e.toString());
        } catch( Exception e) {
        	Log.e("prepareRecorder", e.toString());
        	}
        startRecording();
    }
    
    private void startRecording() {
    	mediaRecorder.start();
		long timeStamp = System.currentTimeMillis() / 1000;
    	int videoLength = 30;
    	int seconds = videoLength;
		while (seconds > 0) {
			int diff = videoLength - (int)((System.currentTimeMillis() / 1000) - timeStamp);
			if (diff < 0) diff = 0;
			if (diff != seconds) seconds = diff;
		}
    	stopRecording();
    }
    
    private void startProvideFeedService() {
    	long timeStamp = System.currentTimeMillis() / 1000;
    	int wait = 5;
    	int seconds = wait;
		while (seconds > 0) {
			int diff = wait - (int)((System.currentTimeMillis() / 1000) - timeStamp);
			if (diff < 0) diff = 0;
			if (diff != seconds) seconds = diff;
		}
    	Intent feedIntent = new Intent(this,ProvideFeedService.class);
    	feedIntent.putExtra("filePath", filePath);
    	Context context = getApplicationContext();
    	context.startService(feedIntent);
    }
    
    private void stopRecording() {
    	Log.d("ProvideFeedActivity", "in stopRecording");
    	retries = 0;
    	new UploadRecordingTask().execute();
    	mediaRecorder.reset();
    }
    
    
    // SurfaceHolder Callbacks

    public void surfaceCreated(SurfaceHolder holder) {
    	new StreamViaSocketTask().execute();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mediaRecorder.release();
    }
  	
    private String getOutputFilePath() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
    	Log.d(TAG,"getting output file path");
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                  Environment.DIRECTORY_PICTURES), "CamCam");


        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("ProvideFeedActivity", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + CamCamActivity.DEVICE_ID + "_"+ timeStamp + ".mp4");
       
        return mediaFile.toString();
    }
    
    public final class UploadRecordingTask extends AsyncTask<String, Boolean, String> {
		@Override
		protected String doInBackground(String...myParams) {
			try {
				Log.d("UploadRecordingTask", "in UploadRecordingTask");
			    File file= new File(filePath);
			    StringBody deviceID = new StringBody(CamCamActivity.DEVICE_ID);
			    
			    DefaultHttpClient httpclient = new DefaultHttpClient();
			    HttpPost httpPost = new HttpPost("http://"+CamCamActivity.DOMAIN+"/feeds.json");
			    MultipartEntity entity = new MultipartEntity();
			    
			    entity.addPart("feed[stream]", new FileBody(file));
			    entity.addPart("feed[device_id]",deviceID);
			    httpPost.setHeader("ContentType", "video/mp4");
			    httpPost.setEntity(entity);

			    HttpResponse response;

			    response = httpclient.execute(httpPost);

			    if (entity != null) {
			        entity.consumeContent();
			    }
			    
			    StatusLine statusLine = response.getStatusLine();
			    int statusCode = statusLine.getStatusCode();
			    Log.d("UploadRecordingTask", "POST response status: "+statusCode);
			    	
			    return response.getStatusLine().toString();
			} catch (Exception ex) {
			    Log.d("UploadRecordingTask", "Upload failed: " + ex.getMessage() +
			        " Stacktrace: " + ex.getStackTrace());
			    return "failed";
			}
		}
    	
    	@Override
        protected void onPostExecute(String result) {
    		Log.d("ProvideFeedActivity", "in onPostExecute: "+result);
    		if (result.equals("failed")) {
    			if (retries < 4) {
		    		Log.d("UploadRecordingTask", "Internal Server Error, Retrying");
		    		retries++;
			    	new UploadRecordingTask().execute();
		    	} else {
		    		Log.d("UploadingRecordingTask", "Internal Server Error, Retried max times, retrying recording");
		        	Intent feedIntent = new Intent(getApplicationContext(), ProvideFeedActivity.class);
		        	feedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		            startActivity(feedIntent);
		    	}
    		}
    	}
    }
}
