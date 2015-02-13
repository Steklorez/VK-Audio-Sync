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
	
	//FOURTH
	public static final String INITIAL_PAGE = "initial_page";
	public static final int MUSIC_LIST_FRAGMENT = 1;
	public static final int DOWNLOAD_MANAGER_FRAGMENT = 5;
	
	
	//BROADCAST RECEIVER
	public static final String OPEN_MENU_DRAWER = "open_menu_drawer";
	public static final String MUSIC_DOWNLOADED = "downloaded";
	public static final String MUSIC_PERCENTAGE_CHANGED = "music_percetage_changed";
	
	public static final String MUSIC_SUCCESSFULLY_DOWNLOADED = "successfully";
	public static final String DOWNLOAD_SERVICE_STOPPED= "stopped";
	
	public static final String SOME_CHECKED = "checked";
	public static final String SOME_DELETED = "deleted";
	public static final String SOME_ADDED = "added";
	
	//wake lock tag
	public static final String PARTIAL_WAKE_LOCK_TAG = "my tag";
	
	//connection data
	public static final String GOOGLE_IMAGE_REQUEST_URL = "https://www.google.ru/search?&safe=off&tbm=isch&tbs=isz:m&q=";
	public static final String DEFAULT_CHARSET = "UTF-8";
	
	//SaveInstanceState
	public static final String EXTRA_MUSIC_COLLECTION = "musicCollection";
	public static final String EXTRA_FRIENDS_GROUPS_COLLECTION = "friendsGroupsCollection";
	public static final String EXTRA_POSX = "posX";
	public static final String EXTRA_PLACE_NAME = "PlaceName";
	public static final String EXTRA_ERROR = "error";
	public static final String EXTRA_ERROR_MSG = "error_message";
	public static final String EXTRA_CHECKED_QUAN = "checked_Quan";
	public static final String EXTRA_EXIST_QUAN = "exist_Quan";
	
	//shared preferences
	public static final String DOWNLOAD_DIRECTORY = "download_directory";
	public static final String DOWNLOAD_SELECTION = "download_selection";
	public static final String AUS_MAIN_LIST_BASE = "aus_main_list_base";
	public static final String ONE_AUDIO_ITEM = "one_audio_item";
	public static final String OTHER_FRAGMENT = "other_fragment";
	public static final String PREFERENCE_IMAGELOADER_PAUSE_ON_SCROLL_KEY = "preference_imageloader_pause_on_scroll";
	public static final String PREFERENCE_IMAGELOADER_PAUSE_ON_FLING_KEY = "preference_imageloader_pause_on_fling";
	public static final String PREFERENCE_IMAGELOADER_CLEAR_CACHE = "preference_imageloader_clear_cache";
	public static final String PREFERENCE_AUTOMATIC_SYNCHRONIZATION = "preference_automatic_synchronization";
	public static final String PREFERENCE_AUTOMATIC_SYNCHRONIZATION_FREQUENCY = "preference_automatic_synchronization_frequency";
	public static final String PREFERENCE_AUTOMATIC_SYNCHRONIZATION_WIFI = "preference_automatic_synchronization_wifi";
	public static final String PREFERENCE_SKIP_BIG = "preference_skip_big";
	public static final String PREFERENCE_SKIP_BIG_SIZE = "preference_skip_big_size";
	public static final String PREFERENCE_SKIP_BIG_LENGTH = "preference_skip_big_length";
	
}
