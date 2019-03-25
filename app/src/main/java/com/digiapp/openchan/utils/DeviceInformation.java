package com.digiapp.openchan.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.digiapp.openchan.AppObj;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.TimeZone;

/**
 * Created by artembogomaz on 1/21/2018.
 */

public class DeviceInformation {
    public static String getOSVersion(){
        return String.valueOf(Build.VERSION.SDK_INT);
    }

    public static JSONObject getDeviceInfo(Context context) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid",getUUID(context));
        jsonObject.put("idfa","");
        jsonObject.put("model",getModel());
        jsonObject.put("osVersion",getOSVersion());
        jsonObject.put("cellNetwork",getCarrierName(context));
        jsonObject.put("bundleId",getBundle(context));
        jsonObject.put("appVersion",getAppVersion(context));
        jsonObject.put("buildNumber",getBuildNumber(context));
        jsonObject.put("connection",getNetworkClass(context));
        jsonObject.put("ts",System.currentTimeMillis());
        TimeZone tz = TimeZone.getDefault();
        jsonObject.put("tz",tz.getDisplayName());
        return jsonObject;
    }

    public static JsonObject getDeviceInfo2(Context context) throws JSONException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("uuid",getUUID(context));
        jsonObject.addProperty("idfa","");
        jsonObject.addProperty("model",getModel());
        jsonObject.addProperty("osVersion",getOSVersion());
        jsonObject.addProperty("cellNetwork",getCarrierName(context));
        jsonObject.addProperty("bundleId",getBundle(context));
        jsonObject.addProperty("appVersion",getAppVersion(context));
        jsonObject.addProperty("buildNumber",getBuildNumber(context));
        jsonObject.addProperty("connection",getNetworkClass(context));
        jsonObject.addProperty("ts",System.currentTimeMillis());
        TimeZone tz = TimeZone.getDefault();
        jsonObject.addProperty("tz",tz.getDisplayName());
        return jsonObject;
    }

    public static String getCarrierName(Context context){
        TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName = manager.getNetworkOperatorName();

        return carrierName;
    }

    public static String getDeviceName(){
        return String.valueOf(Build.DEVICE);
    }

    public static String getDeviceId(){
        String android_id = Settings.Secure.getString(AppObj.getGlobalContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return String.valueOf(android_id);
    }

    public static String getNetworkClass(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo info = cm.getActiveNetworkInfo();
        if(info==null || !info.isConnected())
            return "-"; //not connected
        if(info.getType() == ConnectivityManager.TYPE_WIFI)
            return "WIFI";
        if(info.getType() == ConnectivityManager.TYPE_MOBILE){
            int networkType = info.getSubtype();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                    return "4G";
                default:
                    return "?";
            }
        }
        return "?";
    }


    public static String getAppVersion(Context context){
        final PackageManager packageManager = context.getPackageManager();
        final String packageName = context.getPackageName();

        String appVersionName = "na";

        try {
            appVersionName = packageManager.getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return appVersionName;
    }

    public static String getBuildNumber(Context context){
        final PackageManager packageManager = context.getPackageManager();
        final String packageName = context.getPackageName();

        int buildNumber=0;

        try {
            buildNumber = packageManager.getPackageInfo(packageName, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return ( buildNumber != 0 ? Integer.toString(buildNumber) :  "na") ;
    }

    public static String getBundle(Context mContext){
        return mContext.getPackageName();
    }

    public static String getModel(){
        return String.valueOf(Build.MODEL);
    }

    @SuppressLint("MissingPermission")
    public static String getUUID(Context context){
        String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return android_id;
    }
}
