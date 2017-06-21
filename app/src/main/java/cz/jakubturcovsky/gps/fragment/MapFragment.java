package cz.jakubturcovsky.gps.fragment;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cz.jakubturcovsky.gps.BuildConfig;
import cz.jakubturcovsky.gps.R;
import cz.jakubturcovsky.gps.activity.BaseActivity;
import cz.jakubturcovsky.gps.adapter.LargeSnippetAdapter;
import cz.jakubturcovsky.gps.helper.DialogHelper;
import cz.jakubturcovsky.gps.helper.PermissionsHelper;
import cz.jakubturcovsky.gps.helper.PreferencesHelper;
import cz.jakubturcovsky.gps.service.LocationService;

import static cz.jakubturcovsky.gps.service.LocationService.CardinalDirection.EAST;
import static cz.jakubturcovsky.gps.service.LocationService.CardinalDirection.NORTH;
import static cz.jakubturcovsky.gps.service.LocationService.CardinalDirection.SOUTH;
import static cz.jakubturcovsky.gps.service.LocationService.CardinalDirection.WEST;

public class MapFragment
        extends BaseFragment {

    private static final String TAG = MapFragment.class.getSimpleName();

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private PolylineOptions mPolylineOptions;

    private boolean mTripInProgress;
    private int mLocationCounter;

    private NumberFormat mNumberFormat;
    private DateFormat mDateFormat;
    private BroadcastReceiver mLocationChangedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationService.EXTRA_LOCATION);
            if (location != null) {
                Log.d(TAG,
                        "Latitude = " + location.getLatitude() + "\nLongitude = " + location
                                .getLongitude() + "\nAccuracy = " + location.getAccuracy());

                addLocation(location);
            }
        }
    };

    private LocationService mService;
    private boolean mBound;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocationServiceBinder binder = (LocationService.LocationServiceBinder) service;
            mService = binder.getService();
            mBound = true;

            retrieveTrip();
            invalidateOptionsMenu();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    Unbinder mUnbinder;
    @BindView(R.id.map) MapView mMapView;
    @BindView(R.id.start_stop_trip) FloatingActionButton mStartStopTrip;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected String getTitle() {
        return getString(R.string.navigation_drawer_home);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mNumberFormat = NumberFormat.getInstance();
        mNumberFormat.setMaximumFractionDigits(6);
        mDateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mMapView.onCreate(savedInstanceState);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
        mMapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setInfoWindowAdapter(new LargeSnippetAdapter(getActivity()));
                mMap.getUiSettings().setZoomControlsEnabled(true);

                showMyLocation();
            }
        });
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
        if (mBound) {
            getContext().unbindService(mConnection);
            mBound = false;
            mService = null;
        }

        try {
            getActivity().unregisterReceiver(mLocationChangedReceiver);
        } catch (IllegalArgumentException ignored) {
            // There's no way to check if receiver is already unregistered, so... just in case
        }

        mLocationCounter = 0;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMapView.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mMap == null || mService == null || mPolylineOptions == null) {
            super.onCreateOptionsMenu(menu, inflater);
            return;
        }

        getActivity().getMenuInflater().inflate(R.menu.menu_map, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_show_position:
                DialogHelper.showSelectTripPosition(getActivity());
                break;
        }

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

    @OnClick(R.id.start_stop_trip)
    protected void onStartStopTripClicked() {
        if (!mTripInProgress) {
            mStartStopTrip.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_stop_24dp));
            startTrip();
        } else {
            mStartStopTrip.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_play_arrow_24dp));
            endTrip();
        }
    }

    @Override
    public void onListItemSelected(CharSequence value, int number, int requestCode, @Nullable Bundle data) {
        super.onListItemSelected(value, number, requestCode, data);
        switch (requestCode) {
            case DialogHelper.REQUEST_SELECT_TRIP_POSITION:
                onShowPositionDirectionSelected(number);
                break;
        }
    }

    private void onShowPositionDirectionSelected(@LocationService.CardinalDirection int direction) {
        if (mPolylineOptions == null) {
            Toast.makeText(getActivity(), R.string.map_show_position_empty_trip, Toast.LENGTH_SHORT).show();
            return;
        }

        List<LatLng> points = mPolylineOptions.getPoints();
        if (points == null || mMap == null) {
            return;
        }

        LatLng location = findBorderLocation(points, direction);
        if (location == null) {
            return;
        }

        CameraPosition position = CameraPosition.builder()
                .target(new LatLng(location.latitude, location.longitude))
                .zoom(mMap.getMaxZoomLevel())
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
    }

    public LatLng findBorderLocation(@NonNull List<LatLng> points, @LocationService.CardinalDirection int direction) {
        if (points.size() < 1) {
            return null;
        }

        LatLng result = null;
        for (LatLng latLng : points) {
            if (result == null) {
                result = latLng;
                continue;
            }
            if (direction == NORTH && Double.compare(result.latitude, latLng.latitude) < 0) {
                result = latLng;
            } else if (direction == SOUTH && Double.compare(result.latitude, latLng.latitude) > 0) {
                result = latLng;
            } else if (direction == EAST && Double.compare(result.longitude, latLng.longitude) < 0) {
                result = latLng;
            } else if (direction == WEST && Double.compare(result.longitude, latLng.longitude) > 0) {
                result = latLng;
            }
        }

        return result;
    }

    private void invalidateOptionsMenu() {
        getActivity().invalidateOptionsMenu();
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
        Location location = mLocationManager
                .getLastKnownLocation(mLocationManager.getBestProvider(criteria, false));
        if (location != null) {
            CameraPosition position = CameraPosition.builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .zoom(15)
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
        }

        registerLocationReceiver();
        bindLocationService();
    }

    private void registerLocationReceiver() {
        IntentFilter filter = new IntentFilter(LocationService.ACTION_LOCATION_CHANGED);
        getActivity().registerReceiver(mLocationChangedReceiver, filter);
    }

    private void bindLocationService() {
        Intent intent = LocationService.newIntent(getActivity());
        // Starting before binding ensures that service won't be destroyed after unbinding
        getActivity().startService(intent);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void retrieveTrip() {
        if (!mBound || mService == null) {
            if (BuildConfig.DEBUG) {
                throw new RuntimeException("This should not be called when the LocationService isn't bound!");
            }
            return;
        }

        addLocations(mService.getLocationList());
    }

    private void addLocations(@NonNull List<Location> locations) {
        for (Location location : locations) {
            addLocation(location);
        }
    }

    private void addLocation(@Nullable Location location) {
        if (location == null) {
            return;
        }

        LatLng point = new LatLng(location.getLatitude(), location.getLongitude());

        String snippet = getString(R.string.map_marker_snippet,
                mNumberFormat.format(location.getLatitude()),
                mNumberFormat.format(location.getLongitude()),
                mNumberFormat.format(location.getAccuracy()),
                mDateFormat.format(new Date(location.getTime())),
                location.getProvider());
        mMap.addMarker(new MarkerOptions()
                .position(point)
                .title(getString(R.string.map_marker_title, ++mLocationCounter))
                .snippet(snippet)
                .draggable(false));

        if (mPolylineOptions == null) {
            mPolylineOptions = new PolylineOptions();
            invalidateOptionsMenu();
        }
        mPolylineOptions.add(point);
        mMap.addPolyline(mPolylineOptions);
    }

    private void startTrip() {
        if (mService == null) {
            return;
        }

        mMap.clear();
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(PreferencesHelper.getRouteLineColor());
        mPolylineOptions.width(PreferencesHelper.getRouteLineWidth());
        mLocationCounter = 0;
        invalidateOptionsMenu();

        mTripInProgress = true;

        addLocation(mService.startTrip());
    }

    private void endTrip() {
        if (mService == null) {
            return;
        }

        mTripInProgress = false;
        mService.endTrip();
    }
}
