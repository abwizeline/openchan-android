package com.digiapp.openchan.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import com.digiapp.openchan.AppObj;
import com.digiapp.openchan.R;

@Entity
public class ServerDetails implements Parcelable {

    @PrimaryKey
    public long id;

    @ColumnInfo(name = "ip")
    public String ip;

    @ColumnInfo(name = "geo")
    public String geo;

    @ColumnInfo(name = "icon")
    public String icon;

    @ColumnInfo(name = "server")
    public String server;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "config")
    public String config;

    @ColumnInfo(name = "dynamic")
    public int dynamic;

    @ColumnInfo(name = "active_servers")
    public int active_servers;

    @ColumnInfo(name = "ping")
    public int ping;

    public ServerDetails(){}

    protected ServerDetails(Parcel in) {
        id = in.readLong();
        ip = in.readString();
        geo = in.readString();
        icon = in.readString();
        server = in.readString();
        type = in.readString();
        title = in.readString();
        config = in.readString();
        dynamic = in.readInt();
        active_servers = in.readInt();
        ping = in.readInt();
        isDefault = in.readByte() != 0;
    }

    public static final Creator<ServerDetails> CREATOR = new Creator<ServerDetails>() {
        @Override
        public ServerDetails createFromParcel(Parcel in) {
            return new ServerDetails(in);
        }

        @Override
        public ServerDetails[] newArray(int size) {
            return new ServerDetails[size];
        }
    };

    @Override
    public String toString() {
        return geo;
    }

    @ColumnInfo(name = "isDefault")
    public boolean isDefault = false;

    public static ServerDetails getAutomatic(){
        ServerDetails autoServer = new ServerDetails();
        autoServer.geo = "gl";
        autoServer.icon = "gl";
        autoServer.id = -1;
        autoServer.server = "SERVER ";
        autoServer.ip = "192.168.10.";
        autoServer.type = "1";
        autoServer.title = AppObj.getGlobalContext().getString(R.string.automatic_item);
        autoServer.isDefault = true;
        autoServer.dynamic = 1;
        autoServer.active_servers = 10;
        autoServer.config = "";
        autoServer.ping = 0;

        return autoServer;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(ip);
        dest.writeString(geo);
        dest.writeString(icon);
        dest.writeString(server);
        dest.writeString(type);
        dest.writeString(title);
        dest.writeString(config);
        dest.writeInt(dynamic);
        dest.writeInt(active_servers);
        dest.writeInt(ping);
        dest.writeByte((byte) (isDefault ? 1 : 0));
    }
}
