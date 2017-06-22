package cz.jakubturcovsky.gps.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import cz.jakubturcovsky.gps.BuildConfig;
import cz.jakubturcovsky.gps.helper.PermissionsHelper;
import cz.jakubturcovsky.gps.helper.PreferencesHelper;

public class LocationService
        extends Service
        implements LocationListener {

    private static final String TAG = LocationService.class.getSimpleName();

    public static final String ACTION_LOCATION_CHANGED = "action_location_changed";
    public static final String EXTRA_LOCATION = "extra_location";
    public static final long DEFAULT_ACQUIRE_LOCATION_PERIOD = 300_000L;      // 1min
    public static final long DEFAULT_ACQUIRE_LOCATION_PERIOD_DEBUG = 5_000L;      // 5s

    private static final long MIN_DISTANCE_CHANGE = 20; // 20m
    private static final long MIN_DISTANCE_CHANGE_DEBUG = 1; // 1m

    @IntDef()
    @Retention(RetentionPolicy.SOURCE)
    public @interface CardinalDirection {
        int NORTH = 0;
        int SOUTH = 1;
        int EAST = 2;
        int WEST = 3;
    }

    private LocationServiceBinder mBinder = new LocationServiceBinder();

    private LocationManager mLocationManager;
    private List<Location> mLocationList;

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, LocationService.class);
    }

    @NonNull
    public List<Location> getLocationList() {
        return mLocationList;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationList = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelLocationListener();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed");

        int listSize = mLocationList.size();
        if (mLocationList.get(listSize - 1).getAccuracy() > 100) {
            return;
        }
        mLocationList.add(location);

        Intent intent = new Intent(ACTION_LOCATION_CHANGED);
        intent.putExtra(EXTRA_LOCATION, location);
        sendBroadcast(intent);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, provider + " provider disabled");
        if (resetAcquireLocationTimer(PreferencesHelper.getAcquireLocationPeriod()) != null) {
            return;
        }

        if (!PermissionsHelper.checkFineLocationPermission(this)) {
            Log.w(TAG, "Missing permission");
        } else {
            Log.w(TAG, "All providers are disabled");
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, provider + " provider enabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "Status changed to " + status);
    }

    @Nullable
    public Location startTrip() {
        mLocationList.clear();
        return resetAcquireLocationTimer(PreferencesHelper.getAcquireLocationPeriod());
    }

    public void endTrip() {
        cancelLocationListener();
    }

    @Nullable
    private Location resetAcquireLocationTimer(long period) {
        cancelLocationListener();
        if (BuildConfig.DEBUG && period == 42) {
            return startLocationListener(DEFAULT_ACQUIRE_LOCATION_PERIOD_DEBUG);
        }

        if (period == -1) {
            return startLocationListener(DEFAULT_ACQUIRE_LOCATION_PERIOD);
        } else if (period == 0) {
            cancelLocationListener();
        } else {
            return startLocationListener(period);
        }

        return null;
    }

    public Location startLocationListener(final long period) {
        if (mLocationManager == null) {
            return null;
        }
        if (!PermissionsHelper.checkFineLocationPermission(this)) {
            return null;
        }

        Location location = null;
        String[] providers = new String[]{LocationManager.FUSED_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER};
        for (String provider : providers) {
            if (!isProviderEnabled(provider)) {
                continue;
            }

            location = mLocationManager.getLastKnownLocation(provider);
            if (location == null) {
                continue;
            }

            mLocationList.add(location);
            mLocationManager.requestLocationUpdates(provider,
                    period,
                    BuildConfig.DEBUG ? MIN_DISTANCE_CHANGE_DEBUG : MIN_DISTANCE_CHANGE,
                    this);
            break;
        }

        return location;
    }

    private boolean isProviderEnabled(@NonNull String provider) {
        return mLocationManager.isProviderEnabled(provider);
    }

    public void cancelLocationListener() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
    }

    public class LocationServiceBinder
            extends Binder {

        public LocationService getService() {
            return LocationService.this;
        }
    }
}
