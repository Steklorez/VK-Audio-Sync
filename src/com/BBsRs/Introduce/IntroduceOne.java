package com.BBsRs.Introduce;

import java.util.ArrayList;
import java.util.Collection;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Button;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.VKApiThings.Account;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.perm.kate.api.Api;
import com.perm.kate.api.User;

public class IntroduceOne extends Activity {
	
	private final int REQUEST_LOGIN=1;
	
	Account account = new Account();
	Api api;
	
	private final Handler handler = new Handler();
	
	//preferences 
    SharedPreferences sPref;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    if(getResources().getBoolean(R.bool.portrait_only)){
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    }
	    
	    this.setContentView(R.layout.intro_one);
	    
	    //set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
	    
	    Button login = (Button)this.findViewById(R.id.login);
	    
	    login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				Intent intent = new Intent();
//	            intent.setClass(getApplicationContext(), LoginActivity.class);
//	            startActivityForResult(intent, REQUEST_LOGIN);
				
				//start second part
                startActivity(new Intent(getApplicationContext(), IntroduceTwo.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
			}
		});
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
            	
            	//авторизовались успешно 
                account.access_token=data.getStringExtra("token");
                account.user_id=data.getLongExtra("user_id", 0);
                account.save(IntroduceOne.this);
                api=new Api(account.access_token, Constants.API_ID);
                
            	new Thread(new Runnable(){
					@Override
					public void run() {
						//save user first name and avatar
						try {
							Collection<Long> u = new ArrayList<Long>();
				            u.add(account.user_id);
				            Collection<String> d = new ArrayList<String>();
				            d.add("");
				            
							User userOne = api.getProfiles(u, d, "photo_100", "", "", "").get(0);
							sPref.edit().putString(Constants.USER_AVATAR, userOne.photo_medium_rec).commit();
							sPref.edit().putString(Constants.USER_FIRST_NAME, userOne.first_name).commit();
							secondPart();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
        		}).start();
                
            }
        }
    }
	
	public void secondPart(){
		final Runnable updaterText = new Runnable() {
	        public void run() {
	        	//start second part
                startActivity(new Intent(getApplicationContext(), IntroduceTwo.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
	        }
	    };
	    handler.post(updaterText);
	}

}
