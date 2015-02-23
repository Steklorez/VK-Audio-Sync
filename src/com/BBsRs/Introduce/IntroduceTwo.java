package com.BBsRs.Introduce;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView.BufferType;

import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class IntroduceTwo extends Activity {

	//preferences 
    SharedPreferences sPref;
    
    //with this options we will load images
    DisplayImageOptions options ;
    
    TextView textAgreement;
    AlertDialog alert = null;	
    
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
	    textAgreement = (TextView)this.findViewById(R.id.textagreement);
	    
	    name.setText(String.format(getString(R.string.intro_user_personal_welcome), sPref.getString(Constants.USER_FIRST_NAME, "Undefined")));
        ImageLoader.getInstance().displayImage(sPref.getString(Constants.USER_AVATAR, "http://vk.com/images/deactivated_100.gif"), userAvatar, options);
        
        //set license
	    String sentence = getResources().getString(R.string.clickable_string);
	    textAgreement.setMovementMethod(LinkMovementMethod.getInstance());
	    textAgreement.setText(addClickablePart(sentence), BufferType.SPANNABLE);
	}
	
	private SpannableStringBuilder addClickablePart(String str) {
	    SpannableStringBuilder ssb = new SpannableStringBuilder(str);

	    int idx1 = str.indexOf("[");
	    int idx2 = 0;
	    while (idx1 != -1) {
	        idx2 = str.indexOf("]", idx1) + 1;

	        final String clickString = str.substring(idx1, idx2);
	        ssb.setSpan(new ClickableSpan() {

	            @Override
	            public void onClick(View widget) {
	            	showLicenseDialog();
	            }
	        }, idx1, idx2, 0);
	        idx1 = str.indexOf("[", idx2);
	    }

	    return ssb;
	 }
	
	private void showLicenseDialog(){
		final Context context = IntroduceTwo.this; 								// create context
		AlertDialog.Builder build = new AlertDialog.Builder(context); 				// create build for alert dialog
		build.setTitle(getResources().getString(R.string.agreement_top)); 					// set title
		build.setMessage(getApplicationContext().getResources().getString(R.string.intro_agreement));
		build.setCancelable(true);
		build.setNegativeButton(getString(R.string.ok), null);
		alert = build.create();															// show dialog
		alert.show();
	}
}
