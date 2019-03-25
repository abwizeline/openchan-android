package com.digiapp.openchan.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.digiapp.openchan.AppObj;

import de.blinkt.openvpn.activities.DisconnectVPN;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.fragments.Utils;

import static de.blinkt.openvpn.core.OpenVPNService.ACTION_OFF;
import static de.blinkt.openvpn.core.OpenVPNService.ACTION_ON;

public class CommandsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (!Utils.isInetAvailable()) {
            Log.d("debug","!Utils.isInetAvailable()");
            Toast.makeText(AppObj.getGlobalContext(),"Please, check internet connection",Toast.LENGTH_SHORT).show();
            return;
        }

        final String action = intent.getAction();
        switch (action) {
            case ACTION_OFF:
                Toast.makeText(AppObj.getGlobalContext(),"Disconnecting VPN",Toast.LENGTH_SHORT).show();
                Intent disconnectVPN = new Intent(context, DisconnectVPN.class);
                disconnectVPN.setAction(OpenVPNService.DISCONNECT_VPN);
                context.startActivity(disconnectVPN);
                break;
            case ACTION_ON:
                Toast.makeText(AppObj.getGlobalContext(),"Connecting VPN",Toast.LENGTH_SHORT).show();
                BootReceiver.remoteStartVPN(AppObj.getGlobalContext());
                break;
            default:
                Toast.makeText(AppObj.getGlobalContext(),"Unknown command",Toast.LENGTH_SHORT).show();
        }
    }
}
