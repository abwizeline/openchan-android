package com.digiapp.openchan;

import com.digiapp.openchan.database.UserBean;

/**
 * Created by artembogomaz on 7/26/2017.
 */

public class Core {

    public static String PREF_USER = "PREF_USER";
    public static String PREF_LOGGED = "PREF_LOGGED";
    public static UserBean getUser(){
        TinyDB tinyDB = new TinyDB(AppObj.getGlobalContext());
        return (UserBean) tinyDB.getObject(Core.PREF_USER,UserBean.class);
    }
}
