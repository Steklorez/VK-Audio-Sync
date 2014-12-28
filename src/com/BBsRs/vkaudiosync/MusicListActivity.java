package com.BBsRs.vkaudiosync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;
import org.json.JSONException;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.perm.kate.api.Api;
import com.perm.kate.api.Audio;
import com.perm.kate.api.KException;
import com.perm.kate.api.User;

public class MusicListActivity extends Activity {
	
	private PullToRefreshLayout mPullToRefreshLayout;
	ListView listViewMusic;
    //custom refresh listener where in new thread will load job doing, need to customize for all kind of data
    CustomOnRefreshListener customOnRefreshListener = new CustomOnRefreshListener();
    
    Account account=new Account();
    Api api;
    ArrayList<Audio> musicList;
    
    String UserName = "";

	/** Called when the activity is first created. */
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
        
        
      //refresh on open to load data when app first time started
        mPullToRefreshLayout.setRefreshing(true);
        customOnRefreshListener.onRefreshStarted(null);
	}
	
    public class  CustomOnRefreshListener implements OnRefreshListener{

		@Override
		public void onRefreshStarted(View view) {
			// TODO Auto-generated method stub
			new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                            try {
                            	musicList = api.getAudio(account.user_id, null, null, null, null, null);
                                for (Audio one : musicList){
                                	Log.i("Music", one.title + " - "+one.url);
                                };
                                
                                Collection<Long> u = new ArrayList<Long>();
                                u.add(account.user_id);
                                Collection<String> d = new ArrayList<String>();
                                d.add("");
                                
        						UserName = api.getProfiles(u, d, "", "", "", "").get(0).first_name+" "+api.getProfiles(u, d, "", "", "", "").get(0).last_name;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    mPullToRefreshLayout.setRefreshing(false);
                    
                    MusicAdapter musicAdapter = new MusicAdapter(getApplicationContext(), musicList);

                    // настраиваем список
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View headerView = inflater.inflate(R.layout.ic_simple_music_header);
                    TextView name = (TextView) headerView.findViewById(R.id.name);
                    TextView quanSongs = (TextView) headerView.findViewById(R.id.quanSongs);
                    
                    name.setText(UserName);
                    quanSongs.setText(musicList.size()+" "+getResources().getString(R.string.quan_songs));
                    
                    listViewMusic.addHeaderView(headerView);
                    
                    listViewMusic.setAdapter(musicAdapter);
                    
                    Animation flyUpAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fly_up_anim);
                    listViewMusic.startAnimation(flyUpAnimation);
                }
            }.execute();
		}
         
    }
}
