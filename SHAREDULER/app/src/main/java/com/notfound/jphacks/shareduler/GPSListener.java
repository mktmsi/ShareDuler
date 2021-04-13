package com.notfound.jphacks.shareduler;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;


public class GPSListener extends Activity implements LocationListener {
    private double latitude = 0;
    private double longitude = 0;
    private MyRunnable<Location> _func = null;
    private LocationManager _locationManager = null;
    private int MinTime;
    private int MinDistane;
    private Context _context;

    public GPSListener(Context context, LocationManager locationManager, int mintime, int mindistance) {
        MinTime = mintime;
        MinDistane = mindistance;
        _locationManager = locationManager;
        _locationManager.removeUpdates(this);
        Location location = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();

        _context = context;
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d("get_gps_listener", "おまこれPermissionとれてねーぞ");
            return;
        }
        if (!_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("GPSLISTENER", "GPSが利用できません");
            return;
        }
        _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MinTime, MinDistane, this);
        Log.d("GPSListener", "GPSリクエストを要求しました");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged", "が発生しました");
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
        final SharedPreferences data = _context.getSharedPreferences("save_data", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = data.edit();
        editor.putFloat("longitude", (float) this.longitude);
        editor.putFloat("latitude", (float) this.latitude);
        editor.apply();


        if (ActivityCompat.checkSelfPermission(_context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(_context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        _locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d("onStatusChanged", s);
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }
}
