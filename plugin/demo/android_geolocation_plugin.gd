extends Node

# A wrapper/helper class for the  Android Geolocation Plugin.
# The wrapper will check for permission requests on _ready() and will ask for location permissions 
# if they are not granted.
# The wrapper will then start the geolocation listener if the user grants location permissions.
#
# The wrapper exposes 2 signals that will help you work with the plugin:
# 1. android_location_permission_updated: Emitted when the user accepts/rejects the location permission request.
# 2. android_location_updated: Emitted periodically with the updated geolocation.
class_name AndroidGeolocationPlugin

var _plugin_name = "GeolocationPlugin"
var _android_plugin

# Emitted when the user accepts/rejects the location permission request.
signal android_location_permission_updated(granted: bool)

# Emitted periodically with the updated geolocation.
# The location_dictionary will contain either:
# 1. 2 keys: "latitude" and "longitude". Both keys have float values.
# 2. No keys: Failed to retrieve the location.
signal android_location_updated(location_dictionary: Dictionary)

func _ready():
	if Engine.has_singleton(_plugin_name):
		_android_plugin = Engine.get_singleton(_plugin_name)
		_android_plugin.connect("locationPermission", self.on_location_permission)
		_android_plugin.connect("locationUpdate", self.on_location_update)

		if !_has_location_permission():
			_request_location_permission()
			var granted: bool = await android_location_permission_updated
			if !granted:
				print("User didn't grant location permissions!")
				return

		_start_geolocation_listener()

	else:
		printerr("Couldn't find plugin " + _plugin_name)


# Pings the plugin and returns its name and version.
func _ping() -> String:
	if _android_plugin:
		return _android_plugin.ping()
	else:
		return "Couldn't find plugin" + _plugin_name


# Returns true if location permissions are granted.
# Returns false otherwise.
func _has_location_permission() -> bool:
	if _android_plugin:
		return _android_plugin.hasLocationPermission()
	else:
		return false


# Starts the location permission request.
# The result of the request will be published asynchronously on the android_location_permission_updated signal. 
func _request_location_permission() -> void:
	if _android_plugin:
		_android_plugin.requestLocationPermission()


# Returns true if the geolocation listener is running.
func _is_listening_for_geolocation_updates() -> bool:
	if _android_plugin:
		return _android_plugin.isListeningForGeolocationUpdates()
	else:
		return false


# Starts the geolocation listener if it is not running.
# Returns true if the listener is running successfully.
# Returns false if the listener failed to start.
func _start_geolocation_listener(minTimeMs: int = 5000, minDistanceM: float = 0.0) -> bool:
	if _android_plugin:
		return _android_plugin.startGeolocationListener(minTimeMs, minDistanceM)
	else:
		return false


# Stops the geolocation listener.
func _stop_geolocation_listener() -> void:
	if _android_plugin:
		_android_plugin.stopGeolocationListener()


func on_location_permission(granted: bool) -> void:
	android_location_permission_updated.emit(granted)


func on_location_update(location_dictionary: Dictionary) -> void:
	android_location_updated.emit(location_dictionary)
