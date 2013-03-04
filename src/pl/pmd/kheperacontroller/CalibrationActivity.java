package pl.pmd.kheperacontroller;

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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CalibrationActivity extends Activity implements SensorEventListener {
	private SensorManager sensorManager;
	private Button calibrate_button;
	private boolean isError = false;
	private String error_msg = "Error: ";
	private TextView current_value_textView;
	private TextView max_value_textView;
	private float calibrated_x;
	private float calibrated_y;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("METHOD", "onCreate() calibrationActivity");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
	    					 WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_calibration);
       
        // TextView
        current_value_textView = (TextView) findViewById(R.id.current_value);
        max_value_textView = (TextView) findViewById(R.id.max_value);
        max_value_textView.setText("±" + Integer.toString(getResources().getInteger(R.attr.sensor_maxalpha) -1 ));
        
        // Sensors
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
			sensorManager.registerListener(this, 
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_NORMAL);
			error_msg += getResources().getString(R.string.error_sensorOutOfRange);
		} else {
			isError = true;
			error_msg += this.getResources().getString(R.string.error_sensorNotFound);
		}
		
        // Calibrate button
        calibrate_button = (Button) findViewById(R.id.Do_calibration);
	    calibrate_button.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View arg) {
            	Log.d("UI", "Do calibration button clicked");
            	if (Math.abs(calibrated_x) > (90 - getResources().getInteger(R.attr.sensor_maxalpha))){
            		Toast.makeText(CalibrationActivity.this, error_msg, Toast.LENGTH_SHORT).show();
            	} else {
	            	Intent i = getIntent();
	            	i.putExtra("calibrated_x", calibrated_x);
	            	i.putExtra("calibrated_y", calibrated_y);
	            	Log.i("INFO", "X: " + calibrated_x + " Y: " + calibrated_y);
	            	setResult(RESULT_OK, i);
	            	finish();
            	}
            }
        });
	}

	@Override
	protected void onResume() {
		super.onResume();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		if (isError) {
			Toast.makeText(CalibrationActivity.this, error_msg, Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}
	
	protected void onStop(){
		super.onStop();
		Log.d("METHOD", "onStop() calibrationActivity");
    	setResult(RESULT_CANCELED);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		calibrated_x = event.values[0] * 9;
		if (Math.abs(calibrated_x) > (90 - getResources().getInteger(R.attr.sensor_maxalpha))){
			current_value_textView.setTextAppearance(getApplicationContext(), R.style.ErroFont);
    	} else {
    		current_value_textView.setTextAppearance(getApplicationContext(), R.style.KeywordFont);
    	}
		if (calibrated_x < 0){
			current_value_textView.setText(Integer.toString((int) calibrated_x));
		} else {
			current_value_textView.setText(" " + Integer.toString((int) calibrated_x));
		}
		calibrated_y = event.values[1] * 9;
		
	}
	
}
