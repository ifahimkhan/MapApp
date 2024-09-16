package com.fahim.mapapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.fahim.mapapp.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private Location location;
    private double latitude = -31, longitude = 151;

    public static void getInstance(Context context) {
        context.startActivity(new Intent(context, MapsActivity.class));
    }

    MarkerOptions marker = new MarkerOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.e("TAG", ": oncreaete");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        location = GpsUtil.getLocation(this, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.e("TAG", "getLatitude: " + location.getLatitude());
                Log.e("TAG", "getLongitude: " + location.getLongitude());
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);
                marker.position(latLng)
                        .title("Your Location");
                mMap.addMarker(marker);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
               try {
                   Log.e("TAG", "onStatusChanged: "+provider);
                   LocationListener.super.onStatusChanged(provider, status, extras);
               }catch (Exception e){
                   e.printStackTrace();
               }

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e("TAG", ":onMapReady ");
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        // Add a marker in Sydney and move the camera
        LatLng currentLocation = new LatLng(latitude, longitude);
        mMap.addMarker(marker.position(currentLocation).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

    }
}