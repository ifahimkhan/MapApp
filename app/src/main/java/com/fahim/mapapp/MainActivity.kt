package com.fahim.mapapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fahim.mapapp.databinding.ActivityMainBinding;
import com.fahim.mapapp.service.LocationBroadCastReceiver;
import com.fahim.mapapp.service.LocationService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    private ActivityMainBinding binding;
    private String TAG = MainActivity.class.getName();
    LocationBroadCastReceiver locationBroadCastReceiver=new LocationBroadCastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.map.setOnClickListener(v -> {
            if (!requestLocationPermission(MainActivity.this)) {
//                MapsActivity.getInstance(MainActivity.this);
                showListDialog(MainActivity.this);
            }
        });
        binding.startTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!requestLocationPermission(MainActivity.this)) {
                    showDeviceIdDialog(MainActivity.this);
                }
            }
        });

    }

    public void disableStartButton() {
        binding.startTracking.setEnabled(false);
        binding.startTracking.setAlpha(0.5f);
    }

    public static boolean requestLocationPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return true;
        } else {
            return false;
        }
    }

    public void showDeviceIdDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.device_id_dialog, null);
        final EditText editTextDeviceId = view.findViewById(R.id.editTextDeviceId);

        builder.setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String deviceId = editTextDeviceId.getText().toString();
                        LocationService.getInstance(MainActivity.this, deviceId);
                        sendBroadcast(new Intent("LOCATION_UPDATE"));
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    public void showListDialog(Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("devices");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> items = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String deviceName = childSnapshot.getKey();
                    items.add(deviceName);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Select an item")
                        .setItems(items.toArray(new String[0]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String selectedItem = items.get(which);
                                Log.e(TAG, "selected: " + selectedItem);
                                MapsActivity.getInstance(MainActivity.this, selectedItem);

                            }
                        })
                        .show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisssion Granted!", Toast.LENGTH_SHORT).show();
                showListDialog(MainActivity.this);
            } else {
                Toast.makeText(this, "Permission denied by user!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(locationBroadCastReceiver,new IntentFilter("LOCATION_UPDATE"),Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(locationBroadCastReceiver);
    }
}