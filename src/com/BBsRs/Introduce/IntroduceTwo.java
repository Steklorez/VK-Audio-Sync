package com.BBsRs.Introduce;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.TextView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class IntroduceTwo extends Activity {

	//preferences 
    SharedPreferences sPref;
    
    //with this options we will load images
    DisplayImageOptions options ;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    if(getResources().getBoolean(R.bool.portrait_only)){
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    }
	    
	    this.setContentView(R.layout.intro_two);
	    
	    //set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
	    
	    Button next = (Button)this.findViewById(R.id.next);
	    Button back = (Button)this.findViewById(R.id.back);
	    
	    next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), IntroduceThree.class));
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				finish();
			}
		});
	    
	    back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), IntroduceOne.class));
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				finish();
			}
		});
	    
	    //init image loader
        options = new DisplayImageOptions.Builder()
        .showStubImage(R.drawable.deactivated_100)
        //.showImageForEmptyUri(R.drawable.logo)
        .cacheOnDisc(true)	
        .cacheInMemory(true)					
        .build();
	    
	    TextView name = (TextView)this.findViewById(R.id.text_name);
	    ImageView userAvatar = (ImageView)this.findViewById(R.id.user_avatar);
	    
	    name.setText(String.format(getString(R.string.intro_user_personal_welcome), sPref.getString(Constants.USER_FIRST_NAME, "Undefined")));
        ImageLoader.getInstance().displayImage(sPref.getString(Constants.USER_AVATAR, "http://vk.com/images/deactivated_100.gif"), userAvatar, options);
	}
}
