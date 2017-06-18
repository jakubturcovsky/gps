package cz.jakubturcovsky.gps.fragment;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cz.jakubturcovsky.gps.R;
import cz.jakubturcovsky.gps.activity.BaseActivity;
import cz.jakubturcovsky.gps.helper.PermissionsHelper;

public class MapFragment
        extends BaseFragment {

    private static final String TAG = MapFragment.class.getSimpleName();

    private GoogleMap mMap;

    Unbinder mUnbinder;
    @BindView(R.id.map) MapView mMapView;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    protected String getTitle() {
        return null;        // TODO: 18/06/17 R.string. title
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                if (!PermissionsHelper.checkFineLocationPermission(getActivity())) {
                    ((BaseActivity) getActivity()).requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                    return;
                }

                showMyLocation();
//                LatLng sydney = new LatLng(-34, 151);
////                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(sydney, 10);
//                mMap.animateCamera(cameraUpdate);
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
    public void onDestroyView() {
        super.onDestroyView();
        mMapView.onDestroy();
        mUnbinder.unbind();
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
        mMap.setMyLocationEnabled(true);
    }
}
