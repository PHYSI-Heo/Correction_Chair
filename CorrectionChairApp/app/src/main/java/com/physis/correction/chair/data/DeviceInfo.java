package com.physis.correction.chair.data;

import android.os.Parcel;
import android.os.Parcelable;

public class DeviceInfo implements Parcelable {
    private String name, title, address, pressures;

    public DeviceInfo(String name, String address){
        this.name = name;
        this.address = address;
    }

    public DeviceInfo(String title, String address, String pressures){
        this.title = title;
        this.address = address;
        this.pressures = pressures;
    }

    protected DeviceInfo(Parcel in) {
        name = in.readString();
        title = in.readString();
        address = in.readString();
        pressures = in.readString();
    }

    public static final Creator<DeviceInfo> CREATOR = new Creator<DeviceInfo>() {
        @Override
        public DeviceInfo createFromParcel(Parcel in) {
            return new DeviceInfo(in);
        }

        @Override
        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPressures() {
        return pressures;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(title);
        parcel.writeString(address);
        parcel.writeString(pressures);
    }
}
