package com.BBsRs.vkaudiosync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.perm.kate.api.Api;
import com.perm.kate.api.Audio;
import com.perm.kate.api.User;

public class MusicListActivity extends Activity {
	
	private PullToRefreshLayout mPullToRefreshLayout;
	ListView listViewMusic;
    //custom refresh listener where in new thread will load job doing, need to customize for all kind of data
    CustomOnRefreshListener customOnRefreshListener = new CustomOnRefreshListener();
    
    Account account=new Account();
    Api api;
    ArrayList<Audio> musicList;
    
    String UserName = "";
    String UserAvatarUrl = "";
    
    View headerView = null;
    
    DisplayImageOptions options ;
    ImageLoader imageLoader;
    
    Bitmap bmp;

	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    this.setContentView(R.layout.music_list);
	    
	    //init all views
	    listViewMusic = (ListView)this.findViewById(R.id.listViewMusic);
	    mPullToRefreshLayout = (PullToRefreshLayout)findViewById(R.id.ptr_layout);
	    
	    //retrieve old session
        account.restore(this);
        
        //create new session
        if(account.access_token!=null)
            api=new Api(account.access_token, Constants.API_ID);
	    
	    //init pull to refresh module
        ActionBarPullToRefresh.from(this)
          .allChildrenArePullable()
          .listener(customOnRefreshListener)
          .setup(mPullToRefreshLayout);
        
        //init image loader
        options = new DisplayImageOptions.Builder()
        .showStubImage(R.drawable.deactivated_100)
        //.showImageForEmptyUri(R.drawable.logo)
        .cacheOnDisc(true)	
        .cacheInMemory(true)					
        .build();
        
        imageLoader = ImageLoader.getInstance();
		// Initialize ImageLoader with configuration. Do it once.
	    imageLoader.init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
        
        
      //refresh on open to load data when app first time started
        mPullToRefreshLayout.setRefreshing(true);
        customOnRefreshListener.onRefreshStarted(null);
	}
	
    public class  CustomOnRefreshListener implements OnRefreshListener{

		@Override
		public void onRefreshStarted(View view) {
			// TODO Auto-generated method stub
			new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                            try {
                            	musicList = api.getAudio(account.user_id, null, null, null, null, null);
//                                for (Audio one : musicList){
//                                	Log.i("Music", one.title + " - "+one.url);
//                                };
                                
                                Collection<Long> u = new ArrayList<Long>();
                                u.add(account.user_id);
                                Collection<String> d = new ArrayList<String>();
                                d.add("");
                                
                                User userOne = api.getProfiles(u, d, "", "", "", "").get(0);
                                
        						UserName = userOne.first_name+" "+userOne.last_name;
        						//UserAvatarUrl = userOne.photo_400_orig;
        						
        						URL url = new URL(musicList.get(0).url);
        						URLConnection connection = url.openConnection();
        						InputStream in = connection.getInputStream();
        						File music = new File(getApplicationInfo().dataDir+"/1.mp3");
        						FileOutputStream fos = new FileOutputStream(music);
        						byte[] buf = new byte[1024];
        						
        						int i=0;
        						while (i<1000) {
        						    int len = in.read(buf);
        						    if (len == -1) {
        						        break;
        						    }
        						    i++;
        						    fos.write(buf, 0, len);
        						}
        						in.close();
        						fos.flush();
        						fos.close();
        						
        						try {
        							Mp3File mp3file;
        							mp3file = new Mp3File(getApplicationInfo().dataDir+"/1.mp3");
        							System.out.println("Length of this mp3 is: " + mp3file.getLengthInSeconds() + " seconds");
        					        System.out.println("Bitrate: " + mp3file.getLengthInSeconds() + " kbps " + (mp3file.isVbr() ? "(VBR)" : "(CBR)"));
        					        System.out.println("Sample rate: " + mp3file.getSampleRate() + " Hz");
        					        System.out.println("Has ID3v1 tag?: " + (mp3file.hasId3v1Tag() ? "YES" : "NO"));
        					        System.out.println("Has ID3v2 tag?: " + (mp3file.hasId3v2Tag() ? "YES" : "NO"));
        					        System.out.println("Has custom tag?: " + (mp3file.hasCustomTag() ? "YES" : "NO"));
        					        if (mp3file.hasId3v2Tag()){
        					            ID3v2 id3v2tag = mp3file.getId3v2Tag();
        					            byte[] data = id3v2tag.getAlbumImage();
        					            //converting the bytes to an image
        					            BitmapFactory.Options options = new BitmapFactory.Options();
//        					            options.inMutable = true;
        					            bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        					       }
        						} catch (UnsupportedTagException e) {
        							// TODO Auto-generated catch block
        							e.printStackTrace();
        						} catch (InvalidDataException e) {
        							// TODO Auto-generated catch block
        							e.printStackTrace();
        						} catch (IOException e) {
        							// TODO Auto-generated catch block
        							e.printStackTrace();
        						}
        						
        						
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    //reset list view
                    if (headerView!=null)
                    listViewMusic.removeHeaderView(headerView);
                    
                    mPullToRefreshLayout.setRefreshing(false);
                    
                    MusicAdapter musicAdapter = new MusicAdapter(getApplicationContext(), musicList);

                    // настраиваем список
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    if (headerView==null)
                    headerView = inflater.inflate(R.layout.ic_simple_music_header);
                    TextView name = (TextView) headerView.findViewById(R.id.name);
                    TextView quanSongs = (TextView) headerView.findViewById(R.id.quanSongs);
                    
                    name.setText(UserName);
                    quanSongs.setText(musicList.size()+" "+getResources().getString(R.string.quan_songs));
//                    imageLoader.displayImage(UserAvatarUrl, avatar, options);
                    //avatar.setImageBitmap(bmp);
                    
                    listViewMusic.addHeaderView(headerView);
                    
                    listViewMusic.setAdapter(musicAdapter);
                    
                    Animation flyUpAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fly_up_anim);
                    listViewMusic.startAnimation(flyUpAnimation);
                }
            }.execute();
		}
         
    }
}
