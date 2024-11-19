package com.example.proyecto.centrales

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.proyecto.R
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.config.Configuration
import android.location.Location
import android.widget.EditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import kotlinx.coroutines.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import java.io.InputStreamReader
import com.example.proyecto.Supermarket


class UbicacionFragment : Fragment() {

    private lateinit var locationInput: EditText
    private lateinit var databaseReference: DatabaseReference
    private var alimentos: MutableMap<String, List<Supermarket>> = mutableMapOf()
    private lateinit var mapView: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var userMarker: Marker
    private var lastKnownLocation: GeoPoint? = null
    private var isFirstFix = true
    private var followUserLocation = true
    private var currentRoute: Polyline? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().userAgentValue = requireContext().packageName
        val view = inflater.inflate(R.layout.fragment_ubicacion, container, false)

        // Initialize UI elements
        mapView = view.findViewById(R.id.mapView)
        locationInput = view.findViewById(R.id.locationInput)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        databaseReference = FirebaseDatabase.getInstance().getReference("foods")

        requestGpsPermission()
        loadProductsFromFirebase()

        // Handle product search
        locationInput.setOnEditorActionListener { _, _, _ ->
            val query = locationInput.text.toString().trim()
            if (query.isNotEmpty()) {
                searchProduct(query)
            } else {
                Toast.makeText(requireContext(), "Por favor ingresa un alimento", Toast.LENGTH_SHORT).show()
            }
            true
        }

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
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            initializeMap()
        }
    }

    private fun initializeMap() {
        showUserLocation()
        loadSupermarketsFromJson()
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
    private fun loadSupermarketsFromJson() {
        try {
            val inputStream = requireContext().assets.open("supermarkets.json")
            val reader = InputStreamReader(inputStream)
            val supermarkets: List<Supermarket> = Gson().fromJson(reader, object : TypeToken<List<Supermarket>>() {}.type)
            for (supermarket in supermarkets) {
                addSupermarketMarker(supermarket)
            }
        } catch (e: Exception) {
            Log.e("UbicacionFragment", "Error cargando supermercados: ${e.message}")
        }
    }

    private fun addSupermarketMarker(supermarket: Supermarket) {
        val marker = Marker(mapView)
        marker.position = GeoPoint(supermarket.lat, supermarket.lon)
        marker.title = supermarket.name

        val drawableMarker = ContextCompat.getDrawable(requireContext(), R.mipmap.icon_marker)
        marker.icon = drawableMarker

        // Generar la ruta al hacer clic en el marcador del supermercado
        marker.setOnMarkerClickListener { _, _ ->
            createRoute(supermarket)
            true
        }

        mapView.overlays.add(marker)
    }

    private fun loadProductsFromFirebase() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    alimentos.clear()

                    // Iterate over products
                    for (productSnapshot in snapshot.children) {
                        val productName = productSnapshot.child("name").value as? String ?: continue
                        val supermarketList = mutableListOf<Supermarket>()

                        // Navigate through numeric indices under 'supermarkets'
                        val supermarketsSnapshot = productSnapshot.child("supermarkets")
                        for (supermarketIndex in supermarketsSnapshot.children) {
                            try {
                                val supermarket = supermarketIndex.getValue(Supermarket::class.java)
                                supermarket?.let {
                                    supermarketList.add(it)
                                    Log.d("UbicacionFragment", "Mapped supermarket: ${it.name}")
                                }
                            } catch (e: Exception) {
                                Log.e("UbicacionFragment", "Error mapping supermarket: ${e.message}")
                            }
                        }

                        // Add product and its supermarkets to the map
                        alimentos[productName] = supermarketList
                        Log.d("UbicacionFragment", "Loaded $productName with supermarkets: $supermarketList")
                    }

                    if (alimentos.isEmpty()) {
                        Log.w("UbicacionFragment", "No products with supermarkets found in the database.")
                    }
                } else {
                    Log.e("UbicacionFragment", "No 'foods' node found in the database.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UbicacionFragment", "Error accessing 'foods': ${error.message}")
            }
        })
    }


    private fun createRoute(supermarket: Supermarket) {
        // Eliminar la ruta anterior si existe
        currentRoute?.let {
            mapView.overlays.remove(it)
        }

        lastKnownLocation?.let { userLocation ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Usar OSRMRoadManager para generar la ruta real
                    val roadManager = OSRMRoadManager(requireContext(), "AndroidMapApp")
                    val waypoints = arrayListOf(userLocation, GeoPoint(supermarket.lat, supermarket.lon))
                    val road = roadManager.getRoad(waypoints)

                    withContext(Dispatchers.Main) {
                        // Crear y agregar la nueva ruta
                        val roadOverlay = RoadManager.buildRoadOverlay(road)
                        roadOverlay.outlinePaint.color = Color.RED
                        roadOverlay.outlinePaint.strokeWidth = 10f

                        currentRoute = roadOverlay
                        mapView.overlays.add(roadOverlay)

                        // Alejar la cámara para ver la ruta completa
                        val boundingBox = roadOverlay.bounds
                        mapView.zoomToBoundingBox(boundingBox, true)

                        // Calcular la distancia en kilómetros y mostrarla en un Toast
                        val distanceKm = road.mLength
                        Toast.makeText(
                            requireContext(),
                            "Distancia a ${supermarket.name}: %.1f km".format(distanceKm),
                            Toast.LENGTH_LONG
                        ).show()

                        mapView.invalidate()
                    }
                } catch (e: Exception) {
                    Log.e("UbicacionFragment", "Error creando la ruta: ${e.message}")
                }
            }
        }
    }

    private fun searchProduct(productName: String) {
        val supermarkets = alimentos[productName]
        if (supermarkets != null && supermarkets.isNotEmpty()) {
            mapView.overlays.clear()
            supermarkets.forEach { addSupermarketMarker(it) }
            Toast.makeText(
                requireContext(),
                "El alimento '$productName' está disponible en ${supermarkets.size} supermercados.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                "No se encontró el producto solicitado: $productName",
                Toast.LENGTH_SHORT
            ).show()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeMap()
        } else {
            Log.d("UbicacionFragment", "Permiso de GPS denegado por el usuario.")
        }
    }

}
