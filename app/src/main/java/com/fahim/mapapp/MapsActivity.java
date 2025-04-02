package com.fahim.mapapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.fahim.mapapp.databinding.ActivityMapsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapsActivity extends FragmentActivity {

    private MapView mMap;
    private ActivityMapsBinding binding;
    private double latitude = -31, longitude = 151;
    private MyLocationNewOverlay myLocationOverlay;
    private boolean showCurrentLocationNextTime = true;

    private String selectedDeviceId;

    public static void getInstance(Context context, String selectedDeviceId) {
        context.startActivity(new Intent(context, MapsActivity.class).putExtra("selectedDeviceId", selectedDeviceId));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(BuildConfig.LIBRARY_PACKAGE_NAME);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        selectedDeviceId = getIntent().getStringExtra("selectedDeviceId");
        Log.e("TAG", "onCreate: " + selectedDeviceId);

        mMap = binding.map;
        updateMap();
        binding.currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showCurrentLocationNextTime) {
                    setupLocationOverlay();
                    showCurrentLocationNextTime = false;
                } else {
                    updateMap();
                    showCurrentLocationNextTime = true;
                }
            }
        });


    }

    private void updateMap() {
        GeoPoint geoPoint = new GeoPoint(latitude, longitude);
        mMap.setMultiTouchControls(true);
        mMap.getController().setZoom(15.0);
        mMap.getController().setCenter(geoPoint);
        addMarker(geoPoint, selectedDeviceId);

    }

    private void addMarker(GeoPoint point, String title) {
        Marker marker = new Marker(mMap);
        marker.setPosition(point);
        marker.setTitle(title);
        mMap.getOverlays().clear();
        mMap.getOverlays().add(marker);
        mMap.invalidate(); // Refresh the map
    }

    private void setupLocationOverlay() {
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mMap);
        myLocationOverlay.enableMyLocation(); // Enable GPS location
        myLocationOverlay.enableFollowLocation(); // Auto-follow user
        myLocationOverlay.setDrawAccuracyEnabled(true); // Show accuracy circle
        mMap.getOverlays().add(myLocationOverlay);
    }


    @Override
    protected void onResume() {
        super.onResume();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("devices").child(selectedDeviceId);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String latitude = dataSnapshot.child("latitude").getValue(String.class);
                String longitude = dataSnapshot.child("longitude").getValue(String.class);
                Log.e("TAG", "onDataChange: " + latitude);
                Log.e("TAG", "onDataChange: " + longitude);

                // Do something with the latitude and longitude values
                if (latitude != null && longitude != null) {
                    // Update UI, perform calculations, etc.
                    MapsActivity.this.latitude = Double.parseDouble(latitude);
                    MapsActivity.this.longitude = Double.parseDouble(longitude);
                    updateMap();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
            }
        });
    }


}