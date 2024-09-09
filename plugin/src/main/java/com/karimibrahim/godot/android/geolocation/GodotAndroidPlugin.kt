package com.karimibrahim.godot.android.geolocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import org.godotengine.godot.Dictionary
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot

/**
 * A Godot plugin that listens for Geolocation updates.
 * The plugins uses the [LocationManager.FUSED_PROVIDER] which combines inputs from several
 * other location providers to provide the best possible location fix.
 */
class GodotAndroidPlugin(godot: Godot) : GodotPlugin(godot) {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE: Int = 1
    }

    private val locationPermissionSignal =
        SignalInfo("locationPermission", Boolean::class.javaObjectType)
    private val locationUpdateSignal = SignalInfo("locationUpdate", Dictionary::class.java)

    private val locationManager =
        activity!!.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var isListeningForGeolocationUpdates = false

    private val locationListener = LocationListener { location ->
        logInfo("Received location[$location].")
        val locationDictionary = Dictionary()
        locationDictionary["latitude"] = location.latitude
        locationDictionary["longitude"] = location.longitude
        emitSignal(locationUpdateSignal.name, locationDictionary)
    }

    override fun getPluginName() = BuildConfig.GODOT_PLUGIN_NAME

    override fun getPluginSignals() = setOf(
        locationPermissionSignal,
        locationUpdateSignal
    )

    private fun logInfo(log: String) = Log.i(pluginName, log)
    private fun logError(log: String, ex: Throwable? = null) = Log.e(pluginName, log, ex)

    /*
    A tester method to make sure the plugin is wired correctly and can be called
    from Godot.
     */
    @UsedByGodot
    fun ping(): String {
        val pingString = "${BuildConfig.GODOT_PLUGIN_NAME}-${BuildConfig.GODOT_PLUGIN_VERSION}!"
        logInfo(pingString)
        return pingString
    }

    /**
     * Returns the current state of the location.
     */
    @UsedByGodot
    fun isLocationEnabled() = locationManager.isLocationEnabled

    /**
     * Returns the current status of the [LocationManager.FUSED_PROVIDER].
     */
    @UsedByGodot
    fun isLocationProviderEnabled() =
        locationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER)

    /**
     * Checks if the location permission is granted.
     */
    @UsedByGodot
    fun hasLocationPermission() = ActivityCompat.checkSelfPermission(
        activity!!.applicationContext,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
        activity!!.applicationContext,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    /**
     * Will ask the user for location permission if it is not granted.
     * The method returns immediately and the permission operation executes asynchronously.
     * The result of operations will be published on the [locationPermissionSignal] after the user accepts/rejects the request.
     */
    @UsedByGodot
    fun requestLocationPermission() = ActivityCompat.requestPermissions(
        activity!!,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        LOCATION_PERMISSION_REQUEST_CODE
    )

    /**
     * Returns [true] if the plugin is listening for the geolocation updates.
     * Will return [false] otherwise.
     */
    @UsedByGodot
    fun isListeningForGeolocationUpdates() = isListeningForGeolocationUpdates

    /**
     * Starts the geolocation listener.
     * Will publish the geolocation updates on the [locationUpdateSignal]. The signal will contain
     * a dictionary with 2 keys `latitude` and `longitude`. Both values are of type [Double].
     *
     * The method will return true if the location permission is granted and the listener has started successfully.
     * Calling this method repeatedly will not restart the listener if it is already running.
     *
     * The method will return false if the location permission is not granted, or the listener failed to start.
     */
    @UsedByGodot
    @SuppressLint("MissingPermission")
    fun startGeolocationListener(minTimeMs: Long, minDistanceM: Float): Boolean {
        if (!hasLocationPermission()) {
            return false
        }

        if (!isListeningForGeolocationUpdates) {
            isListeningForGeolocationUpdates = true
            runCatching {
                locationManager.requestLocationUpdates(
                    LocationManager.FUSED_PROVIDER,
                    minTimeMs,
                    minDistanceM,
                    locationListener,
                    Looper.getMainLooper()
                )
            }.getOrElse {
                logError("Failed to start the geolocation listener.", it)
                return false
            }
        }

        return isListeningForGeolocationUpdates
    }

    /**
     * Will stop the geolocation listener if it is running.
     */
    @UsedByGodot
    fun stopGeolocationListener() {
        if (isListeningForGeolocationUpdates) {
            locationManager.removeUpdates(locationListener)
            isListeningForGeolocationUpdates = false
        }
    }

    override fun onMainRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>?,
        grantResults: IntArray?
    ) {
        logInfo("Permissions granted: [${permissions?.joinToString()}], grantResults: [${grantResults?.joinToString()}].")
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults?.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                logInfo("Location permission granted")
                emitSignal(locationPermissionSignal.name, true)
            } else {
                logInfo("Location permission not granted")
                emitSignal(locationPermissionSignal.name, false)
            }
        }
    }

}
