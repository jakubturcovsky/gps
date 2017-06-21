package cz.jakubturcovsky.gps.helper;

import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.widget.AbsListView;

import com.avast.android.dialogs.fragment.ListDialogFragment;
import com.avast.android.dialogs.fragment.SimpleDialogFragment;

import cz.jakubturcovsky.gps.R;

public class DialogHelper {

    public static final String EXTRA_FRAGMENT_TAG = "extra_fragment_tag";

    public static final int REQUEST_ACCESS_FINE_LOCATION_PERMISSION = 1;
    public static final int REQUEST_SELECT_TRIP_POSITION = 2;
    public static final int REQUEST_LOCATION_OFF = 3;

    public static DialogFragment showFineLocationPermissionInfo(@NonNull FragmentActivity activity) {
        SimpleDialogFragment.SimpleDialogBuilder builder = SimpleDialogFragment.createBuilder(activity, activity.getSupportFragmentManager())
                .setTitle(R.string.permission_info_access_coarse_location_title)
                .setMessage(R.string.permission_info_access_coarse_location_message)
                .setPositiveButtonText(android.R.string.ok)
                .setCancelableOnTouchOutside(false)
                .setRequestCode(REQUEST_ACCESS_FINE_LOCATION_PERMISSION);

        return builder.show();
    }

    public static ListDialogFragment showSelectTripPosition(@NonNull FragmentActivity activity) {
        ListDialogFragment.SimpleListDialogBuilder builder = ListDialogFragment.createBuilder(activity, activity.getSupportFragmentManager())
                .setTitle(R.string.map_dialog_show_position_title)
                .setChoiceMode(AbsListView.CHOICE_MODE_SINGLE)
                .setItems(R.array.show_position_options)
                .setRequestCode(REQUEST_SELECT_TRIP_POSITION);

        return builder.show();
    }

    public static DialogFragment showLocationOff(@NonNull FragmentActivity activity) {
        SimpleDialogFragment.SimpleDialogBuilder builder = SimpleDialogFragment.createBuilder(activity, activity.getSupportFragmentManager())
                .setTitle(R.string.location_off_dialog_title)
                .setMessage(R.string.location_off_dialog_message)
                .setPositiveButtonText(android.R.string.ok)
                .setNegativeButtonText(android.R.string.cancel)
                .setCancelableOnTouchOutside(false)
                .setRequestCode(REQUEST_LOCATION_OFF);

        return builder.show();
    }
}
