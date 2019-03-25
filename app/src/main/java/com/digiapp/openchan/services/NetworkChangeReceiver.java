package com.digiapp.openchan.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.List;
import java.util.Observable;

import de.blinkt.openvpn.fragments.Utils;

/**
 * Created by artembogomaz on 3/1/2018.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "NetworkChangeReceiver";

    private static boolean is_hardreset = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(LOG_TAG, "Receieved notification about network status");
        getObservable().connectionChanged();

        if (isNetworkAvailable()) {
            getObservable().internetAvailable();

            // + detect unsecure wifi
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> networkList = wifi.getScanResults();

            //get current connected SSID for comparison to ScanResult
            WifiInfo wi = wifi.getConnectionInfo();
            String currentSSID = wi.getSSID();

            if (networkList != null) {
                for (ScanResult network : networkList) {
                    if (currentSSID.equals(network.SSID)) {

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        boolean insecure_wifi_desc =prefs.getBoolean("insecure_wifi_desc",false);

                        String capabilities =  network.capabilities;
                        if(capabilities.isEmpty() && insecure_wifi_desc){ // unsecure wifi
                            BootReceiver.remoteStartVPN(context);
                        }
                    }
                }
            }
            // - detect unsecure wifi
            BootReceiver.remoteStartVPN(context); // restart vpn in case of network changed

        }else{
            getObservable().noInternet();
        }
    }

    public static class NetworkObservable extends Observable {
        private static NetworkObservable instance = null;

        public String status = "null";


        public String getStatus() {
            return status;
        }

        private NetworkObservable() {
            // Exist to defeat instantiation.
        }

        public void noInternet(){
            setChanged();
            notifyObservers();
            status = "no_internet";
        }

        public void internetAvailable(){
            setChanged();
            notifyObservers();

            status = "has_internet";
        }

        public void connectionChanged(){
            setChanged();
            notifyObservers();

            status = "connection_changed";
        }

        public static NetworkObservable getInstance(){
            if(instance == null){
                instance = new NetworkObservable();
            }
            return instance;
        }
    }

    public static NetworkObservable getObservable() {
        return NetworkObservable.getInstance();
    }


    private boolean isNetworkAvailable() {
        return Utils.isInetAvailable();
    }
}
