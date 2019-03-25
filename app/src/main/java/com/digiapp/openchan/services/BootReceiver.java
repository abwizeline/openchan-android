package com.digiapp.openchan.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.digiapp.openchan.AppObj;

import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.Preferences;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.fragments.Utils;

public class BootReceiver extends BroadcastReceiver {

    static VpnProfile lastConnected = null;

    public static void remoteStartVPN(Context context) {

        if (!Utils.isInetAvailable()) {
            Log.d("debug","!Utils.isInetAvailable()");
            return;
        }

        lastConnected = ProfileManager.getLastConnectedProfile(AppObj.getGlobalContext());
        if(lastConnected!=null){
            Intent startVpnIntent = new Intent(Intent.ACTION_MAIN);
            startVpnIntent.setClass(context, LaunchVPN.class);
            startVpnIntent.putExtra(LaunchVPN.EXTRA_KEY,lastConnected.getUUIDString());
            startVpnIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startVpnIntent.putExtra(LaunchVPN.EXTRA_HIDELOG, true);

            context.startActivity(startVpnIntent);
        }else{
            Toast.makeText(AppObj.getGlobalContext(),"Profile VPN not found",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = Preferences.getDefaultSharedPreferences(context);
        boolean useStartOnBoot = prefs.getBoolean("restartvpnonboot", false);


        final String action = intent.getAction();
        //this.mContext = context;

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            if (useStartOnBoot) {
                remoteStartVPN(context);
            }
        }
    }
}