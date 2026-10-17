package com.fahim.mapapp.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Result of a live Firebase subscription. */
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Ready<T>(val data: T) : LoadState<T>
    data class Failed(val message: String) : LoadState<Nothing>
}

/** Reads device nodes under `devices/` in the Realtime Database. */
class DeviceRepository(
    private val root: DatabaseReference = FirebaseDatabase.getInstance().getReference(DEVICES_PATH)
) {

    /** Emits the full device list every time any device node changes. */
    fun observeDevices(): Flow<LoadState<List<Device>>> = observe(root) { snapshot ->
        snapshot.children
            .mapNotNull { child -> child.key?.let { id -> Device(id, child.toLocation()) } }
            .sortedBy { it.id.lowercase() }
    }

    /** Emits one device's location; `null` data means the node has no valid coordinates yet. */
    fun observeLocation(deviceId: String): Flow<LoadState<DeviceLocation?>> {
        require(deviceId.isNotBlank()) { "deviceId must not be blank" }
        return observe(root.child(deviceId)) { it.toLocation() }
    }

    private fun <T> observe(
        ref: DatabaseReference,
        map: (DataSnapshot) -> T
    ): Flow<LoadState<T>> = callbackFlow {
        trySend(LoadState.Loading)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(LoadState.Ready(map(snapshot)))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(LoadState.Failed(error.message))
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    private fun DataSnapshot.toLocation(): DeviceLocation? =
        parseLocation(child(LATITUDE).value, child(LONGITUDE).value)

    companion object {
        private const val DEVICES_PATH = "devices"
        private const val LATITUDE = "latitude"
        private const val LONGITUDE = "longitude"
    }
}
