package com.BBsRs.vkaudiosync.Adapters;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.CheckBox;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.VKApiThings.Constants;
import com.BBsRs.vkaudiosync.collection.MusicCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MusicAdapter extends BaseAdapter {

	ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
	Context context;
	LayoutInflater inflater;
	DisplayImageOptions options;
	public int checked = 0;
	MenuItem checkAll;
	
	public MusicAdapter (Context _context, ArrayList<MusicCollection> _musicCollection, DisplayImageOptions _options, MenuItem _checkAll){
		musicCollection = _musicCollection;
		context = _context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		options = _options;
		checkAll = _checkAll;
	}
	
	// кол-во элементов
	  @Override
	  public int getCount() {
	    return musicCollection.size();
	  }
	  
	  // элемент по позиции
	  @Override
	  public MusicCollection getItem(int position) {
	    return musicCollection.get(position);
	  }
	  
	  // object
	  public ArrayList<MusicCollection> getObject() {
	    return musicCollection;
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
        
        holder.length.setText(stringPlusZero(String.valueOf((int)(musicCollection.get(position).duration)/60))+":"+stringPlusZero(String.valueOf((int)(musicCollection.get(position).duration)%60)));
        holder.title.setText(String.valueOf(musicCollection.get(position).artist));
        holder.subtitle.setText(String.valueOf(musicCollection.get(position).title));
        holder.checkDownload.setChecked(musicCollection.get(position).checked == 1 ? true : false);
        holder.checkDownload.setEnabled(musicCollection.get(position).exist == 0 ? true : false);
        
        try {
        	ImageLoader.getInstance().displayImage(Constants.GOOGLE_IMAGE_REQUEST_URL + URLEncoder.encode(musicCollection.get(position).artist+ " - "+musicCollection.get(position).title, Constants.DEFAULT_CHARSET), holder.albumArt, options, true);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        holder.checkDownload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				musicCollection.get(position).checked = holder.checkDownload.isChecked() ? 1 : 0;
				checked+=(holder.checkDownload.isChecked() ? +1 : -1);
				
				if (checked>99){
					Toast.makeText(context, context.getResources().getString(R.string.more_than), Toast.LENGTH_LONG).show();
					holder.checkDownload.setChecked(false);
					musicCollection.get(position).checked = holder.checkDownload.isChecked() ? 1 : 0;
					checked+=(holder.checkDownload.isChecked() ? +1 : -1);
				}
				
				if (checked == 0){
					checkAll.setTitle(context.getResources().getString(R.string.check_all));
				} else {
					checkAll.setTitle(context.getResources().getString(R.string.uncheck_all));
				}
			}
		});
        return rowView;
    }
    
	public String stringPlusZero(String arg1) {
		if (arg1.length() == 1)
			return "0" + arg1;
		else
			return arg1;
	}
}
