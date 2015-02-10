package com.BBsRs.vkaudiosync.Fragments;

import org.holoeverywhere.preference.CheckBoxPreference;
import org.holoeverywhere.preference.ListPreference;
import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.Preference.OnPreferenceClickListener;
import org.holoeverywhere.preference.PreferenceFragment;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Toast;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.BBsRs.vkaudiosync.DirChooseActivity;
import com.BBsRs.vkaudiosync.LoaderActivity;
import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SettingsFragment extends PreferenceFragment {
	
	//preferences 
    SharedPreferences sPref;
    
    Preference chooseDir;
    ListPreference ausFrequency;
    CheckBoxPreference aus;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.settings);
        
      //set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        
        chooseDir = (Preference) findPreference ("preference_folder");
        chooseDir.setSummary(sPref.getString(Constants.DOWNLOAD_DIRECTORY, android.os.Environment.getExternalStorageDirectory()+"/Music")+"/");
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
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getSupportActionBar().setTitle(getResources().getStringArray(R.array.slider_menu)[6]);
		getSupportActionBar().setSubtitle(null);
		
		sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		chooseDir.setSummary(sPref.getString(Constants.DOWNLOAD_DIRECTORY, android.os.Environment.getExternalStorageDirectory()+"/Music")+"/");

		int index=0;
		for (String summaryValue : getActivity().getResources().getStringArray(R.array.prefs_aus_freq_entry_values)){
			if (summaryValue.equals(sPref.getString(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION_FREQUENCY, getString(R.string.prefs_aus_freq_default_value)))){
				ausFrequency.setSummary(getActivity().getResources().getStringArray(R.array.prefs_aus_freq_values)[index]);
				break;
			}
			index++;
		}

		sPref.edit().putBoolean(Constants.OTHER_FRAGMENT, true).commit();
	}
	

}
