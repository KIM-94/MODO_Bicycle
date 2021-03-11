package com.example.capstond.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.capstond.R;
import com.example.capstond.db.SQLiteHelper;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class LocationService extends Service {

    SQLiteHelper mSQLiteHelper;

    public double temp_latitude;
    public double temp_longtitude;
    public double temp_dist, add_dist=0;
    public double temp_KmH;

    Date curDate = new Date();
    private long curDateTime = curDate.getTime();
    private long reqDateTime = curDate.getTime();
    private long tempsec = 0;


    public LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {

                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String nowdatetime = datetimeFormat.format(date);

                //현재시간을 요청시간의 형태로 format 후 time 가져오기
                Date curDate = new Date();
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                try {
                    curDate = timeFormat.parse(timeFormat.format(curDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                curDateTime = curDate.getTime();

                long sec = (curDateTime - reqDateTime) / 1000;
                reqDateTime = curDateTime;
                if (sec<0) { sec = 0; }

                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();

                String dist;
                try{
                    temp_dist = distance(latitude, longitude, temp_latitude, temp_longtitude, "kilometer");
                    if (temp_dist>10){
                        temp_dist = 0;
                    }
                    dist = String.format("%.8f", temp_dist);
                } catch (Exception e) {
                    dist = "0.00000000";
                    temp_dist = 0;
                }
                add_dist = add_dist + temp_dist;

                tempsec = tempsec + sec;

                double KmH = 3600.0 / sec * temp_dist;
                temp_KmH = KmH;

                temp_latitude = latitude;
                temp_longtitude = longitude;

                mSQLiteHelper = new SQLiteHelper(getApplicationContext(), "MODObicycle.db", null, 1);
                mSQLiteHelper.insertAllDatas3(nowdatetime, nowdatetime, dist, Double.toString(latitude), Double.toString(longitude));
            }
        }
    };

    private IBinder mIBinder = new MyBinder();

    public class MyBinder extends Binder {
        public LocationService getService(){
            return LocationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        throw new UnsupportedOperationException("Not Yet Implemented");
        Log.e("LOG", "onBind()");
        return mIBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("LOG", "onUnbind()");
        return super.onUnbind(intent);
    }

    private void startLocationService() {
        String channelID = "location_notification_channel";
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                channelID
        );
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE) {
            if (notificationManager != null
                    && notificationManager.getNotificationChannel(channelID) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channelID,
                        "Location Service",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }

            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(400);
            locationRequest.setFastestInterval(2000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.getFusedLocationProviderClient(this)
                    .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            startForeground(Constants.LOCATION_SERVICE_ID, builder.build());
        }
    }

    private void stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(Constants.ACTION_START_LOCATION_SERVICE)) {
                    startLocationService();
                } else if (action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)) {
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startID);
    }


    // Haversine Formula
    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {

        if (lat2 == 1.0){
            return 0;
        }

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (unit == "kilometer") {
            dist = dist * 1.609344;
        } else if(unit == "meter"){
            dist = dist * 1609.344;
        }
        return (dist);
    }
    // This function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    // This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}
