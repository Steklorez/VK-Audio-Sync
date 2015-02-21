
package com.BBsRs.vkaudiosync.Fragments;

import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.Preference.OnPreferenceClickListener;
import org.holoeverywhere.preference.PreferenceFragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.BBsRs.vkaudiosync.R;

public class AboutFragment extends PreferenceFragment {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about);
        
        Preference myPref = (Preference) findPreference("open_vk");
		myPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri
						.parse(getResources().getString(R.string.contacts_vk_url)));
				startActivity(intent);
				// open browser or intent here
				return false;
			}
		});
		
        Preference myPref2 = (Preference) findPreference("open_gmail");
		myPref2.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri
						.parse(getResources().getString(R.string.contacts_gmail_url)));
				startActivity(intent);
				// open browser or intent here
				return false;
			}
		});
		
        Preference myPref3 = (Preference) findPreference("open_github");
		myPref3.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri
						.parse(getResources().getString(R.string.open_source_base_url)));
				startActivity(intent);
				// open browser or intent here
				return false;
			}
		});
		
        Preference myPref4 = (Preference) findPreference("open_group");
		myPref4.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri
						.parse(getResources().getString(R.string.group_vk_url)));
				startActivity(intent);
				// open browser or intent here
				return false;
			}
		});
    }

    @Override
    public void onResume() {
        super.onResume();
		getSupportActionBar().setTitle(getResources().getStringArray(R.array.slider_menu)[7]);
		getSupportActionBar().setSubtitle(null);
    }
}
