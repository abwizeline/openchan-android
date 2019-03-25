package com.digiapp.openchan.viewMain.model;

import android.os.RemoteException;

import com.digiapp.openchan.viewMain.presenters.MainActivityPresenter;

import de.blinkt.openvpn.api.IOpenVPNStatusCallback;

public class OpenVPNStatusCallback extends IOpenVPNStatusCallback.Stub {

    MainActivityPresenter mPresenter;
    public OpenVPNStatusCallback(MainActivityPresenter presenter){
        mPresenter = presenter;
    }

    @Override
    public void newStatus(String uuid, String state, String message, String level) throws RemoteException {
        mPresenter.newStatus(level);
    }
}
