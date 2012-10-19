package com.rushdevo.camcam;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class CamCamActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_cam);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_cam_cam, menu);
        return true;
    }
    
    public void broadcast(View view) {
    	
    }
}
