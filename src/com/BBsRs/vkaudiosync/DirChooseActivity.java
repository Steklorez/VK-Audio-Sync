package com.BBsRs.vkaudiosync;

import java.io.File;

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
        
        File root;
        try {
        	//create dir if still doesnt exist
        	root = new File (sPref.getString(Constants.DOWNLOAD_DIRECTORY, android.os.Environment.getExternalStorageDirectory()+"/Music/"+getString(R.string.app_name))+"/");               
        	if(root.exists()==false) {
        		root.mkdirs();
        	}
        } catch(Exception e) {
        	e.printStackTrace();
        	root = new File(android.os.Environment.getRootDirectory()+"/");
        }
        
        mDialog = DirectoryChooserFragment.newInstance(getResources().getString(R.string.app_name), root.exists() ? root.getAbsolutePath() : "/");
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
