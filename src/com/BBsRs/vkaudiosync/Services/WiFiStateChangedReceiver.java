package com.BBsRs.vkaudiosync.Services;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.BBsRs.vkaudiosync.VKApiThings.Constants;

public class WiFiStateChangedReceiver extends BroadcastReceiver {
	
	String LOG_TAG = "WifiStateChanged";
	
	//preferences 
    SharedPreferences sPref;

	@Override
	public void onReceive(Context context, Intent intent) {
		
		//set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        
        //start aus service only if AUS is enabled by users preferences
        if (sPref.getBoolean(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION, true)){
        	
            SupplicantState supState;
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            supState = wifiInfo.getSupplicantState();

            if (supState.equals(SupplicantState.COMPLETED) && isConnectedViaWifi(context) && !isMyServiceRunning(AutomaticSynchronizationService.class, context)) {
            	Log.i(LOG_TAG, "Wifi is connected start AUS service");
				cancelUpdates(context);
				context.startService(new Intent(context, AutomaticSynchronizationService.class));
            } else {
            	Log.i(LOG_TAG, "Wifi is disconnected or service is already running, nothing todo");
            }
        }
	}
	
    private boolean isConnectedViaWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);     
        return mWifi.isConnected();
   }
	
	private boolean isMyServiceRunning(Class<?> serviceClass, Context context) {			//returns true is service running
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
	
    public static PendingIntent getUpdateIntent(Context context) {
        Intent i = new Intent(context, AutomaticSynchronizationService.class);
        return PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    public static void cancelUpdates(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getUpdateIntent(context));
    }
}
