package cz.jakubturcovsky.gps.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionsHelper {

    public static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 3829;

    public static boolean checkCoarseLocationPermission(@NonNull Context context) {
        if (isAboveMarshmallow() && !isPermissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return false;
        }

        return true;
    }

    public static void requestCoarseLocationPermission(@NonNull Activity activity) {
        requestPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_CODE_ACCESS_COARSE_LOCATION);
    }

    public static void requestPermission(@NonNull Activity activity, @NonNull String permission, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                new String[]{permission},
                requestCode);
    }

    private static boolean isAboveMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private static boolean isPermissionGranted(@NonNull Context context, @NonNull String permission) {
        int permissionState = ContextCompat.checkSelfPermission(context, permission);

        return permissionState == PackageManager.PERMISSION_GRANTED;
    }
}
