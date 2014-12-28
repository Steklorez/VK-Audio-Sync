package com.BBsRs.vkaudiosync;

import java.util.ArrayList;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.ListView;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.perm.kate.api.Api;
import com.perm.kate.api.Audio;

public class MusicListActivity extends Activity {
	
	private PullToRefreshLayout mPullToRefreshLayout;
	ListView listViewMusic;
    //custom refresh listener where in new thread will load job doing, need to customize for all kind of data
    CustomOnRefreshListener customOnRefreshListener = new CustomOnRefreshListener();
    
    Account account=new Account();
    Api api;
    ArrayList<Audio> musicList;

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
                    listViewMusic.setAdapter(musicAdapter);
                    
                    Animation flyUpAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fly_up_anim);
                    listViewMusic.startAnimation(flyUpAnimation);
                }
            }.execute();
		}
         
    }
}
