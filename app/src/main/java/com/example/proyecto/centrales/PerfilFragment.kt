package com.example.proyecto.centrales

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.proyecto.R
import com.example.proyecto.pantallasCuenta.InformacionInicioSesionEditar
import com.example.proyecto.pantallasCuenta.InformacionPersonalEdicion
import java.io.File

class PerfilFragment : Fragment() {

    private val EDITAR_INFORMACION_PERSONAL_REQUEST_CODE = 1
    private lateinit var imageButton: de.hdodenhof.circleimageview.CircleImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar la vista del fragmento
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Encontrar el CircleImageView después de que la vista ha sido creada
        imageButton = view.findViewById(R.id.fotoPerfil)

        // Cargar la imagen guardada si existe
        cargarImagenGuardada()

        // Configurar el botón de información personal para redirigir a la actividad de edición
        val botonInfoPersonal = view.findViewById<ImageButton>(R.id.botoninfopersonal)
        botonInfoPersonal.setOnClickListener {
            val intent = Intent(requireContext(), InformacionPersonalEdicion::class.java)
            startActivityForResult(intent, EDITAR_INFORMACION_PERSONAL_REQUEST_CODE)
        }

        val botonInfoInicioSesion = view.findViewById<ImageButton>(R.id.botoninfoiniciosesion)
        botonInfoInicioSesion.setOnClickListener {
            val intent = Intent(requireContext(), InformacionInicioSesionEditar::class.java)
            startActivity(intent)
        }
    }

    // Método para cargar la imagen desde el almacenamiento interno
    private fun cargarImagenGuardada() {
        val imageFile = File(requireContext().filesDir, "perfil_imagen.png")
        if (imageFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            imageButton.setImageBitmap(bitmap)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDITAR_INFORMACION_PERSONAL_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let {
                val imageBitmap = it.getParcelableExtra<Bitmap>("imageBitmap")
                val imageUri = it.getStringExtra("imageUri")

                if (imageBitmap != null) {
                    // Actualizar el CircleImageView con la nueva imagen
                    imageButton.setImageBitmap(imageBitmap)
                    guardarImagenInternamente(imageBitmap)
                } else if (imageUri != null) {
                    // Actualizar el CircleImageView con la imagen seleccionada de la galería
                    imageButton.setImageURI(Uri.parse(imageUri))
                }
            }
        }
    }

    // Método para guardar la imagen en almacenamiento interno
    private fun guardarImagenInternamente(bitmap: Bitmap) {
        try {
            val file = File(requireContext().filesDir, "perfil_imagen.png")
            val fos = file.outputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
