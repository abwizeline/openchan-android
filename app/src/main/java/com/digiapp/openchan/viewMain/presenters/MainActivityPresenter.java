package com.digiapp.openchan.viewMain.presenters;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.digiapp.openchan.AppObj;
import com.digiapp.openchan.Core;
import com.digiapp.openchan.TinyDB;
import com.digiapp.openchan.database.ServerDetails;
import com.digiapp.openchan.database.UserBean;
import com.digiapp.openchan.utils.FileUtils;
import com.digiapp.openchan.utils.VPNUtils;
import com.digiapp.openchan.viewMain.model.OpenVPNStatusCallback;
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;
import com.hannesdorfmann.mosby3.mvp.MvpView;

import java.io.IOException;

import de.blinkt.openvpn.api.IOpenVPNAPIService;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.core.VpnStatus;

public class MainActivityPresenter extends MvpBasePresenter<MainActivityPresenter.View> {

    public static final int ICS_OPENVPN_PERMISSION = 7;

    protected static IOpenVPNAPIService mService = null;
    OpenVPNStatusCallback mCallback;
    TinyDB mTinyDb;
    String mToken;
    String mLastKnownVPNStatus = "";
    String mDns1 = "";
    String mDns2 = "";
    boolean dnsChanged = false;

    public MainActivityPresenter() {
        mCallback = new OpenVPNStatusCallback(this);
        mTinyDb = new TinyDB(AppObj.getGlobalContext());

        mToken = mTinyDb.getString("token");

        // filling up DNS in parallel
        RequestQueue queue = Volley.newRequestQueue(AppObj.getGlobalContext());
        String url = "https://openchan.com/ip/dns";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    if (response != null && !response.isEmpty()) {
                        String[] dns = response.split("\n");
                        if (dns.length > 0) {
                            mDns1 = dns[0];
                            mDns2 = dns[1];
                        }
                    }

                }, error -> {
        });
        queue.add(stringRequest);

        // init logs
        VpnStatus.initLogCache(AppObj.getGlobalContext().getCacheDir());
    }

    public void setDnsChanged(boolean value) {
        mTinyDb.putBoolean("dns_changed", value);
    }

    public void startVPNClicked() {

        // hard dosconnect vpn
        if (mLastKnownVPNStatus.equalsIgnoreCase("LEVEL_CONNECTED")) {
            disconnectVPN();
            return;
        }

        if (mTinyDb.getString("token") == null || mTinyDb.getString("token").isEmpty()) {
            ifViewAttached(view -> view.askUserToken());
        } else {
            mToken = mTinyDb.getString("token");
            ifViewAttached(view -> view.requestVPN());
        }
    }

    public void setToken(String token) {
        mToken = token;
        mTinyDb.putString("token", token);
    }

    public String getDns1() {
        return mDns1;
    }

    public String getDns2() {
        return mDns2;
    }

    public String getToken() {
        return mToken;
    }

    public void checkToken(String token, String command) {
        RequestQueue queue = Volley.newRequestQueue(AppObj.getGlobalContext());
        String url = "https://openchan.com/auth/" + token;

        ifViewAttached(view -> view.showProgress("Checking Token, please wait..."));
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ifViewAttached(view -> {
                            view.dismissProgress();
                        });

                        if (response.toLowerCase().equalsIgnoreCase("error")
                                || response.toLowerCase().startsWith("the token you have specified")) {
                            ifViewAttached(view -> {
                                view.showMessage("Token not valid");
                                view.tokenFailed(command);
                            });
                            return;
                        }

                        setToken(token);
                        ifViewAttached(view -> {
                            view.tokenSuccess(command);
                        });
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ifViewAttached(view -> {
                    view.dismissProgress();
                    view.tokenFailed(command);
                });
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void newStatus(String level) {

        mLastKnownVPNStatus = level;
        switch (level) {
            case "LEVEL_CONNECTED":
                ifViewAttached(view -> view.showConnectedVpn());
                break;
            case "LEVEL_NOTCONNECTED":
            case "NOPROCESS":
            case "LEVEL_NONETWORK":
                ifViewAttached(view -> view.showDisconnectedVpn());
                break;
            default:
                ifViewAttached(view -> view.showProgressVPN());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ICS_OPENVPN_PERMISSION) {
                try {
                    mService.registerStatusCallback(mCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    startEmbeddedProfile();
                } catch (Exception e) {
                    e.printStackTrace();
                    ifViewAttached(view -> view.showMessage(e.toString()));
                }
            }
        }
    }

    public void disconnectVPN() {
        try {
            mService.disconnectManual();
        } catch (Exception ex) {
            ex.printStackTrace();
            ifViewAttached(view -> view.showMessage(ex.toString()));
        }
    }

    protected void startEmbeddedProfile() throws IOException, RemoteException {
        if (mToken == null || mToken.isEmpty()) {
            ifViewAttached(view -> view.showMessage("No Token"));
            return;
        }

        // save username and password
        UserBean userBean = new UserBean();
        userBean.email = mToken;
        userBean.password = mToken;
        userBean.name = mToken;
        mTinyDb.putBoolean(Core.PREF_LOGGED, true);
        mTinyDb.putObject(Core.PREF_USER, userBean);

        // generate config and start
        String vpnConfig = VPNLaunchHelper.getVPNConfig(AppObj.getGlobalContext().getAssets().open("conf/openchan_conf_tcp.conf"));

        ServerDetails mSelectedLocation = ServerDetails.getAutomatic();

        mSelectedLocation.config = vpnConfig;

        new Thread(() -> VPNUtils.processVPN(vpnConfig, mSelectedLocation, mService)).start();
    }

    public void attachService(IBinder service) {
        mService = IOpenVPNAPIService.Stub.asInterface(service);
        try {
            mService.registerStatusCallback(mCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void detachService() {
        if (mService != null) {
            try {
                mService.unregisterStatusCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mService = null;
        ifViewAttached(view -> view.showDisconnectedVpn());
    }

    public void copyLogs() {

        new Handler(Looper.getMainLooper()).post(() -> {
            StringBuilder log = new StringBuilder();
            try {
                String data = FileUtils.getStringFromFile(VpnStatus.getCacheLogPath());
                log.append(data);
            }catch (Exception ex){
                ex.printStackTrace();
            }

            ClipboardManager clipboard = (ClipboardManager) AppObj.getGlobalContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("openvpn", log.toString());
            clipboard.setPrimaryClip(clip);
        });

    }


    public interface View extends MvpView {
        void showConnectedVpn();

        void showDisconnectedVpn();

        void showProgressVPN();

        void showMessage(String message);

        void askUserToken();

        void requestVPN();

        void showProgress(String message);

        void dismissProgress();

        void tokenSuccess(String command);

        void tokenFailed(String command);
    }
}
