package cz.jakubturcovsky.gps.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import cz.jakubturcovsky.gps.R;

public class SettingsFragment
        extends PreferenceFragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    private PreferenceScreen mScreen;

    @NonNull
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        addPreferencesFromResource(R.xml.settings);
        mScreen = getPreferenceScreen();
    }
}
