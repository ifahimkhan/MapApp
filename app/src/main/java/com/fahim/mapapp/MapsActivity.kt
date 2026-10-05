package com.fahim.mapapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.fahim.mapapp.ui.theme.MapAppTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

class MapsActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SELECTED_DEVICE_ID = "selectedDeviceId"

        @JvmStatic
        fun getInstance(context: Context, selectedDeviceId: String) {
            val intent = Intent(context, MapsActivity::class.java).apply {
                putExtra(EXTRA_SELECTED_DEVICE_ID, selectedDeviceId)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedDeviceId = intent.getStringExtra(EXTRA_SELECTED_DEVICE_ID) ?: ""
        Log.e("TAG", "onCreate: $selectedDeviceId")

        setContent {
            MapAppTheme {
                MapsScreen(
                    selectedDeviceId = selectedDeviceId,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun MapsScreen(
    selectedDeviceId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var latitude by remember { mutableDoubleStateOf(-31.0) }
    var longitude by remember { mutableDoubleStateOf(151.0) }

    val currentLatLng = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLatLng, 15f)
    }
    val markerState = rememberMarkerState(position = currentLatLng)

    // Update marker and camera position when lat/lng changes
    LaunchedEffect(latitude, longitude) {
        val newLatLng = LatLng(latitude, longitude)
        markerState.position = newLatLng
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(newLatLng, 15f))
    }

    // Firebase real-time listener for the selected device
    DisposableEffect(selectedDeviceId) {
        if (selectedDeviceId.isNotEmpty()) {
            val database = FirebaseDatabase.getInstance()
            val myRef: DatabaseReference = database.getReference("devices").child(selectedDeviceId)

            val listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Try to get values as Double first, then fallback to String if necessary
                    val latVal = dataSnapshot.child("latitude").value
                    val lngVal = dataSnapshot.child("longitude").value
                    
                    Log.e("TAG", "onDataChange: lat=$latVal, lng=$lngVal")

                    val newLat = when (latVal) {
                        is Number -> latVal.toDouble()
                        is String -> latVal.toDoubleOrNull()
                        else -> null
                    }
                    
                    val newLng = when (lngVal) {
                        is Number -> lngVal.toDouble()
                        is String -> lngVal.toDoubleOrNull()
                        else -> null
                    }

                    if (newLat != null && newLng != null) {
                        latitude = newLat
                        longitude = newLng
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("TAG", "Firebase error: ${databaseError.message}")
                }
            }

            myRef.addValueEventListener(listener)

            onDispose {
                myRef.removeEventListener(listener)
            }
        } else {
            onDispose {}
        }
    }

    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedDeviceId.isNotEmpty()) "Device: $selectedDeviceId" else "Location Map",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = true,
                    zoomControlsEnabled = true,
                    compassEnabled = true
                )
            ) {
                Marker(
                    state = markerState,
                    title = if (selectedDeviceId.isNotEmpty()) "Device: $selectedDeviceId" else "Current Location",
                    snippet = "Lat: %.4f, Lng: %.4f".format(latitude, longitude)
                )
            }

            // Overlay card displaying coordinates
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Device Location",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Latitude: $latitude",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Longitude: $longitude",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
