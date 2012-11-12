package com.rushdevo.camcam;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ProvideFeedActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provide_feed);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_provide_feed, menu);
        return true;
    }
}
