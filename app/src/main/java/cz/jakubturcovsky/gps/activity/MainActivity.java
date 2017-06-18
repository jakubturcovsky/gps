package cz.jakubturcovsky.gps.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.jakubturcovsky.gps.R;
import cz.jakubturcovsky.gps.fragment.MapFragment;

public class MainActivity
        extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                   FragmentManager.OnBackStackChangedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.fragment_container) FrameLayout mFragmentContainer;
    @BindView(R.id.left_drawer) NavigationView mLeftDrawer;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        /* Navigation drawer */
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mLeftDrawer.setNavigationItemSelectedListener(this);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        mTitle = getTitle();

        if (savedInstanceState == null) {
            replaceFragment(MapFragment.newInstance(), false);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return false;
        }

        return false;
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mTitle);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Handles selecting an item from navigation drawer and displays the right fragment (or something else).
     * <p>
     * Note: {@link MenuItem#setChecked(boolean)} with FALSE doesn't work due to a bug.
     * See https://code.google.com/p/android/issues/detail?id=184089&
     *
     * @param menuItem Selected item
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        mDrawerLayout.closeDrawer(GravityCompat.START);

        if (menuItem.isChecked()) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                menuItem.setChecked(true);
                setTitle(menuItem.getTitle());
                getSupportFragmentManager().popBackStack();
                return true;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return false;
            case R.id.nav_about:
                displayAboutDialog();
                return false;
        }

        return true;
    }

    @Override
    public void onBackStackChanged() {
        FragmentManager manager = getSupportFragmentManager();
        int entryCount = manager.getBackStackEntryCount();
        if (entryCount > 0) {
            mDrawerToggle.setDrawerIndicatorEnabled(false);
        } else {
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        }
    }

    public void resetDrawer() {
        mLeftDrawer.getMenu().getItem(0).setChecked(true);
    }

    private void displayAboutDialog() {
        SimpleDialogFragment.createBuilder(this, getSupportFragmentManager())
                .setTitle(R.string.app_name)
                .setMessage(getString(R.string.about_dialog_message, acquireVersionNumber()))
                .setPositiveButtonText(android.R.string.ok)
                .show();
    }

    private String acquireVersionNumber() {
        try {
            return (getPackageManager().getPackageInfo(getPackageName(), 0)).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "?.?.?";     // TODO: 17/06/17 R.string...
    }
}
