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
import org.holoeverywhere.widget.Toast;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

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
        getActivity().registerReceiver(musicPercentageChanged, new IntentFilter(Constants.MUSIC_PERCENTAGE_CHANGED));
        
        sPref.edit().putBoolean(Constants.OTHER_FRAGMENT, true).commit();
        
        getSupportActionBar().setTitle(getResources().getStringArray(R.array.slider_menu)[5]);
        getSupportActionBar().setSubtitle(getResources().getString(R.string.quan_songs_dm)+" "+musicCollection.size());
        if (musicCollection.size() == 0){
        	errorMessage.setText(getResources().getString(R.string.message_zero_count_audio_dm));
        	relativeErrorLayout.setVisibility(View.VISIBLE);
        	listViewMusic.setVisibility(View.GONE);
        	if (mainMenu != null){
        	mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download_disabled);
	    	mainMenu.findItem(R.id.menu_start_download_service).setEnabled(false);
        	}
        } else {
        	relativeErrorLayout.setVisibility(View.GONE);
        	listViewMusic.setVisibility(View.VISIBLE);
        	if (mainMenu != null && !(isMyServiceRunning(DownloadService.class))){
        		mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download);
        		mainMenu.findItem(R.id.menu_start_download_service).setEnabled(true);
        	}
        }
		
        //set pause on scroll and etc for imageloader
        listViewMusic.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), sPref.getBoolean(Constants.PREFERENCE_IMAGELOADER_PAUSE_ON_SCROLL_KEY, true), sPref.getBoolean(Constants.PREFERENCE_IMAGELOADER_PAUSE_ON_FLING_KEY, true)));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(someDeleted);
		getActivity().unregisterReceiver(musicDownloaded);
		getActivity().unregisterReceiver(musicPercentageChanged);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.dm_menu, menu);
		mainMenu = menu;
		if (isMyServiceRunning(DownloadService.class)){
			mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download_disabled);
			mainMenu.findItem(R.id.menu_start_download_service).setEnabled(false);
		} else {
			if (musicCollection.size() != 0){
				mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download);
				mainMenu.findItem(R.id.menu_start_download_service).setEnabled(true);
			} else {
				mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download_disabled);
				mainMenu.findItem(R.id.menu_start_download_service).setEnabled(false);
			}
		}
		return;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
	      switch (item.getItemId()) {
	      case android.R.id.home:
	    	  i = new Intent(Constants.OPEN_MENU_DRAWER);
	    	  getActivity().sendBroadcast(i);
	    	  break;
	      case R.id.submenu:
	    	  if (musicCollection.size() == 0){
	    		  mainMenu.findItem(R.id.menu_delete_all).setEnabled(false);
	    	  } else {
	    		  mainMenu.findItem(R.id.menu_delete_all).setEnabled(true);
	    	  }
	    	  
	    	  if (isMyServiceRunning(DownloadService.class)){
	    		  mainMenu.findItem(R.id.menu_stop_dm).setEnabled(true);
	    	  } else {
	    		  mainMenu.findItem(R.id.menu_stop_dm).setEnabled(false);
	    	  }
	    	  break;
	      case R.id.menu_start_download_service:
	    	  mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download_disabled);
	    	  mainMenu.findItem(R.id.menu_start_download_service).setEnabled(false);
	    	  
	    	  if (!isMyServiceRunning(DownloadService.class)){
	    		  //start service
	    		  Intent serviceIntent = new Intent(getActivity(), DownloadService.class); 
	    		  getActivity().startService(serviceIntent);
	    	  }
	    	  break;
	      case R.id.menu_delete_all:
	    	  if (musicCollection.size() == 0){
	    		  Toast.makeText(getActivity(), getResources().getString(R.string.delete_all_msg), Toast.LENGTH_LONG).show();
	    	  } else {
	    		  //clean download manager
	    		  for (MusicCollection oneItem : musicCollection){
					i = new Intent(Constants.SOME_DELETED);
					i.putExtra(Constants.ONE_AUDIO_ITEM, (Parcelable)oneItem);
					getActivity().sendBroadcast(i);
	    		  }
	    		  sPref.edit().putString(Constants.DOWNLOAD_SELECTION, "").commit();
	  		  	  this.onResume();
	    	  }
	    	  break;
	      case R.id.menu_stop_dm:
	    	  if (isMyServiceRunning(DownloadService.class)){
	    		  getActivity().stopService(new Intent(getActivity(), DownloadService.class));
	    	  }
	    	  else {
	    		  Toast.makeText(getActivity(), getResources().getString(R.string.stop_dm_msg), Toast.LENGTH_LONG).show();
	    	  }
	    	  mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download);
	    	  mainMenu.findItem(R.id.menu_start_download_service).setEnabled(true);
	    	  break;
	      }
		return true;
	}
	
	private BroadcastReceiver musicDownloaded = new BroadcastReceiver() {

	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if (intent.getExtras().getBoolean(Constants.DOWNLOAD_SERVICE_STOPPED)){
	    		ArrayList<MusicCollection> musicCollectionTemp = musicAdapter.musicCollection;
				if (musicCollectionTemp==null)
            		musicCollectionTemp = new ArrayList<MusicCollection>();
				
				getSupportActionBar().setSubtitle(getResources().getString(R.string.quan_songs_dm)+" "+musicCollectionTemp.size());
				if (musicCollectionTemp.size() == 0){
  		        	errorMessage.setText(getResources().getString(R.string.message_zero_count_audio_dm));
  		        	relativeErrorLayout.setVisibility(View.VISIBLE);
  		        	listViewMusic.setVisibility(View.GONE);
  		        	if (mainMenu != null){
  		          	mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download_disabled);
  		  	    	mainMenu.findItem(R.id.menu_start_download_service).setEnabled(false);
  		          	}
  		        } else {
  		        	relativeErrorLayout.setVisibility(View.GONE);
  		        	listViewMusic.setVisibility(View.VISIBLE);
  		        	if (mainMenu != null && !(isMyServiceRunning(DownloadService.class))){
  		          	mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download);
  		  	    	mainMenu.findItem(R.id.menu_start_download_service).setEnabled(true);
  		          	}
  		        }
	    	} else {
	    		//remove from adapter
				try {
					ArrayList<MusicCollection> musicCollectionTemp = musicAdapter.musicCollection;
					if (musicCollectionTemp==null)
	            		musicCollectionTemp = new ArrayList<MusicCollection>();
	  					int indexTemp=0;
	  				for (MusicCollection one: musicCollectionTemp){
	  					if ((one.aid==((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).aid || (one.title.equals(((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).title) && one.artist.equals(((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).artist))) && intent.getExtras().getBoolean(Constants.MUSIC_SUCCESSFULLY_DOWNLOADED)){
	  						musicAdapter.removeItem(indexTemp);
	  						break;
	  					}
	  					indexTemp++;
	  				}
	  				
	  				getSupportActionBar().setSubtitle(getResources().getString(R.string.quan_songs_dm)+" "+musicCollectionTemp.size());
	  				if (musicCollectionTemp.size() == 0){
	  		        	errorMessage.setText(getResources().getString(R.string.message_zero_count_audio_dm));
	  		        	relativeErrorLayout.setVisibility(View.VISIBLE);
	  		        	listViewMusic.setVisibility(View.GONE);
	  		        	if (mainMenu != null){
	  		          	mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download_disabled);
	  		  	    	mainMenu.findItem(R.id.menu_start_download_service).setEnabled(false);
	  		          	}
	  		        } else {
	  		        	relativeErrorLayout.setVisibility(View.GONE);
	  		        	listViewMusic.setVisibility(View.VISIBLE);
	  		        	if (mainMenu != null && !(isMyServiceRunning(DownloadService.class))){
	  		          	mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download);
	  		  	    	mainMenu.findItem(R.id.menu_start_download_service).setEnabled(true);
	  		          	}
	  		        }
				} catch (Exception e) {
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
  					if ((one.aid==((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).aid) || (one.artist.equals(((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).artist) && one.title.equals(((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).title))){
  						musicCollectionTemp.remove(indexTemp);
  						break;
  					}
  					indexTemp++;
  				}
  				
  				getSupportActionBar().setSubtitle(getResources().getString(R.string.quan_songs_dm)+" "+musicCollectionTemp.size());
  				if (musicCollectionTemp.size() == 0){
  		        	errorMessage.setText(getResources().getString(R.string.message_zero_count_audio_dm));
  		        	relativeErrorLayout.setVisibility(View.VISIBLE);
  		        	listViewMusic.setVisibility(View.GONE);
  		        	if (mainMenu != null){
  		          	mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download_disabled);
  		  	    	mainMenu.findItem(R.id.menu_start_download_service).setEnabled(false);
  		          	}
  		        } else {
  		        	relativeErrorLayout.setVisibility(View.GONE);
  		        	listViewMusic.setVisibility(View.VISIBLE);
  		        	if (mainMenu != null && !(isMyServiceRunning(DownloadService.class))){
  		          	mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download);
  		  	    	mainMenu.findItem(R.id.menu_start_download_service).setEnabled(true);
  		          	}
  		        }
  				
				sPref.edit().putString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(musicCollectionTemp)).commit();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	};
	
	private BroadcastReceiver musicPercentageChanged = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	int index=0;
	    	if (musicAdapter !=null)
	    	for (MusicCollection oneItem : musicAdapter.musicCollection){
	    		if ((oneItem.aid==((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).aid) || (oneItem.artist.equals(((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).artist) && oneItem.title.equals(((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).title))){
	    			musicAdapter.getItem(index).percentage=((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).percentage;
	    			musicAdapter.notifyDataSetChanged();
	    			break;
	    		}
	    		index++;
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
