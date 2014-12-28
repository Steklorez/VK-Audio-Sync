package com.BBsRs.vkaudiosync;

import java.util.ArrayList;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.CheckBox;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.perm.kate.api.Audio;

public class MusicAdapter extends BaseAdapter {

	ArrayList<Audio> musicList;
	Context context;
	LayoutInflater inflater;
	
	public MusicAdapter (Context _context, ArrayList<Audio> _musicList){
		musicList = _musicList;
		context = _context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        
        holder.length.setText(stringPlusZero(String.valueOf((int)(musicList.get(position).duration)/60))+":"+stringPlusZero(String.valueOf((int)(musicList.get(position).duration)%60)));
        holder.title.setText(String.valueOf(musicList.get(position).artist));
        holder.subtitle.setText(String.valueOf(musicList.get(position).title));
        return rowView;
    }
    
	public String stringPlusZero(String arg1) {
		if (arg1.length() == 1)
			return "0" + arg1;
		else
			return arg1;
	}


}
