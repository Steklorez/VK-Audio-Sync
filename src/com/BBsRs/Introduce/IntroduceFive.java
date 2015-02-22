package com.BBsRs.Introduce;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.AdapterView;
import org.holoeverywhere.widget.AdapterView.OnItemSelectedListener;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.CheckBox;
import org.holoeverywhere.widget.Spinner;
import org.holoeverywhere.widget.Toast;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.BBsRs.vkaudiosync.LoaderActivity;
import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.VKApiThings.Account;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.perm.kate.api.Api;

public class IntroduceFive extends Activity {

	//preferences 
    SharedPreferences sPref;
    
    CheckBox aus, ausWifi, joinGroup;
    Spinner frequency;
    
    int check=0;
    
    private final Handler handler = new Handler();
    
    /*----------------------------VK API-----------------------------*/
    Account account=new Account();
    Api api;
    /*----------------------------VK API-----------------------------*/

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    if(getResources().getBoolean(R.bool.portrait_only)){
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    }
	    
	    this.setContentView(R.layout.intro_five);
	    
	    //set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        
        /*----------------------------VK API-----------------------------*/
    	//retrieve old session
        account.restore(getApplicationContext());
        
        //create new session
        if(account.access_token!=null)
            api=new Api(account.access_token, Constants.API_ID);
        /*----------------------------VK API-----------------------------*/
	    
	    Button next = (Button)this.findViewById(R.id.next);
	    Button back = (Button)this.findViewById(R.id.back);
	    joinGroup = (CheckBox)this.findViewById(R.id.joinGroup);
	    
	    next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//join group
				if (joinGroup.isChecked()){
					sPref.edit().putBoolean(Constants.CLICKED_SUBSCRIBE, true).commit();
					
					new Thread (new Runnable(){
						@Override
						public void run() {
							try {
								api.joinGroup(Constants.GROUP_ID, null, null);
								handler.post(new Runnable (){
									@Override
									public void run() {
										Toast.makeText(getApplicationContext(), getString(R.string.join_group_success), Toast.LENGTH_LONG).show();
									}
								});
							} catch (Exception e) {
								handler.post(new Runnable (){
									@Override
									public void run() {
										Toast.makeText(getApplicationContext(), getString(R.string.error_message), Toast.LENGTH_LONG).show();
									}
								});
								e.printStackTrace();
							}
						}
					}).start();
				} 
				
				startActivity(new Intent(getApplicationContext(), LoaderActivity.class));
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
				finish();
			}
		});
	    
	    back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), IntroduceFour.class));
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				finish();
			}
		});
	    
	    aus = (CheckBox)this.findViewById(R.id.aus);
	    ausWifi = (CheckBox)this.findViewById(R.id.ausWifi);
	    frequency = (Spinner)this.findViewById(R.id.frequency);
	    
	    updateViews();
	    
	    aus.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				sPref.edit().putBoolean(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION, isChecked).commit();
				updateViews();
			}
	    });
	    
	    ausWifi.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				sPref.edit().putBoolean(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION_WIFI, isChecked).commit();
				updateViews();
			}
	    });
	    
	    frequency.setOnItemSelectedListener(new OnItemSelectedListener(){    
	    	@Override
	    	public void onItemSelected(AdapterView adapter, View v, int i, long lng) {
	    		check++;
	    		if(check>1){
	    			sPref.edit().putString(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION_FREQUENCY, getApplicationContext().getResources().getStringArray(R.array.prefs_aus_freq_entry_values)[i]).commit();
	    			updateViews();
	    		}
	    	} 
	    	@Override     
	    	public void onNothingSelected(AdapterView<?> parentView) {}
	    }); 
	}
	
	public void updateViews(){
		aus.setChecked(sPref.getBoolean(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION, true));
		ausWifi.setChecked(sPref.getBoolean(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION_WIFI, true));
		
		int index=0;
		for (String summaryValue : getApplicationContext().getResources().getStringArray(R.array.prefs_aus_freq_entry_values)){
			if (summaryValue.equals(sPref.getString(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION_FREQUENCY, getString(R.string.prefs_aus_freq_default_value)))){
				frequency.setSelection(index);
				break;
			}
			index++;
		}
		
		ausWifi.setEnabled(sPref.getBoolean(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION, true));
		frequency.setEnabled(sPref.getBoolean(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION, true));
	}

}
