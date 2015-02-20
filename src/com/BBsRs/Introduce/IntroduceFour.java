package com.BBsRs.Introduce;

import java.util.ArrayList;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.RadioButton;
import org.holoeverywhere.widget.TextView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;

public class IntroduceFour extends Activity {

	//preferences 
    SharedPreferences sPref;
    
    TextView questionText;
    
    ArrayList <RadioButton> radios = new ArrayList <RadioButton>();
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    if(getResources().getBoolean(R.bool.portrait_only)){
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    }
	    
	    this.setContentView(R.layout.intro_four);
	    
	    //set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
	    
	    Button next = (Button)this.findViewById(R.id.next);
	    Button back = (Button)this.findViewById(R.id.back);
	    
	    next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), IntroduceFive.class));
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				finish();
			}
		});
	    
	    back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), IntroduceThree.class));
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				finish();
			}
		});
	    radios.add((RadioButton)this.findViewById(R.id.radioButton1));
	    radios.add((RadioButton)this.findViewById(R.id.radioButton2));
	    
	    questionText = (TextView)this.findViewById(R.id.text_question_w);
	    
	    updateViews();
	    
	    for (final RadioButton one : radios){
	    	one.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (isChecked){
						int index=0;
						for (String summaryValue : getApplicationContext().getResources().getStringArray(R.array.prefs_what_todo_reach_max_size_values)){
							if (summaryValue.equals(String.valueOf(one.getText()))){
								break;
							}
							index++;
						}
					
			       		sPref.edit().putString(Constants.PREFERENCE_WHAT_TODO_REACH_MAX_SIZE, getResources().getStringArray(R.array.prefs_what_todo_reach_max_size_entry_values)[index]).commit();
			       		
			       		updateViews();
					}
				}
	    	});
		}
	}
	
	public void updateViews(){
		int index=0;
		for (String summaryValue : getApplicationContext().getResources().getStringArray(R.array.prefs_what_todo_reach_max_size_entry_values)){
			if (summaryValue.equals(sPref.getString(Constants.PREFERENCE_WHAT_TODO_REACH_MAX_SIZE, getString(R.string.prefs_what_todo_reach_max_size_default_value)))){
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
		
		if (sPref.getString(Constants.PREFERENCE_MAX_SIZE, getString(R.string.prefs_max_size_default_value)).equals("0"))
			questionText.setText(getString(R.string.intro_what_todo_unlimited));
		else {
			index=0;
			for (String summaryValue : getApplicationContext().getResources().getStringArray(R.array.prefs_max_size_entry_values)){
				if (summaryValue.equals(sPref.getString(Constants.PREFERENCE_MAX_SIZE, getString(R.string.prefs_max_size_default_value)))){
					questionText.setText(String.format(getString(R.string.intro_what_todo_sized), getApplicationContext().getResources().getStringArray(R.array.prefs_max_size_values)[index]));
					break;
				}
				index++;
			}
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateViews();
	}

}
