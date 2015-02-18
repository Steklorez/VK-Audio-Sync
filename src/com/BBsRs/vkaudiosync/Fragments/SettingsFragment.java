package com.BBsRs.vkaudiosync.Fragments;

import org.holoeverywhere.preference.CheckBoxPreference;
import org.holoeverywhere.preference.ListPreference;
import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.Preference.OnPreferenceChangeListener;
import org.holoeverywhere.preference.Preference.OnPreferenceClickListener;
import org.holoeverywhere.preference.PreferenceFragment;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Toast;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StatFs;

import com.BBsRs.vkaudiosync.DirChooseActivity;
import com.BBsRs.vkaudiosync.LoaderActivity;
import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.Services.AutomaticSynchronizationService;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SettingsFragment extends PreferenceFragment {
	
	//preferences 
    SharedPreferences sPref;
    
    Preference chooseDir;
    ListPreference ausFrequency, skipBigSize, skipBigLength, maxSize, reachMaxSize;
    CheckBoxPreference aus, ausWifi, skipBig;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.settings);
        
      //set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        
        chooseDir = (Preference) findPreference ("preference_folder");
        chooseDir.setSummary(sPref.getString(Constants.DOWNLOAD_DIRECTORY, android.os.Environment.getExternalStorageDirectory()+"/Music/"+getString(R.string.app_name))+"/");
        chooseDir.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
				getActivity().startActivity(new Intent(getActivity(), DirChooseActivity.class));
				return false;
			}
        });
        
        Preference clearCache = (Preference) findPreference (Constants.PREFERENCE_IMAGELOADER_CLEAR_CACHE);
        clearCache.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
				try {
				ImageLoader.getInstance().clearMemoryCache();
				ImageLoader.getInstance().clearDiskCache();
				} catch (Exception e){
					e.printStackTrace();
				} finally {
					Toast.makeText(getActivity(), getString(R.string.imageloader_clear_cache_success), Toast.LENGTH_LONG).show();
				}
				return false;
			}
        });
        
        Preference logOut = (Preference) findPreference ("preference_logout");
        logOut.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
				sPref.edit().clear().commit();
				getActivity().startActivity(new Intent(getActivity(), LoaderActivity.class));
				getActivity().finish();
				return false;
			}
        });
        
        ausFrequency = (ListPreference) findPreference(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION_FREQUENCY);
        aus = (CheckBoxPreference) findPreference(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION);
        ausWifi = (CheckBoxPreference) findPreference(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION_WIFI);
        skipBig = (CheckBoxPreference) findPreference(Constants.PREFERENCE_SKIP_BIG);
        skipBigSize = (ListPreference)findPreference(Constants.PREFERENCE_SKIP_BIG_SIZE);
        skipBigLength = (ListPreference)findPreference(Constants.PREFERENCE_SKIP_BIG_LENGTH);
        maxSize = (ListPreference)findPreference(Constants.PREFERENCE_MAX_SIZE);
        reachMaxSize = (ListPreference)findPreference(Constants.PREFERENCE_WHAT_TODO_REACH_MAX_SIZE);
        
        reachMaxSize.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				reachMaxSize.setValue((String) newValue);
				updateViews();
				return false;
			}
        });
        
        maxSize.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				
				StatFs stat = new StatFs(sPref.getString(Constants.DOWNLOAD_DIRECTORY, android.os.Environment.getExternalStorageDirectory()+"/Music/"+getString(R.string.app_name))+"/");
		       	long sdAvailSize = (long)stat.getBlockCount() * (long)stat.getBlockSize();
				
		       	if (Long.valueOf((String) newValue)>=sdAvailSize){
		       		maxSize.setValue("0");
		       		Toast.makeText(getActivity(), String.format(getString(R.string.prefs_max_size_too_huge), (double) sdAvailSize/1024/1024/1024), Toast.LENGTH_LONG).show();
		       	} else { 
		       		maxSize.setValue((String) newValue);
		       	}
				updateViews();
				return false;
			}
        });
        
        skipBig.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				skipBig.setChecked(!skipBig.isChecked());
				updateViews();
				return false;
			}
        });
        
        ausFrequency.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				ausFrequency.setValue((String) newValue);
				updateViews();
				
				//cancel next update event
				cancelUpdates(getActivity());
				
				if (isMyServiceRunning(AutomaticSynchronizationService.class)){
					getActivity().stopService(new Intent(getActivity(), AutomaticSynchronizationService.class));
				}
				getActivity().startService(new Intent(getActivity(), AutomaticSynchronizationService.class));
				return false;
			}
        });
        
        aus.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				aus.setChecked(!aus.isChecked());
				
				updateViews();
				
				//clear queue if we disable this func
				if (!aus.isChecked()){
					sPref.edit().putString(Constants.AUS_MAIN_LIST_BASE, "").commit();
				}
				
				//cancel next update event
				cancelUpdates(getActivity());
				
				if (aus.isChecked()){
					if (!isMyServiceRunning(AutomaticSynchronizationService.class)){
						getActivity().startService(new Intent(getActivity(), AutomaticSynchronizationService.class));
					}
				} else {
					if (isMyServiceRunning(AutomaticSynchronizationService.class)){
						getActivity().stopService(new Intent(getActivity(), AutomaticSynchronizationService.class));
					}
				}
				return false;
			}
        });
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getSupportActionBar().setTitle(getResources().getStringArray(R.array.slider_menu)[6]);
		getSupportActionBar().setSubtitle(null);
		
		sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		chooseDir.setSummary(sPref.getString(Constants.DOWNLOAD_DIRECTORY, android.os.Environment.getExternalStorageDirectory()+"/Music/"+getString(R.string.app_name))+"/");

		updateViews();

		sPref.edit().putBoolean(Constants.OTHER_FRAGMENT, true).commit();
	}
	
	public void updateViews(){
		skipBigSize.setEnabled(sPref.getBoolean(Constants.PREFERENCE_SKIP_BIG, true));
		skipBigLength.setEnabled(sPref.getBoolean(Constants.PREFERENCE_SKIP_BIG, true));
		aus.setSummary(sPref.getBoolean(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION, true) ? getString(R.string.prefs_aus_summary_enabled) : getString(R.string.prefs_aus_summary));
		ausFrequency.setEnabled(sPref.getBoolean(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION, true));
		ausWifi.setEnabled(sPref.getBoolean(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION, true));
		int index=0;
		for (String summaryValue : getActivity().getResources().getStringArray(R.array.prefs_aus_freq_entry_values)){
			if (summaryValue.equals(sPref.getString(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION_FREQUENCY, getString(R.string.prefs_aus_freq_default_value)))){
				ausFrequency.setSummary(getActivity().getResources().getStringArray(R.array.prefs_aus_freq_values)[index]);
				break;
			}
			index++;
		}
		
		index=0;
		for (String summaryValue : getActivity().getResources().getStringArray(R.array.prefs_skip_big_size_entry_values)){
			if (summaryValue.equals(sPref.getString(Constants.PREFERENCE_SKIP_BIG_SIZE, getString(R.string.prefs_skip_big_size_default_value)))){
				skipBigSize.setSummary(getActivity().getResources().getStringArray(R.array.prefs_skip_big_size_values)[index]);
				break;
			}
			index++;
		}
		
		index=0;
		for (String summaryValue : getActivity().getResources().getStringArray(R.array.prefs_skip_big_length_entry_values)){
			if (summaryValue.equals(sPref.getString(Constants.PREFERENCE_SKIP_BIG_LENGTH, getString(R.string.prefs_skip_big_length_default_value)))){
				skipBigLength.setSummary(getActivity().getResources().getStringArray(R.array.prefs_skip_big_length_values)[index]);
				break;
			}
			index++;
		}
		
		index=0;
		for (String summaryValue : getActivity().getResources().getStringArray(R.array.prefs_max_size_entry_values)){
			if (summaryValue.equals(sPref.getString(Constants.PREFERENCE_MAX_SIZE, getString(R.string.prefs_max_size_default_value)))){
				maxSize.setSummary(getActivity().getResources().getStringArray(R.array.prefs_max_size_values)[index]);
				break;
			}
			index++;
		}
		
		index=0;
		for (String summaryValue : getActivity().getResources().getStringArray(R.array.prefs_what_todo_reach_max_size_entry_values)){
			if (summaryValue.equals(sPref.getString(Constants.PREFERENCE_WHAT_TODO_REACH_MAX_SIZE, getString(R.string.prefs_what_todo_reach_max_size_default_value)))){
				reachMaxSize.setSummary(getActivity().getResources().getStringArray(R.array.prefs_what_todo_reach_max_size_values)[index]);
				break;
			}
			index++;
		}
	}
	
    private boolean isMyServiceRunning(Class<?> serviceClass) {			//returns true is service running
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
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
