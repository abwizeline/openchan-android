/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.RemoteException;

import com.digiapp.openchan.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Vector;

import de.blinkt.openvpn.VpnProfile;

public class VPNLaunchHelper {
    private static final String MININONPIEVPN = "nopie_openvpn";
    private static final String MINIPIEVPN = "pie_openvpn";
    private static final String OVPNCONFIGFILE = "android.conf";

    static int mLocalIncrement;
    static VpnProfile[] mBackupProfiles;

    private static String writeMiniVPN(Context context) {
        String[] abis;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            abis = getSupportedABIsLollipop();
        else
            //noinspection deprecation
            abis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};

        String nativeAPI = NativeUtils.getNativeAPI();
        if (!nativeAPI.equals(abis[0])) {
            VpnStatus.logWarning(R.string.abi_mismatch, Arrays.toString(abis), nativeAPI);
            abis = new String[]{nativeAPI};
        }

        for (String abi : abis) {

            File vpnExecutable = new File(context.getCacheDir(), "c_" + getMiniVPNExecutableName() + "." + abi);
            if ((vpnExecutable.exists() && vpnExecutable.canExecute()) || writeMiniVPNBinary(context, abi, vpnExecutable)) {
                return vpnExecutable.getPath();
            }
        }

        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String[] getSupportedABIsLollipop() {
        return Build.SUPPORTED_ABIS;
    }

    private static String getMiniVPNExecutableName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            return MINIPIEVPN;
        else
            return MININONPIEVPN;
    }


    public static String[] replacePieWithNoPie(String[] mArgv) {
        mArgv[0] = mArgv[0].replace(MINIPIEVPN, MININONPIEVPN);
        return mArgv;
    }


    static String[] buildOpenvpnArgv(Context c) {
        Vector<String> args = new Vector<>();

        String binaryName = writeMiniVPN(c);
        // Add fixed paramenters
        //args.add("/data/data/de.blinkt.openvpn/lib/openvpn");
        if (binaryName == null) {
            VpnStatus.logError("Error writing minivpn binary");
            return null;
        }

        args.add(binaryName);

        args.add("--config");
        args.add(getConfigFilePath(c));

        return args.toArray(new String[args.size()]);
    }

    private static boolean writeMiniVPNBinary(Context context, String abi, File mvpnout) {
        try {
            InputStream mvpn;

            try {
                mvpn = context.getAssets().open(getMiniVPNExecutableName() + "." + abi);
            } catch (IOException errabi) {
                VpnStatus.logInfo("Failed getting assets for archicture " + abi);
                return false;
            }


            FileOutputStream fout = new FileOutputStream(mvpnout);

            byte buf[] = new byte[4096];

            int lenread = mvpn.read(buf);
            while (lenread > 0) {
                fout.write(buf, 0, lenread);
                lenread = mvpn.read(buf);
            }
            fout.close();

            if (!mvpnout.setExecutable(true)) {
                VpnStatus.logError("Failed to make OpenVPN executable");
                return false;
            }


            return true;
        } catch (IOException e) {
            VpnStatus.logException(e);
            return false;
        }

    }

    public static String getVPNConfig(String ovpnFile) throws IOException, RemoteException {
        // TODO fix this
        InputStream conf = new FileInputStream(ovpnFile); // lockerApplication.getInstance().getApplicationContext().getAssets().open(ovpnFile); //client1.ovpn test.conf
        InputStreamReader isr = new InputStreamReader(conf);
        BufferedReader br = new BufferedReader(isr);
        String config = "";
        String line;
        while (true) {
            line = br.readLine();
            if (line == null)
                break;
            config += line + "\n";
        }
        br.readLine();

        return config;
    }

    public static String getVPNConfig(InputStream conf) throws IOException, RemoteException {
        InputStreamReader isr = new InputStreamReader(conf);
        BufferedReader br = new BufferedReader(isr);
        String config = "";
        String line;
        while (true) {
            line = br.readLine();
            if (line == null)
                break;
            config += line + "\n";
        }
        br.readLine();

        return config;
    }

    public static void startOpenVpn(VpnProfile startprofile, Context context) {
        startOpenVpn(startprofile, context, 0, null);
    }

    public static void startOpenVpn(VpnProfile startprofile, Context context, int increment, VpnProfile[] backupProfiles) {

        if (backupProfiles == null
                || backupProfiles.length == 0) {
            backupProfiles = new VpnProfile[1];
            backupProfiles[0] = startprofile;
        }

        mLocalIncrement = increment; // TODO remove this
        mBackupProfiles = backupProfiles;

        Intent startVPN = startprofile.prepareStartService(context);

        startVPN.putExtra("increment", increment);
        startVPN.putExtra("backupProfiles", backupProfiles);

        if (startVPN != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                /*String CHANNEL_ID = "my_channel_01";
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        context.getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_DEFAULT);

                ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

                Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText("").build();*/

                // startForeground(CHANNEL_ID,notification);
                // context.startForegroundService(startVPN);
                context.startForegroundService(startVPN);
            }else{
                context.startService(startVPN);
            }
        }
    }


    public static String getConfigFilePath(Context context) {
        return context.getCacheDir().getAbsolutePath() + "/" + OVPNCONFIGFILE;
    }

}
