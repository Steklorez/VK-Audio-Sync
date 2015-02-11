package com.BBsRs.vkaudiosync.Services;

import java.util.Date;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class AutomaticSynchronizationService extends Service {
	
	private static String LOG_TAG = "AUS Service";
	
	private MainMusicListUpdateTask mTask;
	
	//preferences 
    SharedPreferences sPref;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Log.i(LOG_TAG, "start command");
    	
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

		@Override
		protected Void doInBackground(Void... params) {
			Log.i(LOG_TAG, "doing stuff");
			try {
				Thread.sleep((int) (0.25 * 60 * 1000)); // half half minute sleep
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
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
}
