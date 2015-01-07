package com.BBsRs.vkaudiosync.Adapters;

import java.util.ArrayList;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.BBsRs.vkaudiosync.R;
import com.BBsRs.vkaudiosync.collection.FriendsGroupsCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FriendsGroupsAdapter extends BaseAdapter {

	ArrayList<FriendsGroupsCollection> friendsGroupsCollection = new ArrayList<FriendsGroupsCollection>();
	Context context;
	LayoutInflater inflater;
	DisplayImageOptions options;
	String google = "https://www.google.ru/search?&safe=off&tbm=isch&tbs=isz:m&q=";
	String charset = "UTF-8";
	
	public FriendsGroupsAdapter (Context _context, ArrayList<FriendsGroupsCollection> _friendsGroupsCollection, DisplayImageOptions _options){
		friendsGroupsCollection = _friendsGroupsCollection;
		context = _context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		options = _options;
	}
	
	// кол-во элементов
	  @Override
	  public int getCount() {
	    return friendsGroupsCollection.size();
	  }

	  // элемент по позиции
	  @Override
	  public FriendsGroupsCollection getItem(int position) {
	    return friendsGroupsCollection.get(position);
	  }

	  // id по позиции
	  @Override
	  public long getItemId(int position) {
	    return position;
	  }
	  
    static class ViewHolder {
        public TextView title;
        public ImageView avatarArt;
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
    	final ViewHolder holder;
        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.ic_simple_friend_group, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) rowView.findViewById(R.id.title);
            holder.avatarArt = (ImageView)rowView.findViewById(R.id.cover_art);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        
        holder.title.setText(String.valueOf(friendsGroupsCollection.get(position).groupFriendName));
        
        ImageLoader.getInstance().displayImage(friendsGroupsCollection.get(position).iconUrl, holder.avatarArt, options);
        
        return rowView;
    }
    
	public String stringPlusZero(String arg1) {
		if (arg1.length() == 1)
			return "0" + arg1;
		else
			return arg1;
	}
}
