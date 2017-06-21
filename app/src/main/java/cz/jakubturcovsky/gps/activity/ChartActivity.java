package cz.jakubturcovsky.gps.activity;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.jakubturcovsky.gps.R;
import cz.jakubturcovsky.gps.fragment.ChartFragment;

public class ChartActivity
        extends BaseActivity {

    private static final String TAG = ChartActivity.class.getSimpleName();

    public static final String EXTRA_LOCATIONS = "extra_locations";

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.fragment_container) FrameLayout mFragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent() == null || getIntent().getExtras() == null) {
            finish();
        }

        if (savedInstanceState == null) {
            ArrayList<Location> locations = getIntent().getParcelableArrayListExtra(EXTRA_LOCATIONS);
            replaceFragment(ChartFragment.newInstance(locations), false);
        }
    }
}
