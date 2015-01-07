/*Class to desc all data about iconUrls on simple news page!
  Include: year, img path, author's nickyear, number of iconUrls, current rating by user's, and url link to full news page
  Also here supported writeToParcel to save this data after rotate screen on all of the devices!
  Author Roman Gaitbaev writed for AstroNews.ru 
  http://vk.com/romzesrover 
  Created: 18.08.2013 00:58*/

/*Modified to lenfilm at 22 06 2014 */

package com.BBsRs.vkaudiosync.collection;

import android.os.Parcel;
import android.os.Parcelable;

public class FriendsGroupsCollection implements Parcelable {
  
    public long gfid;
    public String groupFriendName;
    public String iconUrl;
  

  public FriendsGroupsCollection(long _gfid, String _groupFriendName, String _iconUrl) {
	    gfid = _gfid;
	    groupFriendName = _groupFriendName;
	    iconUrl = _iconUrl;
  }


@Override
public int describeContents() {
	return 0;
}

private FriendsGroupsCollection(Parcel in) {
	gfid = in.readLong();
    groupFriendName = in.readString();
    iconUrl = in.readString();
}

@Override
public void writeToParcel(Parcel out, int flags) {
	 out.writeLong(gfid);
     out.writeString(groupFriendName);
     out.writeString(iconUrl);
}

public static final Parcelable.Creator<FriendsGroupsCollection> CREATOR = new Parcelable.Creator<FriendsGroupsCollection>() {
    public FriendsGroupsCollection  createFromParcel(Parcel in) {
        return new FriendsGroupsCollection (in);
    }

    public FriendsGroupsCollection [] newArray(int size) {
        return new FriendsGroupsCollection [size];
    }
};
}