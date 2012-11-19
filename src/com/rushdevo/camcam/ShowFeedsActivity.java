package com.rushdevo.camcam;

import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ShowFeedsActivity extends ListActivity {

	private ArrayAdapter<String> adapter;
	private JSONArray deviceArray;
	private ArrayList<String> deviceNames;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_feeds);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		new GetUserDevicesTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_show_feeds, menu);
		ListView listView = (ListView)findViewById(R.id.device_list);
		listView.setClickable(true);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

		  @Override
		  public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
			 JSONObject device = (JSONObject) deviceArray.get(position);
		     new GetFeedsForDeviceTask().execute((String) device.get("id"));
		  }
		});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// feed list
	
	private void initializeList() {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceNames);
        setListAdapter(adapter);
	}
	
	// async tasks 
	
	public final class GetUserDevicesTask extends AsyncTask<String, Boolean, String> {

		@Override
		protected String doInBackground(String... params) {
			String result = "";
			try {
			    HttpClient httpClient = new DefaultHttpClient();
			    String url = "http://"+CamCamActivity.DOMAIN+"/users/"+CamCamActivity.USER_ID+"/devices.json";
			    HttpResponse response = httpClient.execute(new HttpGet(url));
			    HttpEntity entity = response.getEntity();
			    result = EntityUtils.toString(entity);
			} catch (Exception e) {
			    Log.d("ShowFeedsActivity", "Network exception");
			}
			return result;
        }		
		
		@Override
        protected void onPostExecute(String result) {
			  Object obj=JSONValue.parse(result);
			  deviceArray = (JSONArray)obj;
			  deviceNames = new ArrayList<String>();
			  for (int i = 0; i < deviceArray.size(); ++i) {
				    JSONObject device = (JSONObject) deviceArray.get(i);
				    deviceNames.add((String) device.get("name"));
			  }
			  initializeList();
		}	
	}
	
	public final class GetFeedsForDeviceTask extends AsyncTask<String, Boolean, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			
		}
		
	}
	

}
