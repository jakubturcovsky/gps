package cz.jakubturcovsky.gps.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import com.avast.android.dialogs.iface.ISimpleDialogCancelListener;
import com.avast.android.dialogs.iface.ISimpleDialogDismissListener;
import com.avast.android.dialogs.iface.ISimpleDialogListener;

import cz.jakubturcovsky.gps.R;

public abstract class BaseFragment
        extends Fragment
        implements ISimpleDialogListener,
                   ISimpleDialogCancelListener,
                   ISimpleDialogDismissListener {

    private static final String TAG = BaseFragment.class.getSimpleName();

    protected abstract String getTitle();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getTitle() != null) {
            getActivity().setTitle(getTitle());
        } else {
            getActivity().setTitle(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home && getActivity().getSupportFragmentManager()
                                                             .getBackStackEntryCount() > 0) {
            getActivity().getSupportFragmentManager().popBackStack();
        }

        return false;
    }

    @Override
    public void onNegativeButtonClicked(int requestCode, @Nullable Bundle data) {

    }

    @Override
    public void onNeutralButtonClicked(int requestCode, @Nullable Bundle data) {

    }

    @Override
    public void onPositiveButtonClicked(int requestCode, @Nullable Bundle data) {

    }

    @Override
    public void onCancelled(int requestCode, @Nullable Bundle data) {

    }

    @Override
    public void onDismissed(int requestCode, @Nullable Bundle data) {

    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        if (!addToBackStack) {
            fragmentManager.popBackStack(fragment.getClass().getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        FragmentTransaction trans = fragmentManager.beginTransaction();
        trans.replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName());
        if (addToBackStack) {
            trans.addToBackStack(fragment.getClass().getSimpleName());
        }
        trans.commit();
    }

    /**
     * Could handle back press.
     *
     * @return true if back press was handled
     */
    public boolean onBackPressed() {
        return false;
    }
}
