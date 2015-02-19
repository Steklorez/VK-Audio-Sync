package com.BBsRs.vkaudiosync;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.BBsRs.Introduce.IntroduceOne;
import com.BBsRs.vkaudiosync.Services.AutomaticSynchronizationService;
import com.BBsRs.vkaudiosync.VKApiThings.Account;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.perm.kate.api.Api;

public class LoaderActivity extends Activity {

	Account account = new Account();
	Api api;

	// for timer
	private timer CountDownTimer;
	
	private final int REQUEST_LOGIN=1;
	
	//preferences 
    SharedPreferences sPref;

	public class timer extends CountDownTimer {
		public timer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
//			if(api!=null){
//
//				try {
//		        	Intent refresh = new Intent(getApplicationContext(), ContentShowActivity.class);
//  			        refresh.putExtra(Constants.INITIAL_PAGE, Constants.MUSIC_LIST_FRAGMENT);
//  					//restart activity
//  					startActivity(refresh);   
//  					// stop curr activity
//  					finish();
//		        } catch (Exception e) {
//		            e.printStackTrace();
//		        }
//	        }else{
//	        	Intent intent = new Intent();
//	            intent.setClass(getApplicationContext(), LoginActivity.class);
//	            startActivityForResult(intent, REQUEST_LOGIN);
//	        }
			
			Intent intent = new Intent();
            intent.setClass(getApplicationContext(), IntroduceOne.class);
            startActivity(intent);
		}

		@Override
		public void onTick(long arg0) {
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.loader);
		
        //set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        
		// Восстановление сохранённой сессии
		account.restore(this);

		// Если сессия есть создаём API для обращения к серверу
		if (account.access_token != null)
			api = new Api(account.access_token, Constants.API_ID);
		
		if (api !=null){
			if (!isMyServiceRunning(AutomaticSynchronizationService.class) && sPref.getBoolean(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION, true)){
        		getApplicationContext().startService(new Intent(getApplicationContext(), AutomaticSynchronizationService.class));
        	}
		}

		CountDownTimer = new timer(3000, 1000); // timer to 2 seconds (tick one
												// second)
		CountDownTimer.start(); // start timer
		// TODO Auto-generated method stub
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                //авторизовались успешно 
                account.access_token=data.getStringExtra("token");
                account.user_id=data.getLongExtra("user_id", 0);
                account.save(LoaderActivity.this);
                api=new Api(account.access_token, Constants.API_ID);
                activityRefresh();
            }
        }
    }
	
	private void activityRefresh(){
		Intent refresh = new Intent(getApplicationContext(), LoaderActivity.class);
		//restart activity
	    startActivity(refresh);   
	    //set no animation
	    overridePendingTransition(0, 0);
	    // stop curr activity
	    finish();
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
