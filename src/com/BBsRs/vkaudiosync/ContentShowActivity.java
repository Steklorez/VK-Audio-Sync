package com.BBsRs.vkaudiosync;

import java.util.Calendar;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.addon.AddonSlider;
import org.holoeverywhere.addon.Addons;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.slider.SliderMenu;
import org.holoeverywhere.widget.CheckBox;
import org.holoeverywhere.widget.RelativeLayout;
import org.holoeverywhere.widget.Toast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.BBsRs.vkaudiosync.Fragments.AboutFragment;
import com.BBsRs.vkaudiosync.Fragments.DownloadManagerFragment;
import com.BBsRs.vkaudiosync.Fragments.FriendsGroupsListFragment;
import com.BBsRs.vkaudiosync.Fragments.MusicListFragment;
import com.BBsRs.vkaudiosync.Fragments.SettingsFragment;
import com.BBsRs.vkaudiosync.VKApiThings.Account;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.perm.kate.api.Api;

@Addons(AddonSlider.class)
public class ContentShowActivity extends Activity {
	public AddonSlider.AddonSliderA addonSlider() {
	      return addon(AddonSlider.class);
	}
	
	//preferences 
    SharedPreferences sPref;
	
	SliderMenu sliderMenu;
	
    /*----------------------------VK API-----------------------------*/
    Account account=new Account();
    Api api;
    /*----------------------------VK API-----------------------------*/
    
    //for retrieve data from activity
    Bundle bundle;
    
    //alert dialog
    AlertDialog alert = null;
    
    private final Handler handler = new Handler();
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    //set icon
	    getSupportActionBar().setIcon(R.drawable.ic_logo_small);
	    
	   	/*----------------------------VK API-----------------------------*/
    	//retrieve old session
        account.restore(getApplicationContext());
        
        //create new session
        if(account.access_token!=null)
            api=new Api(account.access_token, Constants.API_ID);
        /*----------------------------VK API-----------------------------*/
        
	    //init slider menu
        sliderMenu = addonSlider().obtainDefaultSliderMenu(R.layout.menu);
        
        //set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        
        Bundle bundleMyMusic = new Bundle();
        bundleMyMusic.putLong(Constants.BUNDLE_USER_ID, account.user_id);
        bundleMyMusic.putInt(Constants.BUNDLE_MUSIC_TYPE, Constants.MAIN_MUSIC_USER);
        bundleMyMusic.putInt(Constants.BUNDLE_MAIN_WALL_TYPE, Constants.MAIN_MUSIC);
        
        Bundle bundleFriends = new Bundle();
        bundleFriends.putLong(Constants.BUNDLE_USER_ID, account.user_id);
        bundleFriends.putInt(Constants.BUNDLE_FRIENDS_GROUPS_TYPE, Constants.FRIENDS);
        
        Bundle bundleGroups = new Bundle();
        bundleGroups.putLong(Constants.BUNDLE_USER_ID, account.user_id);
        bundleGroups.putInt(Constants.BUNDLE_FRIENDS_GROUPS_TYPE, Constants.GROUPS);
        
        sliderMenu.add(getResources().getStringArray(R.array.slider_menu)[0].toUpperCase()).setCustomLayout(R.layout.custom_slider_menu_item).clickable(false).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.slider_menu)[1], MusicListFragment.class, bundleMyMusic, new int[]{R.color.slider_menu_custom_color_black, R.color.slider_menu_custom_color_orange}).setTextAppereanceInverse(1);
        sliderMenu.add(getResources().getStringArray(R.array.slider_menu)[2], FriendsGroupsListFragment.class, bundleFriends, new int[]{R.color.slider_menu_custom_color_black, R.color.slider_menu_custom_color_orange}).setTextAppereanceInverse(1);
        sliderMenu.add(getResources().getStringArray(R.array.slider_menu)[3], FriendsGroupsListFragment.class, bundleGroups, new int[]{R.color.slider_menu_custom_color_black, R.color.slider_menu_custom_color_orange}).setTextAppereanceInverse(1);
        sliderMenu.add(getResources().getStringArray(R.array.slider_menu)[4].toUpperCase()).setCustomLayout(R.layout.custom_slider_menu_item).clickable(false).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.slider_menu)[5], DownloadManagerFragment.class, new int[]{R.color.slider_menu_custom_color_black, R.color.slider_menu_custom_color_orange}).setTextAppereanceInverse(1);
        sliderMenu.add(getResources().getStringArray(R.array.slider_menu)[6], SettingsFragment.class, new int[]{R.color.slider_menu_custom_color_black, R.color.slider_menu_custom_color_orange}).setTextAppereanceInverse(1);
        sliderMenu.add(getResources().getStringArray(R.array.slider_menu)[7], AboutFragment.class, new int[]{R.color.slider_menu_custom_color_black, R.color.slider_menu_custom_color_orange}).setTextAppereanceInverse(1);
        
        bundle = getIntent().getExtras(); 

        if (bundle != null) {
        	if (savedInstanceState == null)
                sliderMenu.setCurrentPage(bundle.getInt(Constants.INITIAL_PAGE));
        } else {
        	if (savedInstanceState == null)
                sliderMenu.setCurrentPage(1);
        }
        
        showNotification();
	}
	
	//show an sponsor's to app
	public void showNotification(){
		//init all dates
		Calendar firstLaunchDate = Calendar.getInstance();
		firstLaunchDate.setTimeInMillis(sPref.getLong(Constants.FIRST_LAUNCH_TIME, 0));
		
		Calendar shownNotification = Calendar.getInstance();
		shownNotification.setTimeInMillis(sPref.getLong(Constants.SHOWN_NOTIFICATION, 0));
		
		Calendar currentDate = Calendar.getInstance();
		currentDate.setTimeInMillis(System.currentTimeMillis());
		
		//add 3 days to shown notification
		shownNotification.add(Calendar.DATE, +2);
		
		if ((shownNotification.before(currentDate)) && ((!sPref.getBoolean(Constants.CLICKED_REVIEW, false)) || (!sPref.getBoolean(Constants.CLICKED_SUBSCRIBE, false))) && (!sPref.getBoolean(Constants.DONT_SHOW_AGAIN, false))){
			sPref.edit().putLong(Constants.SHOWN_NOTIFICATION, System.currentTimeMillis()).commit();
			
			//end of trial try to buy it
 			final Context context = ContentShowActivity.this; 								// create context
 			AlertDialog.Builder build = new AlertDialog.Builder(context); 				// create build for alert dialog
    		
    		LayoutInflater inflater = (LayoutInflater)context.getSystemService
    			      (Context.LAYOUT_INFLATER_SERVICE);
    		
    		View content = inflater.inflate(R.layout.dialog_content_sponsor, null);
    		
    		CheckBox dontShowAgain = (CheckBox)content.findViewById(R.id.dontshow);
    		dontShowAgain.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					sPref.edit().putBoolean(Constants.DONT_SHOW_AGAIN, isChecked).commit();
				}
    		});
    		
    		final RelativeLayout joinVK = (RelativeLayout)content.findViewById(R.id.join_vk_group);
    		if (sPref.getBoolean(Constants.CLICKED_SUBSCRIBE, false))
    			joinVK.setVisibility(View.GONE);
    		joinVK.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					sPref.edit().putBoolean(Constants.CLICKED_SUBSCRIBE, true).commit();
					
					new Thread (new Runnable(){
						@Override
						public void run() {
							try {
								api.joinGroup(Constants.GROUP_ID, null, null);
								handler.post(new Runnable (){
									@Override
									public void run() {
										Toast.makeText(getApplicationContext(), getString(R.string.join_group_success), Toast.LENGTH_LONG).show();
									}
								});
							} catch (Exception e) {
								handler.post(new Runnable (){
									@Override
									public void run() {
										Toast.makeText(getApplicationContext(), getString(R.string.error_message), Toast.LENGTH_LONG).show();
									}
								});
								e.printStackTrace();
							}
						}
					}).start();
					joinVK.setVisibility(View.GONE);
					if (sPref.getBoolean(Constants.CLICKED_REVIEW, false))
						alert.dismiss();
				}
			});
    		
    		final RelativeLayout makeReview = (RelativeLayout)content.findViewById(R.id.make_review);
    		if (sPref.getBoolean(Constants.CLICKED_REVIEW, false))
    			makeReview.setVisibility(View.GONE);
    		makeReview.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					sPref.edit().putBoolean(Constants.CLICKED_REVIEW, true).commit();
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("market://search/?q=pname:com.BBsRs.vkaudiosync"));
					startActivity(intent);
					makeReview.setVisibility(View.GONE);
					if (sPref.getBoolean(Constants.CLICKED_SUBSCRIBE, false))
						alert.dismiss();
				}
			});
    		build.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					alert.dismiss();
				}
			});
    		build.setView(content);
    		alert = build.create();															// show dialog
    		alert.show();
		}
		
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(openMenuDrawer);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		//turn up download receiver
        registerReceiver(openMenuDrawer, new IntentFilter(Constants.OPEN_MENU_DRAWER));
	}
	
	private BroadcastReceiver openMenuDrawer = new BroadcastReceiver() {

	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if (addonSlider().isDrawerOpen(addonSlider().getLeftView()))
	    		  addonSlider().closeLeftView();
	    	  else 
	    		  addonSlider().openLeftView();
	    }
	};

}
