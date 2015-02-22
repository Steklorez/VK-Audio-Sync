package com.BBsRs.vkaudiosync;

import java.util.Calendar;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.RelativeLayout;
import org.holoeverywhere.widget.Toast;
import org.jsoup.Jsoup;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;

import com.BBsRs.Introduce.IntroduceOne;
import com.BBsRs.vkaudiosync.Services.AutomaticSynchronizationService;
import com.BBsRs.vkaudiosync.VKApiThings.Account;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.perm.kate.api.Api;

public class LoaderActivity extends Activity {

	Account account = new Account();
	Api api;

	// for timer
	private timer CountDownTimer;
	
	private final Handler handler = new Handler();
	
	//preferences 
    SharedPreferences sPref;
    
    //alert dialog
    AlertDialog alert = null;
    
    /*--------------------INIT IN APP BILLING-------------------------*/
    //inAppBillingData
    // PRODUCT & SUBSCRIPTION IDS
	private BillingProcessor bp;
	private boolean readyToPurchase = false;
	/*--------------------INIT IN APP BILLING-------------------------*/

	public class timer extends CountDownTimer {
		public timer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			if(api!=null){
				new Thread (new Runnable(){
					@Override
					public void run() {
						int launch = 2;
						try {
							launch = Integer.parseInt(Jsoup.connect(Constants.LAUNCH_RULE).userAgent(getResources().getString(R.string.user_agent)).timeout(getResources().getInteger(R.integer.user_timeout)).get().text());
						} catch (Exception e) {
							launch = 2;
							e.printStackTrace();
						}
						
						int paid = 2;
						try {
							paid = Integer.parseInt(Jsoup.connect(Constants.PAID_RULE).userAgent(getResources().getString(R.string.user_agent)).timeout(getResources().getInteger(R.integer.user_timeout)).get().text());
						} catch (Exception e) {
							paid = 2;
							e.printStackTrace();
						}
						
						if (launch==2){
							
							if (paid==2 && isItsTimeToChoose()){
								//we need show uncancleable dialog with buy app interface
								handler.post(new Runnable(){
									@Override
									public void run() {
										showDialog();
									}
								});
							} else {
								//start application its free at this moment still
								Intent refresh = new Intent(getApplicationContext(), ContentShowActivity.class);
								refresh.putExtra(Constants.INITIAL_PAGE, Constants.MUSIC_LIST_FRAGMENT);
								//restart activity
								startActivity(refresh);   
								// stop curr activity
		  						finish();
							}
						} else {
							handler.post(new Runnable (){
								@Override
								public void run() {
									Toast.makeText(getApplicationContext(), "Sorry service is unavailable", Toast.LENGTH_LONG).show();
								}
							});
							// stop curr activity
		  					finish();
						}
	  					
					}
				}).start();

	        }else{
	        	Intent intent = new Intent();
	            intent.setClass(getApplicationContext(), IntroduceOne.class);
	            startActivity(intent);
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
		
        //set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        
        /*--------------------INIT IN APP BILLING-------------------------*/
        bp = new BillingProcessor(this, Constants.LICENSE_KEY, new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(String productId, TransactionDetails details) {
            	activityRefresh();
            }
            @Override
            public void onBillingError(int errorCode, Throwable error) {
            	Toast.makeText(getApplicationContext(), "Sorry service is unavailable", Toast.LENGTH_LONG).show();
            	finish();
            }
            @Override
            public void onBillingInitialized() {
                readyToPurchase = true;
                bp.loadOwnedPurchasesFromGoogle();
                startMainTask();
            }
            @Override
            public void onPurchaseHistoryRestored() {
            }
        });
        /*--------------------INIT IN APP BILLING-------------------------*/

	}
	
	public void startMainTask(){
		bp.consumePurchase(Constants.BUY_ITEM_HIGH);
		
		// Восстановление сохранённой сессии
		account.restore(this);

		// Если сессия есть создаём API для обращения к серверу
		if (account.access_token != null)
			api = new Api(account.access_token, Constants.API_ID);
		
		if (api !=null){
			if (!isMyServiceRunning(AutomaticSynchronizationService.class) && sPref.getBoolean(Constants.PREFERENCE_AUTOMATIC_SYNCHRONIZATION, true)){
        		getApplicationContext().startService(new Intent(getApplicationContext(), AutomaticSynchronizationService.class));
        	}
			CountDownTimer = new timer(3000, 1000); // timer to 2 seconds (tick one // second)
			CountDownTimer.start(); // start timer
		} else{
			Intent intent = new Intent();
            intent.setClass(getApplicationContext(), IntroduceOne.class);
            startActivity(intent);
            // stop curr activity
			finish();
		}
	}
	
	//show an sponsor's to app
	public void showDialog(){
		//end of trial try to buy it
		final Context context = LoaderActivity.this; 								// create context
		AlertDialog.Builder build = new AlertDialog.Builder(context); 				// create build for alert dialog
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService
			      (Context.LAYOUT_INFLATER_SERVICE);
		
		View content = inflater.inflate(R.layout.dialog_content_buy, null);
		
		final RelativeLayout buyApp = (RelativeLayout)content.findViewById(R.id.buy_app);
		buyApp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (readyToPurchase)
					bp.purchase(Constants.BUY_ITEM_HIGH);
			}
		});

		build.setView(content);
		alert = build.create();															// show dialog
		alert.setCanceledOnTouchOutside(false);
		alert.setOnCancelListener(new OnCancelListener(){
			@Override
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		});
		alert.show();
	}
	
	public boolean isItsTimeToChoose(){
		//if user on high
		if (bp.isPurchased(Constants.BUY_ITEM_HIGH))	
			return false;
		
		//init all dates
		Calendar firstLaunchDate = Calendar.getInstance();
		firstLaunchDate.setTimeInMillis(sPref.getLong(Constants.FIRST_LAUNCH_TIME, 0));
		
		Calendar currentDate = Calendar.getInstance();
		currentDate.setTimeInMillis(System.currentTimeMillis());
		
		
		//add 10 days to first launch
		firstLaunchDate.add(Calendar.DATE, +9);
		
		if (firstLaunchDate.before(currentDate))
			return true;
		else
			return false;
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
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
	
    @Override
    public void onDestroy() {
        if (bp != null) 
            bp.release();

        super.onDestroy();
    }

}
