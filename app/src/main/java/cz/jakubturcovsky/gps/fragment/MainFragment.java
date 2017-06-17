package cz.jakubturcovsky.gps.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cz.jakubturcovsky.gps.R;

public class MainFragment
        extends BaseFragment {

    private static final String TAG = MainFragment.class.getSimpleName();

    Unbinder mUnbinder;
    @BindView(R.id.sample_text) TextView mSampleText;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    protected String getTitle() {
        return null;        // TODO: 17/06/17 Add title
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mSampleText.setText(stringFromJNI());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
