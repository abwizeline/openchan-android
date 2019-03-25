package com.digiapp.openchan.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.digiapp.openchan.AppObj;
import com.digiapp.openchan.Core;
import com.digiapp.openchan.R;
import com.digiapp.openchan.TinyDB;
import com.digiapp.openchan.database.ConfigBean;
import com.digiapp.openchan.database.ServerDetails;
import com.digiapp.openchan.database.UserBean;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.api.IOpenVPNAPIService;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.ProfileManager;

public class VPNUtils {

    public static void processVPN(String config,
                                  ServerDetails serverDetails,
                                  IOpenVPNAPIService mService) {

        // saving all profiles to local storage
        ArrayList<String> vpnProfiles = new ArrayList();

        ConfigBean.Details[] data = new ConfigBean.Details[1];
        data[0] = ConfigBean.fromServerDetails(serverDetails);

        VpnProfile vpn = prepareVpnProfile(ConfigBean.fromServerDetails(serverDetails), AppObj.getGlobalContext());
        if (vpn != null) {
            vpnProfiles.add(vpn.getUUIDString());
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppObj.getGlobalContext(), AppObj.getGlobalContext().getString(R.string.goes_wrong_profiles), Toast.LENGTH_LONG).show();
                }
            }, 0);
            return;
        }

        try {
            mService.startProfile(vpn.getUUIDString());
            //  mService.startProfileList(vpnProfiles.toArray(new String[vpnProfiles.size()]));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void processVPN(ConfigBean.Details[] data,
                                  ServerDetails serverDetails,
                                  IOpenVPNAPIService mService) {

        // saving all profiles to local storage
        ArrayList<String> vpnProfiles = new ArrayList();

        VpnProfile lastConnected = ProfileManager.getLastConnectedProfile(AppObj.getGlobalContext());
        if (lastConnected != null &&
                lastConnected.mName.startsWith(serverDetails.geo)) {
            vpnProfiles.add(lastConnected.getUUIDString());
        }

        for (ConfigBean.Details details : data) {
            VpnProfile vpn = prepareVpnProfile(details, AppObj.getGlobalContext());
            if (vpn != null) {
                vpnProfiles.add(vpn.getUUIDString());
            }
        }

        try {
            mService.startProfileList(vpnProfiles.toArray(new String[vpnProfiles.size()]));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static VpnProfile prepareVpnProfile(ConfigBean.Details details, Context context) {

        String profileName = details.CountryShort + " " + details.IP;
        ProfileManager profileManager = ProfileManager.getInstance(AppObj.getGlobalContext());
        VpnProfile vpnProfile = profileManager.getProfileByName(profileName);
        if (vpnProfile != null) {
            profileManager.removeProfile(context, vpnProfile);
        }

        ConfigParser cp = new ConfigParser();
        try {
            cp.parseConfig(new StringReader(details.OpenVPN_ConfigData));

            vpnProfile = cp.convertProfile();
            vpnProfile.mName = profileName;

            UserBean userBean = Core.getUser();

            vpnProfile.mUsername = userBean.email;
            vpnProfile.mPassword = userBean.password;

            ProfileManager.updateLRU(context, vpnProfile);

            ProfileManager pm = ProfileManager.getInstance(context);
            pm.addProfile(vpnProfile);
            pm.saveProfile(context, vpnProfile);
            pm.saveProfileList(context);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppObj.getGlobalContext());
            boolean split_apps = prefs.getBoolean("split_apps", false);
            if (split_apps) {
                TinyDB tinydb = new TinyDB(context);
                vpnProfile.mAllowedAppsVpnAreDisallowed = false;
            }

            if (prefs.getBoolean("restartvpnonboot", false)) {
                if (vpnProfile != null) {
                    prefs.edit().putString("alwaysOnVpn", vpnProfile.getUUIDString());
                }
            }

            return vpnProfile;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ConfigParser.ConfigParseError configParseError) {
            configParseError.printStackTrace();
            return null;
        }
    }

}
