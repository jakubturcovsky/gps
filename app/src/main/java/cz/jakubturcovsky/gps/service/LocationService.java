package cz.jakubturcovsky.gps.service;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
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
    public static final String ACTION_PROVIDER_DOWN = "action_provider_down";
    public static final String ACTION_MISSING_PERMISSION = "action_missing_permission";
    public static final long DEFAULT_ACQUIRE_LOCATION_PERIOD = 300_000L;      // 1min
    public static final long DEFAULT_ACQUIRE_LOCATION_PERIOD_DEBUG = 5_000L;      // 5s

    private static final long MIN_DISTANCE_CHANGE = 10; // 10m
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

        mLocationList.add(location);

        Intent intent = new Intent(ACTION_LOCATION_CHANGED);
        intent.putExtra(EXTRA_LOCATION, location);
        sendBroadcast(intent);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, provider + " provider disabled");
        if ((LocationManager.GPS_PROVIDER.equals(provider) && isNetworkProviderEnabled())
                || (LocationManager.NETWORK_PROVIDER.equals(provider) && isGpsProviderEnabled())) {
            resetAcquireLocationTimer(PreferencesHelper.getAcquireLocationPeriod());
        } else if (!PermissionsHelper.checkFineLocationPermission(this)) {
            sendProviderDown();
        } else {
            sendPermissionMissing();
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
        boolean gpsProviderEnabled = isGpsProviderEnabled();
        boolean networkProviderEnabled = isNetworkProviderEnabled();

        if (!gpsProviderEnabled && !networkProviderEnabled) {
            // No network provider is enabled
            // TODO: 18/06/17 Move user to settings
            return null;
        }

        if (!PermissionsHelper.checkFineLocationPermission(this)) {
            // TODO: 18/06/17 Request permission
            return null;
        }

        Location location = null;
        if (networkProviderEnabled) {
            Log.d(TAG, "Network provider");
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    period,
                    BuildConfig.DEBUG ? MIN_DISTANCE_CHANGE_DEBUG : MIN_DISTANCE_CHANGE,
                    this);
            if (mLocationManager != null) {
                location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }
        // If GPS enabled, get latitude/longitude using GPS Services
        if (gpsProviderEnabled) {
            if (location == null) {
                Log.d(TAG, "GPS provider");
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        period,
                        BuildConfig.DEBUG ? MIN_DISTANCE_CHANGE_DEBUG : MIN_DISTANCE_CHANGE,
                        this);
                if (mLocationManager != null) {
                    location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }
        }

        mLocationList.add(location);

        return location;
    }

    public boolean isGpsProviderEnabled() {
        return isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public boolean isNetworkProviderEnabled() {
        return isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean isProviderEnabled(@NonNull String provider) {
        return mLocationManager.isProviderEnabled(provider);
    }

    public void cancelLocationListener() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
    }

    private void sendProviderDown() {
        Intent intent = new Intent(ACTION_PROVIDER_DOWN);
        sendBroadcast(intent);
    }

    private void sendPermissionMissing() {
        Intent intent = new Intent(ACTION_MISSING_PERMISSION);
        sendBroadcast(intent);
    }

    /**
     * Function to show settings alert dialog.
     * On pressing the Settings button it will launch Settings Options.
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is disabled");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing the Settings button.
        alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // On pressing the cancel button
        alertDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public class LocationServiceBinder
            extends Binder {

        public LocationService getService() {
            return LocationService.this;
        }
    }
}
