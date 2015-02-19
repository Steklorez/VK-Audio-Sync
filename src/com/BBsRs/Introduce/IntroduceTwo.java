package com.BBsRs.Introduce;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.TextView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.BBsRs.vkaudiosync.DirChooseActivity;
import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;

public class IntroduceTwo extends Activity {

	//preferences 
    SharedPreferences sPref;
    
    TextView directoryText;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    this.setContentView(R.layout.intro_two);
	    
	    //set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
	    
	    Button next = (Button)this.findViewById(R.id.next);
	    Button back = (Button)this.findViewById(R.id.back);
	    
	    next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), IntroduceThree.class));
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
				finish();
			}
		});
	    
	    back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), IntroduceOne.class));
				overridePendingTransition(R.anim.push_left_out, R.anim.push_left_in);
				finish();
			}
		});
	    
	    Button directory = (Button)this.findViewById(R.id.directory);
	    directoryText = (TextView)this.findViewById(R.id.directory_text);
	    TextView message = (TextView)this.findViewById(R.id.text_message);
	    
	    directory.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), DirChooseActivity.class));
			}
		});
	    
	    message.setText(String.format(getString(R.string.intro_3), sPref.getString(Constants.USER_FIRST_NAME, "Roman")));
	}
	
	@Override
	public void onResume() {
		super.onResume();
		directoryText.setText(sPref.getString(Constants.DOWNLOAD_DIRECTORY, android.os.Environment.getExternalStorageDirectory()+"/Music/"+getString(R.string.app_name))+"/");
	}

}
