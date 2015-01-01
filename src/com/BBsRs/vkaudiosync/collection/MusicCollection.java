/*Class to desc all data about titles on simple news page!
  Include: year, img path, author's nickyear, number of titles, current rating by user's, and url link to full news page
  Also here supported writeToParcel to save this data after rotate screen on all of the devices!
  Author Roman Gaitbaev writed for AstroNews.ru 
  http://vk.com/romzesrover 
  Created: 18.08.2013 00:58*/

/*Modified to lenfilm at 22 06 2014 */

package com.BBsRs.vkaudiosync.collection;

import android.os.Parcel;
import android.os.Parcelable;

public class MusicCollection implements Parcelable {
  
    public long aid;
    public long owner_id;
    public String artist;
    public String title;
    public long duration;
    public String url;
    public Long lyrics_id;
    public int checked;
  

  public MusicCollection(long _aid, long _owner_id, String _artist, String _title, long _duration, String _url, Long _lyrics_id, int _checked) {
	    aid = _aid;
	    owner_id = _owner_id;
	    artist = _artist;
	    title = _title;
	    duration = _duration;
	    url = _url;
	    lyrics_id = _lyrics_id;
	    checked = _checked;
  }


@Override
public int describeContents() {
	return 0;
}

private MusicCollection(Parcel in) {
	aid = in.readLong();
    owner_id = in.readLong();
    artist = in.readString();
    title = in.readString();
    duration = in.readLong();
    url = in.readString();
    lyrics_id = in.readLong();
    checked = in.readInt();
}

@Override
public void writeToParcel(Parcel out, int flags) {
	 out.writeLong(aid);
     out.writeLong(owner_id);
     out.writeString(artist);
     out.writeString(title);
     out.writeLong(duration);
     out.writeString(url);
     out.writeLong(lyrics_id != null ? lyrics_id : 0);
     out.writeInt(checked);
}

public static final Parcelable.Creator<MusicCollection> CREATOR = new Parcelable.Creator<MusicCollection>() {
    public MusicCollection  createFromParcel(Parcel in) {
        return new MusicCollection (in);
    }

    public MusicCollection [] newArray(int size) {
        return new MusicCollection [size];
    }
};
}