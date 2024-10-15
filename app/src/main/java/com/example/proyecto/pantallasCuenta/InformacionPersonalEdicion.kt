package com.example.proyecto.pantallasCuenta

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.proyecto.R
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class InformacionPersonalEdicion : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 100
    private val GALLERY_REQUEST_CODE = 101
    private val CAMERA_PERMISSION_CODE = 102

    private lateinit var fotoPerfil: ImageView
    private var selectedImageUri: Uri? = null
    private var imageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informacion_personal_edicion)

        fotoPerfil = findViewById(R.id.fotoPerfil)
        val btnCamera: ImageButton = findViewById(R.id.imageButton2)
        val btnGallery: ImageButton = findViewById(R.id.imageButton)
        val celularEditText = findViewById<EditText>(R.id.celular)
        val masaEditText = findViewById<EditText>(R.id.masa)
        val estaturaEditText = findViewById<EditText>(R.id.Estatura)
        val guardarButton = findViewById<Button>(R.id.button)

        // Cargar la imagen guardada al iniciar la actividad
        cargarImagenGuardada()

        btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                // Solicitar permiso para la cámara
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            }
        }

        btnGallery.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
        }

        guardarButton.setOnClickListener {
            val celular = celularEditText.text.toString()
            val masa = masaEditText.text.toString()
            val estatura = estaturaEditText.text.toString()

            // Crear objeto JSON
            val datosJSON = JSONObject()
            datosJSON.put("celular", celular)
            datosJSON.put("masa", masa)
            datosJSON.put("estatura", estatura)

            // Guardar en archivo .json
            guardarEnJSON(datosJSON)

            // Guardar la imagen seleccionada en almacenamiento interno
            imageBitmap?.let {
                guardarImagenEnDispositivo(it)
            }

            // Devolver el resultado a PerfilFragment
            val resultIntent = Intent()
            imageBitmap?.let {
                resultIntent.putExtra("imageBitmap", it)
            }
            selectedImageUri?.let {
                resultIntent.putExtra("imageUri", it.toString())
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    imageBitmap = data?.extras?.get("data") as? Bitmap
                    imageBitmap?.let {
                        fotoPerfil.setImageBitmap(it) // Muestra la imagen en el ImageView
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    selectedImageUri = data?.data
                    fotoPerfil.setImageURI(selectedImageUri) // Muestra la imagen seleccionada de la galería
                }
            }
        }
    }

    private fun guardarEnJSON(datos: JSONObject) {
        try {
            val fileName = "datos_personales.json"
            val file = File(filesDir, fileName)

            FileOutputStream(file).use { fos ->
                fos.write(datos.toString().toByteArray())
            }

            Toast.makeText(this, "Datos guardados exitosamente", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Guardar la imagen en el almacenamiento interno
    private fun guardarImagenEnDispositivo(bitmap: Bitmap) {
        try {
            val file = File(filesDir, "perfil_imagen.png")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()

            Toast.makeText(this, "Imagen guardada exitosamente", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error guardando la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    // Cargar la imagen guardada en el almacenamiento interno
    private fun cargarImagenGuardada() {
        val imageFile = File(filesDir, "perfil_imagen.png")
        if (imageFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            fotoPerfil.setImageBitmap(bitmap)
        }
    }
}
