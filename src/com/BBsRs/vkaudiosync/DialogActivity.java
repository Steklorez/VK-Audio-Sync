package com.BBsRs.vkaudiosync;

import java.io.IOException;
import java.util.ArrayList;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.app.DialogFragment;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.ListView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.BBsRs.vkaudiosync.Adapters.DialogActivityMusicAdapter;
import com.BBsRs.vkaudiosync.Application.ObjectSerializer;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.BBsRs.vkaudiosync.collection.MusicCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

public class DialogActivity extends Activity {
	
	ArrayList<MusicCollection> musicCollectionSuccessfullyDeleted = new ArrayList<MusicCollection>();
	ArrayList<MusicCollection> musicCollectionSuccessfullyDownloaded = new ArrayList<MusicCollection>();
	ArrayList<MusicCollection> musicCollectionCommonList = new ArrayList<MusicCollection>();
	
	//preferences 
    SharedPreferences sPref;
    
    //with this options we will load images
    DisplayImageOptions options ;
    
    String LOG_TAG = "DialogActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
		//set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
        //init image loader
        options = new DisplayImageOptions.Builder()
        .showStubImage(R.drawable.ic_simple_music_stub)
        //.showImageForEmptyUri(R.drawable.logo)
        .cacheOnDisc(true)	
        .cacheInMemory(true)					
        .build();
	    
		try {
			musicCollectionSuccessfullyDeleted = (ArrayList<MusicCollection>) ObjectSerializer.deserialize(sPref.getString(Constants.SUCCESSFULLY_DELETED, ObjectSerializer.serialize(new ArrayList<MusicCollection>())));
			if (musicCollectionSuccessfullyDeleted == null)
				musicCollectionSuccessfullyDeleted = new ArrayList<MusicCollection>();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			musicCollectionSuccessfullyDownloaded = (ArrayList<MusicCollection>) ObjectSerializer.deserialize(sPref.getString(Constants.SUCCESSFULLY_DOWNLOADED, ObjectSerializer.serialize(new ArrayList<MusicCollection>())));
			if (musicCollectionSuccessfullyDownloaded == null)
				musicCollectionSuccessfullyDownloaded = new ArrayList<MusicCollection>();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//create common list
		if (musicCollectionSuccessfullyDownloaded.size()!=0)
			musicCollectionCommonList.add(new MusicCollection(0, 0, getString(R.string.notify_downloaded)+" "+musicCollectionSuccessfullyDownloaded.size(), null, 0, null, null, 0, 0, 0));
		musicCollectionCommonList.addAll(musicCollectionSuccessfullyDownloaded);
		if (musicCollectionSuccessfullyDeleted.size()!=0)
			musicCollectionCommonList.add(new MusicCollection(0, 0, getString(R.string.notify_deleted)+" "+musicCollectionSuccessfullyDeleted.size(), null, 0, null, null, 0, 0, 0));
		musicCollectionCommonList.addAll(musicCollectionSuccessfullyDeleted);
		
		Log.i(LOG_TAG, "Deleted: " + musicCollectionSuccessfullyDeleted.size()+" Downloaded: "+musicCollectionSuccessfullyDownloaded.size());
	    
	    //showing dialog
		final DialogFragment alertDialog = new DialogFragment(){
  			private View makeNumberPicker() {
  		        View content = getLayoutInflater().inflate(
  		                R.layout.dialog_content);
  		        ListView musicList = (ListView)content.findViewById(R.id.listView1);
  		        
  		        DialogActivityMusicAdapter musicAdapter = new DialogActivityMusicAdapter(getActivity(), musicCollectionCommonList, options);
  		        musicList.setAdapter(musicAdapter);
  		        return content;
  		    }

  		    @Override
  		    public Dialog onCreateDialog(Bundle savedInstanceState) {
  		        AlertDialog.Builder builder = new AlertDialog.Builder(getSupportActivity(), getTheme());
  		        builder.setView(makeNumberPicker());
  		        builder.setCancelable(false);
  		        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismiss();
					}
				});
  		        return builder.create();
  		    }
  		    
  		    @Override
			public void onDismiss(DialogInterface dialogInterface) {
				finish();
			}
  		};
  		alertDialog.show(getSupportFragmentManager());
	}
	
	@Override
	public void onDestroy(){
		//nulling bases
		sPref.edit().putString(Constants.SUCCESSFULLY_DOWNLOADED, "").commit();
		sPref.edit().putString(Constants.SUCCESSFULLY_DELETED, "").commit();
		
		super.onDestroy();
	}

}
