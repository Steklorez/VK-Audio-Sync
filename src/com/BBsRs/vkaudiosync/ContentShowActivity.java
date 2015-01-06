package com.BBsRs.vkaudiosync;

import org.holoeverywhere.addon.AddonSlider;
import org.holoeverywhere.addon.AddonSlider.AddonSliderA;
import org.holoeverywhere.addon.Addons;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.slider.SliderMenu;
import org.holoeverywhere.widget.Toast;

import android.os.Bundle;
import android.view.MenuItem;

import com.BBsRs.vkaudiosync.Fragments.MusicListFragment;

@Addons(AddonSlider.class)
public class ContentShowActivity extends Activity {
	public AddonSlider.AddonSliderA addonSlider() {
	      return addon(AddonSlider.class);
	}
	
	SliderMenu sliderMenu;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    //init slider menu
        sliderMenu = addonSlider().obtainDefaultSliderMenu(R.layout.menu);
        addonSlider().setOverlayActionBar(false);
        
        sliderMenu.add(getResources().getStringArray(R.array.slider_menu)[0].toUpperCase()).setCustomLayout(R.layout.custom_slider_menu_item).clickable(false).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.slider_menu)[1], MusicListFragment.class, new int[]{R.color.slider_menu_custom_color_black, R.color.slider_menu_custom_color_orange}).setTextAppereanceInverse(1);
        
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
