package cz.jakubturcovsky.gps.service;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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

    public static final long DEFAULT_ACQUIRE_LOCATION_PERIOD = 5_000L;      // 1min

    private static final long MIN_DISTANCE_CHANGE = 1; // 10m

    private LocationManager mLocationManager;
    private Location mLocation;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        resetAcquireLocationTimer(PreferencesHelper.getAcquireLocationPeriod());

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

        Intent intent = new Intent(ACTION_LOCATION_CHANGED);
        intent.putExtra(EXTRA_LOCATION, location);
        sendBroadcast(intent);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "Provider disabled");
        if ((LocationManager.GPS_PROVIDER.equals(provider) && isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                || (LocationManager.NETWORK_PROVIDER.equals(provider) && isProviderEnabled(LocationManager.GPS_PROVIDER))) {
            resetAcquireLocationTimer(PreferencesHelper.getAcquireLocationPeriod());
        } else if (!PermissionsHelper.checkFineLocationPermission(this)) {
            sendProviderDown();
        } else {
            sendPermissionMissing();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "Provider enabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "Status changed to " + status);
    }

    private void resetAcquireLocationTimer(long period) {
        cancelLocationListener();
        if (period == -1) {
            startLocationListener(DEFAULT_ACQUIRE_LOCATION_PERIOD);
        } else if (period == 0) {
            cancelLocationListener();
        } else {
            startLocationListener(period);
        }
    }

    public Location startLocationListener(final long period) {
        boolean gpsProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gpsProviderEnabled && !networkProviderEnabled) {
            // No network provider is enabled
            // TODO: 18/06/17 Move user to settings
            return null;
        }

        if (!PermissionsHelper.checkFineLocationPermission(this)) {
            // TODO: 18/06/17 Request permission
            return null;
        }

        if (networkProviderEnabled) {
            Log.d(TAG, "Network provider");
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, period, MIN_DISTANCE_CHANGE, this);
            if (mLocationManager != null) {
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }
        // If GPS enabled, get latitude/longitude using GPS Services
        if (gpsProviderEnabled) {
            if (mLocation == null) {
                Log.d(TAG, "GPS provider");
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, period, MIN_DISTANCE_CHANGE, this);
                if (mLocationManager != null) {
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }
        }

        return mLocation;
    }

    public void cancelLocationListener() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
    }

    private boolean isProviderEnabled(@NonNull String provider) {
        return mLocationManager.isProviderEnabled(provider);
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
}
