package com.BBsRs.vkaudiosync.Fragments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.app.DialogFragment;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.app.ProgressDialog;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.NumberPicker;
import org.holoeverywhere.widget.NumberPicker.OnValueChangeListener;
import org.holoeverywhere.widget.RelativeLayout;
import org.holoeverywhere.widget.TextView;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import com.BBsRs.vkaudiosync.Application.ObjectSerializer;
import com.BBsRs.vkaudiosync.VKApiThings.Account;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.BBsRs.vkaudiosync.collection.MusicCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.perm.kate.api.Api;
import com.perm.kate.api.Attachment;
import com.perm.kate.api.Audio;
import com.perm.kate.api.Group;
import com.perm.kate.api.KException;
import com.perm.kate.api.User;
import com.perm.kate.api.WallMessage;

public class MusicListFragment extends Fragment {
	
	//preferences 
    SharedPreferences sPref;
	
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
    
    //to post in UI thread from others threads
    Handler handler = new Handler();
    
    //quan of exist
    public int quanOfExist = 0;
    
	
	@SuppressWarnings("deprecation")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.list);
		
		//set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
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
    	
    	//clean ab title and subtitle
     	getSupportActionBar().setTitle("");
     	getSupportActionBar().setSubtitle("");
    	
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
        .showStubImage(R.drawable.ic_simple_music_stub)
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
	    	musicCollection = savedInstanceState.getParcelableArrayList(Constants.EXTRA_MUSIC_COLLECTION);
	    	error = savedInstanceState.getBoolean(Constants.EXTRA_ERROR);
	    	if ((musicCollection.size()>0)) {
	    		musicAdapter = new MusicAdapter(getActivity(), musicCollection, options, savedInstanceState.getInt(Constants.EXTRA_CHECKED_QUAN));
	    		
	    		quanOfExist = savedInstanceState.getInt(Constants.EXTRA_EXIST_QUAN);
	    		PlaceName = savedInstanceState.getString(Constants.EXTRA_PLACE_NAME);
                
                listViewMusic.setAdapter(musicAdapter);
                listViewMusic.setSelection(savedInstanceState.getInt(Constants.EXTRA_POSX));
                
                if (mainMenu != null)
	    			mainMenu.findItem(R.id.menu_check_all).setVisible(true);
	    	} else {
	    		if (error){
	    			if (mainMenu != null)
    	    			mainMenu.findItem(R.id.menu_check_all).setVisible(false);
	    			errorMessage.setText(R.string.error_message);
        			errorRetryButton.setVisibility(View.VISIBLE);
	    		} else {
	    			mPullToRefreshLayout.setRefreshing(true);
	    			customOnRefreshListener.onRefreshStarted(null);	
	    		}
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
        
		return contentView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (musicCollection !=null && listViewMusic!=null && PlaceName != null){
		 outState.putParcelableArrayList(Constants.EXTRA_MUSIC_COLLECTION, musicCollection);
		 outState.putInt(Constants.EXTRA_POSX,  listViewMusic.getFirstVisiblePosition());
		 outState.putString(Constants.EXTRA_PLACE_NAME,  PlaceName);
		 outState.putBoolean(Constants.EXTRA_ERROR, error);
		 outState.putInt(Constants.EXTRA_CHECKED_QUAN,  musicAdapter.checked);
		 outState.putInt(Constants.EXTRA_EXIST_QUAN,  quanOfExist);
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.ml_menu, menu);
		mainMenu = menu;
		mainMenu.findItem(R.id.menu_check_all).setVisible(false);
		return;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	      switch (item.getItemId()) {
	      case android.R.id.home:
	    	  Intent i = new Intent(Constants.OPEN_MENU_DRAWER);
	    	  getActivity().sendBroadcast(i);
	    	  break;
	      case R.id.menu_main:
	    	  bundle.putInt(Constants.BUNDLE_MAIN_WALL_TYPE, Constants.MAIN_MUSIC);
           	  mPullToRefreshLayout.setRefreshing(true);
           	  customOnRefreshListener.onRefreshStarted(null);
	    	  break;
	      case R.id.menu_wall:
           	  bundle.putInt(Constants.BUNDLE_MAIN_WALL_TYPE, Constants.WALL_MUSIC);
           	  mPullToRefreshLayout.setRefreshing(true);
           	  customOnRefreshListener.onRefreshStarted(null);
	    	  break;
	      case R.id.menu_check_all:
	    	  if (musicCollection !=null && musicAdapter !=null && musicCollection.size()>0){
	    	  	 if (String.valueOf(item.getTitle()).equals(getResources().getString(R.string.check_all))){
	    	  		DialogFragment NumberPickerDialog = new DialogFragment(){
	    	  			private int currValue=1;
	    	  			private View makeNumberPicker() {
	    	  		        View view = getLayoutInflater().inflate(
	    	  		                R.layout.number_picker_demo);
	    	  		        NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.numberPicker);
	    	  		        numberPicker.setMinValue(musicCollection.size()-quanOfExist == 0 ? 0 : 1);
	    	  		        numberPicker.setMaxValue(musicCollection.size()-quanOfExist);
	    	  		        numberPicker.setValue(musicCollection.size()-quanOfExist == 0 ? 0 : 1);
	    	  		        numberPicker.setOnValueChangedListener(new OnValueChangeListener(){
								@Override
								public void onValueChange(NumberPicker picker,
										int oldVal, int newVal) {
									currValue = newVal;
								}
	    	  		        });
	    	  		        return view;
	    	  		    }

	    	  		    @Override
	    	  		    public Dialog onCreateDialog(Bundle savedInstanceState) {
	    	  		        AlertDialog.Builder builder = new AlertDialog.Builder(getSupportActivity(), getTheme());
	    	  		        builder.setView(makeNumberPicker());
	    	  		        builder.setNegativeButton(R.string.cancel, null);
	    	  		        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									final ProgressDialog prDialog = new ProgressDialog(getSupportActivity(), getTheme());
							        prDialog.setIndeterminate(true);
							        prDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
							        prDialog.setMessage(getText(R.string.wait_check));
							        prDialog.setCancelable(false);
							        prDialog.setCanceledOnTouchOutside(false);
							        prDialog.show();
							        
							        new Thread(new Runnable(){
										@Override
										public void run() {
											int index=0;
							    	  		for (MusicCollection oneItem : musicCollection){
							    	  			if (musicAdapter.checked>currValue-1)
							    	  				break;
							    	  			if (oneItem.checked == 0 && oneItem.exist == 0){
							    	  				oneItem.checked = 1;
							    	  				musicAdapter.getItem(index).checked = 1;
							    	  				
							    	  				//add music to global download manager
							    	  				try {
							    	  					ArrayList<MusicCollection> musicCollectionTemp = (ArrayList<MusicCollection>) ObjectSerializer.deserialize(sPref.getString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(new ArrayList<MusicCollection>())));
							    	  					if (musicCollectionTemp==null)
						                            		musicCollectionTemp = new ArrayList<MusicCollection>();
							    	  					musicCollectionTemp.add(oneItem);
														sPref.edit().putString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(musicCollectionTemp)).commit();
													} catch (IOException e) {
														e.printStackTrace();
													}

							    	  				musicAdapter.checked++;
							    	  			}
							    	  			index++;
							   	   		  	}
							    	  		final Runnable dismissDialog = new Runnable() {
							    		        public void run() {
							    		        	prDialog.dismiss();
							    		        	musicAdapter.notifyDataSetChanged();
							    		        }
							    		    };
							    		    handler.post(dismissDialog);
										}
							        }).start();
								}
							});
	    	  		        return builder.create();
	    	  		    }
	    	  		};
	    	  		
	    	  		NumberPickerDialog.show(getSupportActivity());
	    	  		
	    	  		if ((musicCollection.size()-quanOfExist) != 0)	
	    	  			item.setTitle(getResources().getString(R.string.uncheck_all));
	    	  				
	    	  	 } else	{
	    	  		final ProgressDialog prDialog = new ProgressDialog(getSupportActivity());
			        prDialog.setIndeterminate(true);
			        prDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			        prDialog.setMessage(getText(R.string.wait_uncheck));
			        prDialog.setCancelable(false);
			        prDialog.setCanceledOnTouchOutside(false);
			        prDialog.show();
			        
			        new Thread(new Runnable(){
						@Override
						public void run() {
							int index=0;
			    	  		for (MusicCollection oneItem : musicCollection){
			    	  			if (oneItem.checked == 1 && oneItem.exist == 0){
			    	  				oneItem.checked = 0;
			    	  				musicAdapter.getItem(index).checked = 0;
			    	  				
			    	  				//remove music from glabal download manager
			    	  				try {
			    	  					ArrayList<MusicCollection> musicCollectionTemp = (ArrayList<MusicCollection>) ObjectSerializer.deserialize(sPref.getString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(new ArrayList<MusicCollection>())));
			    	  					if (musicCollectionTemp==null)
		                            		musicCollectionTemp = new ArrayList<MusicCollection>();
			    	  					int indexTemp=0;
			    	  					for (MusicCollection one: musicCollectionTemp){
			    	  						if (one.aid==oneItem.aid || (one.artist.equals(oneItem.artist) && one.title.equals(oneItem.title))){
			    	  							musicCollectionTemp.remove(indexTemp);
			    	  							break;
			    	  						}
			    	  						indexTemp++;
			    	  					}
										sPref.edit().putString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(musicCollectionTemp)).commit();
									} catch (IOException e) {
										e.printStackTrace();
									}
			    	  				
			    	  				musicAdapter.checked--;
			    	  			}
			    	  			index++;
			   	   		  	}
			    	  		final Runnable dismissDialog = new Runnable() {
			    		        public void run() {
			    		        	prDialog.dismiss();
			    		        	musicAdapter.notifyDataSetChanged();
			    		        }
			    		    };
			    		    handler.post(dismissDialog);
						}
			        }).start();
	    	  		 item.setTitle(getResources().getString(R.string.check_all));
	    	  	 }
	    	  }
	    	  break;
	      }
		return true;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(musicDownloaded);
		getActivity().unregisterReceiver(someChecked);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		//turn up download receiver
        getActivity().registerReceiver(musicDownloaded, new IntentFilter(Constants.MUSIC_DOWNLOADED));
        getActivity().registerReceiver(someChecked, new IntentFilter(Constants.SOME_CHECKED));
        
    	 // set action bar
    	getSupportActionBar().setTitle(PlaceName);
    	if (!customOnRefreshListener.isRefreshing ){
    		switch (bundle.getInt(Constants.BUNDLE_MAIN_WALL_TYPE)){
    		case Constants.MAIN_MUSIC:
    			getSupportActionBar().setSubtitle(musicCollection.size()+" "+getResources().getString(R.string.quan_songs_main));
    			break;
    		case Constants.WALL_MUSIC:
    			getSupportActionBar().setSubtitle(musicCollection.size()+" "+getResources().getString(R.string.quan_songs_wall));
    			break;
    		}
    		if (sPref.getBoolean(Constants.OTHER_FRAGMENT, false)){
    			musicCollection = new ArrayList<MusicCollection>();
	    		musicAdapter = new MusicAdapter(getActivity(), musicCollection, options, 0);
	    		listViewMusic.setAdapter(musicAdapter);
	    		
    			mPullToRefreshLayout.setRefreshing(true);
				customOnRefreshListener.onRefreshStarted(null);	
				sPref.edit().putBoolean(Constants.OTHER_FRAGMENT, false).commit();
    		}
    	}
    	
    	//other fragment false
        sPref.edit().putBoolean(Constants.OTHER_FRAGMENT, false).commit();
	}
	
	private BroadcastReceiver musicDownloaded = new BroadcastReceiver() {

	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if (intent.getExtras().getBoolean(Constants.DOWNLOAD_SERVICE_STOPPED)){
	    	} else {
	    		try {
	    		int index = 0;
	    			for (MusicCollection oneItem : musicCollection){
	    				if (oneItem.aid==((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).aid || (oneItem.title.equals(((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).title) && oneItem.artist.equals(((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).artist))){
	    					oneItem.checked = intent.getExtras().getBoolean(Constants.MUSIC_SUCCESSFULLY_DOWNLOADED) ? 1 : 0;
	    					oneItem.exist = intent.getExtras().getBoolean(Constants.MUSIC_SUCCESSFULLY_DOWNLOADED) ? 1 : 0;
	    					musicAdapter.getItem(index).checked = intent.getExtras().getBoolean(Constants.MUSIC_SUCCESSFULLY_DOWNLOADED) ? 1 : 0;
	    					musicAdapter.getItem(index).exist = intent.getExtras().getBoolean(Constants.MUSIC_SUCCESSFULLY_DOWNLOADED) ? 1 : 0;
	    					musicAdapter.notifyDataSetChanged();
	    					quanOfExist++;
	    					break;
	    				}
	    				index++;
	    			}
	    		} catch (Exception e){
	    			e.printStackTrace();
	    		}
	    	}
	    }
	};
	
	private BroadcastReceiver someChecked = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	
	    	//remove or add music to global download manager
			try {
				ArrayList<MusicCollection> musicCollectionTemp = (ArrayList<MusicCollection>) ObjectSerializer.deserialize(sPref.getString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(new ArrayList<MusicCollection>())));
				if (musicCollectionTemp==null)
            		musicCollectionTemp = new ArrayList<MusicCollection>();
				if (((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).checked == 1){
					musicCollectionTemp.add((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM));
				} else {
  					int indexTemp=0;
  					for (MusicCollection one: musicCollectionTemp){
  						if (one.aid==((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).aid || (one.title.equals(((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).title) && one.artist.equals(((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).artist))){
  							musicCollectionTemp.remove(indexTemp);
  							break;
  						}
  						indexTemp++;
  					}
				}
				sPref.edit().putString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(musicCollectionTemp)).commit();
			} catch (IOException e) {
				e.printStackTrace();
			}
				
	    	if (intent.getExtras().getBoolean(Constants.SOME_CHECKED)){
	    		if (mainMenu != null)
	    			mainMenu.findItem(R.id.menu_check_all).setTitle(getResources().getString(R.string.uncheck_all));
	    	} else {
	    		if (mainMenu != null)
	    			mainMenu.findItem(R.id.menu_check_all).setTitle(getResources().getString(R.string.check_all));
	    	}
	    }
	};
	
    public class  CustomOnRefreshListener implements OnRefreshListener{

    	public boolean isRefreshing = false;
    	public byte isAudioDisabled = 0;
    	private int quanOfChecked = 0;
    	
		@Override
		public void onRefreshStarted(View view) {
			isRefreshing = true;
    		getSupportActionBar().setSubtitle(getResources().getString(R.string.refreshing));
			new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                	if (bundle != null){
                            try {
                            	error=true;
                            	musicCollection = new ArrayList<MusicCollection>();
                            	ArrayList<Audio> musicList = new ArrayList<Audio>();
                            	
                            	quanOfChecked = quanOfExist = 0;
                            	
                            	switch (bundle.getInt(Constants.BUNDLE_MAIN_WALL_TYPE)){
                            	case Constants.MAIN_MUSIC:
                            		switch (bundle.getInt(Constants.BUNDLE_MUSIC_TYPE)){
                                	case Constants.MAIN_MUSIC_USER:
                                		Collection<Long> u = new ArrayList<Long>();
                                        u.add(bundle.getLong(Constants.BUNDLE_USER_ID));
                                        Collection<String> d = new ArrayList<String>();
                                        d.add("");

                                        User userOne = api.getProfiles(u, d, "", "", "", "").get(0);
                                        PlaceName = userOne.first_name+" "+userOne.last_name;
                                        
                                		musicList = api.getAudio(bundle.getLong(Constants.BUNDLE_USER_ID), null, null, null, null, null);
                                		break;
                                	case Constants.MAIN_MUSIC_GROUP:
                                		for (Group one :  api.getUserGroups(bundle.getLong(Constants.BUNDLE_USER_ID)))
                                        	if (one.gid == bundle.getLong(Constants.BUNDLE_GROUP_ID))
                                        		PlaceName = one.name;
                                		
                                		musicList = api.getAudio(null, bundle.getLong(Constants.BUNDLE_GROUP_ID), null, null, null, null);
                                		break;
                                	}
                            		break;
                            	case Constants.WALL_MUSIC:
                            		ArrayList<WallMessage> wallMessageList = new ArrayList<WallMessage>();
                            		switch (bundle.getInt(Constants.BUNDLE_MUSIC_TYPE)){
                                	case Constants.MAIN_MUSIC_USER:
                                		Collection<Long> u = new ArrayList<Long>();
                                        u.add(bundle.getLong(Constants.BUNDLE_USER_ID));
                                        Collection<String> d = new ArrayList<String>();
                                        d.add("");

                                        User userOne = api.getProfiles(u, d, "", "", "", "").get(0);
                                        PlaceName = userOne.first_name+" "+userOne.last_name;
                                        
                                		while (true){
                                			ArrayList<WallMessage> wallMessageListTemp = api.getWallMessages(bundle.getLong(Constants.BUNDLE_USER_ID), 100, wallMessageList.size(), null);
                                			wallMessageList.addAll(wallMessageListTemp);
                                			if (wallMessageListTemp.size()<100 || wallMessageList.size()>=1000)
                                				break;
                                		}
                                		for (WallMessage one : wallMessageList){
                                			for (Attachment oneA : one.attachments)
                                				if (oneA.audio != null)
                                				musicList.add(oneA.audio);
                                		}
                                		break;
                                	case Constants.MAIN_MUSIC_GROUP:
                                		for (Group one :  api.getUserGroups(bundle.getLong(Constants.BUNDLE_USER_ID)))
                                        	if (one.gid == bundle.getLong(Constants.BUNDLE_GROUP_ID))
                                        		PlaceName = one.name;
                                		
                                		while (true){
                                			ArrayList<WallMessage> wallMessageListTemp = api.getWallMessages(-bundle.getLong(Constants.BUNDLE_GROUP_ID), 100, wallMessageList.size(), null);
                                			wallMessageList.addAll(wallMessageListTemp);
                                			if (wallMessageListTemp.size()<100 || wallMessageList.size()>=1000)
                                				break;
                                		}
                                		for (WallMessage one : wallMessageList){
                                			for (Attachment oneA : one.attachments)
                                				if (oneA.audio != null)
                                				musicList.add(oneA.audio);
                                		}
                                		break;
                                	}
                            		break;
                            	}
                            	ArrayList<MusicCollection> musicCollectionTemp = (ArrayList<MusicCollection>) ObjectSerializer.deserialize(sPref.getString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(new ArrayList<MusicCollection>())));
                            	if (musicCollectionTemp==null)
                            		musicCollectionTemp = new ArrayList<MusicCollection>();
                            	for (Audio one : musicList){
                            		f = new File(sPref.getString(Constants.DOWNLOAD_DIRECTORY, android.os.Environment.getExternalStorageDirectory()+"/Music")+"/"+(one.artist+" - "+one.title+".mp3").replaceAll("[\\/:*?\"<>|]", ""));
                            		if (f.exists()){
                            			quanOfExist++;
                            			musicCollection.add(new MusicCollection(one.aid, one.owner_id, one.artist, one.title, one.duration, one.url, one.lyrics_id, 1, 1));
                            		}
                            		else 
                            			musicCollection.add(new MusicCollection(one.aid, one.owner_id, one.artist, one.title, one.duration, one.url, one.lyrics_id, 0, 0));
                            		for (MusicCollection oneDM : musicCollectionTemp){
                            			if (oneDM.aid == one.aid || (oneDM.title.equals(one.title) && oneDM.artist.equals(one.artist))){
                            				musicCollection.get(musicCollection.size()-1).checked = 1;
                            				quanOfChecked++;
                            			}
                            		}
                            	}
                            	
                            	if (musicCollection.size() != 0)
                            		error=false;
                            	else 
                            		isAudioDisabled = 2;
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
            				} catch (KException e) {
            					error=true;
            					isAudioDisabled = 1;
            	        		Log.e(LOG_TAG, "audio is disabled");
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
                    isRefreshing = false;
                    try {
                    	mPullToRefreshLayout.setRefreshing(false);
                    	
                    	// set action bar
                		getSupportActionBar().setTitle(PlaceName);
                		switch (bundle.getInt(Constants.BUNDLE_MAIN_WALL_TYPE)){
                		case Constants.MAIN_MUSIC:
                			getSupportActionBar().setSubtitle(musicCollection.size()+" "+getResources().getString(R.string.quan_songs_main));
                			break;
                		case Constants.WALL_MUSIC:
                			getSupportActionBar().setSubtitle(musicCollection.size()+" "+getResources().getString(R.string.quan_songs_wall));
                			break;
                		}
                		
                    	if (!error){
                    		listViewMusic.setVisibility(View.VISIBLE);
                    		relativeErrorLayout.setVisibility(View.GONE);
                    	
                    		musicAdapter = new MusicAdapter(getActivity(), musicCollection, options, quanOfChecked);

                    
                   			listViewMusic.setAdapter(musicAdapter);
                   			
                   			Animation flyUpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_up_anim);
                   			listViewMusic.startAnimation(flyUpAnimation);
                   			
                   			if (quanOfChecked != 0){
                	    		if (mainMenu != null){
                	    			mainMenu.findItem(R.id.menu_check_all).setTitle(getResources().getString(R.string.uncheck_all));
                	    			mainMenu.findItem(R.id.menu_check_all).setVisible(true);
                	    		}
                	    	} else {
                	    		if (mainMenu != null){
                	    			mainMenu.findItem(R.id.menu_check_all).setTitle(getResources().getString(R.string.check_all));
                	    			mainMenu.findItem(R.id.menu_check_all).setVisible(true);
                	    		}
                	    	}
                   		} else {
                   			if (mainMenu != null)
            	    			mainMenu.findItem(R.id.menu_check_all).setVisible(false);
                   			listViewMusic.setVisibility(View.GONE);
                   			relativeErrorLayout.setVisibility(View.VISIBLE);
                   			errorRetryButton.setEnabled(true);
                   			// set action bar
                    		getSupportActionBar().setTitle(PlaceName);
                    		switch (isAudioDisabled){
                    		case 2:
                    			errorMessage.setText(R.string.error_message_zero_count_audio);
                    			errorRetryButton.setVisibility(View.GONE);
                    			break;
                    		case 1:
                    			errorMessage.setText(R.string.error_message_audio_disabled);
                    			errorRetryButton.setVisibility(View.GONE);
                    			break;
                    		case 0:
                    			errorMessage.setText(R.string.error_message);
                    			errorRetryButton.setVisibility(View.VISIBLE);
                    			break;
                    		}
                   		}
                   } catch (NullPointerException e){
                	   e.printStackTrace();
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
