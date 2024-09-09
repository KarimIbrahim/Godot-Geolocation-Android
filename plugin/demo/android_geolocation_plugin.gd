extends Node
class_name AndroidGeolocationPlugin

var _plugin_name = "GeolocationPlugin"
var _android_plugin

signal android_location_permission_updated(granted: bool)
signal android_location_updated(location_dictionary: Dictionary)


func _ready():
	if Engine.has_singleton(_plugin_name):
		_android_plugin = Engine.get_singleton(_plugin_name)
		_android_plugin.connect("locationPermission", self._on_location_permission)
		_android_plugin.connect("locationUpdate", self._on_location_update)

		if !_has_location_permission():
			print('requesting location permission')
			_request_location_permission()
			var granted: bool = await android_location_permission_updated
			print('granted: ', granted)

		_start_geolocation_listener()

	else:
		printerr("Couldn't find plugin " + _plugin_name)


func _ping() -> String:
	if _android_plugin:
		return _android_plugin.ping()
	else:
		return "Couldn't find plugin" + _plugin_name


func _has_location_permission() -> bool:
	if _android_plugin:
		return _android_plugin.hasLocationPermission()
	else:
		return false


func _request_location_permission() -> void:
	if _android_plugin:
		_android_plugin.requestLocationPermission()


func _is_listening_for_geolocation_updates() -> bool:
	if _android_plugin:
		return _android_plugin.isListeningForGeolocationUpdates()
	else:
		return false


func _start_geolocation_listener(minTimeMs: int = 5000, minDistanceM: float = 0.0) -> bool:
	print('starting geolocation listnener with: minTimeMs[', minTimeMs, '], minDistanceM[', minDistanceM, ']')
	if _android_plugin:
		return _android_plugin.startGeolocationListener(minTimeMs, minDistanceM)
	else:
		return false


func _stop_geolocation_listener() -> void:
	if _android_plugin:
		_android_plugin.stopGeolocationListener()


func _on_location_permission(granted: bool) -> void:
	android_location_permission_updated.emit(granted)


func _on_location_update(location_dictionary: Dictionary) -> void:
	android_location_updated.emit(location_dictionary)
