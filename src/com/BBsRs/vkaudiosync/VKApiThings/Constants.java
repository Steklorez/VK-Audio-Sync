package com.BBsRs.vkaudiosync.VKApiThings;

public class Constants {
    //VK API
    public static String API_ID="4701734";
    public static String RIGHTS_REQUEST = "friends,audio,groups,offline,wall";
    //VK API
    
    
    public static final String BUNDLE_USER_ID = "user_id";
    public static final String BUNDLE_GROUP_ID = "group_id";
    
    //FIRST
	public static final String BUNDLE_MUSIC_TYPE = "music_type";
	
	public static final int MAIN_MUSIC_USER = 0;
	public static final int MAIN_MUSIC_GROUP = 1;
	
	//SECOND
	public static final String BUNDLE_FRIENDS_GROUPS_TYPE = "friends_groups_type";
	
	public static final int FRIENDS = 0;
	public static final int GROUPS = 1;
	
	//THIRD
	public static final String BUNDLE_MAIN_WALL_TYPE = "main_wall_type";
	
	public static final int MAIN_MUSIC = 0;
	public static final int WALL_MUSIC = 1;
	
	public static final String FRIENDS_FRAGMENT = "friends_fragment";
	public static final String GROUPS_FRAGMENT = "groups_fragment";
	
	//BROADCAST RECEIVER
	public static final String OPEN_MENU_DRAWER = "open_menu_drawer";
	public static final String MUSIC_DOWNLOADED = "downloaded";
	
	public static final String MUSIC_AID_DOWNLOADED = "aid";
	public static final String MUSIC_SUCCESSFULLY_DOWNLOADED = "successfully";
	public static final String DOWNLOAD_SERVICE_STOPPED= "stopped";
	
	public static final String SOME_CHECKED = "checked";
	public static final String SOME_DELETED = "deleted";
	
	//wake lock tag
	public static final String PARTIAL_WAKE_LOCK_TAG = "my tag";
	
	//connection data
	public static final String GOOGLE_IMAGE_REQUEST_URL = "https://www.google.ru/search?&safe=off&tbm=isch&tbs=isz:m&q=";
	public static final String DEFAULT_CHARSET = "UTF-8";
	
	//DOWNLOAD SERVICE
	public static final String EXTRA_MUSIC_COLLECTION = "musicCollection";
	
	//shared preferences
	public static final String DOWNLOAD_DIRECTORY = "download_directory";
	public static final String DOWNLOAD_SELECTION = "download_selection";
	public static final String ONE_AUDIO_ITEM = "one_audio_item";
	

}
