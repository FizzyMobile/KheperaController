package pl.pmd.kheperacontroller;

import java.io.IOException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
//import android.net.Uri;
//import android.widget.VideoView;

public class ControllerActivity extends Activity implements SensorEventListener {
	private int tmpN = 8;
	private final float constT = 0.25f;
	
	private Robot robot;
	private String robot_address;
	private String camera_address;
	private SensorManager sensorManager;
	//private VideoView videoView;
	private boolean isCamera = true;
	
	private float zero_x;
	//private float zero_y;
	float tmpMaxAlpha;
	float tmpMaxBeta;
	float tmpVMax;
	private boolean isError = false;
	private String error_msg = "Error: ";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("METHOD", "onCreate() ControllerActicity");
        super.onCreate(savedInstanceState);
        
        // Getting received data
        Intent i = getIntent();
        camera_address = i.getStringExtra("camera_address");
        robot_address = i.getStringExtra("robot_address");
		Log.d("INFO", "robot: " + robot_address + " camera: " + camera_address);
        zero_x = i.getFloatExtra("zero_x", (float)this.getResources().getInteger(R.attr.sensor_default_x));
        //zero_y = i.getFloatExtra("zero_y", (float)this.getResources().getInteger(R.attr.sensor_default_y));
		tmpMaxAlpha = (float) this.getResources().getInteger(R.attr.sensor_maxalpha);
		tmpMaxBeta = (float) this.getResources().getInteger(R.attr.sensor_maxbeta);
		tmpVMax = this.getResources().getInteger(R.attr.sensor_vmax);
		
		// VideoView 
		//videoView = (VideoView)this.findViewById(R.id.videoView);
		if (camera_address == null || camera_address.contains("NUL")){
			isCamera = false;
		}

        robot = new Robot();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
	    					 WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_controller);

		// Sensor setting
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {	
			isError = true;
			error_msg += this.getResources().getString(R.string.error_sensorNotFound);
		}
	}
	
	@Override
	protected void onResume() {
		Log.d("METHOD", "onResume() ControllerActicity");
		super.onResume();
		
		Log.d("NETWORK", "Connecting to robot: " + robot_address);
		try {
			robot.connect(robot_address, 3000);
		} catch (UnknownHostException e) {
			isError = true;
			error_msg += this.getResources().getString(R.string.error_robotConnection);
		} catch (IOException e) {
			isError = true;
			error_msg += this.getResources().getString(R.string.error_robotConnection);
		}
		
		if (isError) {
			Toast.makeText(ControllerActivity.this, error_msg, Toast.LENGTH_SHORT).show();
			Log.d("METHOD", "finish() ControllerActicity");
			finish();
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			if (isCamera){
				//videoView.setVideoURI(Uri.parse("rtsp://192.168.0.106:554/live.3gp"));
				//videoView.requestFocus();
				//videoView.start();
			}
			sensorManager.registerListener(this, 
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_FASTEST);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("METHOD", "onPause() ControllerActivity");
		if(robot.isConnected()){
			robot.stop();
			robot.disconnect();
		}
		if (!isError) {
			sensorManager.unregisterListener(this);
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float tmpWx = event.values[0] * 9; //roll
		float tmpWy = event.values[1] * 9; //pitch
		tmpWx -= zero_x;
		if(Math.abs(tmpWx) > tmpMaxAlpha) tmpWx = (tmpWx < 0) ? -tmpMaxAlpha : tmpMaxAlpha;
		if(Math.abs(tmpWy) > tmpMaxBeta) tmpWy = (tmpWy < 0) ? -tmpMaxBeta : tmpMaxBeta;
		int tmpVCurrent = (int) (-1 * ((int) (tmpWx * tmpN / tmpMaxAlpha)) * tmpVMax / tmpN);
		int tmpVCurrentLower = (int) (tmpVCurrent * (1 - Math.abs(tmpWy) / tmpMaxBeta * (1 - constT)));
		if(robot.isConnected()){
			if(tmpWy < 0) robot.move(tmpVCurrentLower, tmpVCurrent);
			else if (tmpWy > 0) robot.move(tmpVCurrent, tmpVCurrentLower);
			else robot.move(tmpVCurrent, tmpVCurrent);
		}
		Log.d("onSensorChanged", Float.toString(tmpWx));
		Log.d("onSensorChanged", Integer.toString(tmpVCurrent) + " " +Integer.toString(tmpVCurrentLower));
	}
	
}
