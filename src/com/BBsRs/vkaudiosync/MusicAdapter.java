package com.BBsRs.vkaudiosync;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.CheckBox;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.perm.kate.api.Audio;

public class MusicAdapter extends BaseAdapter {

	ArrayList<Audio> musicList;
	Context context;
	LayoutInflater inflater;
	DisplayImageOptions options;
    ImageLoader imageLoader;
	String google = "https://www.google.ru/search?&safe=off&tbm=isch&tbs=isz:m&q=";
	String charset = "UTF-8";
	
	public MusicAdapter (Context _context, ArrayList<Audio> _musicList, DisplayImageOptions _options, ImageLoader _imageLoader){
		musicList = _musicList;
		context = _context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		options = _options;
        imageLoader = _imageLoader;
	}
	
	// кол-во элементов
	  @Override
	  public int getCount() {
	    return musicList.size();
	  }

	  // элемент по позиции
	  @Override
	  public Object getItem(int position) {
	    return musicList.get(position);
	  }

	  // id по позиции
	  @Override
	  public long getItemId(int position) {
	    return position;
	  }
	
    static class ViewHolder {
        public TextView length;
        public TextView title;
        public TextView subtitle;
        public CheckBox checkDownload;
        public ImageView albumArt;
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
    	final ViewHolder holder;
        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.ic_simple_music, parent, false);
            holder = new ViewHolder();
            holder.length = (TextView) rowView.findViewById(R.id.length);
            holder.title = (TextView) rowView.findViewById(R.id.title);
            holder.subtitle = (TextView) rowView.findViewById(R.id.subtitle);
            holder.checkDownload = (CheckBox) rowView.findViewById(R.id.checlDownload);
            holder.albumArt = (ImageView)rowView.findViewById(R.id.cover_art);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        
        holder.length.setText(stringPlusZero(String.valueOf((int)(musicList.get(position).duration)/60))+":"+stringPlusZero(String.valueOf((int)(musicList.get(position).duration)%60)));
        holder.title.setText(String.valueOf(musicList.get(position).artist));
        holder.subtitle.setText(String.valueOf(musicList.get(position).title));
        
        try {
			imageLoader.displayImage(google + URLEncoder.encode(musicList.get(position).artist+ " - "+musicList.get(position).title, charset), holder.albumArt, options, true);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        
        return rowView;
    }
    
	public String stringPlusZero(String arg1) {
		if (arg1.length() == 1)
			return "0" + arg1;
		else
			return arg1;
	}


}
