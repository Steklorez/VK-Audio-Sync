package com.BBsRs.vkaudiosync.Services;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.BBsRs.vkaudiosync.VKApiThings.Constants;

public class BootUpCatcherReceiver extends BroadcastReceiver {
	
	//preferences 
    SharedPreferences sPref;
    
    String LOG_TAG = "BootReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		//set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sPref.getBoolean(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION, true)){
        	Log.i(LOG_TAG, "start service");
        	context.startService(new Intent(context, AutomaticSynchronizationService.class));
        }	
	}

}
