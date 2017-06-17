package cz.jakubturcovsky.gps.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.avast.android.dialogs.iface.ISimpleDialogCancelListener;
import com.avast.android.dialogs.iface.ISimpleDialogDismissListener;
import com.avast.android.dialogs.iface.ISimpleDialogListener;

import java.util.ArrayList;
import java.util.List;

import cz.jakubturcovsky.gps.R;
import cz.jakubturcovsky.gps.fragment.BaseFragment;

public abstract class BaseActivity
        extends AppCompatActivity
        implements ISimpleDialogListener,
                   ISimpleDialogCancelListener,
                   ISimpleDialogDismissListener {

    private static final String TAG = BaseActivity.class.getSimpleName();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = super.onOptionsItemSelected(item);
        return handled;
    }

    @Override
    public void onNegativeButtonClicked(int requestCode, Bundle data) {
        for (BaseFragment fragment : getVisibleFragments()) {
            fragment.onNegativeButtonClicked(requestCode, data);
        }
    }

    @Override
    public void onNeutralButtonClicked(int requestCode, Bundle data) {
        for (BaseFragment fragment : getVisibleFragments()) {
            fragment.onNeutralButtonClicked(requestCode, data);
        }
    }

    @Override
    public void onPositiveButtonClicked(int requestCode, Bundle data) {
        for (BaseFragment fragment : getVisibleFragments()) {
            fragment.onPositiveButtonClicked(requestCode, data);
        }
    }

    @Override
    public void onCancelled(int requestCode, Bundle data) {
        for (BaseFragment fragment : getVisibleFragments()) {
            fragment.onCancelled(requestCode, data);
        }
    }

    @Override
    public void onDismissed(int requestCode, @Nullable Bundle data) {
        for (BaseFragment fragment : getVisibleFragments()) {
            fragment.onDismissed(requestCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        //noinspection RestrictedApi
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList == null) {
            super.onBackPressed();
            return;
        }

        boolean handled = false;
        for(Fragment f : fragmentList) {
            if(f instanceof BaseFragment) {
                handled = ((BaseFragment)f).onBackPressed();

                if(handled) {
                    break;
                }
            }
        }

        if(!handled) {
            super.onBackPressed();
        }
    }

    public List<BaseFragment> getVisibleFragments(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        //noinspection RestrictedApi
        List<Fragment> fragments = fragmentManager.getFragments();
        List<BaseFragment> result = new ArrayList<>();
        if (fragments != null){
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isVisible() && fragment instanceof BaseFragment) {
                    result.add((BaseFragment) fragment);
                }
            }
        }

        return result;
    }

    /**
     * Replaces content fragment.
     *
     * @param fragment Fragment, which is used for content.
     * @param addToBackStack Add transaction returnable.
     */
    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        if (!addToBackStack) {
            getSupportFragmentManager()
                    .popBackStack(fragment.getClass().getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName());
        if (addToBackStack) {
            trans.addToBackStack(fragment.getClass().getSimpleName());
        }
        trans.commit();
    }

    /**
     * Navigates to a parent Activity
     */
    public void navigateUp() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
        } else {
            NavUtils.navigateUpTo(this, upIntent);
        }
    }
}
