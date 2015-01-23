package com.BBsRs.vkaudiosync.Fragments;

import java.io.IOException;
import java.util.ArrayList;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import com.BBsRs.vkaudiosync.Application.ObjectSerializer;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.BBsRs.vkaudiosync.collection.MusicCollection;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class DownloadManagerFragment extends Fragment {
	
	//preferences 
    SharedPreferences sPref;
    
    //music collection
    ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState){
		//set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        
        try {
        	musicCollection = (ArrayList<MusicCollection>) ObjectSerializer.deserialize(sPref.getString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(new ArrayList<MusicCollection>())));
        	if (musicCollection==null)
        		musicCollection = new ArrayList<MusicCollection>();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        for (MusicCollection one : musicCollection){
        	Log.i("ewewe", one.artist+" "+one.title);
        }
		return null;
	}

}
