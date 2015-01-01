package com.BBsRs.vkaudiosync.Services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.BBsRs.vkaudiosync.collection.MusicCollection;

public class DownloadService extends Service {
	
	String LOG_TAG = "DownloadService";
	ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
	
	PowerManager pm;
	PowerManager.WakeLock wl;
	
	private final Handler handler = new Handler();
	
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate() {
		super.onCreate();
	}
	
	@SuppressWarnings("unchecked")
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle extras = intent.getExtras(); 
		if(extras == null)
			this.stopSelf();
		else
		{
		musicCollection = (ArrayList<MusicCollection>) extras.get("musicCollection");
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
		wl.acquire();
		
		startDownloadChecking();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void onDestroy() {
		wl.release();
		super.onDestroy();
	}
	
	public void stopServiceCustom(){
		final Runnable updaterText = new Runnable() {
	        public void run() {
	        	stopSelf();
	        }
	    };
	    handler.post(updaterText);
	}
	
	public void startDownloadChecking(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				int index = 0;
				for (MusicCollection oneItem : musicCollection){
					if (oneItem.checked == 1 && oneItem.exist == 0) {
						Intent i = new Intent("DOWNLOADED");
						i.putExtra("index", index);
						i.putExtra("successfully", DownloadFromUrl(oneItem.url, (oneItem.artist+" - "+oneItem.title+".mp3").replaceAll("[\\/:*?\"<>|]", "")));
						sendBroadcast(i);
					}
					index++;
				}
				stopServiceCustom();
			}
		}).start();
	}
	
	public boolean DownloadFromUrl(String DownloadUrl, String fileName) {

		   try {
		           File root = android.os.Environment.getExternalStorageDirectory();               

		           File dir = new File (root.getAbsolutePath() + "/Music");
		           if(dir.exists()==false) {
		                dir.mkdirs();
		           }

		           URL url = new URL(DownloadUrl); //you can write here any link
		           File file = new File(dir, fileName);

		           long startTime = System.currentTimeMillis();
		           Log.d("DownloadManager", "download begining");
		           Log.d("DownloadManager", "download url:" + url);
		           Log.d("DownloadManager", "downloaded file name:" + fileName);

		           /* Open a connection to that URL. */
		           URLConnection ucon = url.openConnection();

		           /*
		            * Define InputStreams to read from the URLConnection.
		            */
		           InputStream is = ucon.getInputStream();
		           BufferedInputStream bis = new BufferedInputStream(is);

		           /*
		            * Read bytes to the Buffer until there is nothing more to read(-1).
		            */
		           ByteArrayBuffer baf = new ByteArrayBuffer(5000);
		           int current = 0;
		           while ((current = bis.read()) != -1) {
		              baf.append((byte) current);
		           }


		           /* Convert the Bytes read to a String. */
		           FileOutputStream fos = new FileOutputStream(file);
		           fos.write(baf.toByteArray());
		           fos.flush();
		           fos.close();
		           Log.d("DownloadManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");
		           return true;
		   } catch (IOException e) {
		       Log.d("DownloadManager", "Error: " + e);
		       return false;
		   }

		}

}
