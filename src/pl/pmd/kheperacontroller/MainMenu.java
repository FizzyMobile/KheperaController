package pl.pmd.kheperacontroller;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class MainMenu extends Activity {
	private String message_title;
	private String message_content;
	private Spinner spinner_camera, spinner_robot;
	private static HashMap<String, String> cameras_list;
	private static HashMap<String, String> robots_list;
	private Button calibrate_button;
	private Button start_button;
	private float calibrated_x;
	private float calibrated_y;

// ON CREATE
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("METHOD", "onCreate() MainMenu");
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// Main Menu styling 
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
	    					 WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// Setting content view
	    setContentView(R.layout.activity_main_menu);
	    super.onCreate(savedInstanceState);
	    //if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) return;
	    // Globals
	    spinner_camera = (Spinner) findViewById(R.id.Spinner_camera);
	    spinner_robot = (Spinner) findViewById(R.id.Spinner_robot);
	    cameras_list = new HashMap<String, String>();
	    robots_list = new HashMap<String, String>();
	    calibrated_x = (float)this.getResources().getInteger(R.attr.sensor_default_x);
	    calibrated_y = (float)this.getResources().getInteger(R.attr.sensor_default_y);
	    
	    // Logic
	    try {
	    	getListItems(cameras_list, "camera");
	    	getListItems(robots_list, "robot");
	    }
	    catch (XmlPullParserException e) {
            Toast.makeText(this, "Could not retrieve the current parser event", Toast.LENGTH_SHORT).show();  
        }
        catch (IOException e) {  
            Toast.makeText(this, "Could not read XML file", Toast.LENGTH_SHORT).show();
        }  
	    
	    addItemsOnSpinner(spinner_camera, cameras_list);
	    addItemsOnSpinner(spinner_robot, robots_list);
	    
	    // START button 
	    start_button = (Button) findViewById(R.id.Button_start);
	    start_button.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View arg) {
                Log.d("UI", "Start button clicked");
            	//Starting a new Intent
                Intent cotrollerActivity = new Intent(getApplicationContext(), ControllerActivity.class);
 
                //Sending data to another Activity
                cotrollerActivity.putExtra("camera_address", cameras_list.get(((String)spinner_camera.getSelectedItem())));
                cotrollerActivity.putExtra("robot_address", robots_list.get(((String)spinner_robot.getSelectedItem())));
                cotrollerActivity.putExtra("zero_x", calibrated_x);
                cotrollerActivity.putExtra("zero_y", calibrated_y);
                
                startActivity(cotrollerActivity);
            }
        });
	    
	 // Calibrate button 
	    calibrate_button = (Button) findViewById(R.id.Button_calibrate);
	    calibrate_button.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View arg) {
            	Log.d("UI", "Calibrate button clicked");
            	//Starting a new Intent
                Intent calibrationActivity = new Intent(getApplicationContext(), CalibrationActivity.class);
                startActivityForResult(calibrationActivity, 1);
            }
        });
	}
	
	public void getListItems(HashMap<String, String> list, String tagName) throws XmlPullParserException, IOException {
		Log.d("METHOD", "getListItems");
		XmlResourceParser parser = getResources().getXml(R.xml.addresses);
        
		int event = parser.getEventType();
        boolean isCorrectTag = false;
        String name = "empty";
        String value = "empty";
        
        while (event != XmlPullParser.END_DOCUMENT)   
        {
            event = parser.getEventType();  
  
            if (event == XmlPullParser.START_TAG && parser.getName().contentEquals(tagName))   
            {
            	Log.d("XML", "Start tag: " + parser.getName());
                name = parser.getIdAttribute();
                isCorrectTag = true;  
            }   
            else if (event == XmlPullParser.TEXT && isCorrectTag)  
            {
            	Log.d("XML", "Text found");
            	value = parser.getText();
            	if (value.equals("null")) value = null;
            	Log.d("XML", "put K: " + name + " V: " + value);
            	list.put(name, value);
                isCorrectTag = false;
            }
            parser.next();
        } 
        parser.close();
	}

	private void addItemsOnSpinner(Spinner spinner, HashMap<String, String> map) {
		List<String> list = Collections.list(Collections.enumeration(map.keySet()));
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
			android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(dataAdapter);
	}
	
// ON ACTIVITY RESULT
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("METHOD", "onActivityResult()");
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (data.getExtras().containsKey("calibrated_x")){
				calibrated_x = data.getFloatExtra("calibrated_x", calibrated_x);
			}
			if (data.getExtras().containsKey("calibrated_y")){
				calibrated_y = data.getFloatExtra("calibrated_y", calibrated_y);
			}
		}
	}

// ON RESUME
	@Override
	protected void onResume() {
		Log.d("METHOD", "onResume() MainMenu");
		super.onResume();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
// ON PAUSE
	@Override
	protected void onPause() {
		super.onPause();
		
	}

	
// ON SAVE/RESTORE INSTANCE STATE
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d("METHOD","onSaveInstanceState");
		outState.putInt("spinner_robot_index", ((Spinner)findViewById(R.id.Spinner_robot)).getSelectedItemPosition());
		outState.putInt("spinner_camera_index", ((Spinner)findViewById(R.id.Spinner_camera)).getSelectedItemPosition());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.d("METHOD","onRestoreInstanceState");
		spinner_camera.setSelection(savedInstanceState.getInt("spinner_camera_index"));
		spinner_robot.setSelection(savedInstanceState.getInt("spinner_robot_index"));
	}

// ON CREATE OPTIONS MENU	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}
	
	 @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
		message_title = new String();
		message_content = new String();
		
		switch (item.getItemId())
        {
	        case R.id.menu_about:
	            //Toast.makeText(MainMenu.this, "Help is Selected", Toast.LENGTH_SHORT).show();
	        	Log.d("UI", "About menu selected");
                message_title = this.getResources().getString(R.string.about_message_title);
                message_content = this.getResources().getString(R.string.about_message_content);
                show_message(message_title, message_content);
	            return true;
	        case R.id.menu_help:
	        	Log.d("UI", "Help menu selected");
                message_title = this.getResources().getString(R.string.help_message_title);
                message_content = this.getResources().getString(R.string.help_message_content);
                show_message(message_title, message_content);
	            return true;
	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }

	private void show_message(String message_title, String message_content) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainMenu.this);
		alertDialog.setTitle(message_title);
        alertDialog.setMessage(message_content);
        alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            // User pressed OK button. Write Logic Here
            	Log.d("UI", "OK, Message from menu disposed");
            }
        });
        alertDialog.show();
        Log.d("UI", "Message from menu showed");
	}
}
