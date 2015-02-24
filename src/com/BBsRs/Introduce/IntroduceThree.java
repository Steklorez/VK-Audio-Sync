package com.BBsRs.Introduce;

import java.util.ArrayList;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.RadioButton;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.StatFs;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.BBsRs.vkaudiosync.DirChooseActivity;
import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;

public class IntroduceThree extends Activity {

	//preferences 
    SharedPreferences sPref;
    
    TextView directoryText;
    
    ArrayList <RadioButton> radios = new ArrayList <RadioButton>();
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    if(getResources().getBoolean(R.bool.portrait_only)){
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    }
	    
	    this.setContentView(R.layout.intro_three);
	    
	    //set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
	    
	    Button next = (Button)this.findViewById(R.id.next);
	    Button back = (Button)this.findViewById(R.id.back);
	    
	    next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), IntroduceFour.class));
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				finish();
			}
		});
	    
	    back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), IntroduceTwo.class));
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				finish();
			}
		});
	    radios.add((RadioButton)this.findViewById(R.id.radioButton1));
	    radios.add((RadioButton)this.findViewById(R.id.radioButton2));
	    radios.add((RadioButton)this.findViewById(R.id.radioButton3));
	    radios.add((RadioButton)this.findViewById(R.id.radioButton4));
	    radios.add((RadioButton)this.findViewById(R.id.radioButton5));
	    radios.add((RadioButton)this.findViewById(R.id.radioButton6));
	    
	    Button directory = (Button)this.findViewById(R.id.directory);
	    directoryText = (TextView)this.findViewById(R.id.directory_text);
	    
	    updateViews();
	    
	    for (final RadioButton one : radios){
	    	one.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (isChecked){
						int index=0;
						for (String summaryValue : getApplicationContext().getResources().getStringArray(R.array.prefs_max_size_values)){
							if (summaryValue.equals(String.valueOf(one.getText()))){
								break;
							}
							index++;
						}
						
						try {
							StatFs stat = new StatFs(sPref.getString(Constants.DOWNLOAD_DIRECTORY, android.os.Environment.getExternalStorageDirectory()+"/Music/"+getString(R.string.app_name))+"/");
							long sdAvailSize = (long)stat.getBlockCount() * (long)stat.getBlockSize();
					
							if (Long.valueOf(getResources().getStringArray(R.array.prefs_max_size_entry_values)[index])>=sdAvailSize){
								sPref.edit().putString(Constants.PREFERENCE_MAX_SIZE, "0").commit();
								Toast.makeText(getApplicationContext(), String.format(getString(R.string.prefs_max_size_too_huge), (double) sdAvailSize/1024/1024/1024), Toast.LENGTH_LONG).show();
							} else { 
								sPref.edit().putString(Constants.PREFERENCE_MAX_SIZE, getResources().getStringArray(R.array.prefs_max_size_entry_values)[index]).commit();
							}
						} catch (Exception e){
							e.printStackTrace();
							sPref.edit().putString(Constants.PREFERENCE_MAX_SIZE, getResources().getStringArray(R.array.prefs_max_size_entry_values)[index]).commit();
						}
			       		updateViews();
					}
				}
	    	});
		}
	    
	    directory.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), DirChooseActivity.class));
			}
		});
	}
	
	public void updateViews(){
		int index=0;
		for (String summaryValue : getApplicationContext().getResources().getStringArray(R.array.prefs_max_size_entry_values)){
			if (summaryValue.equals(sPref.getString(Constants.PREFERENCE_MAX_SIZE, getString(R.string.prefs_max_size_default_value)))){
				break;
			}
			index++;
		}
		
		int secIndex=0;
		for (RadioButton one : radios){
			if (secIndex!=index)
				one.setChecked(false);
			else 
				one.setChecked(true);
			secIndex++;
		}
		
		directoryText.setText(sPref.getString(Constants.DOWNLOAD_DIRECTORY, android.os.Environment.getExternalStorageDirectory()+"/Music/"+getString(R.string.app_name))+"/");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateViews();
	}

}
