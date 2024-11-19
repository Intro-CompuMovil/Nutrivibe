package com.example.proyecto

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class UbicacionAlimentoActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var userMarker: Marker
    private var lastKnownLocation: GeoPoint? = null
    private var followUserLocation = true
    private var currentRoute: Polyline? = null
    private var supermarkets: List<Supermarket> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_ubicacion_alimento)

        // Retrieve the supermarkets from the Intent
        supermarkets = intent.getSerializableExtra("supermarkets") as? List<Supermarket> ?: emptyList()
        Log.d("UbicacionAlimentoActivity", "Received supermarkets: $supermarkets")

        // If no supermarkets are provided, log an error and show a message
        if (supermarkets.isEmpty()) {
            Log.e("UbicacionAlimentoActivity", "Supermarkets list is empty or null.")
            Toast.makeText(this, "No hay ubicaciones disponibles para este alimento.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize the map
        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        requestGpsPermission()

        mapView.setOnTouchListener { _, _ ->
            followUserLocation = false
            false
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        if (::locationOverlay.isInitialized) {
            locationOverlay.enableMyLocation()
            lastKnownLocation?.let { location ->
                mapView.controller.setCenter(location)
                updateUserMarker(location)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        if (::locationOverlay.isInitialized) {
            locationOverlay.disableMyLocation()
        }
    }

    private fun requestGpsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            initializeMap()
        }
    }

    private fun initializeMap() {
        showUserLocation()
        addSupermarketMarkers()
    }

    private fun showUserLocation() {
        val userIcon = ContextCompat.getDrawable(this, R.mipmap.icon_user_map)
        val userBitmap = drawableToBitmap(userIcon)

        userMarker = Marker(mapView)
        userMarker.icon = BitmapDrawable(resources, userBitmap)
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        locationOverlay = object : MyLocationNewOverlay(GpsMyLocationProvider(this), mapView) {
            override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
                super.onLocationChanged(location, source)
                location?.let {
                    val userLocation = GeoPoint(it.latitude, it.longitude)
                    lastKnownLocation = userLocation
                    runOnUiThread {
                        updateUserMarker(userLocation)
                        if (followUserLocation) {
                            mapView.controller.setZoom(15.0)
                            mapView.controller.animateTo(userLocation)
                        }
                    }
                }
            }
        }

        locationOverlay.enableMyLocation()
        mapView.overlays.add(locationOverlay)

        lastKnownLocation?.let { location ->
            mapView.controller.setCenter(location)
            updateUserMarker(location)
        }

        locationOverlay.runOnFirstFix {
            val myLocation = locationOverlay.myLocation
            if (myLocation != null) {
                runOnUiThread {
                    mapView.controller.setZoom(15.0)
                    mapView.controller.animateTo(myLocation)
                    updateUserMarker(myLocation)
                }
            }
        }
    }

    private fun updateUserMarker(location: GeoPoint) {
        userMarker.position = location
        if (!mapView.overlays.contains(userMarker)) {
            mapView.overlays.add(userMarker)
        }
        mapView.invalidate()
    }

    private fun addSupermarketMarkers() {
        Log.d("UbicacionAlimentoActivity", "Adding supermarket markers: $supermarkets")

        if (supermarkets.isEmpty()) {
            Log.e("UbicacionAlimentoActivity", "Supermarket list is empty. No markers will be added.")
            return
        }

        for (supermarket in supermarkets) {
            val marker = Marker(mapView)
            marker.position = GeoPoint(supermarket.lat, supermarket.lon)
            marker.title = supermarket.name

            val drawableMarker = ContextCompat.getDrawable(this, R.mipmap.icon_marker)
            marker.icon = drawableMarker

            marker.setOnMarkerClickListener { _, _ ->
                createRoute(supermarket)
                true
            }

            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    private fun createRoute(supermarket: Supermarket) {
        currentRoute?.let {
            mapView.overlays.remove(it)
        }

        lastKnownLocation?.let { userLocation ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val roadManager = OSRMRoadManager(this@UbicacionAlimentoActivity, "AndroidMapApp")
                    val waypoints = arrayListOf(userLocation, GeoPoint(supermarket.lat, supermarket.lon))
                    val road = roadManager.getRoad(waypoints)

                    withContext(Dispatchers.Main) {
                        val roadOverlay = RoadManager.buildRoadOverlay(road)
                        roadOverlay.outlinePaint.color = ContextCompat.getColor(this@UbicacionAlimentoActivity, R.color.red)
                        roadOverlay.outlinePaint.strokeWidth = 10f

                        currentRoute = roadOverlay
                        mapView.overlays.add(roadOverlay)

                        val boundingBox = roadOverlay.bounds
                        mapView.zoomToBoundingBox(boundingBox, true)

                        val distanceKm = road.mLength
                        Toast.makeText(
                            this@UbicacionAlimentoActivity,
                            "Distancia a ${supermarket.name}: %.1f km".format(distanceKm),
                            Toast.LENGTH_LONG
                        ).show()

                        mapView.invalidate()
                    }
                } catch (e: Exception) {
                    Log.e("UbicacionAlimentoActivity", "Error creando la ruta: ${e.message}")
                }
            }
        }
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeMap()
        } else {
            Log.d("UbicacionAlimentoActivity", "Permiso de GPS denegado por el usuario.")
        }
    }
}
