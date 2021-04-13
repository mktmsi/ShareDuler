package com.notfound.jphacks.shareduler;

/**
 * Created by owner on 2017/10/22.
 */

public class DB_Record {
    private double latitude;
    private double longitude;
    private double gps_latitude;
    private double gps_longitude;
    private int id;
    private int mode;
    private long t;
    private long a;
    private int noticeFlag;

    public DB_Record(double latitude, double longitude, double gps_latitude, double gps_longitude, int id, int mode, long t, long a, int noticeFlag) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.gps_latitude = gps_latitude;
        this.gps_longitude = gps_longitude;
        this.id = id;
        this.mode = mode;
        this.t = t;
        this.a = a;
        this.noticeFlag = noticeFlag;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getGps_latitude() {
        return gps_latitude;
    }

    public double getGps_longitude() {
        return gps_longitude;
    }

    public int getId() {
        return id;
    }

    public int getMode() {
        return mode;
    }

    public long getT() {
        return t;
    }

    public long getA() {
        return a;
    }

    public int getNoticeFlag() {
        return noticeFlag;
    }
}
