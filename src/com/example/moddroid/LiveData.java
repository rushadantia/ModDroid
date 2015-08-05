package com.example.moddroid;

import java.io.ByteArrayOutputStream;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class LiveData extends Activity {

	private int address;

	private Modbus modbus;
	private String ip;
	private volatile boolean GO = true;
	private Thread dataThread;
	private GraphView graph;
	private Boolean scaleable = true;
	private LineGraphSeries<DataPoint> series;
	private double time = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_live_data);

		//gets the address from the intent
		Bundle bundle = getIntent().getExtras();
		address = Integer.parseInt(((String) bundle.get("address")).trim());
		ip = bundle.getString("ip").trim();
		modbus = new Modbus(ip, Modbus.DEFAULT_PORT);

		graph = (GraphView)findViewById(R.id.graph);
		graph.getViewport().setBackgroundColor(Color.argb(255, 222, 222, 222));

		series = new LineGraphSeries<DataPoint>();

		//make each point tappable
		series.setOnDataPointTapListener(new OnDataPointTapListener() {

			public void onTap(Series arg0, DataPointInterface arg1) {
				Toast.makeText(getApplicationContext(), "Time: "+arg1.getX()+"\nValue: "+arg1.getY(), Toast.LENGTH_SHORT).show();
			}			
		});

		graph.addSeries(series);
		graph.getViewport().setYAxisBoundsManual(true);
		series.setDrawDataPoints(true);
		series.setColor(Color.RED);
		series.setDataPointsRadius(5);
		series.setThickness(5);

		//a thread to get the data
		dataThread = new Thread(new Runnable() {

			public void run() {	

				//for resizing the bars
				float min = Float.MAX_VALUE;
				float max = 1;

				while(GO) {
					try {
						//get start time for the contol loop
						long startTime = System.currentTimeMillis();

						final float data = modbus.getDataFromInputRegister(address);
						final DataPoint dp = new DataPoint(time, data);

						//reisizing the bars
						if(data>max)
							max = data;
						if(data<min&&data!=-1)
							min = data;

						final float mn = min;
						final float mx = max;

						//rerun the data on the main ui thread
						runOnUiThread(new Runnable() {

							public void run() {

								if(data!=-1) {
									series.appendData(dp, true, 500);

									graph.onDataChanged(true, false);

									synchronized(scaleable) {

										if(scaleable) {
											graph.getViewport().setMinY(mn-.1);
											graph.getViewport().setMaxY(mx+.1);	

										}else {
											graph.getViewport().setMinY(4);
											graph.getViewport().setMaxY(20);	
										}
									}
								}
							}
						});

						time += 5;

						Thread.sleep(5000-(System.currentTimeMillis()-startTime));

					} catch (InterruptedException e) {

						e.printStackTrace();
					}
				}
			}
		});

		dataThread.start();
	}

	//close thread
	public void onBackPressed() {
		Toast.makeText(getApplicationContext(), "Please Wait\nTerminating Thread", Toast.LENGTH_LONG).show();
		GO = false;

		try {dataThread.join();} catch (InterruptedException e) {e.printStackTrace();}	

		finish();

		super.onBackPressed();
	}


	//self explanatory
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.main, menu);  // Locate MenuItem with ShareActionProvider

		return true;
	}

	//when something is selected do something
	public boolean onOptionsItemSelected(MenuItem item) {
		synchronized (scaleable) {
			if (item.getItemId() == R.id.fourTwenty) 
				scaleable = false;
			else if(item.getItemId() == R.id.scaled) 
				scaleable = true;
			else if (item.getItemId() == R.id.screenShot) 
				shareScreenshot();

		}

		return true;
	}

	private void shareScreenshot() {

		try {
			View screenView = getWindow().getDecorView();
			screenView.setDrawingCacheEnabled(true);

			Bitmap b = Bitmap.createBitmap(screenView.getDrawingCache());
			screenView.setDrawingCacheEnabled(false);

			Intent share = new Intent(Intent.ACTION_SEND);
			share.setType("image/jpeg");
		
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			b.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
			String path = MediaStore.Images.Media.insertImage(getContentResolver(),b, "Title", null);
			
			Uri imageUri =  Uri.parse(path);
			share.putExtra(Intent.EXTRA_STREAM, imageUri);
			
			startActivity(Intent.createChooser(share, "Select"));
		}catch(Exception exception) {
			exception.printStackTrace();
		}
	}

}

