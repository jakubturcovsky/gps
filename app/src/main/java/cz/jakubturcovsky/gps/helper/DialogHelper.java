package cz.jakubturcovsky.gps.helper;

import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;

import cz.jakubturcovsky.gps.R;

public class DialogHelper {

    public static final String EXTRA_FRAGMENT_TAG = "extra_fragment_tag";

    public static final int REQUEST_ACCESS_FINE_LOCATION_PERMISSION = 1;

    public static DialogFragment showCoarseLocationPermissionInfo(@NonNull AppCompatActivity activity) {
        SimpleDialogFragment.SimpleDialogBuilder builder = SimpleDialogFragment.createBuilder(activity, activity.getSupportFragmentManager())
                .setTitle(R.string.permission_info_access_coarse_location_title)
                .setMessage(R.string.permission_info_access_coarse_location_message)
                .setPositiveButtonText(android.R.string.ok)
                .setNegativeButtonText(android.R.string.cancel)
                .setRequestCode(DialogHelper.REQUEST_ACCESS_FINE_LOCATION_PERMISSION);

        return builder.show();
    }
}
