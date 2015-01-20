package com.BBsRs.vkaudiosync;

import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.BBsRs.vkaudiosync.VKApiThings.Constants;


public class DirChooseActivity extends Activity implements DirectoryChooserFragment.OnFragmentInteractionListener {

    private DirectoryChooserFragment mDialog;
    
  //preferences 
    SharedPreferences sPref;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
        mDialog = DirectoryChooserFragment.newInstance(getResources().getString(R.string.app_name), sPref.getString(Constants.DOWNLOAD_DIRECTORY, android.os.Environment.getExternalStorageDirectory()+"/Music")+"/");
        mDialog.show(getFragmentManager(), null);
    }

    @Override
    public void onSelectDirectory(@NonNull final String path) {
    	sPref.edit().putString(Constants.DOWNLOAD_DIRECTORY, path).commit();
        mDialog.dismiss();
        finish();
    }

    @Override
    public void onCancelChooser() {
        mDialog.dismiss();
        finish();
    }
}
