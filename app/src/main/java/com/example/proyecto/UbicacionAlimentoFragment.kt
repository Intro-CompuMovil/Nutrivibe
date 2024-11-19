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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import com.example.proyecto.R
import com.example.proyecto.Alimento
import com.example.proyecto.Supermarket

class UbicacionAlimentoFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var userMarker: Marker
    private var lastKnownLocation: GeoPoint? = null
    private var isFirstFix = true
    private var followUserLocation = true
    private var currentRoute: Polyline? = null
    private lateinit var supermarkets: List<Supermarket> // Stores supermarkets from the selected Alimento

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().userAgentValue = requireContext().packageName

        val view = inflater.inflate(R.layout.fragment_ubicacion_alimento, container, false)

        // Retrieve the supermarkets list passed as arguments
        arguments?.let {
            supermarkets = it.getSerializable("supermarkets") as List<Supermarket>
        }

        mapView = view.findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        requestGpsPermission()

        mapView.setOnTouchListener { _, _ ->
            followUserLocation = false
            false
        }

        return view
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
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
        val userIcon = ContextCompat.getDrawable(requireContext(), R.mipmap.icon_user_map)
        val userBitmap = drawableToBitmap(userIcon)

        userMarker = Marker(mapView)
        userMarker.icon = BitmapDrawable(resources, userBitmap)
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        locationOverlay = object : MyLocationNewOverlay(GpsMyLocationProvider(context), mapView) {
            override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
                super.onLocationChanged(location, source)
                location?.let {
                    val userLocation = GeoPoint(it.latitude, it.longitude)
                    lastKnownLocation = userLocation
                    requireActivity().runOnUiThread {
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
                requireActivity().runOnUiThread {
                    mapView.controller.setZoom(15.0)
                    mapView.controller.animateTo(myLocation)
                    updateUserMarker(myLocation)
                    isFirstFix = false
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
        for (supermarket in supermarkets) {
            val marker = Marker(mapView)
            marker.position = GeoPoint(supermarket.lat, supermarket.lon)
            marker.title = supermarket.name

            val drawableMarker = ContextCompat.getDrawable(requireContext(), R.mipmap.icon_marker)
            marker.icon = drawableMarker

            // Generate route on marker click
            marker.setOnMarkerClickListener { _, _ ->
                createRoute(supermarket)
                true
            }

            mapView.overlays.add(marker)
        }
    }

    private fun createRoute(supermarket: Supermarket) {
        currentRoute?.let {
            mapView.overlays.remove(it)
        }

        lastKnownLocation?.let { userLocation ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val roadManager = OSRMRoadManager(requireContext(), "AndroidMapApp")
                    val waypoints = arrayListOf(userLocation, GeoPoint(supermarket.lat, supermarket.lon))
                    val road = roadManager.getRoad(waypoints)

                    withContext(Dispatchers.Main) {
                        val roadOverlay = RoadManager.buildRoadOverlay(road)
                        roadOverlay.outlinePaint.color = ContextCompat.getColor(requireContext(), R.color.red)
                        roadOverlay.outlinePaint.strokeWidth = 10f

                        currentRoute = roadOverlay
                        mapView.overlays.add(roadOverlay)

                        val boundingBox = roadOverlay.bounds
                        mapView.zoomToBoundingBox(boundingBox, true)

                        val distanceKm = road.mLength
                        Toast.makeText(
                            requireContext(),
                            "Distancia a ${supermarket.name}: %.1f km".format(distanceKm),
                            Toast.LENGTH_LONG
                        ).show()

                        mapView.invalidate()
                    }
                } catch (e: Exception) {
                    Log.e("UbicacionAlimentoFragment", "Error creando la ruta: ${e.message}")
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
            Log.d("UbicacionAlimentoFragment", "Permiso de GPS denegado por el usuario.")
        }
    }
}
