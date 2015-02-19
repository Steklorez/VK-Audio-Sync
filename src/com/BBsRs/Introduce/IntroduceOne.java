package com.BBsRs.Introduce;

import java.util.ArrayList;
import java.util.Collection;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Button;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.BBsRs.vkaudiosync.LoginActivity;
import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.VKApiThings.Account;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.perm.kate.api.Api;
import com.perm.kate.api.User;

public class IntroduceOne extends Activity {
	
	private final int REQUEST_LOGIN=1;
	
	Account account = new Account();
	Api api;
	
	//preferences 
    SharedPreferences sPref;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    this.setContentView(R.layout.intro_one);
	    
	    Button login = (Button)this.findViewById(R.id.login);
	    
	    login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
	            intent.setClass(getApplicationContext(), LoginActivity.class);
	            startActivityForResult(intent, REQUEST_LOGIN);
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
                
                //save user first name
				try {
					Collection<Long> u = new ArrayList<Long>();
		            u.add(account.user_id);
		            Collection<String> d = new ArrayList<String>();
		            d.add("");
		            
					User userOne = api.getProfiles(u, d, "", "", "", "").get(0);
					sPref.edit().putString(Constants.USER_FIRST_NAME, userOne.first_name).commit();
				} catch (Exception e) {
					e.printStackTrace();
				}
                
                
                //start second part
                startActivity(new Intent(getApplicationContext(), IntroduceTwo.class));
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                finish();
            }
        }
    }

}
