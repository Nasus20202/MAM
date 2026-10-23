package dev.nasuta.mam.lab4

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MainActivity : AppCompatActivity() {
    private lateinit var locationTextView: TextView
    private lateinit var map: MapView
    private lateinit var marker: Marker

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var mapCentered = false

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions.getOrDefault(ACCESS_FINE_LOCATION, false) -> {
                    Log.d(javaClass.simpleName, "Precise location access granted.")
                    startLocationUpdates()
                }
                permissions.getOrDefault(ACCESS_COARSE_LOCATION, false) -> {
                    Log.d(javaClass.simpleName, "Approximate location access granted.")
                    startLocationUpdates()
                }
                else -> Log.d(javaClass.simpleName, "No location access granted.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        locationTextView = findViewById(R.id.locationTextView)
        locationTextView.text = "Awaiting location"

        map = findViewById(R.id.mapView)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        Configuration.getInstance().userAgentValue = applicationContext.packageName
        map.controller.setZoom(15.5)

        marker = Marker(map)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        map.overlays.add(marker)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = LocationListener { location -> onLocationChanged(location) }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_DENIED
            && ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_DENIED
        ) {
            locationPermissionRequest.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
        } else {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        locationManager.removeUpdates(locationListener)
    }

    private fun startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
        } catch (e: SecurityException) {
            Log.d(javaClass.simpleName, "No location access granted.")
        }
    }

    private fun onLocationChanged(location: Location) {
        locationTextView.text = String.format(
            "Latitude: %.6f\nLongitude: %.6f\nSpeed: %.1f m/s\nBearing: %.0f°",
            location.latitude, location.longitude, location.speed, location.bearing
        )

        val point = GeoPoint(location.latitude, location.longitude)
        if (!mapCentered) {
            map.controller.setCenter(point)
            mapCentered = true
        }
        marker.position = point
        map.invalidate()
    }
}
