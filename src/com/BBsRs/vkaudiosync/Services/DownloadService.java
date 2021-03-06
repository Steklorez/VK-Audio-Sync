package com.BBsRs.vkaudiosync.Services;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.StatFs;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.BBsRs.vkaudiosync.ContentShowActivity;
import com.BBsRs.vkaudiosync.DialogActivity;
import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.Application.ObjectSerializer;
import com.BBsRs.vkaudiosync.VKApiThings.Account;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.BBsRs.vkaudiosync.collection.MusicCollection;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.perm.kate.api.Api;

public class DownloadService extends Service {
	
	//preferences 
    SharedPreferences sPref;
	
	String LOG_TAG = "DownloadService";
	
	ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
	ArrayList<MusicCollection> musicCollectionSuccessfullyDeleted = new ArrayList<MusicCollection>();
	ArrayList<MusicCollection> musicCollectionSuccessfullyDownloaded = new ArrayList<MusicCollection>();
	
	PowerManager pm;
	PowerManager.WakeLock wl;
	
	private final Handler handler = new Handler();
	
	NotificationManager mNotificationManager;
	PendingIntent contentIntent;
	NotificationCompat.Builder mBuilder;
	
	boolean isServiceStopped = false, skipCurrentDownloading = false;
	
	MusicCollection currentTrackDownloading;
	
	int currentDownloadingIndex=0, totalQuanToDownload=0, NotID = 1;
	
	/*----------------------------VK API-----------------------------*/
    Account account=new Account();
    Api api;
    /*----------------------------VK API-----------------------------*/
	
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void onCreate() {
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		super.onCreate();
	}
	
	@SuppressWarnings("unchecked")
	public int onStartCommand(Intent intent, int flags, int startId) {
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.PARTIAL_WAKE_LOCK_TAG);
		wl.acquire();
		//set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
        /*----------------------------VK API-----------------------------*/
    	//retrieve old session
        account.restore(getApplicationContext());
        
        //create new session
        if(account.access_token!=null)
            api=new Api(account.access_token, Constants.API_ID);
        /*----------------------------VK API-----------------------------*/
        
        try {
        	musicCollectionSuccessfullyDeleted = (ArrayList<MusicCollection>) ObjectSerializer.deserialize(sPref.getString(Constants.SUCCESSFULLY_DELETED, ObjectSerializer.serialize(new ArrayList<MusicCollection>())));
			if (musicCollectionSuccessfullyDeleted==null)
				musicCollectionSuccessfullyDeleted = new ArrayList<MusicCollection>();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
        
		try {
			musicCollection = (ArrayList<MusicCollection>) ObjectSerializer.deserialize(sPref.getString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(new ArrayList<MusicCollection>())));
			if (musicCollection==null)
	       		musicCollection = new ArrayList<MusicCollection>();
			
	       	totalQuanToDownload = musicCollection.size();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
		getApplicationContext().registerReceiver(someDeleted, new IntentFilter(Constants.SOME_DELETED));
		getApplicationContext().registerReceiver(someAdded, new IntentFilter(Constants.SOME_ADDED));
        setPendingNotification();
		startDownloadChecking();
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void onDestroy() {
		sendBroadcastAboutDestroyingService();
		isServiceStopped = true;
		mNotificationManager.cancel(0);
		getApplicationContext().unregisterReceiver(someDeleted);
		getApplicationContext().unregisterReceiver(someAdded);
		
		if ((sPref.getBoolean(Constants.PREFERENCE_NOTIFY_RESULT, true)) && (!(musicCollectionSuccessfullyDownloaded.size()==0 && musicCollectionSuccessfullyDeleted.size()==0))){
			try {
				sPref.edit().putString(Constants.SUCCESSFULLY_DOWNLOADED, ObjectSerializer.serialize(musicCollectionSuccessfullyDownloaded)).commit();
				sPref.edit().putString(Constants.SUCCESSFULLY_DELETED, ObjectSerializer.serialize(musicCollectionSuccessfullyDeleted)).commit();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// define sound URI, the sound to be played when there's a notification
			Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			
			// define intent to open main page
			contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, DialogActivity.class), Notification.FLAG_AUTO_CANCEL);        
		
			mBuilder.setContentTitle(getString(R.string.app_name))
			.setContentText(getString(R.string.notify_downloaded)+" "+musicCollectionSuccessfullyDownloaded.size()+" "+getString(R.string.notify_deleted)+" "+musicCollectionSuccessfullyDeleted.size())
			.setSmallIcon(R.drawable.ic_menu_download)
			.setContentIntent(contentIntent)
			.setOngoing(false)
			.setProgress(0, 0, false)
			.setAutoCancel(true)
			.setSound(soundUri);
			mNotificationManager.notify(100, mBuilder.build());
		}
		
		wl.release();
		super.onDestroy();
	}
	
	public void sendBroadcastAboutDestroyingService(){
		Intent i = new Intent(Constants.MUSIC_DOWNLOADED);
		i.putExtra(Constants.DOWNLOAD_SERVICE_STOPPED, true);
		sendBroadcast(i);
	}
	
	public void stopServiceCustom(){
		final Runnable updaterText = new Runnable() {
	        public void run() {
	        	stopSelf();
	        }
	    };
	    handler.post(updaterText);
	}
	
	private void setPendingNotification(){
		mBuilder = new NotificationCompat.Builder(this);
		contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, ContentShowActivity.class).putExtra(Constants.INITIAL_PAGE, Constants.DOWNLOAD_MANAGER_FRAGMENT), Notification.FLAG_ONGOING_EVENT);        
	}
	
	private BroadcastReceiver someDeleted = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if ((((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).aid == currentTrackDownloading.aid) || (((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).artist.equals(currentTrackDownloading.artist) && ((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).title.equals(currentTrackDownloading.title))){
	    		skipCurrentDownloading = true;
	    		totalQuanToDownload --;
	    		currentDownloadingIndex--;
	    	} else {
	    		totalQuanToDownload --;
	    		
	    		mBuilder.setContentTitle("["+(currentDownloadingIndex+1)+" "+getApplicationContext().getResources().getString(R.string.of)+" "+totalQuanToDownload+"] "+currentTrackDownloading.artist+" - "+currentTrackDownloading.title);
	    		mNotificationManager.notify(0, mBuilder.build());
	    	}
	   	}
	};
	
	private BroadcastReceiver someAdded = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if ((((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).aid == currentTrackDownloading.aid) || (((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).artist.equals(currentTrackDownloading.artist) && ((MusicCollection)intent.getExtras().getParcelable(Constants.ONE_AUDIO_ITEM)).title.equals(currentTrackDownloading.title))){
	    		//do nothing
	    	} else {
	    		totalQuanToDownload++;
	    		
	    		mBuilder.setContentTitle("["+(currentDownloadingIndex+1)+" "+getApplicationContext().getResources().getString(R.string.of)+" "+totalQuanToDownload+"] "+currentTrackDownloading.artist+" - "+currentTrackDownloading.title);
	    		mNotificationManager.notify(0, mBuilder.build());
	    	}
	   	}
	};
	
	public void startDownloadChecking(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				while (true){
					//just load new music collection
					try {
						musicCollection = (ArrayList<MusicCollection>) ObjectSerializer.deserialize(sPref.getString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(new ArrayList<MusicCollection>())));
						if (musicCollection==null)
				       		musicCollection = new ArrayList<MusicCollection>();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					//if here no songs stop service
					if (musicCollection.size()==0 || isServiceStopped)
						break;
					
					if (sPref.getBoolean(Constants.PREFERENCE_REVERSE_DOWNLOADING, false)){
						currentTrackDownloading = musicCollection.get(musicCollection.size()-1);
					} else{
						currentTrackDownloading = musicCollection.get(0);
					}
					skipCurrentDownloading = false;
					
					mBuilder.setContentTitle("["+(currentDownloadingIndex+1)+" "+getApplicationContext().getResources().getString(R.string.of)+" "+totalQuanToDownload+"] "+currentTrackDownloading.artist+" - "+currentTrackDownloading.title)
					.setContentText(getResources().getString(R.string.dm_inprogrees))
					.setSmallIcon(R.drawable.notification_animated_icon)
					.setContentIntent(contentIntent)
					.setOngoing(true)
					.setProgress(100, 0, false);
					mNotificationManager.notify(0, mBuilder.build());
	
					boolean isSuccessfullyDownloaded = DownloadFromUrl(currentTrackDownloading, (currentTrackDownloading.artist+" - "+currentTrackDownloading.title).replaceAll("[\\/:*?\"<>|]", ""));
	
					if (isSuccessfullyDownloaded){
						removeFromDM(currentTrackDownloading);
						if (account.user_id == currentTrackDownloading.owner_id)
							addToExistingBase(currentTrackDownloading);
					}
				
					Intent i = new Intent(Constants.MUSIC_DOWNLOADED);
					i.putExtra(Constants.ONE_AUDIO_ITEM, (Parcelable)currentTrackDownloading);
					i.putExtra(Constants.MUSIC_SUCCESSFULLY_DOWNLOADED, isSuccessfullyDownloaded);
					i.putExtra(Constants.DOWNLOAD_SERVICE_STOPPED, false);
					sendBroadcast(i);
					currentDownloadingIndex++;
				}
				stopServiceCustom();
			}
		}).start();
	}
	
	public void removeFromDM (MusicCollection itemToRemove){
		//remove from global download manager
		try {
			ArrayList<MusicCollection> musicCollectionTemp = (ArrayList<MusicCollection>) ObjectSerializer.deserialize(sPref.getString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(new ArrayList<MusicCollection>())));
			if (musicCollectionTemp==null)
        		musicCollectionTemp = new ArrayList<MusicCollection>();
					int indexTemp=0;
				for (MusicCollection one: musicCollectionTemp){
					if ((one.aid==itemToRemove.aid || (one.title.equals(itemToRemove.title) && one.artist.equals(itemToRemove.artist)))){
						musicCollectionTemp.remove(indexTemp);
						break;
					}
					indexTemp++;
				}
			sPref.edit().putString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(musicCollectionTemp)).commit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addToExistingBase (MusicCollection itemToAdd){
		//update existing base with new realies 
		try {
			ArrayList<MusicCollection> musicCollectionExistingBase = (ArrayList<MusicCollection>) ObjectSerializer.deserialize(sPref.getString(Constants.AUS_MAIN_LIST_BASE, ObjectSerializer.serialize(new ArrayList<MusicCollection>())));
        	if (musicCollectionExistingBase==null)
        		musicCollectionExistingBase = new ArrayList<MusicCollection>();
        	musicCollectionExistingBase.add(itemToAdd);
			sPref.edit().putString(Constants.AUS_MAIN_LIST_BASE, ObjectSerializer.serialize(musicCollectionExistingBase)).commit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean DownloadFromUrl(MusicCollection oneItem, String fileName) {
		File file = null;
		Intent i;

		   try {
		           File root = new File (sPref.getString(Constants.DOWNLOAD_DIRECTORY, android.os.Environment.getExternalStorageDirectory()+"/Music/"+getString(R.string.app_name))+"/");               

		           File dir = new File (root.getAbsolutePath());
		           if(dir.exists()==false) {
		                dir.mkdirs();
		           }

		           URL url = new URL(oneItem.url); //you can write here any link
		           file = new File(dir, fileName);
		           
		           if (new File(file.getAbsolutePath()+".mp3").exists())
		        	   return true;

		           long startTime = System.currentTimeMillis();
		           Log.d("DownloadManager", "download begining");
		           Log.d("DownloadManager", "download url:" + url);
		           Log.d("DownloadManager", "downloaded file name:" + fileName);

		           /* Open a connection to that URL. */
		           URLConnection conexion = url.openConnection();
		           conexion.connect();
		           int lenghtOfFile = conexion.getContentLength();
		       	   Log.d("DownloadManager", "Lenght of file: " + lenghtOfFile);
		       	   
		       	   if (sPref.getBoolean(Constants.PREFERENCE_SKIP_BIG, true)){
		       		   if (lenghtOfFile>Integer.parseInt(sPref.getString(Constants.PREFERENCE_SKIP_BIG_SIZE, getString(R.string.prefs_skip_big_size_default_value)))){
		       			   Log.d(LOG_TAG, "skip, track is too huge by size");
		       			   mBuilder.setContentTitle(getApplicationContext().getResources().getString(R.string.no_file)+" "+oneItem.artist+" "+oneItem.title)
	       				   .setContentText(getResources().getString(R.string.file_huge_size_msg))
	       				   .setSmallIcon(R.drawable.ic_menu_download_disabled)
	       				   .setContentIntent(contentIntent)
	       				   .setOngoing(false)
	       				   .setAutoCancel(true)
	       				   .setProgress(0, 0, false);
	       				   mNotificationManager.notify(NotID, mBuilder.build());
	       				   NotID++;
		       			   return true;
		       		   } else {
		       			   if (oneItem.duration>Integer.parseInt(sPref.getString(Constants.PREFERENCE_SKIP_BIG_LENGTH, getString(R.string.prefs_skip_big_length_default_value)))){
		       				   Log.d(LOG_TAG, "skip, track is too huge by duration");
		       				   mBuilder.setContentTitle(getApplicationContext().getResources().getString(R.string.no_file)+" "+oneItem.artist+" "+oneItem.title)
		       				   .setContentText(getResources().getString(R.string.file_huge_length_msg))
		       				   .setSmallIcon(R.drawable.ic_menu_download_disabled)
		       				   .setContentIntent(contentIntent)
		       				   .setOngoing(false)
		       				   .setAutoCancel(true)
		       				   .setProgress(0, 0, false);
		       				   mNotificationManager.notify(NotID, mBuilder.build());
		       				   NotID++;
		       				   return true;
		       			   }
		       		   }
		       	   }
		       	   
		       	   //here we check if we reach maxSize directory by users preferences
		       	   long maxSizeDirectory = Long.parseLong(sPref.getString(Constants.PREFERENCE_MAX_SIZE, getString(R.string.prefs_max_size_default_value)));
		       	   long realSizeDirectory = dirSize(root);
		       	   
		       	   if (maxSizeDirectory!=0){
		       		   if (realSizeDirectory+((long)lenghtOfFile*2)>maxSizeDirectory){
		       			   Log.d("DownloadManager", String.format("No free space by users preferences. We need: %f Mb We have: %f Mb", (double)((long)lenghtOfFile*2)/1024/1024, (double)(maxSizeDirectory-realSizeDirectory)/1024/1024));
		       			   
		       			   if (sPref.getString(Constants.PREFERENCE_WHAT_TODO_REACH_MAX_SIZE, getString(R.string.prefs_what_todo_reach_max_size_default_value)).contains("1")){
		       				   //notify if just notify
		       				   isServiceStopped = true;
			       		   
		       				   mBuilder.setContentTitle(getApplicationContext().getResources().getString(R.string.no_free_space))
			       		   		.setContentText(getResources().getString(R.string.no_free_space_user_msg))
			       		   		.setSmallIcon(R.drawable.ic_menu_download_disabled)
			       		   		.setContentIntent(contentIntent)
			       		   		.setOngoing(false)
			       		   		.setAutoCancel(true)
			       		   		.setProgress(0, 0, false);
		       				   mNotificationManager.notify(NotID, mBuilder.build());
		       				   NotID++;
		       			   } else {
		       				   //delete old files
		       				   while (realSizeDirectory+((long)lenghtOfFile*2)>maxSizeDirectory){
		       					   File fileToDelete = getOldestFile(root, null);
		       					   if (fileToDelete!=null){
		       						   	if (fileToDelete.exists())
		       						   		fileToDelete.delete();
		       					   		musicCollectionSuccessfullyDeleted.add(new MusicCollection(1, 0, fileToDelete.getName().substring(0, fileToDelete.getName().indexOf(" - ")), fileToDelete.getName().substring(fileToDelete.getName().indexOf(" - ")+3, fileToDelete.getName().length()-4), 0, null, null, 0, 0, 0));
		       					   		Log.d("DownloadManager", String.format("We are deleted oldest file: %s", fileToDelete.getName()));
		       					   		realSizeDirectory = dirSize(root);
		       					   } else 
		       						    break;
		       				   }
		       				   
		       			   }
		       		   }
		       	   }
		       	   
		       	   //here we check if we have needed free space on external storage
		       	   StatFs stat = new StatFs(root.getPath());
		       	   long sdAvailSize = (long)stat.getAvailableBlocks() * (long)stat.getBlockSize();
		       	   
		       	   if ((long)lenghtOfFile*2 > sdAvailSize){
		       		   Log.d("DownloadManager", "no free space, avail only " + sdAvailSize);
		       		   
		       		   if (sPref.getString(Constants.PREFERENCE_WHAT_TODO_REACH_MAX_SIZE, getString(R.string.prefs_what_todo_reach_max_size_default_value)).contains("1")){
		       			   //notify if just notify
		       		
		       			   isServiceStopped = true;
		       		   
		       			   mBuilder.setContentTitle(getApplicationContext().getResources().getString(R.string.no_free_space))
		       			   .setContentText(getResources().getString(R.string.no_free_space_msg))
		       			   .setSmallIcon(R.drawable.ic_menu_download_disabled)
		       			   .setContentIntent(contentIntent)
		       			   .setOngoing(false)
		       			   .setAutoCancel(true)
		       			   .setProgress(0, 0, false);
		       			   mNotificationManager.notify(NotID, mBuilder.build());
		       			   NotID++;
		       		} else {
	       				   //delete old files
	       				   while (realSizeDirectory+((long)lenghtOfFile*2)>maxSizeDirectory){
	       					   File fileToDelete = getOldestFile(root, null);
	       					   if (fileToDelete!=null){
	       						   	if (fileToDelete.exists())
	       						   		fileToDelete.delete();
	       					   		musicCollectionSuccessfullyDeleted.add(new MusicCollection(1, 0, fileToDelete.getName().substring(0, fileToDelete.getName().indexOf(" - ")), fileToDelete.getName().substring(fileToDelete.getName().indexOf(" - ")+3, fileToDelete.getName().length()-4), 0, null, null, 0, 0, 0));
	       					   		Log.d("DownloadManager", String.format("We are deleted oldest file: %s", fileToDelete.getName()));
	       					   		realSizeDirectory = dirSize(root);
	       					   } else 
	       						    break;
	       				   }
	       			   }
		       	   }
		           /*
		            * Define InputStreams to read from the URLConnection.
		            */
		       	   InputStream input = new BufferedInputStream(url.openStream());
		       	   OutputStream output = new FileOutputStream(file);

		       	   byte data[] = new byte[1024];

		       	   long total = 0;
		       	   int count, last=0;
		       	   long shownIn = System.currentTimeMillis(), sendIn = System.currentTimeMillis();

		    		while ((count = input.read(data)) != -1) {
		    			total += count;
		    			//set not
		    			if (((int)((total*100)/lenghtOfFile) > last) && (System.currentTimeMillis() - shownIn) > 250){
		    				last = (int)((total*100)/lenghtOfFile);
		    				shownIn = System.currentTimeMillis();
		    				mBuilder.setProgress(100, last, false);
		    				mNotificationManager.notify(0, mBuilder.build());
		    				
		    				if ((System.currentTimeMillis() - sendIn) > 700){
		    					sendIn = System.currentTimeMillis();
		    					//send broadcast about percentage of track
		    					oneItem.percentage=last;
		    					i = new Intent(Constants.MUSIC_PERCENTAGE_CHANGED);
								i.putExtra(Constants.ONE_AUDIO_ITEM, (Parcelable)oneItem);
								sendBroadcast(i);
		    				}
		    			}
		    			if (!isServiceStopped && !skipCurrentDownloading){
		    				output.write(data, 0, count);
		    			} else {
		    				if (isServiceStopped){
		    					mNotificationManager.cancel(0);
		    				}
		    				//send that song to 0% downloaded
	 		           		oneItem.percentage=0;
	 		           		i = new Intent(Constants.MUSIC_PERCENTAGE_CHANGED);
	 		           		i.putExtra(Constants.ONE_AUDIO_ITEM, (Parcelable)oneItem);
	 		           		sendBroadcast(i);
		    				break;
		    			}
		    		}

		    		output.flush();
		    		output.close();
		    		input.close();
		           Log.d("DownloadManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");

		           /*setting up cover art and fix tags so far as we can!*/
		           if (!isServiceStopped && !skipCurrentDownloading){
		        	   mBuilder.setContentText(getResources().getString(R.string.dm_inprogrees_cover))
						.setProgress(0, 0, true);
			           mNotificationManager.notify(0, mBuilder.build());
			           
			           //send that song to 100% downloaded
			           oneItem.percentage=100;
			           i = new Intent(Constants.MUSIC_PERCENTAGE_CHANGED);
			           i.putExtra(Constants.ONE_AUDIO_ITEM, (Parcelable)oneItem);
			           sendBroadcast(i);
	    			} else {
	    				if (isServiceStopped){
	    					mNotificationManager.cancel(0);
	    				}
	    				file.delete();
	    				
	    				//send that song to 0% downloaded
	 		           	oneItem.percentage=0;
	 		           	i = new Intent(Constants.MUSIC_PERCENTAGE_CHANGED);
	 		           	i.putExtra(Constants.ONE_AUDIO_ITEM, (Parcelable)oneItem);
	 		           	sendBroadcast(i);
	    				return false;
	    			}
		           
		           
		           Log.d("DownloadManager", "download cover art");
		           //download bitmap from web
		           Bitmap bmp = ImageLoader.getInstance().loadImageSync(Constants.GOOGLE_IMAGE_REQUEST_URL + URLEncoder.encode(oneItem.artist+" - "+oneItem.title, Constants.DEFAULT_CHARSET), true);
		           if (bmp==null) 
		        	   bmp = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.ic_simple_music_stub);
		           
		           Log.d("DownloadManager", "crop and compress bitmap to png");
		           ByteArrayOutputStream stream = new ByteArrayOutputStream();
		           centerCrop(bmp).compress(Bitmap.CompressFormat.PNG, 100, stream);
					byte[] byteArray = stream.toByteArray();
					
					Mp3File mp3file = new Mp3File(file.getAbsolutePath());
					
					
					Log.d("DownloadManager", "setting tags with cover art");
					
					Log.d("DownloadManager", "create new tags");
					ID3v2 id3v2Tag = new ID3v24Tag();
					
					try {
						if (mp3file.hasId3v2Tag()){
							Log.d("DownloadManager", "setting up new tags from existing tags, if they are correct");
							if (mp3file.getId3v2Tag().getAlbum() !=null && !mp3file.getId3v2Tag().getAlbum().contains("vk.com"))
								id3v2Tag.setAlbum(mp3file.getId3v2Tag().getAlbum());
							if (mp3file.getId3v2Tag().getAlbumArtist() !=null && !mp3file.getId3v2Tag().getAlbumArtist().contains("vk.com"))
								id3v2Tag.setAlbumArtist(mp3file.getId3v2Tag().getAlbumArtist());
							if (mp3file.getId3v2Tag().getArtist() !=null && !mp3file.getId3v2Tag().getArtist().contains("vk.com"))
								id3v2Tag.setArtist(mp3file.getId3v2Tag().getArtist());
							if (mp3file.getId3v2Tag().getChapters() !=null && !mp3file.getId3v2Tag().getChapters().contains("vk.com"))
								id3v2Tag.setChapters(mp3file.getId3v2Tag().getChapters());
							if (mp3file.getId3v2Tag().getChapterTOC() !=null && !mp3file.getId3v2Tag().getChapterTOC().contains("vk.com"))
								id3v2Tag.setChapterTOC(mp3file.getId3v2Tag().getChapterTOC());
							if (mp3file.getId3v2Tag().getComment() !=null && !mp3file.getId3v2Tag().getComment().contains("vk.com"))
								id3v2Tag.setComment(mp3file.getId3v2Tag().getComment());
							if (mp3file.getId3v2Tag().getComposer() !=null && !mp3file.getId3v2Tag().getComposer().contains("vk.com"))
								id3v2Tag.setComposer(mp3file.getId3v2Tag().getComposer());
							if (mp3file.getId3v2Tag().getCopyright() !=null && !mp3file.getId3v2Tag().getCopyright().contains("vk.com"))
								id3v2Tag.setCopyright(mp3file.getId3v2Tag().getCopyright());
							if (mp3file.getId3v2Tag().getEncoder() !=null && !mp3file.getId3v2Tag().getEncoder().contains("vk.com"))
								id3v2Tag.setEncoder(mp3file.getId3v2Tag().getEncoder());
//							if (mp3file.getId3v2Tag().getGenre() !=null)
								id3v2Tag.setGenre(mp3file.getId3v2Tag().getGenre());
							if (mp3file.getId3v2Tag().getGenreDescription() !=null && !mp3file.getId3v2Tag().getGenreDescription().contains("vk.com"))
								id3v2Tag.setGenreDescription(mp3file.getId3v2Tag().getGenreDescription());
							if (mp3file.getId3v2Tag().getItunesComment() !=null && !mp3file.getId3v2Tag().getItunesComment().contains("vk.com"))
								id3v2Tag.setItunesComment(mp3file.getId3v2Tag().getItunesComment());
							if (mp3file.getId3v2Tag().getOriginalArtist() !=null && !mp3file.getId3v2Tag().getOriginalArtist().contains("vk.com"))
								id3v2Tag.setOriginalArtist(mp3file.getId3v2Tag().getOriginalArtist());
//							if (mp3file.getId3v2Tag().getPadding() !=null)
								id3v2Tag.setPadding(mp3file.getId3v2Tag().getPadding());
							if (mp3file.getId3v2Tag().getPartOfSet() !=null && !mp3file.getId3v2Tag().getPartOfSet().contains("vk.com"))
								id3v2Tag.setPartOfSet(mp3file.getId3v2Tag().getPartOfSet());
							if (mp3file.getId3v2Tag().getPublisher() !=null && !mp3file.getId3v2Tag().getPublisher().contains("vk.com"))
								id3v2Tag.setPublisher(mp3file.getId3v2Tag().getPublisher());
							if (mp3file.getId3v2Tag().getTitle() !=null && !mp3file.getId3v2Tag().getTitle().contains("vk.com"))
								id3v2Tag.setTitle(mp3file.getId3v2Tag().getTitle());
							if (mp3file.getId3v2Tag().getTrack() !=null && !mp3file.getId3v2Tag().getTrack().contains("vk.com"))
								id3v2Tag.setTrack(mp3file.getId3v2Tag().getTrack());
							if (mp3file.getId3v2Tag().getUrl() !=null)
								id3v2Tag.setUrl(mp3file.getId3v2Tag().getUrl());
							if (mp3file.getId3v2Tag().getYear() !=null && !mp3file.getId3v2Tag().getYear().contains("vk.com"))
								id3v2Tag.setYear(mp3file.getId3v2Tag().getYear());
						}
					} catch (Exception e){
						e.printStackTrace();
					}
					
					Log.d("DownloadManager", "set new tags (image, artist if still not exist, and title if still not exist)");
					id3v2Tag.setAlbumImage(byteArray, "image/png");
					id3v2Tag.setArtist(id3v2Tag.getArtist()==null ? oneItem.artist : id3v2Tag.getArtist());
					id3v2Tag.setTitle(id3v2Tag.getTitle()==null ? oneItem.title : id3v2Tag.getTitle());
					id3v2Tag.setAlbum(id3v2Tag.getAlbum()==null ? oneItem.title : id3v2Tag.getAlbum());
						
					//fix tags error when try to save (remove unsupported old tags)
					mp3file.removeCustomTag();
					mp3file.removeId3v1Tag();
					mp3file.removeId3v2Tag();
					
					//setting up new tags
					mp3file.setId3v2Tag(id3v2Tag);
					

					
					Log.d("DownloadManager", "save .mp3 file");
					mp3file.save(file.getAbsolutePath()+".mp3");
					
					Log.d("DownloadManager", "sent intent that new mp3 file added to library");
					Intent intent =new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					intent.setData(Uri.fromFile(new File(file.getAbsolutePath()+".mp3")));
					sendBroadcast(intent);
					
					Log.d("DownloadManager", "delete downloaded file");
					file.delete();
					
					//send that song to 101% downloaded
			        oneItem.percentage=101;
			        i = new Intent(Constants.MUSIC_PERCENTAGE_CHANGED);
			        i.putExtra(Constants.ONE_AUDIO_ITEM, (Parcelable)oneItem);
			        sendBroadcast(i);
					
			        musicCollectionSuccessfullyDownloaded.add(oneItem);
		           return true;
		   } catch (IOException e) {
			   Log.d("DownloadManager", "Error: " + e);
			   if (("Error: " + e).contains("Connection timed out") || ("Error: " + e).contains("java.io.FileNotFoundException:")){
				   mBuilder.setContentTitle(getApplicationContext().getResources().getString(R.string.no_file)+" "+oneItem.artist+" "+oneItem.title)
				   .setContentText(getResources().getString(R.string.no_file_msg))
				   .setSmallIcon(R.drawable.ic_menu_download_disabled)
				   .setContentIntent(contentIntent)
				   .setOngoing(false)
				   .setAutoCancel(true)
				   .setProgress(0, 0, false);
				   mNotificationManager.notify(NotID, mBuilder.build());
				   NotID++;
				   return true;
			   } else {
				   noConnectionError(file);
				   return false;
			   }
		   } catch (NotSupportedException e) {
			   noConnectionError(file);
			   e.printStackTrace();
			   return false;
		   } catch (UnsupportedTagException e) {
			   noConnectionError(file);
			   e.printStackTrace();
			   return false;
		   } catch (InvalidDataException e) {
			   noConnectionError(file);
			   e.printStackTrace();
			   return false;
		   } catch (Exception e){
			   noConnectionError(file);
			   e.printStackTrace();
			   return false;
		   }
		   

		}
	
	/**
	 * Return the oldest file in a directory
	 */
	 private File getOldestFile(File parentDir, File parentOldest) {
		 	File oldest = parentOldest;
		 	if (oldest == null) if (parentDir.listFiles().length!=0) oldest = parentDir.listFiles()[0]; else return null;
		    File[] files = parentDir.listFiles();
		    for (File file : files) {
		        if (file.isDirectory()) {
		            oldest = getOldestFile(file, oldest);
		        } else {
		            if(file.getName().endsWith(".mp3") && file.lastModified()<oldest.lastModified()){
		            	oldest = file;
		            }
		        }
		    }
		    return oldest;
		}
	
	/**
	 * Return the size of a directory in bytes
	 */
	private static long dirSize(File dir) {

	    if (dir.exists()) {
	        long result = 0;
	        File[] fileList = dir.listFiles();
	        for(int i = 0; i < fileList.length; i++) {
	            // Recursive call if it's a directory
	            if(fileList[i].isDirectory()) {
	                result += dirSize(fileList [i]);
	            } else {
	                // Sum the file size in bytes
	                result += fileList[i].length();
	            }
	        }
	        return result; // return the file size
	    }
	    return 0;
	}
	
	public void noConnectionError(File file){
		isServiceStopped = true;
		
		if (file!=null) {
	    	   File mp3 = new File(file.getAbsolutePath()+".mp3");
	    	   if (mp3!=null)
	    	   mp3.delete();
	    	   file.delete();
	       }
		   
		   mBuilder.setContentTitle(getApplicationContext().getResources().getString(R.string.no_conn))
		   .setContentText(getResources().getString(R.string.no_conn_msg))
		   .setSmallIcon(R.drawable.ic_menu_download_disabled)
		   .setContentIntent(contentIntent)
		   .setOngoing(false)
		   .setAutoCancel(true)
		   .setProgress(0, 0, false);
		   mNotificationManager.notify(NotID, mBuilder.build());
		   NotID++;
	}
	
	public Bitmap centerCrop(Bitmap srcBmp) {
		Bitmap dstBmp;
		if (srcBmp.getWidth() >= srcBmp.getHeight()){

			  dstBmp = Bitmap.createBitmap(
			     srcBmp, 
			     srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
			     0,
			     srcBmp.getHeight(), 
			     srcBmp.getHeight()
			     );

			}else{

			  dstBmp = Bitmap.createBitmap(
			     srcBmp,
			     0, 
			     srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
			     srcBmp.getWidth(),
			     srcBmp.getWidth() 
			     );
			}
		return dstBmp;
	}

}
