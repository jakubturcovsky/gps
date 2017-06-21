package cz.jakubturcovsky.gps.fragment;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cz.jakubturcovsky.gps.R;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;

public class ChartFragment
        extends BaseFragment {

    private static final String TAG = ChartFragment.class.getSimpleName();

    public static final String ARG_LOCATIONS = "arg_locations";

    private ArrayList<Location> mLocations;

    Unbinder mUnbinder;
    @BindView(R.id.chart) ColumnChartView mChart;

    public static ChartFragment newInstance(ArrayList<Location> locations) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_LOCATIONS, locations);
        fragment.setArguments(args);

        return fragment;
    }
    @Override
    protected String getTitle() {
        return getString(R.string.accuracy_chart_title);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            getActivity().finish();
        }

        mLocations = getArguments().getParcelableArrayList(ARG_LOCATIONS);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        generateData();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    private void generateData() {
        List<Column> columns = new ArrayList<>();
        ArrayList<AxisValue> axisValues = new ArrayList<>();

        int i = 0;
        if (mLocations.size() > 10) {
            i = mLocations.size() - 11;
        }
        for (; i < mLocations.size(); i++) {
            Location location = mLocations.get(i);
            List<SubcolumnValue> values = new ArrayList<>();
            values.add(new SubcolumnValue(location.getAccuracy(), ChartUtils.pickColor()));

            Column column = new Column(values);
            columns.add(column);

            axisValues.add(new AxisValue(i));
        }

        ColumnChartData data = new ColumnChartData(columns);

        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        axisX.setName(getString(R.string.accuracy_chart_axis_x_label));
        axisX.setValues(axisValues);
        axisY.setName(getString(R.string.accuracy_chart_axis_y_label));

        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        mChart.setColumnChartData(data);
    }
}
