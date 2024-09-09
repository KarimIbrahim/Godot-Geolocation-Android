extends Node2D

# This is the entry point for the test project.
# The file contains several buttons and signals to test the plugin.
# Feel free to change this file as much as you for you own testing.

@export var log_label: Label
@export var geolocation_status_label: Label
@export var android_plugin: AndroidGeolocationPlugin

func _ready():
	android_plugin.android_location_permission_updated.connect(self._on_location_permission)
	android_plugin.android_location_updated.connect(self._on_location_update)

func _process(delta):
	var is_listening = android_plugin._is_listening_for_geolocation_updates()
	geolocation_status_label.text = str('Is Listening: ', is_listening)


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
	var latitude: float = location_dictionary["latitude"]
	var longitude: float = location_dictionary["longitude"]
	log_label.text = str('Location Update: Latitude[', latitude, '], Longitude[', longitude, ']')
