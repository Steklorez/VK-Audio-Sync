package com.BBsRs.vkaudiosync;

import org.holoeverywhere.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.BBsRs.vkaudiosync.VKApiThings.Account;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.perm.kate.api.Api;

public class LoaderActivity extends Activity {

	Account account = new Account();
	Api api;

	// for timer
	private timer CountDownTimer;
	
	private final int REQUEST_LOGIN=1;

	public class timer extends CountDownTimer {
		public timer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			if(api!=null){
	            Intent refresh = new Intent(getApplicationContext(), ContentShowActivity.class);
				//restart activity
			    startActivity(refresh);   
			    // stop curr activity
			    finish();
	        }else{
	        	Intent intent = new Intent();
	            intent.setClass(getApplicationContext(), LoginActivity.class);
	            startActivityForResult(intent, REQUEST_LOGIN);
	        }
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
		// Восстановление сохранённой сессии
		account.restore(this);

		// Если сессия есть создаём API для обращения к серверу
		if (account.access_token != null)
			api = new Api(account.access_token, Constants.API_ID);

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

}
