package com.BBsRs.vkaudiosync;

import java.util.ArrayList;
import java.util.Collection;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.BBsRs.vkaudiosync.collection.MusicCollection;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
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
    ImageLoader imageLoader;
    
    ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();

	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    this.setContentView(R.layout.music_list);
	    
	    //init all views
	    listViewMusic = (ListView)this.findViewById(R.id.listViewMusic);
	    mPullToRefreshLayout = (PullToRefreshLayout)findViewById(R.id.ptr_layout);
	    
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
        .showStubImage(R.drawable.deactivated_100)
        //.showImageForEmptyUri(R.drawable.logo)
        .cacheOnDisc(true)	
        .cacheInMemory(true)					
        .build();
        
        imageLoader = ImageLoader.getInstance();
		// Initialize ImageLoader with configuration. Do it once.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
		.threadPriority(Thread.NORM_PRIORITY - 2)
		.denyCacheImageMultipleSizesInMemory()
		.diskCacheFileNameGenerator(new Md5FileNameGenerator())
		.diskCacheSize(25 * 1024 * 1024) // 25 Mb
		.tasksProcessingOrder(QueueProcessingType.LIFO)
		//.writeDebugLogs() // Remove for release app
		.build();
        
	    imageLoader.init(config);
        
        
      //refresh on open to load data when app first time started
	    if(savedInstanceState == null) {
	    	 mPullToRefreshLayout.setRefreshing(true);
	         customOnRefreshListener.onRefreshStarted(null);
	    }
	    else{
	    	musicCollection = savedInstanceState.getParcelableArrayList("musicCollection");
	    	if ((musicCollection.size()>1)) {
	    		MusicAdapter musicAdapter = new MusicAdapter(getApplicationContext(), musicCollection, options, imageLoader);
	    		
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
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		 outState.putParcelableArrayList("musicCollection", musicCollection);
		 outState.putInt("posX",  listViewMusic.getFirstVisiblePosition());
		 outState.putString("UserName",  UserName);
	}
	
    public class  CustomOnRefreshListener implements OnRefreshListener{

		@Override
		public void onRefreshStarted(View view) {
			// TODO Auto-generated method stub
			new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                            try {
                            	
                            	musicCollection = new ArrayList<MusicCollection>();
                            	
                            	for (Audio one : api.getAudio(account.user_id, null, null, null, null, null)){
                            		musicCollection.add(new MusicCollection(one.aid, one.owner_id, one.artist, one.title, one.duration, one.url, one.lyrics_id, 0));
                            	}
                            	
                                Collection<Long> u = new ArrayList<Long>();
                                u.add(account.user_id);
                                Collection<String> d = new ArrayList<String>();
                                d.add("");
                                
                                User userOne = api.getProfiles(u, d, "", "", "", "").get(0);
                                
        						UserName = userOne.first_name+" "+userOne.last_name;
        						
        						
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    //reset list view
                    if (headerView!=null)
                    listViewMusic.removeHeaderView(headerView);
                    
                    mPullToRefreshLayout.setRefreshing(false);
                    
                    MusicAdapter musicAdapter = new MusicAdapter(getApplicationContext(), musicCollection, options, imageLoader);

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
                }
            }.execute();
		}
         
    }
}
