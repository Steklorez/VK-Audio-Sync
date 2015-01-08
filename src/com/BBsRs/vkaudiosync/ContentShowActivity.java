package com.BBsRs.vkaudiosync;

import org.holoeverywhere.addon.AddonSlider;
import org.holoeverywhere.addon.Addons;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.slider.SliderMenu;

import android.os.Bundle;
import android.view.MenuItem;

import com.BBsRs.vkaudiosync.Fragments.FriendsGroupsListFragment;
import com.BBsRs.vkaudiosync.Fragments.MusicListFragment;
import com.BBsRs.vkaudiosync.VKApiThings.Account;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.perm.kate.api.Api;

@Addons(AddonSlider.class)
public class ContentShowActivity extends Activity {
	public AddonSlider.AddonSliderA addonSlider() {
	      return addon(AddonSlider.class);
	}
	
	SliderMenu sliderMenu;
	
    /*----------------------------VK API-----------------------------*/
    Account account=new Account();
    Api api;
    /*----------------------------VK API-----------------------------*/
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	   	/*----------------------------VK API-----------------------------*/
    	//retrieve old session
        account.restore(getApplicationContext());
        /*----------------------------VK API-----------------------------*/
        
	    //init slider menu
        sliderMenu = addonSlider().obtainDefaultSliderMenu(R.layout.menu);
        addonSlider().setOverlayActionBar(false);
        
        Bundle bundleMyMusic = new Bundle();
        bundleMyMusic.putLong(Constants.BUNDLE_USER_ID, account.user_id);
        bundleMyMusic.putInt(Constants.BUNDLE_MUSIC_TYPE, Constants.MAIN_MUSIC_USER);
        
        Bundle bundlePopular = new Bundle();
        bundlePopular.putLong(Constants.BUNDLE_USER_ID, account.user_id);
        bundlePopular.putInt(Constants.BUNDLE_MUSIC_TYPE, Constants.POPULAR);
        
        Bundle bundleRecommendations = new Bundle();
        bundleRecommendations.putLong(Constants.BUNDLE_USER_ID, account.user_id);
        bundleRecommendations.putInt(Constants.BUNDLE_MUSIC_TYPE, Constants.RECOMMENDATIONS);
        
        Bundle bundleFriends = new Bundle();
        bundleFriends.putLong(Constants.BUNDLE_USER_ID, account.user_id);
        bundleFriends.putInt(Constants.BUNDLE_FRIENDS_GROUPS_TYPE, Constants.FRIENDS);
        
        Bundle bundleGroups = new Bundle();
        bundleGroups.putLong(Constants.BUNDLE_USER_ID, account.user_id);
        bundleGroups.putInt(Constants.BUNDLE_FRIENDS_GROUPS_TYPE, Constants.GROUPS);
        
        sliderMenu.add(getResources().getStringArray(R.array.slider_menu)[0].toUpperCase()).setCustomLayout(R.layout.custom_slider_menu_item).clickable(false).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.slider_menu)[1], MusicListFragment.class, bundleMyMusic, new int[]{R.color.slider_menu_custom_color_black, R.color.slider_menu_custom_color_orange}).setTextAppereanceInverse(1);
        sliderMenu.add(getResources().getStringArray(R.array.slider_menu)[2], MusicListFragment.class, bundleRecommendations, new int[]{R.color.slider_menu_custom_color_black, R.color.slider_menu_custom_color_orange}).setTextAppereanceInverse(1);
        sliderMenu.add(getResources().getStringArray(R.array.slider_menu)[3], MusicListFragment.class, bundlePopular, new int[]{R.color.slider_menu_custom_color_black, R.color.slider_menu_custom_color_orange}).setTextAppereanceInverse(1);
        sliderMenu.add(getResources().getStringArray(R.array.slider_menu)[4], FriendsGroupsListFragment.class, bundleFriends, new int[]{R.color.slider_menu_custom_color_black, R.color.slider_menu_custom_color_orange}).setTextAppereanceInverse(1);
        sliderMenu.add(getResources().getStringArray(R.array.slider_menu)[5], FriendsGroupsListFragment.class, bundleGroups, new int[]{R.color.slider_menu_custom_color_black, R.color.slider_menu_custom_color_orange}).setTextAppereanceInverse(1);
        
//        sliderMenu.add(label, fragmentClass, fragmentArguments, colors)
        
        if (savedInstanceState == null)
        sliderMenu.setCurrentPage(1);
	
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	      switch (item.getItemId()) {
	      case android.R.id.home:
	    	  if (addonSlider().isDrawerOpen(addonSlider().getLeftView()))
	    		  addonSlider().closeLeftView();
	    	  else 
	    		  addonSlider().openLeftView();
	    	  break;
	      }
		return true;
	}

}
