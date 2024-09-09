# Godot Geolocation Android Plugin
An Android plugin for Godot to listen for the geolocation updates. The plugin is using the new Godot 4 plugin system. The plugin uses the Android `LocationManager` to eliminate the need for the Google Play Services.
The implementation is kept simple to maintain a small and lightweight footprint. However, it can easily be extended to support other use cases. Contributions are welcome, please feel free to submit feedback, pull requests, or fork the project as needed.

The plugin is generated from the [`Godot Android templates`](https://github.com/m4gr3d/Godot-Android-Plugin-Template) and is inspired by the great work of [`WolfBearGames Plugin`](https://github.com/WolfBearGames/Godot-GeolocationPlugin-Android).

## Contents
* The gradle project for Android plugin: [`plugin`](plugin)
* The pre-built binaries for the plugin to be used as-is in your Godot project: [`plugin/demo/addons/GeolocationPlugin`](plugin/demo/addons/GeolocationPlugin)
* A wrapper/helper class to work with the plugin in GDScript: [`plugin/demo/android_geolocation_plugin.gd`](plugin/demo/android_geolocation_plugin.gd)
* A demo project to test the plugin: [`plugin/demo`](plugin/demo)

## Usage
**Note:** [Android Studio](https://developer.android.com/studio) is the recommended IDE for developing the Godot Android plugins.

### `I don't know what I'm doing` Guide
1. Copy [`plugin/demo/addons/GeolocationPlugin`](plugin/demo/addons/GeolocationPlugin) directory under the `addons` directory in your Godot project
2. Copy [`plugin/demo/android_geolocation_plugin.gd`](plugin/demo/android_geolocation_plugin.gd) to your scripts directory in your Godot project
3. Create a `Node` in your scene and call it `AndroidGeolocationPlugin`
4. Attach the `android_geolocation_plugin.gd` to the `AndroidGeolocationPlugin` node (Alternatively, you can experiment with `Globals`, however, I haven't tested that setup yet)
5. Reference the `AndroidGeolocationPlugin` in your GDScripts either by path, or by exports (e.g. `@export var android_plugin: AndroidGeolocationPlugin`)
6. Create a listener for the geolocation updates:
  ```gdscript
  func _on_location_update(location_dictionary: Dictionary) -> void:
	  var latitude: float = location_dictionary["latitude"]
	  var longitude: float = location_dictionary["longitude"]
	  log_label.text = str('Location Update: Latitude[', latitude, '], Longitude[', longitude, ']')
  ```
7. Connect the location update signal with the listener you just created in the `_ready()` method:
  ```gdscript
  func _ready():
	  android_plugin.android_location_updated.connect(self._on_location_update)
  ```
8. Export your project using the Android template. Don't forget to enable these 2 permissions under `Export window -> Options tab`:
  ```
  Access Coarse Location
  Access Fine Location
  ```
9. Have fun!!


### `Show me the secret sauce` Guide
1. Start from the [`plugin/demo`](plugin/demo) project
2. Open the project in Godot
3. Navigate to [`plugin/demo/main.gd`](plugin/demo/main.gd). This is the entry point which uses the plugin wrapper [`plugin/demo/android_geolocation_plugin.gd`](plugin/demo/android_geolocation_plugin.gd) to communicate with the plugin
4. The plugin wrapper exposes the below methods and signals. This class is what you want to use in your project to call the plugin. Feel free to modify as need to suit your use-case:
  ```gdscript
  # Emitted when the user accepts/rejects the location permission request.
  signal android_location_permission_updated(granted: bool)

  # Emitted periodically with the updated geolocation.
  # The location_dictionary will contain either:
  # 1. 2 keys: "latitude" and "longitude". Both keys have float values.
  # 2. No keys: Failed to retrieve the location.
  signal android_location_updated(location_dictionary: Dictionary)


  # Pings the plugin the returns its name and version.
  func _ping() -> String

  # Returns true if location permissions are granted.
  # Returns false otherwise.
  func _has_location_permission() -> bool

  # Starts the location permission request.
  # The result of the request will be published asynchronously on the android_location_permission_updated signal. 
  func _request_location_permission() -> void

  # Returns true if the geolocation listener is running.
  func _is_listening_for_geolocation_updates() -> bool

  # Starts the geolocation listener if it is not running.
  # Returns true if the listener is running successfully.
  # Returns false if the listener failed to start.
  func _start_geolocation_listener(minTimeMs: int = 5000, minDistanceM: float = 0.0) -> bool

  # Stops the geolocation listener.
  func _stop_geolocation_listener() -> void

  ```
5. The Android plugin code is in [`GodotAndroidPlugin.kt`](plugin/src/main/java/com/karimibrahim/godot/android/geolocation/GodotAndroidPlugin.kt)
6. The plugin uses the `LocationManager.FUSED_PROVIDER`. Location updates won't publish if the device does not have any available or enabled providers
7. The plugin publishes permission and location updates asynchronously through 2 separate signals 
8. All operations are idempotent
9. Min SDK version is 28


### Building the Android plugin
- You don't technically need to build the plugin, unless you need to modify the gradle project. The pre-built binaries (aar) are included under the [`plugin/demo/addons/GeolocationPlugin/bin`](plugin/demo/addons/GeolocationPlugin/bin)
- In a terminal window, navigate to the project's root directory ([`Godot-Geolocation-Android`](Godot-Geolocation-Android)) and run the following command:
```
./gradlew assemble
```
- On successful completion of the build, the output files can be found in [`plugin/demo/addons`](plugin/demo/addons)

### Testing the Android plugin
You can use the included [Godot demo project](plugin/demo/project.godot) to test the built Android plugin

- Open the demo in Godot (4.2 or higher)
- Navigate to `Project` -> `Project Settings...` -> `Plugins`, and ensure the plugin is enabled
- Install the Godot Android build template by clicking on `Project` -> `Install Android Build Template...`
- Open [`plugin/demo/main.gd`](plugin/demo/main.gd) and update the logic as needed 
- Connect an Android device to your machine and run the demo on it

#### Tips
Additional dependencies added to [`plugin/build.gradle.kts`](plugin/build.gradle.kts) should be added to the `_get_android_dependencies`
function in [`plugin/export_scripts_template/export_plugin.gd`](plugin/export_scripts_template/export_plugin.gd).
