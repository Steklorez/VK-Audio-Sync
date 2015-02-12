package com.BBsRs.vkaudiosync.Services;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.Application.ObjectSerializer;
import com.BBsRs.vkaudiosync.VKApiThings.Account;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.BBsRs.vkaudiosync.collection.MusicCollection;
import com.perm.kate.api.Api;
import com.perm.kate.api.Audio;

public class AutomaticSynchronizationService extends Service {
	
	private static String LOG_TAG = "AUS Service";
	
	private MainMusicListUpdateTask mTask;
	
	//preferences 
    SharedPreferences sPref;

    /*----------------------------VK API-----------------------------*/
    Account account=new Account();
    Api api;
    /*----------------------------VK API-----------------------------*/
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Log.i(LOG_TAG, "start command");
    	
    	/*----------------------------VK API-----------------------------*/
    	//retrieve old session
        account.restore(getApplicationContext());
        
        //create new session
        if(account.access_token!=null)
            api=new Api(account.access_token, Constants.API_ID);
        /*----------------------------VK API-----------------------------*/
    	
    	//set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	
    	boolean active = mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED;
    	
		if (active) {
			Log.i(LOG_TAG, "task is already running");
			return super.onStartCommand(intent, flags, startId);
		}
    	
    	mTask = new MainMusicListUpdateTask();
        mTask.execute();
    	
		return super.onStartCommand(intent, flags, startId);
    }
    
    public void onDestroy() {
    	Log.i(LOG_TAG, "destroy");
		if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED) {
			mTask.cancel(true);
			mTask = null;
		}
		super.onDestroy();
	}
    
    private class MainMusicListUpdateTask extends AsyncTask<Void, Void, Void> {
        private WakeLock mWakeLock;
        private Context mContext;
        //music collection
        ArrayList<MusicCollection> musicCollectionExistingBase = new ArrayList<MusicCollection>();
        ArrayList<MusicCollection> musicCollectionLoadedBase = new ArrayList<MusicCollection>();
        ArrayList<MusicCollection> musicCollectionToDelete = new ArrayList<MusicCollection>();
        ArrayList<MusicCollection> musicCollectionToDownload = new ArrayList<MusicCollection>();
        File f;

        public MainMusicListUpdateTask() {
            Log.i(LOG_TAG, "Starting music update task");
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOG_TAG);
            mWakeLock.setReferenceCounted(false);
            mContext = AutomaticSynchronizationService.this;
        }

        @Override
        protected void onPreExecute() {
        	Log.i(LOG_TAG, "ACQUIRING WAKELOCK");
            mWakeLock.acquire();
        }

		@SuppressWarnings("unchecked")
		@Override
		protected Void doInBackground(Void... params) {
			Log.i(LOG_TAG, "load and check songs");
			try {
				//read existing base
				musicCollectionExistingBase = (ArrayList<MusicCollection>) ObjectSerializer.deserialize(sPref.getString(Constants.AUS_MAIN_LIST_BASE, ObjectSerializer.serialize(new ArrayList<MusicCollection>())));
	        	if (musicCollectionExistingBase==null)
	        		musicCollectionExistingBase = new ArrayList<MusicCollection>();
	        	
	        	//read current vk base
	        	ArrayList<Audio> musicList = api.getAudio(account.user_id, null, null, null, null, null);
	        	
	        	//if we first time sync library, set and save that all songs is loaded
	        	if (musicCollectionExistingBase.size()==0){ 
	        		for (Audio one : musicList){
	        			musicCollectionExistingBase.add(new MusicCollection(one.aid, one.owner_id, one.artist, one.title, one.duration, one.url, one.lyrics_id, 1, 1, 101));
                	}
	        		sPref.edit().putString(Constants.AUS_MAIN_LIST_BASE, ObjectSerializer.serialize(musicCollectionExistingBase)).commit();
	        	}
	        	
	        	//set up current and correct vk base 
	        	for (Audio one : musicList){
            		f = new File(sPref.getString(Constants.DOWNLOAD_DIRECTORY, android.os.Environment.getExternalStorageDirectory()+"/Music")+"/"+(one.artist+" - "+one.title+".mp3").replaceAll("[\\/:*?\"<>|]", ""));
            		if (f.exists())
            			musicCollectionLoadedBase.add(new MusicCollection(one.aid, one.owner_id, one.artist, one.title, one.duration, one.url, one.lyrics_id, 1, 1, 101));
            		else 
            			musicCollectionLoadedBase.add(new MusicCollection(one.aid, one.owner_id, one.artist, one.title, one.duration, one.url, one.lyrics_id, 0, 0, 0));
	        	}
	        	
	        	//compare our lists and catch what we need to delete
	        	for (MusicCollection oneExistingItem : musicCollectionExistingBase){
	        		boolean deleteItem = false;
	        		for (MusicCollection oneLoadedItem : musicCollectionLoadedBase){
	        			if ((oneExistingItem.aid == oneLoadedItem.aid) || (oneExistingItem.artist.equals(oneLoadedItem.artist) && oneExistingItem.title.equals(oneLoadedItem.title))){
	        				deleteItem = false;
	        				break;
	        			} else {
	        				deleteItem = true;
	        			}
	        		}
	        		if (deleteItem){
	        			Log.i(LOG_TAG, "WE NEED DELETE: "+oneExistingItem.artist+" - "+oneExistingItem.title);
	        			musicCollectionToDelete.add(oneExistingItem);
	        		}
	        	}
	        	
	        	if (musicCollectionToDelete.size()==0)
	        		Log.i(LOG_TAG, "nothing to delete");
	        	else {
	        		Log.i(LOG_TAG, "deleting songs");
	        		for (MusicCollection oneItemDelete : musicCollectionToDelete){
	        			f = new File(sPref.getString(Constants.DOWNLOAD_DIRECTORY, android.os.Environment.getExternalStorageDirectory()+"/Music")+"/"+(oneItemDelete.artist+" - "+oneItemDelete.title+".mp3").replaceAll("[\\/:*?\"<>|]", ""));
	        			if (f.exists()){
	        				f.delete();
	    					Intent intent =new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	    					intent.setData(Uri.fromFile(new File(f.getAbsolutePath())));
	    					sendBroadcast(intent);
	        			}
	        		}
	        	}
	        	
	        	//compare our lists and catch what we need to download
	        	for (MusicCollection oneLoadedItem : musicCollectionLoadedBase){
	        		boolean addItem = false;
	        		for (MusicCollection oneExistingItem : musicCollectionExistingBase){
	        			if ((oneLoadedItem.aid == oneExistingItem.aid) || (oneLoadedItem.artist.equals(oneExistingItem.artist) && oneLoadedItem.title.equals(oneExistingItem.title))){
	        				addItem = false;
	        				break;
	        			} else {
	        				addItem = true;
	        			}
	        		}
	        		if (addItem){
	        			Log.i(LOG_TAG, "WE NEED TO DOWNLOAD: "+oneLoadedItem.artist+" - "+oneLoadedItem.title);
	        			musicCollectionToDownload.add(oneLoadedItem);
	        		}
	        	}
	        	
	        	if (musicCollectionToDownload.size()==0)
	        		Log.i(LOG_TAG, "nothing to download");
	        	else {
	        		if (!isMyServiceRunning(DownloadService.class)){
	        			Log.i(LOG_TAG, "start downloading");
	        			//save the list to dm
	    	        	sPref.edit().putString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(musicCollectionToDownload)).commit();
	        			
	  	    		  	//start service
	  	    		  	Intent serviceIntent = new Intent(mContext, DownloadService.class); 
	  	    		  	mContext.startService(serviceIntent);
	  	    	  	} else {
	  	    	  		Log.i(LOG_TAG, "service is already running do it next time");
	  	    	  	}
	        	}
	        	
	        	//update existing base with new realies 
	        	sPref.edit().putString(Constants.AUS_MAIN_LIST_BASE, ObjectSerializer.serialize(musicCollectionLoadedBase)).commit();
	        	
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.i(LOG_TAG, "successfully doed stuff");
			finish(true);
		}

		@Override
		protected void onCancelled() {
			Log.i(LOG_TAG, "error in stuff, cancel");
			finish(false);
		}

		private void finish(boolean success) {
			if (success) {
				Log.i(LOG_TAG, "Music list successfully updated");
				if (sPref.getBoolean(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION, false))	
					scheduleUpdate(mContext, Integer.parseInt(sPref.getString(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION_FREQUENCY, getString(R.string.prefs_aus_freq_default_value))));
			} else if (isCancelled()) {
				// cancelled, likely due to lost network - we'll get restarted
				// when network comes back
			} else {
				// failure, schedule next download in 30 minutes
				Log.i(LOG_TAG, "Music list refresh failed, scheduling update in 30 minutes");
				long interval = 30 * 60 * 1000;
				if (sPref.getBoolean(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION, false))		
					scheduleUpdate(mContext, interval);
			}

			Log.i(LOG_TAG, "RELEASING WAKELOCK");
			mWakeLock.release();
			stopSelf();
		}
	}
    
    private static void scheduleUpdate(Context context, long timeFromNow) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long due = System.currentTimeMillis() + timeFromNow;

        Log.i(LOG_TAG, "Scheduling next update at " + new Date(due));
        am.set(AlarmManager.RTC_WAKEUP, due, getUpdateIntent(context));
    }
    
    public static PendingIntent getUpdateIntent(Context context) {
        Intent i = new Intent(context, AutomaticSynchronizationService.class);
        return PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    public static void cancelUpdates(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getUpdateIntent(context));
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
