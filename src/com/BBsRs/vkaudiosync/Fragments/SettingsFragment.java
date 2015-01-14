package com.BBsRs.vkaudiosync.Fragments;

import org.holoeverywhere.preference.PreferenceFragment;

import android.os.Bundle;

import com.BBsRs.vkaudiosync.R;

public class SettingsFragment extends PreferenceFragment {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.settings);
	}

}
