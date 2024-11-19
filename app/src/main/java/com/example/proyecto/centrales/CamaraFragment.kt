package com.example.proyecto.centrales

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.proyecto.DetallesCamara
import com.example.proyecto.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CamaraFragment : Fragment() {

    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val CAMERA_REQUEST_CODE = 1

    private lateinit var overlay: ConstraintLayout
    private lateinit var buttonCamera: Button
    private lateinit var buttonGallery: Button
    private lateinit var overlayContinueButton: Button

    private var currentPhotoUri: Uri? = null

    // Registrar el contrato para abrir la galería con el image picker avanzado
    private val galleryPicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                Log.d("CamaraFragment", "Imagen seleccionada de la galería: $it")
                openDetallesCamara(it) // Enviar la URI seleccionada a la actividad de detalles
            } ?: run {
                Log.e("CamaraFragment", "Error: URI nulo al seleccionar imagen de galería")
                Toast.makeText(requireContext(), "No se pudo seleccionar la imagen", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camara, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar vistas
        buttonCamera = view.findViewById(R.id.buttonCamara)
        buttonGallery = view.findViewById(R.id.buttonGaleria)
        overlay = view.findViewById(R.id.cameraOverlay)
        overlayContinueButton = view.findViewById(R.id.overlayContinueButton)

        // Ocultar el BottomNavigationView al mostrar el overlay
        val bottomNavigation = requireActivity().findViewById<View>(R.id.bottom_navigation)

        // Configurar el botón de cámara
        buttonCamera.setOnClickListener {
            bottomNavigation.visibility = View.GONE
            showOverlay()
        }

        // Configurar el botón de galería
        buttonGallery.setOnClickListener {
            openGallery()
        }

        // Configurar el botón continuar en el overlay
        overlayContinueButton.setOnClickListener {
            bottomNavigation.visibility = View.VISIBLE
            hideOverlay()
            openCameraWithPermission()
        }
    }

    private fun showOverlay() {
        Log.d("CamaraFragment", "Mostrando overlay")
        overlay.visibility = View.VISIBLE
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 500
        overlay.startAnimation(fadeIn)
    }

    private fun hideOverlay() {
        Log.d("CamaraFragment", "Ocultando overlay")
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.duration = 500
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                overlay.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        overlay.startAnimation(fadeOut)
    }

    private fun openCameraWithPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            Log.d("CamaraFragment", "Solicitando permiso de cámara")
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun openCamera() {
        try {
            val storageDir = requireContext().externalCacheDir
            if (storageDir == null) {
                Log.e("CamaraFragment", "No se encontró el directorio de caché")
                Toast.makeText(requireContext(), "Error al acceder al almacenamiento", Toast.LENGTH_SHORT).show()
                return
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFile = File.createTempFile("IMG_$timeStamp", ".jpg", storageDir)

            currentPhotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                imageFile
            )

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
            }

            Log.d("CamaraFragment", "Iniciando cámara con URI: $currentPhotoUri")

            if (cameraIntent.resolveActivity(requireContext().packageManager) != null) {
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
            } else {
                Log.e("CamaraFragment", "No se encontró aplicación de cámara disponible")
                Toast.makeText(requireContext(), "No se encontró aplicación de cámara", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("CamaraFragment", "Error al iniciar la cámara", e)
            Toast.makeText(requireContext(), "Error al abrir la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        Log.d("CamaraFragment", "Abriendo galería")
        galleryPicker.launch("image/*")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            currentPhotoUri?.let {
                Log.d("CamaraFragment", "Imagen capturada con URI: $it")
                openDetallesCamara(it)
            } ?: run {
                Log.e("CamaraFragment", "Error: URI nulo después de capturar imagen")
                Toast.makeText(requireContext(), "Error al capturar la imagen", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("CamaraFragment", "Error al capturar imagen o usuario canceló la acción")
        }
    }

    private fun openDetallesCamara(imageUri: Uri) {
        Log.d("CamaraFragment", "Abriendo DetallesCamara con URI: $imageUri")
        val intent = Intent(requireContext(), DetallesCamara::class.java)
        intent.putExtra("imageUri", imageUri.toString())
        startActivity(intent)
    }
}
