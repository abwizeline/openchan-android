package com.digiapp.openchan;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Base64;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.digiapp.openchan.database.AppDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.fabric.sdk.android.Fabric;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

/**
 * Created by artembogomaz on 7/26/2017.
 */

public class AppObj extends MultiDexApplication {

    public static Context globalContext;
    private static AppDatabase DB_INSTANCE;
    final public static String db_name = "vpnapp_db";

    public static AppDatabase getDefaultInstance() {
        if (DB_INSTANCE == null) {
            DB_INSTANCE =
                    Room.databaseBuilder(globalContext, AppDatabase.class, AppObj.db_name).allowMainThreadQueries().build();
        }

        return DB_INSTANCE;
    }

    public static Context getGlobalContext() {
        return globalContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(this);
        globalContext = getApplicationContext();
        Fabric.with(this, new Crashlytics());

        // Add code to print out the key hash
        if(BuildConfig.DEBUG) {
            try {
                PackageInfo info = getPackageManager().getPackageInfo(
                        "com.digiapp.openchan",
                        PackageManager.GET_SIGNATURES);
                for (Signature signature : info.signatures) {
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        // init fonts
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/exo2_semibold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
    }
}
