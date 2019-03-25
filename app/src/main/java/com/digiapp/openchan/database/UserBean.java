package com.digiapp.openchan.database;

import android.os.Parcel;
import android.os.Parcelable;


public class UserBean implements Parcelable {

    public int id;

    public String email;

    public String system_info;

    public String name;

    public String password;

    public int enabled;

    public String deviceInfo;

    public String deviceId;

    public int expired;

    public String ref_code;

    public long subscription_expire;

    public UserBean() {

    }

    protected UserBean(Parcel in) {
        id = in.readInt();
        email = in.readString();
        system_info = in.readString();
        name = in.readString();
        password = in.readString();
        enabled = in.readInt();
        deviceInfo = in.readString();
        deviceId = in.readString();
        expired = in.readInt();
        ref_code = in.readString();
        subscription_expire = in.readLong();
    }

    public static final Creator<UserBean> CREATOR = new Creator<UserBean>() {
        @Override
        public UserBean createFromParcel(Parcel in) {
            return new UserBean(in);
        }

        @Override
        public UserBean[] newArray(int size) {
            return new UserBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(email);
        dest.writeString(system_info);
        dest.writeString(name);
        dest.writeString(password);
        dest.writeInt(enabled);
        dest.writeString(deviceInfo);
        dest.writeString(deviceId);
        dest.writeInt(expired);
        dest.writeString(ref_code);
        dest.writeLong(subscription_expire);
    }
}
