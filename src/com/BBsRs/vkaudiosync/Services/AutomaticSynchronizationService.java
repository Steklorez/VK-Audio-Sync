package com.BBsRs.vkaudiosync.Services;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.BBsRs.vkaudiosync.ContentShowActivity;
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
        int successfullyDeleted = 0;

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
			
			if (sPref.getBoolean(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION_WIFI, true)){
				ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (!mWifi.isConnected()) {
					Log.i(LOG_TAG, "no wifi connection");
					return null;
				}
			}
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
	    					successfullyDeleted++;
	        			}
	        			//delete from existing base
	        			ArrayList<MusicCollection> musicCollectionTemp = (ArrayList<MusicCollection>) ObjectSerializer.deserialize(sPref.getString(Constants.AUS_MAIN_LIST_BASE, ObjectSerializer.serialize(new ArrayList<MusicCollection>())));
	        			if (musicCollectionTemp==null)
	                		musicCollectionTemp = new ArrayList<MusicCollection>();
	        					int indexTemp=0;
	        				for (MusicCollection one: musicCollectionTemp){
	        					if ((one.aid==oneItemDelete.aid || (one.title.equals(oneItemDelete.title) && one.artist.equals(oneItemDelete.artist)))){
	        						musicCollectionTemp.remove(indexTemp);
	        						break;
	        					}
	        					indexTemp++;
	        				}
	                	
	        			sPref.edit().putString(Constants.AUS_MAIN_LIST_BASE, ObjectSerializer.serialize(musicCollectionTemp)).commit();
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
	        	
	        	if (musicCollectionToDownload.size()==0){
	        		Log.i(LOG_TAG, "nothing to download");
	        		if (sPref.getBoolean(Constants.PREFERENCE_NOTIFY_RESULT, true) && successfullyDeleted!=0){
	        			// define sound URI, the sound to be played when there's a notification
	        			Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	    			
	        			// define intent to open main page
	        			PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(mContext, ContentShowActivity.class).putExtra(Constants.INITIAL_PAGE, Constants.MUSIC_LIST_FRAGMENT), Notification.FLAG_ONLY_ALERT_ONCE);        
	    		
	        			NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	        			NotificationCompat.Builder mBuilder  = new NotificationCompat.Builder(mContext);
	        			
	        			mBuilder.setContentTitle(getString(R.string.app_name))
	        			.setContentText(getString(R.string.notify_downloaded)+" "+0+" "+getString(R.string.notify_deleted)+" "+successfullyDeleted)
	        			.setSmallIcon(R.drawable.ic_menu_download)
	        			.setContentIntent(contentIntent)
	        			.setOngoing(false)
	        			.setSound(soundUri);
	        			mNotificationManager.notify(100, mBuilder.build());
	        		}
	        	}
	        	else {
	        		if (!isMyServiceRunning(DownloadService.class)){
	        			Log.i(LOG_TAG, "start downloading");
	        			//save the list to dm
	    	        	sPref.edit().putString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(musicCollectionToDownload)).commit();
	        			
	  	    		  	//start service
	  	    		  	Intent serviceIntent = new Intent(mContext, DownloadService.class); 
	  	    		  	serviceIntent.putExtra(Constants.INTENT_SUCCESSFULLY_DELETED, successfullyDeleted);
	  	    		  	mContext.startService(serviceIntent);
	  	    	  	} else {
	  	    	  		Log.i(LOG_TAG, "service is already running do it next time");
	  	    	  	}
	        	}
			} catch (Exception e) {
				this.cancel(false);
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
				if (sPref.getBoolean(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION, true))	
					scheduleUpdate(mContext, Integer.parseInt(sPref.getString(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION_FREQUENCY, getString(R.string.prefs_aus_freq_default_value))));
			} else {
				// failure, schedule next download in 30 minutes
				Log.i(LOG_TAG, "Music list refresh failed, scheduling update in 30 minutes");
				long interval = 30 * 60 * 1000;
				if (sPref.getBoolean(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION, true))		
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
