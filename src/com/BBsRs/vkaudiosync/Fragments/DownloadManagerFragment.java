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

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.Adapters.DownloadManagerMusicAdapter;
import com.BBsRs.vkaudiosync.Application.ObjectSerializer;
import com.BBsRs.vkaudiosync.Services.DownloadService;
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
    
    //menu settings
    Menu mainMenu = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState){
		View contentView = inflater.inflate(R.layout.list);
		
		//set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        
        //enable menu
    	setHasOptionsMenu(true);
        
        //init all views
	    listViewMusic = (ListView)contentView.findViewById(R.id.listView);
    	relativeErrorLayout = (RelativeLayout)contentView.findViewById(R.id.errorLayout);
    	errorMessage = (TextView)contentView.findViewById(R.id.errorMessage);
    	errorRetryButton = (Button)contentView.findViewById(R.id.errorRetryButton);
    	errorRetryButton.setVisibility(View.GONE);
    	
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
        getActivity().registerReceiver(musicDownloaded, new IntentFilter(Constants.MUSIC_DOWNLOADED));
        
        sPref.edit().putBoolean(Constants.OTHER_FRAGMENT, true).commit();
        
        getSupportActionBar().setTitle(getResources().getStringArray(R.array.slider_menu)[5]);
        if (musicCollection.size() == 0){
        	errorMessage.setText(getResources().getString(R.string.message_zero_count_audio_dm));
        	relativeErrorLayout.setVisibility(View.VISIBLE);
        	listViewMusic.setVisibility(View.GONE);
        	getSupportActionBar().setSubtitle(getResources().getString(R.string.message_zero_count_audio_dm));
        } else {
        	relativeErrorLayout.setVisibility(View.GONE);
        	listViewMusic.setVisibility(View.VISIBLE);
        	getSupportActionBar().setSubtitle(musicCollection.size()+" "+getResources().getString(R.string.quan_songs_dm));
        }
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(someDeleted);
		getActivity().unregisterReceiver(musicDownloaded);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.dm_menu, menu);
		mainMenu = menu;
		if (isMyServiceRunning(DownloadService.class)){
			mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download_disabled);
			mainMenu.findItem(R.id.menu_start_download_service).setEnabled(false);
		} 
		return;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	      switch (item.getItemId()) {
	      case android.R.id.home:
	    	  Intent i = new Intent(Constants.OPEN_MENU_DRAWER);
	    	  getActivity().sendBroadcast(i);
	    	  break;
	      case R.id.menu_start_download_service:
	    	  mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download_disabled);
	    	  mainMenu.findItem(R.id.menu_start_download_service).setEnabled(false);
	    	  //start service
	    	  Intent serviceIntent = new Intent(getActivity(), DownloadService.class); 
	    	  getActivity().startService(serviceIntent);
	    	  break;
	      }
		return true;
	}
	
	private BroadcastReceiver musicDownloaded = new BroadcastReceiver() {

	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if (intent.getExtras().getBoolean(Constants.DOWNLOAD_SERVICE_STOPPED)){
	    		if (mainMenu!=null){
	    			mainMenu.findItem(R.id.menu_start_download_service).setEnabled(true);
	    			mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download);
	    		}
	    	} else {
	    		//remove from global download manager
				try {
					ArrayList<MusicCollection> musicCollectionTemp = (ArrayList<MusicCollection>) ObjectSerializer.deserialize(sPref.getString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(new ArrayList<MusicCollection>())));
					if (musicCollectionTemp==null)
	            		musicCollectionTemp = new ArrayList<MusicCollection>();
	  					int indexTemp=0;
	  				for (MusicCollection one: musicCollectionTemp){
	  					if ((one.aid==intent.getExtras().getLong(Constants.MUSIC_AID_DOWNLOADED)) && intent.getExtras().getBoolean(Constants.MUSIC_SUCCESSFULLY_DOWNLOADED)){
	  						musicAdapter.removeItem(indexTemp);
	  						musicCollectionTemp.remove(indexTemp);
	  						break;
	  					}
	  					indexTemp++;
	  				}
	  				
	  				if (musicCollectionTemp.size() == 0){
	  		        	errorMessage.setText(getResources().getString(R.string.message_zero_count_audio_dm));
	  		        	relativeErrorLayout.setVisibility(View.VISIBLE);
	  		        	listViewMusic.setVisibility(View.GONE);
	  		        	getSupportActionBar().setSubtitle(getResources().getString(R.string.message_zero_count_audio_dm));
	  		        } else {
	  		        	relativeErrorLayout.setVisibility(View.GONE);
	  		        	listViewMusic.setVisibility(View.VISIBLE);
	  		        	getSupportActionBar().setSubtitle(musicCollectionTemp.size()+" "+getResources().getString(R.string.quan_songs_dm));
	  		        }
	  				
					sPref.edit().putString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(musicCollectionTemp)).commit();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	}
	    }
	};
	
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
  				
  				if (musicCollectionTemp.size() == 0){
  		        	errorMessage.setText(getResources().getString(R.string.message_zero_count_audio_dm));
  		        	relativeErrorLayout.setVisibility(View.VISIBLE);
  		        	listViewMusic.setVisibility(View.GONE);
  		        	getSupportActionBar().setSubtitle(getResources().getString(R.string.message_zero_count_audio_dm));
  		        } else {
  		        	relativeErrorLayout.setVisibility(View.GONE);
  		        	listViewMusic.setVisibility(View.VISIBLE);
  		        	getSupportActionBar().setSubtitle(musicCollectionTemp.size()+" "+getResources().getString(R.string.quan_songs_dm));
  		        }
  				
				sPref.edit().putString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(musicCollectionTemp)).commit();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	};
	
    private boolean isMyServiceRunning(Class<?> serviceClass) {			//returns true is service running
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
