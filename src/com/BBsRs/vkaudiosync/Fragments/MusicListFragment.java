package com.BBsRs.vkaudiosync.Fragments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.RelativeLayout;
import org.holoeverywhere.widget.TextView;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.Adapters.MusicAdapter;
import com.BBsRs.vkaudiosync.Services.DownloadService;
import com.BBsRs.vkaudiosync.VKApiThings.Account;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.BBsRs.vkaudiosync.collection.MusicCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.perm.kate.api.Api;
import com.perm.kate.api.Audio;
import com.perm.kate.api.Group;
import com.perm.kate.api.User;

public class MusicListFragment extends Fragment {
	
	//android views where shows content
	private PullToRefreshLayout mPullToRefreshLayout;
	ListView listViewMusic;
	RelativeLayout relativeErrorLayout;
	TextView errorMessage;
	Button errorRetryButton;
	
    //custom refresh listener where in new thread will load job doing, need to customize for all kind of data
    CustomOnRefreshListener customOnRefreshListener = new CustomOnRefreshListener();
    
    /*----------------------------VK API-----------------------------*/
    Account account=new Account();
    Api api;
    /*----------------------------VK API-----------------------------*/
    
    //user name
    String PlaceName = "";
    
    //with this options we will load images
    DisplayImageOptions options ;
    
    //music collection
    ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
    
    //menu settings
    Menu mainMenu = null;
    
    //adapter to listview
    MusicAdapter musicAdapter;
    
    //flag for error
    boolean error=false;
    
    //LOG_TAG for log
    String LOG_TAG = "MusicListActivity";
    
    //with this file we check if music file already exist
    File f;
    
    //for retrieve data from activity
    Bundle bundle;

	
	@SuppressWarnings("deprecation")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.list);
		
		//retrieve bundle
		bundle = this.getArguments();
		
		//enable menu
    	setHasOptionsMenu(true);
		
		//init all views
	    listViewMusic = (ListView)contentView.findViewById(R.id.listView);
	    mPullToRefreshLayout = (PullToRefreshLayout)contentView.findViewById(R.id.ptr_layout);
    	relativeErrorLayout = (RelativeLayout)contentView.findViewById(R.id.errorLayout);
    	errorMessage = (TextView)contentView.findViewById(R.id.errorMessage);
    	errorRetryButton = (Button)contentView.findViewById(R.id.errorRetryButton);
    	
    	/*----------------------------VK API-----------------------------*/
    	//retrieve old session
        account.restore(getActivity());
        
        //create new session
        if(account.access_token!=null)
            api=new Api(account.access_token, Constants.API_ID);
        /*----------------------------VK API-----------------------------*/
        
        //init pull to refresh module
        ActionBarPullToRefresh.from(getActivity())
          .allChildrenArePullable()
          .listener(customOnRefreshListener)
          .setup(mPullToRefreshLayout);
        
        //init image loader
        options = new DisplayImageOptions.Builder()
        .showStubImage(R.drawable.ic_music_stub)
        //.showImageForEmptyUri(R.drawable.logo)
        .cacheOnDisc(true)	
        .cacheInMemory(true)					
        .build();
        
        //refresh on open to load data when app first time started
	    if(savedInstanceState == null) {
	    	 mPullToRefreshLayout.setRefreshing(true);
	         customOnRefreshListener.onRefreshStarted(null);
	      // set action bar
         	getSupportActionBar().setTitle("");
         	getSupportActionBar().setSubtitle("");
	    }
	    else{
	    	musicCollection = savedInstanceState.getParcelableArrayList("musicCollection");
	    	error = savedInstanceState.getBoolean("error");
	    	if ((musicCollection.size()>1)) {
	    		musicAdapter = new MusicAdapter(getActivity(), musicCollection, options);
	    		
	    		PlaceName = savedInstanceState.getString("PlaceName");
                
                // set action bar
            	getSupportActionBar().setTitle(PlaceName);
            	getSupportActionBar().setSubtitle(musicCollection.size()+" "+getResources().getString(R.string.quan_songs));
                
                listViewMusic.setAdapter(musicAdapter);
                listViewMusic.setSelection(savedInstanceState.getInt("posX"));
	    	}
	    	
	    	else {
	    		mPullToRefreshLayout.setRefreshing(true);
	         	customOnRefreshListener.onRefreshStarted(null);	
	         	 // set action bar
	         	getSupportActionBar().setTitle("");
	         	getSupportActionBar().setSubtitle("");
	    	}
	    }
	    
	    //programing error button
        errorRetryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPullToRefreshLayout.setRefreshing(true);
		        customOnRefreshListener.onRefreshStarted(null);
		        errorRetryButton.setEnabled(false);
		        // set action bar
	         	getSupportActionBar().setTitle("");
	         	getSupportActionBar().setSubtitle("");
			}
		});
        
		return contentView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (musicCollection !=null && listViewMusic!=null && PlaceName != null){
		 outState.putParcelableArrayList("musicCollection", musicCollection);
		 outState.putInt("posX",  listViewMusic.getFirstVisiblePosition());
		 outState.putString("PlaceName",  PlaceName);
		 outState.putBoolean("error", error);
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main, menu);
		mainMenu = menu;
		if (isMyServiceRunning(DownloadService.class)){
			mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download_disabled);
			mainMenu.findItem(R.id.menu_start_download_service).setEnabled(false);
			mPullToRefreshLayout.setRefreshing(true);
		} 
		return;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	      switch (item.getItemId()) {
	      case R.id.menu_start_download_service:
	    	  //start task animation
	    	  mPullToRefreshLayout.setRefreshing(true);
	    	  //disable this menu button
	    	  item.setEnabled(false); 
	    	  item.setIcon(R.drawable.ic_menu_download_disabled);
	    	  //disable pull to refresh
	    	  //start service
	    	  Intent serviceIntent = new Intent(getActivity(), DownloadService.class); 
	    	  serviceIntent.putExtra("musicCollection", musicCollection);
	    	  getActivity().startService(serviceIntent);
	    	  break;
	      }
		return true;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(musicDownloaded);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		//turn up download receiver
        getActivity().registerReceiver(musicDownloaded, new IntentFilter("DOWNLOADED"));
	}
	
	private BroadcastReceiver musicDownloaded = new BroadcastReceiver() {

	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if (intent.getExtras().getBoolean("service_stopped")){
	    		//stop task animation
	    		mPullToRefreshLayout.setRefreshing(false);
	    		if (mainMenu!=null){
	    			mainMenu.findItem(R.id.menu_start_download_service).setEnabled(true);
	    			mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download);
	    		}
	    	} else {
	    		if (musicAdapter!=null){
	    			musicAdapter.getItem(intent.getExtras().getInt("index")).checked = intent.getExtras().getBoolean("successfully") ? 1 : 0;
	    			musicAdapter.getItem(intent.getExtras().getInt("index")).exist = intent.getExtras().getBoolean("successfully") ? 1 : 0;
	    			musicAdapter.notifyDataSetChanged();
	    		}
	    		if (musicCollection!=null){
	    			musicCollection.get(intent.getExtras().getInt("index")).checked = intent.getExtras().getBoolean("successfully") ? 1 : 0;
	    			musicCollection.get(intent.getExtras().getInt("index")).exist = intent.getExtras().getBoolean("successfully") ? 1 : 0;
	    		}
	    	}
	    }
	};
	
    public class  CustomOnRefreshListener implements OnRefreshListener{

		@Override
		public void onRefreshStarted(View view) {
			// TODO Auto-generated method stub
			new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                	if (bundle != null && ((mainMenu != null && mainMenu.findItem(R.id.menu_start_download_service).isEnabled()) || mainMenu == null)){
                            try {
                            	error=true;
                            	musicCollection = new ArrayList<MusicCollection>();
                            	ArrayList<Audio> musicList = new ArrayList<Audio>();
                            	
                            	switch (bundle.getInt(Constants.BUNDLE_MUSIC_TYPE)){
                            	case Constants.MAIN_MUSIC_USER:
                            		musicList = api.getAudio(bundle.getLong(Constants.BUNDLE_USER_ID), null, null, null, null, null);
                            		
                            		 Collection<Long> u = new ArrayList<Long>();
                                     u.add(bundle.getLong(Constants.BUNDLE_USER_ID));
                                     Collection<String> d = new ArrayList<String>();
                                     d.add("");

                                     User userOne = api.getProfiles(u, d, "", "", "", "").get(0);
                                     PlaceName = userOne.first_name+" "+userOne.last_name;
                            		break;
                            	case Constants.MAIN_MUSIC_GROUP:
                            		musicList = api.getAudio(null, bundle.getLong(Constants.BUNDLE_GROUP_ID), null, null, null, null);
                                    for (Group one :  api.getUserGroups(bundle.getLong(Constants.BUNDLE_USER_ID)))
                                    	if (one.gid == bundle.getLong(Constants.BUNDLE_GROUP_ID))
                                    		PlaceName = one.name;
                            		break;
                            	case Constants.RECOMMENDATIONS:
                            		musicList = api.getAudioRecommendations();
                            		PlaceName = getActivity().getResources().getStringArray(R.array.slider_menu)[2];
                            		break;
                            	case Constants.POPULAR:
                            		musicList = api.getAudioPopular();
                            		PlaceName = getActivity().getResources().getStringArray(R.array.slider_menu)[3];
                            		break;
                            	}
                            	for (Audio one : musicList){
                            		f = new File(android.os.Environment.getExternalStorageDirectory()+"/Music/"+(one.artist+" - "+one.title+".mp3").replaceAll("[\\/:*?\"<>|]", ""));
                            		if (f.exists())
                            			musicCollection.add(new MusicCollection(one.aid, one.owner_id, one.artist, one.title, one.duration, one.url, one.lyrics_id, 1, 1));
                            		else 
                            			musicCollection.add(new MusicCollection(one.aid, one.owner_id, one.artist, one.title, one.duration, one.url, one.lyrics_id, 0, 0));
                            	}
                            	
        						error=false;
                            } catch (NotFoundException e) {
                            	error=true;
            					Log.e(LOG_TAG, "data Error");
            					e.printStackTrace();
            				} catch (IOException e) {
            					error=true;
            					Log.e(LOG_TAG, "Load Error");
            					e.printStackTrace();
            				} catch (NullPointerException e) {
            					error=true;
            	        		Log.e(LOG_TAG, "null Load Error"); 
            					e.printStackTrace();
            				} catch (Exception e) {
            					error=true;
            	        		Log.e(LOG_TAG, "other Load Error");
            					e.printStackTrace();
            				}
                	}
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    if (!isMyServiceRunning(DownloadService.class))
                    mPullToRefreshLayout.setRefreshing(false);
                    if (!error){
                    	listViewMusic.setVisibility(View.VISIBLE);
                    	relativeErrorLayout.setVisibility(View.GONE);
                    	
                    	musicAdapter = new MusicAdapter(getActivity(), musicCollection, options);

                    	// set action bar
                    	getSupportActionBar().setTitle(PlaceName);
                    	getSupportActionBar().setSubtitle(musicCollection.size()+" "+getResources().getString(R.string.quan_songs));
                    
                    	listViewMusic.setAdapter(musicAdapter);
                    
                    	Animation flyUpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_up_anim);
                    	listViewMusic.startAnimation(flyUpAnimation);
                    } else {
                    	listViewMusic.setVisibility(View.GONE);
                    	relativeErrorLayout.setVisibility(View.VISIBLE);
                    	errorRetryButton.setEnabled(true);
                    }
                }
            }.execute();
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
}
