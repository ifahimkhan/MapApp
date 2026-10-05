package com.fahim.mapapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fahim.mapapp.service.LocationBroadCastReceiver
import com.fahim.mapapp.service.LocationService
import com.fahim.mapapp.ui.theme.MapAppTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : ComponentActivity() {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 123

        @JvmStatic
        fun requestLocationPermission(activity: Activity): Boolean {
            val hasFine = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            return if (!hasFine && !hasCoarse) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
                true
            } else {
                false
            }
        }
    }

    private val tag = MainActivity::class.java.name
    private var isStartButtonEnabledState by mutableStateOf(true)

    private val locationBroadCastReceiver = LocationBroadCastReceiver {
        disableStartButton()
    }

    fun disableStartButton() {
        isStartButtonEnabledState = false
    }

    @SuppressLint("UnsafeImplicitIntentLaunch")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MapAppTheme {
                MainScreen(
                    isStartButtonEnabled = isStartButtonEnabledState,
                    onStartTracking = { deviceId ->
                        LocationService.getInstance(this@MainActivity, deviceId)
                        sendBroadcast(Intent(LocationService.ACTION_LOCATION_UPDATE))
                        disableStartButton()
                        MapsActivity.getInstance(this@MainActivity, deviceId)
                    },
                    onDeviceSelected = { deviceId ->
                        Log.e(tag, "selected: $deviceId")
                        MapsActivity.getInstance(this@MainActivity, deviceId)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(LocationService.ACTION_LOCATION_UPDATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(locationBroadCastReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(locationBroadCastReceiver, filter,Context.RECEIVER_NOT_EXPORTED)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(locationBroadCastReceiver)
        } catch (e: IllegalArgumentException) {
            Log.e(tag, "Receiver not registered: $e")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isStartButtonEnabled: Boolean,
    onStartTracking: (String) -> Unit,
    onDeviceSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var showDeviceIdDialog by remember { mutableStateOf(false) }
    var showDeviceListDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val requiredPermissions = remember {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissions.toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val fineGranted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            Toast.makeText(context, "Permission Granted!", Toast.LENGTH_SHORT).show()
            pendingAction?.invoke()
        } else {
            Toast.makeText(context, "Permission denied by user!", Toast.LENGTH_SHORT).show()
        }
        pendingAction = null
    }

    fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun runWithPermissionCheck(action: () -> Unit) {
        if (hasLocationPermissions()) {
            action()
        } else {
            pendingAction = action
            permissionLauncher.launch(requiredPermissions)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MapApp") },
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
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )

                    Text(
                        text = "Location Tracker",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = if (isStartButtonEnabled)
                            "Track this device's location in real-time or view other devices on Google Maps."
                        else
                            "Tracking is active! This device is transmitting location updates.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Start Tracking Button
                    Button(
                        onClick = {
                            runWithPermissionCheck {
                                showDeviceIdDialog = true
                            }
                        },
                        enabled = isStartButtonEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .alpha(if (isStartButtonEnabled) 1.0f else 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isStartButtonEnabled) "Start Tracking" else "Tracking Active",
                            fontSize = 16.sp
                        )
                    }

                    // Track Location in Map Button
                    OutlinedButton(
                        onClick = {
                            runWithPermissionCheck {
                                showDeviceListDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Track Location in Map!",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    // Device ID Input Dialog
    if (showDeviceIdDialog) {
        DeviceIdDialog(
            onDismiss = { showDeviceIdDialog = false },
            onConfirm = { deviceId ->
                showDeviceIdDialog = false
                onStartTracking(deviceId)
            }
        )
    }

    // Device List Selection Dialog
    if (showDeviceListDialog) {
        DeviceListDialog(
            onDismiss = { showDeviceListDialog = false },
            onDeviceSelected = { selectedDevice ->
                showDeviceListDialog = false
                onDeviceSelected(selectedDevice)
            }
        )
    }
}

@Composable
fun DeviceIdDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var deviceIdText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Device ID") },
        text = {
            Column {
                OutlinedTextField(
                    value = deviceIdText,
                    onValueChange = {
                        deviceIdText = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text("Device ID") },
                    placeholder = { Text("e.g. device_123") },
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text("Device ID cannot be empty")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (deviceIdText.trim().isNotEmpty()) {
                        onConfirm(deviceIdText.trim())
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeviceListDialog(
    onDismiss: () -> Unit,
    onDeviceSelected: (String) -> Unit
) {
    val devices = remember { mutableStateListOf<String>() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference("devices")

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                devices.clear()
                for (childSnapshot in dataSnapshot.children) {
                    val key = childSnapshot.key
                    if (key != null) {
                        devices.add(key)
                    }
                }
                isLoading = false
            }

            override fun onCancelled(databaseError: DatabaseError) {
                errorMessage = databaseError.message
                isLoading = false
            }
        })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select a Device") },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 300.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator()
                    }
                    errorMessage != null -> {
                        Text(
                            text = "Error: $errorMessage",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    devices.isEmpty() -> {
                        Text(
                            text = "No devices currently broadcasting.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(devices) { deviceName ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onDeviceSelected(deviceName) }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = deviceName,
                                        fontSize = 16.sp,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
