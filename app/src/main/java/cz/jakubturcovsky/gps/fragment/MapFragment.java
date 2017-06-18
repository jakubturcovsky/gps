package cz.jakubturcovsky.gps.fragment;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cz.jakubturcovsky.gps.R;
import cz.jakubturcovsky.gps.activity.BaseActivity;
import cz.jakubturcovsky.gps.adapter.LargeSnippetAdapter;
import cz.jakubturcovsky.gps.helper.PermissionsHelper;
import cz.jakubturcovsky.gps.helper.PreferencesHelper;
import cz.jakubturcovsky.gps.service.LocationService;

public class MapFragment
        extends BaseFragment {

    private static final String TAG = MapFragment.class.getSimpleName();

    private BroadcastReceiver mLocationChangedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationService.EXTRA_LOCATION);
            if (location != null) {
                Log.d(TAG,
                        "Latitude = " + location.getLatitude() + "\nLongitude = " + location
                                .getLongitude() + "\nAccuracy = " + location.getAccuracy());
                LatLng point = new LatLng(location.getLatitude(), location.getLongitude());

                mLocationList.add(location);
                NumberFormat numberFormat = NumberFormat.getInstance();
                DateFormat dateFormat = DateFormat
                        .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());
                String snippet = getString(R.string.map_marker_snippet,
                        numberFormat.format(location.getLatitude()),
                        numberFormat.format(location.getLongitude()),
                        numberFormat.format(location.getAccuracy()),
                        dateFormat.format(new Date(location.getTime())),
                        getString(R.string.map_source));
                mMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title(getString(R.string.map_marker_title, mLocationList.size()))
                        .snippet(snippet)
                        .draggable(false));

                mPolylineOptions.add(point);
                mMap.addPolyline(mPolylineOptions);
            }
        }
    };

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private List<Location> mLocationList;
    private PolylineOptions mPolylineOptions;

    private boolean mJourneyInProgress;

    Unbinder mUnbinder;
    @BindView(R.id.map) MapView mMapView;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    protected String getTitle() {
        return getString(R.string.navigation_drawer_home);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setInfoWindowAdapter(new LargeSnippetAdapter(getActivity()));

                getActivity().invalidateOptionsMenu();

                IntentFilter filter = new IntentFilter(LocationService.ACTION_LOCATION_CHANGED);
                getActivity().registerReceiver(mLocationChangedReceiver, filter);

                showMyLocation();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mLocationChangedReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMapView.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mMap == null) {
            super.onCreateOptionsMenu(menu, inflater);
            return;
        }

        getActivity().getMenuInflater().inflate(R.menu.menu_map, menu);
        MenuItem startItem = menu.findItem(R.id.action_start_journey);
        MenuItem endItem = menu.findItem(R.id.action_end_journey);
        if (mJourneyInProgress) {
            startItem.setVisible(false);
            endItem.setVisible(true);
        } else {
            startItem.setVisible(true);
            endItem.setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mJourneyInProgress = item.getItemId() == R.id.action_start_journey;
        switch (item.getItemId()) {
            case R.id.action_start_journey:
                mMap.clear();
                mPolylineOptions = new PolylineOptions();
                mPolylineOptions.color(PreferencesHelper.getRouteLineColor());
                mPolylineOptions.width(PreferencesHelper.getRouteLineWidth());
                mLocationList = new ArrayList<>();
                getActivity().startService(LocationService.newIntent(getActivity()));
                break;
            case R.id.action_end_journey:
                getActivity().stopService(LocationService.newIntent(getActivity()));
                break;
        }

        getActivity().invalidateOptionsMenu();

        return true;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onPermissionGranted(@NonNull String permission) {
        super.onPermissionGranted(permission);
        if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)) {
            showMyLocation();
        }
    }

    private void showMyLocation() {
        if (!PermissionsHelper.checkFineLocationPermission(getActivity())) {
            ((BaseActivity) getActivity()).requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        // Enable 'My location' button
        mMap.setMyLocationEnabled(true);

        // Zoom to my location
        Criteria criteria = new Criteria();
        Location location = mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(criteria, false));
        if (location != null) {
            mMap.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
        }
    }
}
