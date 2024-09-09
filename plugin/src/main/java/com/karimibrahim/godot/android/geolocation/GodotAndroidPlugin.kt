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

    private fun logInfo(output: String) = Log.i(pluginName, output)

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

    @UsedByGodot
    fun isLocationEnabled() = locationManager.isLocationEnabled

    @UsedByGodot
    fun isLocationProviderEnabled() =
        locationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER)

    @UsedByGodot
    fun hasLocationPermission() = ActivityCompat.checkSelfPermission(
        activity!!.applicationContext,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
        activity!!.applicationContext,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    @UsedByGodot
    fun requestLocationPermission() = ActivityCompat.requestPermissions(
        activity!!,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        LOCATION_PERMISSION_REQUEST_CODE
    )

    @UsedByGodot
    fun isListeningForGeolocationUpdates() = isListeningForGeolocationUpdates

    @UsedByGodot
    @SuppressLint("MissingPermission")
    fun startGeolocationListener(minTimeMs: Long, minDistanceM: Float): Boolean {
        if(!hasLocationPermission()) {
            return false
        }

        if (!isListeningForGeolocationUpdates) {
            isListeningForGeolocationUpdates = true
            locationManager.requestLocationUpdates(
                LocationManager.FUSED_PROVIDER,
                minTimeMs,
                minDistanceM,
                locationListener,
                Looper.getMainLooper()
            )
        }

        return isListeningForGeolocationUpdates
    }

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
