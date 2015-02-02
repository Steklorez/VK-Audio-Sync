package com.BBsRs.vkaudiosync.Services;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.StatFs;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.BBsRs.vkaudiosync.ContentShowActivity;
import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.Application.ObjectSerializer;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.BBsRs.vkaudiosync.collection.MusicCollection;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DownloadService extends Service {
	
	//preferences 
    SharedPreferences sPref;
	
	String LOG_TAG = "DownloadService";
	
	ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
	
	PowerManager pm;
	PowerManager.WakeLock wl;
	
	private final Handler handler = new Handler();
	
	NotificationManager mNotificationManager;
	Notification not;
	PendingIntent contentIntent;
	NotificationCompat.Builder mBuilder;
	
	boolean isServiceStopped = false;
	
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate() {
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		super.onCreate();
	}
	
	@SuppressWarnings("unchecked")
	public int onStartCommand(Intent intent, int flags, int startId) {
		//set up preferences
        sPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
		try {
	       	musicCollection = (ArrayList<MusicCollection>) ObjectSerializer.deserialize(sPref.getString(Constants.DOWNLOAD_SELECTION, ObjectSerializer.serialize(new ArrayList<MusicCollection>())));
	       	if (musicCollection==null)
	       		musicCollection = new ArrayList<MusicCollection>();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.PARTIAL_WAKE_LOCK_TAG);
		wl.acquire();
		
        setPendingNotification();
		startDownloadChecking();
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void onDestroy() {
		sendBroadcastAboutDestroyingService();
		isServiceStopped = true;
		wl.release();
		mNotificationManager.cancel(0);
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
	
	public void startDownloadChecking(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				int index = 0;
				for (MusicCollection oneItem : musicCollection){
					if (!isServiceStopped){
							mBuilder.setContentTitle("["+(index+1)+" "+getApplicationContext().getResources().getString(R.string.of)+" "+musicCollection.size()+"] "+oneItem.artist+" - "+oneItem.title)
								.setContentText(getResources().getString(R.string.dm_inprogrees))
								.setSmallIcon(R.drawable.ic_menu_download)
								.setContentIntent(contentIntent)
								.setOngoing(true)
								.setProgress(100, 0, false);
						mNotificationManager.notify(0, mBuilder.build());
					
						boolean isSuccessfullyDownloaded = DownloadFromUrl(oneItem, (oneItem.artist+" - "+oneItem.title).replaceAll("[\\/:*?\"<>|]", ""));
					
						if (isSuccessfullyDownloaded)
							removeFromDM(oneItem);
					
						Intent i = new Intent(Constants.MUSIC_DOWNLOADED);
						i.putExtra(Constants.ONE_AUDIO_ITEM, (Parcelable)oneItem);
						i.putExtra(Constants.MUSIC_SUCCESSFULLY_DOWNLOADED, isSuccessfullyDownloaded);
						i.putExtra(Constants.DOWNLOAD_SERVICE_STOPPED, false);
						sendBroadcast(i);
						index++;
					} else {
						break;
					}
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
	
	public boolean DownloadFromUrl(MusicCollection oneItem, String fileName) {
		File file = null;

		   try {
		           File root = new File (sPref.getString(Constants.DOWNLOAD_DIRECTORY, android.os.Environment.getExternalStorageDirectory()+"/Music")+"/");               

		           File dir = new File (root.getAbsolutePath());
		           if(dir.exists()==false) {
		                dir.mkdirs();
		           }

		           URL url = new URL(oneItem.url); //you can write here any link
		           file = new File(dir, fileName);

		           long startTime = System.currentTimeMillis();
		           Log.d("DownloadManager", "download begining");
		           Log.d("DownloadManager", "download url:" + url);
		           Log.d("DownloadManager", "downloaded file name:" + fileName);

		           /* Open a connection to that URL. */
		           URLConnection conexion = url.openConnection();
		           conexion.connect();
		           int lenghtOfFile = conexion.getContentLength();
		       	   Log.d("DownloadManager", "Lenght of file: " + lenghtOfFile);
		       	   
		       	   StatFs stat = new StatFs(root.getPath());
		       	   long sdAvailSize = (long)stat.getAvailableBlocks() * (long)stat.getBlockSize();
		       	   if ((long)lenghtOfFile*2 > sdAvailSize){
		       		   Log.d("DownloadManager", "no free space, avail only " + sdAvailSize);
		       		
		       		   isServiceStopped = true;
		       		   
		       		   mBuilder.setContentTitle(getApplicationContext().getResources().getString(R.string.no_free_space))
		       		   .setContentText(getResources().getString(R.string.no_free_space_msg))
		       		   .setSmallIcon(R.drawable.ic_menu_download)
		       		   .setContentIntent(contentIntent)
		       		   .setOngoing(false)
		       		   .setProgress(0, 0, false);
		       		   mNotificationManager.notify(1, mBuilder.build());
		       	   }
		           /*
		            * Define InputStreams to read from the URLConnection.
		            */
		       	   InputStream input = new BufferedInputStream(url.openStream());
		       	   OutputStream output = new FileOutputStream(file);

		       	   byte data[] = new byte[1024];

		       	   long total = 0;
		       	   int count, last=0;
		       	   long shownIn = System.currentTimeMillis();

		    		while ((count = input.read(data)) != -1) {
		    			total += count;
		    			//set not
		    			if (((int)((total*100)/lenghtOfFile) > last) && (System.currentTimeMillis() - shownIn) > 250){
		    				last = (int)((total*100)/lenghtOfFile);
		    				shownIn = System.currentTimeMillis();
		    				mBuilder.setProgress(100, last, false);
		    				mNotificationManager.notify(0, mBuilder.build());
		    			}
		    			if (!isServiceStopped){
		    				output.write(data, 0, count);
		    			} else {
		    				mNotificationManager.cancel(0);
		    				break;
		    			}
		    		}

		    		output.flush();
		    		output.close();
		    		input.close();
		           Log.d("DownloadManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");
		           
		           /*setting up cover art and fix tags so far as we can!*/
		           if (!isServiceStopped){
		        	   mBuilder.setContentText(getResources().getString(R.string.dm_inprogrees_cover))
						.setProgress(100, 100, false);
			           mNotificationManager.notify(0, mBuilder.build());
	    			} else {
	    				mNotificationManager.cancel(0);
	    				file.delete();
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
//						if (mp3file.getId3v2Tag().getGenre() !=null)
						id3v2Tag.setGenre(mp3file.getId3v2Tag().getGenre());
						if (mp3file.getId3v2Tag().getGenreDescription() !=null && !mp3file.getId3v2Tag().getGenreDescription().contains("vk.com"))
						id3v2Tag.setGenreDescription(mp3file.getId3v2Tag().getGenreDescription());
						if (mp3file.getId3v2Tag().getItunesComment() !=null && !mp3file.getId3v2Tag().getItunesComment().contains("vk.com"))
						id3v2Tag.setItunesComment(mp3file.getId3v2Tag().getItunesComment());
						if (mp3file.getId3v2Tag().getOriginalArtist() !=null && !mp3file.getId3v2Tag().getOriginalArtist().contains("vk.com"))
						id3v2Tag.setOriginalArtist(mp3file.getId3v2Tag().getOriginalArtist());
//						if (mp3file.getId3v2Tag().getPadding() !=null)
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
					
		           return true;
		   } catch (IOException e) {
			   Log.d("DownloadManager", "Error: " + e);
			   if (e.getMessage().contains("Connection timed out")){
				   mBuilder.setContentTitle(getApplicationContext().getResources().getString(R.string.no_file)+" "+oneItem.artist+" "+oneItem.title)
				   .setContentText(getResources().getString(R.string.no_file_msg))
				   .setSmallIcon(R.drawable.ic_menu_download)
				   .setContentIntent(contentIntent)
				   .setOngoing(false)
				   .setProgress(0, 0, false);
				   mNotificationManager.notify(2, mBuilder.build());
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
		   }

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
		   .setSmallIcon(R.drawable.ic_menu_download)
		   .setContentIntent(contentIntent)
		   .setOngoing(false)
		   .setProgress(0, 0, false);
		   mNotificationManager.notify(1, mBuilder.build());
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
