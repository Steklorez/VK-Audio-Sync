package com.BBsRs.vkaudiosync.Adapters;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.BBsRs.vkaudiosync.collection.MusicCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DialogActivityMusicAdapter extends BaseAdapter {

	public ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
	Context context;
	LayoutInflater inflater;
	DisplayImageOptions options;
	public int checked = 0;
	
	public DialogActivityMusicAdapter (Context _context, ArrayList<MusicCollection> _musicCollection, DisplayImageOptions _options){
		musicCollection = _musicCollection;
		context = _context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		options = _options;
	}
	
	  @Override
	  public int getCount() {
	    return musicCollection.size();
	  }
	  
	  @Override
	  public MusicCollection getItem(int position) {
	    return musicCollection.get(position);
	  }
	  
	  // remove item from list
	  public void removeItem(int postion) {
		  musicCollection.remove(postion);
		  this.notifyDataSetChanged();
	  }

	  @Override
	  public long getItemId(int position) {
	    return position;
	  }
	  
    static class ViewHolder {
        public TextView length;
        public TextView title;
        public TextView subtitle;
        public ImageView deleteItem;
        public ImageView albumArt;
        public TextView percentage;
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
    	final ViewHolder holder;
        View rowView = convertView;
        if (musicCollection.get(position).aid!=0){
        	if (rowView == null || rowView.findViewById(R.id.length)==null) {
            	rowView = inflater.inflate(R.layout.ic_simple_music_dm, parent, false);
            	holder = new ViewHolder();
            	holder.length = (TextView) rowView.findViewById(R.id.length);
            	holder.title = (TextView) rowView.findViewById(R.id.title);
            	holder.subtitle = (TextView) rowView.findViewById(R.id.subtitle);
            	holder.deleteItem = (ImageView) rowView.findViewById(R.id.deleteItem);
            	holder.albumArt = (ImageView)rowView.findViewById(R.id.cover_art);
            	holder.percentage = (TextView)rowView.findViewById(R.id.percentage);
            	rowView.setTag(holder);
        	} else {
            	holder = (ViewHolder) rowView.getTag();
        	}
        
        
        	holder.length.setText(stringPlusZero(String.valueOf((int)(musicCollection.get(position).duration)/60))+":"+stringPlusZero(String.valueOf((int)(musicCollection.get(position).duration)%60)));
        	holder.title.setText(String.valueOf(musicCollection.get(position).artist));
        	holder.subtitle.setText(String.valueOf(musicCollection.get(position).title));
        
        	holder.percentage.setVisibility(View.GONE);
        	holder.deleteItem.setVisibility(View.INVISIBLE);
        	holder.deleteItem.setImageDrawable(null);
        
        	try {
        		ImageLoader.getInstance().displayImage(Constants.GOOGLE_IMAGE_REQUEST_URL + URLEncoder.encode(musicCollection.get(position).artist+ " - "+musicCollection.get(position).title, Constants.DEFAULT_CHARSET), holder.albumArt, options, true);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
        	return rowView;
        } else {
        	rowView = inflater.inflate(R.layout.custom_slider_menu_item, parent, false);
        	TextView title = (TextView) rowView.findViewById(android.R.id.text1);
        	title.setText(String.valueOf(musicCollection.get(position).artist.toUpperCase()));
        	return rowView;
        }
    }
    
	public String stringPlusZero(String arg1) {
		if (arg1.length() == 1)
			return "0" + arg1;
		else
			return arg1;
	}
}
