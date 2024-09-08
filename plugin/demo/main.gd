extends Node2D

var _plugin_name = "GeolocationPlugin"
var _android_plugin

@export var log_label: Label
@export var geolocation_status_label: Label

func _ready():
	if Engine.has_singleton(_plugin_name):
		_android_plugin = Engine.get_singleton(_plugin_name)
		_android_plugin.connect("locationPermission", self._on_location_permission)
		_android_plugin.connect("locationUpdate", self._on_location_update)
	else:
		printerr("Couldn't find plugin " + _plugin_name)

func _process(delta):
	var is_listening = false
	var is_location_enabled = false
	var is_location_provider_enabled = false
	if _android_plugin:
		is_listening = _android_plugin.isListeningForGeolocationUpdates()
		is_location_enabled = _android_plugin.isLocationEnabled()
		is_location_provider_enabled = _android_plugin.isLocationProviderEnabled()

	geolocation_status_label.text = str('Is Listening: ', is_listening, '\nIs Location Enabled: ',
		is_location_enabled, '\nIs Location Provider Enabled: ', is_location_provider_enabled)


func _on_Button_pressed() -> void:
	if _android_plugin:
		var plugin_result = _android_plugin.ping()
		log_label.text = plugin_result


func _on_permission_button_pressed() -> void:
	if _android_plugin:
		_android_plugin.requestLocationPermission()


func _on_has_permission_button_pressed() -> void:
	if _android_plugin:
		var result = _android_plugin.hasLocationPermission()
		log_label.text = str(result)


func _on_start_listening_button_pressed() -> void:
	if _android_plugin:
		var minTimeMs: int = 5000
		var minDistanceM: float = 0.0
		var plugin_result = _android_plugin.startGeolocationListener(minTimeMs, minDistanceM)
		log_label.text = str('Started listinening: ', plugin_result)


func _on_stop_listening_button_pressed() -> void:
	if _android_plugin:
		_android_plugin.stopGeolocationListener()

func _on_location_permission(granted: bool) -> void:
	log_label.text = str('Location permission: ', granted)

func _on_location_update(location_dictionary: Dictionary) -> void:
	log_label.text = str('Location update: ', location_dictionary)
