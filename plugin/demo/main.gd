extends Node2D

@export var log_label: Label
@export var geolocation_status_label: Label
@export var android_plugin: AndroidGeolocationPlugin

func _ready():
	android_plugin.android_location_permission_updated.connect(self._on_location_permission)
	android_plugin.android_location_updated.connect(self._on_location_update)

func _process(delta):
	var is_listening = false
	var is_location_enabled = false
	var is_location_provider_enabled = false
	is_listening = android_plugin._is_listening_for_geolocation_updates()

	geolocation_status_label.text = str('Is Listening: ', is_listening, '\nIs Location Enabled: ',
		is_location_enabled, '\nIs Location Provider Enabled: ', is_location_provider_enabled)


func _on_Button_pressed() -> void:
	log_label.text = android_plugin._ping()


func _on_permission_button_pressed() -> void:
	android_plugin._request_location_permission()


func _on_has_permission_button_pressed() -> void:
	log_label.text = str(android_plugin._has_location_permission())


func _on_start_listening_button_pressed() -> void:
		var minTimeMs: int = 5000
		var minDistanceM: float = 0.0
		var plugin_result = android_plugin._start_geolocation_listener(minTimeMs, minDistanceM)
		log_label.text = str('Started listinening: ', plugin_result)


func _on_stop_listening_button_pressed() -> void:
	android_plugin._stop_geolocation_listener()

func _on_location_permission(granted: bool) -> void:
	log_label.text = str('Location permission: ', granted)

func _on_location_update(location_dictionary: Dictionary) -> void:
	log_label.text = str('Location update: ', location_dictionary)
