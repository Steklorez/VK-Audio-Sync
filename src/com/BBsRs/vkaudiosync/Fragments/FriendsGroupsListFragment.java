package com.BBsRs.vkaudiosync.Fragments;

import java.io.IOException;
import java.util.ArrayList;

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
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.Adapters.FriendsGroupsAdapter;
import com.BBsRs.vkaudiosync.Services.DownloadService;
import com.BBsRs.vkaudiosync.VKApiThings.Account;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.BBsRs.vkaudiosync.collection.FriendsGroupsCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.perm.kate.api.Api;
import com.perm.kate.api.Group;
import com.perm.kate.api.User;

public class FriendsGroupsListFragment extends Fragment {
	
	//android views where shows content
	private PullToRefreshLayout mPullToRefreshLayout;
	ListView listViewFriendsGroups;
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
    ArrayList<FriendsGroupsCollection> friendsGroupsCollection = new ArrayList<FriendsGroupsCollection>();
    
    //adapter to listview
    FriendsGroupsAdapter friendsGroupsAdapter;
    
    //flag for error
    boolean error=false;
    
    //LOG_TAG for log
    String LOG_TAG = "FriendsGroupsFragment";
    
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
	    listViewFriendsGroups = (ListView)contentView.findViewById(R.id.listView);
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
        .showStubImage(R.drawable.deactivated_100)
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
	    	friendsGroupsCollection = savedInstanceState.getParcelableArrayList("friendsGroupsCollection");
	    	error = savedInstanceState.getBoolean("error");
	    	if ((friendsGroupsCollection.size()>1)) {
	    		friendsGroupsAdapter = new FriendsGroupsAdapter(getActivity(), friendsGroupsCollection, options);
	    		
	    		PlaceName = savedInstanceState.getString("PlaceName");
                
                // set action bar
            	getSupportActionBar().setTitle(PlaceName);
            	switch (bundle.getInt(Constants.BUNDLE_FRIENDS_GROUPS_TYPE)){
            	case Constants.FRIENDS:
            		getSupportActionBar().setSubtitle(friendsGroupsCollection.size()+" "+getResources().getString(R.string.quan_people));
            		break;
            	case Constants.GROUPS:
            		getSupportActionBar().setSubtitle(friendsGroupsCollection.size()+" "+getResources().getString(R.string.quan_groups));
            		break;
            	}
                
                listViewFriendsGroups.setAdapter(friendsGroupsAdapter);
                listViewFriendsGroups.setSelection(savedInstanceState.getInt("posX"));
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
        
        //programing on item click listener
        listViewFriendsGroups.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Bundle bundleMusic = new Bundle();
				final FragmentTransaction ft = getFragmentManager().beginTransaction();
				
	           	switch (bundle.getInt(Constants.BUNDLE_FRIENDS_GROUPS_TYPE)){
            	case Constants.FRIENDS:
            		bundleMusic.putLong(Constants.BUNDLE_USER_ID, friendsGroupsCollection.get(position).gfid);
            		bundleMusic.putInt(Constants.BUNDLE_MUSIC_TYPE, Constants.MAIN_MUSIC_USER);
            		ft.addToBackStack(Constants.FRIENDS_FRAGMENT);
            		break;
            	case Constants.GROUPS:
            		bundleMusic.putLong(Constants.BUNDLE_USER_ID, bundle.getLong(Constants.BUNDLE_USER_ID));
            		bundleMusic.putLong(Constants.BUNDLE_GROUP_ID, friendsGroupsCollection.get(position).gfid);
            		bundleMusic.putInt(Constants.BUNDLE_MUSIC_TYPE, Constants.MAIN_MUSIC_GROUP);
            		ft.addToBackStack(Constants.GROUPS_FRAGMENT);
            		break;
            	}
				
	           	MusicListFragment musicListFragment = new MusicListFragment();
	           	
	           	musicListFragment.setArguments(bundleMusic);
	           	
				//replace fragment
				ft.replace(R.id.contentView, musicListFragment); 
				ft.commit(); 
			}
        });
        
		return contentView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (friendsGroupsCollection !=null && listViewFriendsGroups!=null && PlaceName != null){
		 outState.putParcelableArrayList("friendsGroupsCollection", friendsGroupsCollection);
		 outState.putInt("posX",  listViewFriendsGroups.getFirstVisiblePosition());
		 outState.putString("PlaceName",  PlaceName);
		 outState.putBoolean("error", error);
		}
	}
	
    public class  CustomOnRefreshListener implements OnRefreshListener{

		@Override
		public void onRefreshStarted(View view) {
			// TODO Auto-generated method stub
			new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                	if (bundle != null){
                            try {
                            	error=true;
                            	friendsGroupsCollection = new ArrayList<FriendsGroupsCollection>();
                            	
                            	
                            	switch (bundle.getInt(Constants.BUNDLE_FRIENDS_GROUPS_TYPE)){
                            	case Constants.FRIENDS:
                            		ArrayList<User> FriendsList = new ArrayList<User>();
                            		FriendsList = api.getFriends(bundle.getLong(Constants.BUNDLE_USER_ID), "photo_100", null, null, null);
//                            		api.getFCriends(user_id, fields, lid, captcha_key, captcha_sid)
//                            		api.getfr
                            		PlaceName = getResources().getStringArray(R.array.slider_menu)[4];
                            		
                            		for (User one : FriendsList)
                            		friendsGroupsCollection.add(new FriendsGroupsCollection(one.uid, one.first_name+" "+one.last_name, one.photo_medium_rec));
                            		
                            		error=false;
                            		break;
                            	case Constants.GROUPS:
                            		ArrayList<Group> GroupsList = new ArrayList<Group>();
                            		GroupsList = api.getUserGroups(bundle.getLong(Constants.BUNDLE_USER_ID));
                            		
                            		PlaceName = getResources().getStringArray(R.array.slider_menu)[5];
                            		
                            		for (Group one : GroupsList)
                                		friendsGroupsCollection.add(new FriendsGroupsCollection(one.gid, one.name, one.photo_medium));
                            		
                            		error=false;
                            		break;
                            	}
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
                    	listViewFriendsGroups.setVisibility(View.VISIBLE);
                    	relativeErrorLayout.setVisibility(View.GONE);
                    	
                    	friendsGroupsAdapter = new FriendsGroupsAdapter(getActivity(), friendsGroupsCollection, options);

                    	// set action bar
                    	getSupportActionBar().setTitle(PlaceName);
                    	switch (bundle.getInt(Constants.BUNDLE_FRIENDS_GROUPS_TYPE)){
                    	case Constants.FRIENDS:
                    		getSupportActionBar().setSubtitle(friendsGroupsCollection.size()+" "+getResources().getString(R.string.quan_people));
                    		break;
                    	case Constants.GROUPS:
                    		getSupportActionBar().setSubtitle(friendsGroupsCollection.size()+" "+getResources().getString(R.string.quan_groups));
                    		break;
                    	}
                    	
                    
                    	listViewFriendsGroups.setAdapter(friendsGroupsAdapter);
                    
                    	Animation flyUpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_up_anim);
                    	listViewFriendsGroups.startAnimation(flyUpAnimation);
                    } else {
                    	listViewFriendsGroups.setVisibility(View.GONE);
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
