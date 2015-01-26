package com.BBsRs.vkaudiosync.Fragments;

import java.io.IOException;
import java.util.ArrayList;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.RelativeLayout;
import org.holoeverywhere.widget.TextView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.Adapters.DownloadManagerMusicAdapter;
import com.BBsRs.vkaudiosync.Application.ObjectSerializer;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.BBsRs.vkaudiosync.collection.MusicCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

public class DownloadManagerFragment extends Fragment {
	
	//preferences 
    SharedPreferences sPref;
    
    //music collection
    ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
    
    //android views where shows content
  	ListView listViewMusic;
  	RelativeLayout relativeErrorLayout;
  	TextView errorMessage;
  	Button errorRetryButton;
  	
  	//with this options we will load images
    DisplayImageOptions options ;
    
    //adapter to listview
    DownloadManagerMusicAdapter musicAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState){
		View contentView = inflater.inflate(R.layout.list);
		
		//set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        
        //init all views
	    listViewMusic = (ListView)contentView.findViewById(R.id.listView);
    	relativeErrorLayout = (RelativeLayout)contentView.findViewById(R.id.errorLayout);
    	errorMessage = (TextView)contentView.findViewById(R.id.errorMessage);
    	errorRetryButton = (Button)contentView.findViewById(R.id.errorRetryButton);
    	
    	//clean ab title and subtitle
     	getSupportActionBar().setTitle("");
     	getSupportActionBar().setSubtitle("");
     	
     	//init image loader
        options = new DisplayImageOptions.Builder()
        .showStubImage(R.drawable.ic_simple_music_stub)
        //.showImageForEmptyUri(R.drawable.logo)
        .cacheOnDisc(true)	
        .cacheInMemory(true)					
        .build();
        
		return contentView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getSupportActionBar().setTitle(getResources().getStringArray(R.array.slider_menu)[5]);
		getSupportActionBar().setSubtitle(null);
		
		try {
        	musicCollection = (ArrayList<MusicCollection>) ObjectSerializer.deserialize(sPref.getString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(new ArrayList<MusicCollection>())));
        	if (musicCollection==null)
        		musicCollection = new ArrayList<MusicCollection>();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        musicAdapter = new DownloadManagerMusicAdapter(getActivity(), musicCollection, options);
        listViewMusic.setAdapter(musicAdapter);
        
        //register delete receiver
        getActivity().registerReceiver(someDeleted, new IntentFilter(Constants.SOME_DELETED));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(someDeleted);
	}
	
	private BroadcastReceiver someDeleted = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	
	    	//remove from global download manager
			try {
				ArrayList<MusicCollection> musicCollectionTemp = (ArrayList<MusicCollection>) ObjectSerializer.deserialize(sPref.getString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(new ArrayList<MusicCollection>())));
				if (musicCollectionTemp==null)
            		musicCollectionTemp = new ArrayList<MusicCollection>();
  					int indexTemp=0;
  				for (MusicCollection one: musicCollectionTemp){
  					if (one.aid==((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).aid){
  						musicCollectionTemp.remove(indexTemp);
  						break;
  					}
  					indexTemp++;
  				}
				sPref.edit().putString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(musicCollectionTemp)).commit();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	};

}
