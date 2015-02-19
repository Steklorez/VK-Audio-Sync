package com.BBsRs.Introduce;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.TextView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.BBsRs.vkaudiosync.ContentShowActivity;
import com.BBsRs.vkaudiosync.DirChooseActivity;
import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;

public class IntroduceFive extends Activity {

	//preferences 
    SharedPreferences sPref;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    this.setContentView(R.layout.intro_five);
	    
	    //set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
	    
	    Button next = (Button)this.findViewById(R.id.next);
	    Button back = (Button)this.findViewById(R.id.back);
	    
	    next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), ContentShowActivity.class));
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
				finish();
			}
		});
	    
	    back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), IntroduceFour.class));
				overridePendingTransition(R.anim.push_left_out, R.anim.push_left_in);
				finish();
			}
		});
	}

}
