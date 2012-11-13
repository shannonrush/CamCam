package com.rushdevo.camcam;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class ProvideFeedActivity extends Activity implements SurfaceHolder.Callback {
	MediaRecorder mRecorder;
    SurfaceHolder mHolder;
    boolean mPrepared = false;
    
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

        mRecorder = new MediaRecorder();
        initRecorder();
        setContentView(R.layout.activity_provide_feed);

        SurfaceView cameraView = (SurfaceView) findViewById(R.id.surface_camera);
        mHolder = cameraView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void initRecorder() {
        mRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    	mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
    	mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
    	mRecorder.setOrientationHint(90);
    	mRecorder.setVideoSize(1280, 720);
    	mRecorder.setVideoFrameRate(30);

    	
    	String filePath = getOutputMediaFile().toString();
        mRecorder.setOutputFile(filePath);
        Log.d("ProvideFeedActivity", "FILEPATH: "+filePath);
    }
    
    private void prepareRecorder() {
    	Log.d("ProvideFeedActivity", "about to prepare");
    	mRecorder.setPreviewDisplay(mHolder.getSurface());

        try {
            mRecorder.prepare();
            mPrepared = true;
            Log.d("ProvideFeedActivity", "right after prepare");        
        } catch (IllegalStateException e) {
        	Log.e("ProvideFeedActivity", e.toString());
        } catch (IOException e) {
        	Log.e("prepareRecorder_IOException", e.toString());
        } catch( Exception e) {
        	Log.e("prepareRecorder", e.toString());
        	}
        Log.d("ProvideFeedActivity","after prepare");   
        startRecording();
    }
    
    private void startRecording() {
    	Log.d("ProvideFeedActivity", "in startRecording");
    	mRecorder.start();
    	// record some seconds of video then stop
		long timeStamp = System.currentTimeMillis() / 1000;
    	int videoLength = 15;
    	int seconds = videoLength;
		while (seconds > 0) {
			int diff = videoLength - (int)((System.currentTimeMillis() / 1000) - timeStamp);
			if (diff < 0) diff = 0;
			if (diff != seconds) seconds = diff;
		}
    	stopRecording();
    }
    
    private void stopRecording() {
    	Log.d("ProvideFeedActivity", "in stopRecording");
    	mRecorder.reset();
		mPrepared = false;
    }
    
    private static File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                  Environment.DIRECTORY_PICTURES), "CamCam");


        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + CamCamActivity.DEVICE_ID + "_"+ timeStamp + ".mp4");
       
        return mediaFile;
    }
    
    // SurfaceHolder Callbacks

    public void surfaceCreated(SurfaceHolder holder) {
    	Log.d("ProvideFeedActivity", "in surfaceCreated");
    	prepareRecorder();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mRecorder.release();
    }
  	
}
