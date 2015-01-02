package com.BBsRs.vkaudiosync;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.RelativeLayout;
import org.holoeverywhere.widget.TextView;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.BBsRs.vkaudiosync.Services.DownloadService;
import com.BBsRs.vkaudiosync.VKApiThings.Account;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.BBsRs.vkaudiosync.collection.MusicCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.perm.kate.api.Api;
import com.perm.kate.api.Audio;
import com.perm.kate.api.User;

public class MusicListActivity extends Activity {
	
	private PullToRefreshLayout mPullToRefreshLayout;
	ListView listViewMusic;
    //custom refresh listener where in new thread will load job doing, need to customize for all kind of data
    CustomOnRefreshListener customOnRefreshListener = new CustomOnRefreshListener();
    
    Account account=new Account();
    Api api;
    
    String UserName = "";
    String UserAvatarUrl = "";
    
    View headerView = null;
    
    DisplayImageOptions options ;
    
    ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
    
    Menu mainMenu = null;
    MusicAdapter musicAdapter;
    
    //flag for error
    boolean error=false;
    
    //LOG_TAG for log
    String LOG_TAG = "MusicListActivity";
    
    RelativeLayout relativeErrorLayout;
    TextView errorMessage;
    Button errorRetryButton;
    File f;

	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    this.setContentView(R.layout.music_list);
	    
	    //init all views
	    listViewMusic = (ListView)this.findViewById(R.id.listViewMusic);
	    mPullToRefreshLayout = (PullToRefreshLayout)findViewById(R.id.ptr_layout);
    	relativeErrorLayout = (RelativeLayout)findViewById(R.id.errorLayout);
    	errorMessage = (TextView)findViewById(R.id.errorMessage);
    	errorRetryButton = (Button)findViewById(R.id.errorRetryButton);
	    
	    //retrieve old session
        account.restore(this);
        
        //create new session
        if(account.access_token!=null)
            api=new Api(account.access_token, Constants.API_ID);
	    
	    //init pull to refresh module
        ActionBarPullToRefresh.from(this)
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
	    }
	    else{
	    	musicCollection = savedInstanceState.getParcelableArrayList("musicCollection");
	    	error = savedInstanceState.getBoolean("error");
	    	if ((musicCollection.size()>1)) {
	    		musicAdapter = new MusicAdapter(getApplicationContext(), musicCollection, options);
	    		
	    		LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (headerView==null)
                headerView = inflater.inflate(R.layout.ic_simple_music_header);
                TextView name = (TextView) headerView.findViewById(R.id.name);
                TextView quanSongs = (TextView) headerView.findViewById(R.id.quanSongs);
                
                UserName = savedInstanceState.getString("UserName");
                
                name.setText(UserName);
                quanSongs.setText(musicCollection.size()+" "+getResources().getString(R.string.quan_songs));
                
                listViewMusic.addHeaderView(headerView);
                
                listViewMusic.setAdapter(musicAdapter);
                listViewMusic.setSelection(savedInstanceState.getInt("posX"));
	    	}
	    	
	    	else {
	    		mPullToRefreshLayout.setRefreshing(true);
	         	customOnRefreshListener.onRefreshStarted(null);	
	    	}
	    }
	    
	    //programing error button
        errorRetryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPullToRefreshLayout.setRefreshing(true);
		        customOnRefreshListener.onRefreshStarted(null);
		        errorRetryButton.setEnabled(false);
			}
		});
        
        //turn up download receiver
        registerReceiver(musicDownloaded, new IntentFilter("DOWNLOADED"));
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		 outState.putParcelableArrayList("musicCollection", musicCollection);
		 outState.putInt("posX",  listViewMusic.getFirstVisiblePosition());
		 outState.putString("UserName",  UserName);
		 outState.putBoolean("error", error);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		mainMenu = menu;
		return true;
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
	    	  Intent serviceIntent = new Intent(this, DownloadService.class); 
	    	  serviceIntent.putExtra("musicCollection", musicCollection);
	    	  startService(serviceIntent);
	    	  break;
	      }
		return true;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(musicDownloaded);
	}
	
	private BroadcastReceiver musicDownloaded = new BroadcastReceiver() {

	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if (musicAdapter!=null){
	    		musicAdapter.getItem(intent.getExtras().getInt("index")).checked = intent.getExtras().getBoolean("successfully") ? 1 : 0;
	    		musicAdapter.getItem(intent.getExtras().getInt("index")).exist = intent.getExtras().getBoolean("successfully") ? 1 : 0;
	    		musicAdapter.notifyDataSetChanged();
	    	}
	    	if (intent.getExtras().getBoolean("service_stopped")){
	    		//stop task animation
	    		mPullToRefreshLayout.setRefreshing(false);
	    		if (mainMenu!=null){
	    			mainMenu.findItem(R.id.menu_start_download_service).setEnabled(true);
	    			mainMenu.findItem(R.id.menu_start_download_service).setIcon(R.drawable.ic_menu_download);
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
                	if (mainMenu !=null && mainMenu.findItem(R.id.menu_start_download_service).isEnabled()){
                            try {
                            	error=true;
                            	musicCollection = new ArrayList<MusicCollection>();
                            	
                            	for (Audio one : api.getAudio(account.user_id, null, null, null, null, null)){
                            		f = new File(android.os.Environment.getExternalStorageDirectory()+"/Music/"+(one.artist+" - "+one.title+".mp3").replaceAll("[\\/:*?\"<>|]", ""));
                            		if (f.exists())
                            			musicCollection.add(new MusicCollection(one.aid, one.owner_id, one.artist, one.title, one.duration, one.url, one.lyrics_id, 1, 1));
                            		else 
                            			musicCollection.add(new MusicCollection(one.aid, one.owner_id, one.artist, one.title, one.duration, one.url, one.lyrics_id, 0, 0));
                            	}
                            	
                                Collection<Long> u = new ArrayList<Long>();
                                u.add(account.user_id);
                                Collection<String> d = new ArrayList<String>();
                                d.add("");
                                
                                User userOne = api.getProfiles(u, d, "", "", "", "").get(0);
                                
        						UserName = userOne.first_name+" "+userOne.last_name;
        						
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
                    mPullToRefreshLayout.setRefreshing(false);
                    if (!error){
                    	listViewMusic.setVisibility(View.VISIBLE);
                    	relativeErrorLayout.setVisibility(View.GONE);
                    	
                    	//reset list view
                    	if (headerView!=null)
                    		listViewMusic.removeHeaderView(headerView);
                    
                    	musicAdapter = new MusicAdapter(getApplicationContext(), musicCollection, options);

                    	// setting up list
                    	LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    	if (headerView==null)
                    		headerView = inflater.inflate(R.layout.ic_simple_music_header);
                    	TextView name = (TextView) headerView.findViewById(R.id.name);
                    	TextView quanSongs = (TextView) headerView.findViewById(R.id.quanSongs);
                    
                    	name.setText(UserName);
                    	quanSongs.setText(musicCollection.size()+" "+getResources().getString(R.string.quan_songs));
                    
                    	listViewMusic.addHeaderView(headerView);
                    
                    	listViewMusic.setAdapter(musicAdapter);
                    
                    	Animation flyUpAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fly_up_anim);
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
}
