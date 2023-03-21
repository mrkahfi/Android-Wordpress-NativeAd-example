package com.app.wprestapi;

public class Config {

    /*
     * POST_LAST_POSITION_BEFORE_AD represent after what position at every page an Ad is supposed to appear
     * This should be Even number, 2, 4, 6 and should be between 0 and 10
     */
    public static final int POST_LAST_POSITION_BEFORE_AD = 6;
    public static final int POSTS_PER_PAGE = 10;

    public static final int CATEGORIES_PER_PAGE = 20;

    public static final boolean DISPLAY_SHORT_DESCRIPTION_IN_POST_LIST = false;
    public static final boolean DISPLAY_CATEGORIES_IN_POST_LIST = true;

    public static final boolean ENABLE_RTL_MODE = false;

}
