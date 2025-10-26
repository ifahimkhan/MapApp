package com.fahim.mapapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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

    private static final String TAG = "MapsActivity";
    private static final String EXTRA_DEVICE_ID = "selectedDeviceId";
    private static final double DEFAULT_LATITUDE = -31.0;
    private static final double DEFAULT_LONGITUDE = 151.0;
    private static final double DEFAULT_ZOOM = 15.0;

    private MapView mMap;
    private ActivityMapsBinding binding;
    private double latitude = DEFAULT_LATITUDE;
    private double longitude = DEFAULT_LONGITUDE;
    private MyLocationNewOverlay myLocationOverlay;
    private boolean isShowingCurrentLocation = false;
    private Marker deviceMarker;
    private ValueEventListener firebaseListener;
    private DatabaseReference deviceRef;

    private String selectedDeviceId;

    public static void getInstance(Context context, String selectedDeviceId) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.putExtra(EXTRA_DEVICE_ID, selectedDeviceId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure OSMDroid
        Configuration.getInstance().setUserAgentValue(BuildConfig.LIBRARY_PACKAGE_NAME);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        selectedDeviceId = getIntent().getStringExtra(EXTRA_DEVICE_ID);
        if (selectedDeviceId == null) {
            Log.e(TAG, "No device ID provided");
            finish();
            return;
        }

        Log.d(TAG, "Device ID: " + selectedDeviceId);

        mMap = binding.map;
        setupMap();
        setupClickListeners();
    }

    private void setupMap() {
        mMap.setMultiTouchControls(true);
        mMap.getController().setZoom(DEFAULT_ZOOM);

        // Create marker once
        deviceMarker = new Marker(mMap);
        deviceMarker.setTitle(selectedDeviceId);
        mMap.getOverlays().add(deviceMarker);

        updateMapPosition();
    }

    private void setupClickListeners() {
        binding.currentLocation.setOnClickListener(v -> toggleLocationMode());
    }

    private void toggleLocationMode() {
        if (isShowingCurrentLocation) {
            // Switch back to device location
            removeLocationOverlay();
            updateMapPosition();
            isShowingCurrentLocation = false;
        } else {
            // Switch to current location
            setupLocationOverlay();
            isShowingCurrentLocation = true;
        }
    }

    private void updateMapPosition() {
        GeoPoint geoPoint = new GeoPoint(latitude, longitude);
        mMap.getController().setCenter(geoPoint);

        // Update marker position
        if (deviceMarker != null) {
            deviceMarker.setPosition(geoPoint);
            mMap.invalidate();
        }
    }

    private void setupLocationOverlay() {
        if (myLocationOverlay == null) {
            myLocationOverlay = new MyLocationNewOverlay(
                    new GpsMyLocationProvider(this), mMap
            );
            myLocationOverlay.setDrawAccuracyEnabled(true);
        }

        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();

        if (!mMap.getOverlays().contains(myLocationOverlay)) {
            mMap.getOverlays().add(myLocationOverlay);
        }
    }

    private void removeLocationOverlay() {
        if (myLocationOverlay != null) {
            myLocationOverlay.disableFollowLocation();
            myLocationOverlay.disableMyLocation();
            mMap.getOverlays().remove(myLocationOverlay);
        }
    }

    private void setupFirebaseListener() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        deviceRef = database.getReference("devices").child(selectedDeviceId);

        firebaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.w(TAG, "Device data not found");
                    return;
                }

                String latStr = dataSnapshot.child("latitude").getValue(String.class);
                String lonStr = dataSnapshot.child("longitude").getValue(String.class);

                if (latStr != null && lonStr != null) {
                    try {
                        double newLat = Double.parseDouble(latStr);
                        double newLon = Double.parseDouble(lonStr);

                        // Only update if values changed
                        if (latitude != newLat || longitude != newLon) {
                            latitude = newLat;
                            longitude = newLon;
                            Log.d(TAG, "Location updated: " + latitude + ", " + longitude);

                            // Only update map if not following current location
                            if (!isShowingCurrentLocation) {
                                updateMapPosition();
                            }
                        }
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Invalid coordinate format", e);
                    }
                } else {
                    Log.w(TAG, "Latitude or longitude is null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase error: " + error.getMessage());
            }
        };

        deviceRef.addValueEventListener(firebaseListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMap.onResume();
        setupFirebaseListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMap.onPause();

        // Remove Firebase listener to prevent memory leaks
        if (deviceRef != null && firebaseListener != null) {
            deviceRef.removeEventListener(firebaseListener);
        }

        // Disable location updates
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up resources
        if (myLocationOverlay != null) {
            myLocationOverlay.onDetach(mMap);
        }

        binding = null;
    }
}