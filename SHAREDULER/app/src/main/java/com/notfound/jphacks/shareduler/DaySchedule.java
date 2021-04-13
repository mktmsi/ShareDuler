package com.notfound.jphacks.shareduler;

public class DaySchedule {
    int id;
    private String schedule;
    private long time;
    private String location = "";
    private double latitude;
    private double longitude;
    private int creator;
    private String url;

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getLat() {
        return latitude;
    }

    public void setLat(double lat) {
        this.latitude = lat;
    }

    public double getLon() {
        return longitude;
    }

    public void setLon(double lon) {
        this.longitude = lon;
    }

    public int getCreator() {
        return creator;
    }

    public void setCreator(int creator) {
        this.creator = creator;
    }

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

}
