package com.BBsRs.vkaudiosync;

import org.holoeverywhere.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.BBsRs.vkaudiosync.VKApiThings.Account;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.perm.kate.api.Api;

public class LoaderAcrivity extends Activity {

	Account account = new Account();
	Api api;

	// for timer
	private timer CountDownTimer;

	public class timer extends CountDownTimer {
		public timer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			if(api!=null){
	            Intent refresh = new Intent(getApplicationContext(), MusicListActivity.class);
				//restart activity
			    startActivity(refresh);   
			    // stop curr activity
			    finish();
	        }else{
	            Intent refresh = new Intent(getApplicationContext(), LoginActivity.class);
				//restart activity
			    startActivity(refresh);   
			    // stop curr activity
			    finish();
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

}
