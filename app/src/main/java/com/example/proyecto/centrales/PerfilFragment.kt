package com.example.proyecto.centrales

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.proyecto.R
import com.example.proyecto.pantallasCuenta.InformacionInicioSesionEditar
import com.example.proyecto.pantallasCuenta.InformacionPersonalEdicion
import com.example.proyecto.registro.InicioSesion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File

class PerfilFragment : Fragment() {

    private val EDITAR_INFORMACION_PERSONAL_REQUEST_CODE = 1
    private lateinit var imageButton: de.hdodenhof.circleimageview.CircleImageView
    private lateinit var nombreTextView: TextView
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nombreTextView = view.findViewById(R.id.textView17)

        // Cargar el nombre desde Firebase
        cargarNombreDesdeFirebase()

        imageButton = view.findViewById(R.id.fotoPerfil)
        cargarImagenGuardada()

        val botonInfoPersonal = view.findViewById<ImageButton>(R.id.botoninfopersonal)
        val botonCerrarSesion = view.findViewById<ImageButton>(R.id.botoncerrarsesion)
        val botonInfoInicioSesion = view.findViewById<ImageButton>(R.id.botoninfoiniciosesion)

        // Configurar clic en el botón de información personal
        botonInfoPersonal.setOnClickListener {
            val intent = Intent(requireContext(), InformacionPersonalEdicion::class.java)
            startActivityForResult(intent, EDITAR_INFORMACION_PERSONAL_REQUEST_CODE)
        }

        // Configurar clic en el botón de cerrar sesión
        botonCerrarSesion.setOnClickListener {
            cerrarSesion()


        }

        // Configurar clic en el botón de información de inicio de sesión
        botonInfoInicioSesion.setOnClickListener {
            val intent = Intent(requireContext(), InformacionInicioSesionEditar::class.java)
            startActivity(intent)
        }
    }

    private fun cerrarSesion() {
        // Cerrar sesión en Firebase
        FirebaseAuth.getInstance().signOut()

        // Limpiar los datos de sesión guardados en SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // Borra todos los datos guardados
        editor.apply()

        // Mostrar un mensaje de sesión cerrada
        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()

        // Redirigir a la actividad de inicio de sesión
        val intent = Intent(requireContext(), InicioSesion::class.java)
        startActivity(intent)

        // Finalizar la actividad actual para que no se pueda volver a ella
        requireActivity().finish()
    }


    private fun cargarNombreDesdeFirebase() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.reference.child("users").child(userId)

            userRef.child("name").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombre = snapshot.value as? String
                    if (nombre != null) {
                        nombreTextView.text = nombre
                        Log.d("PerfilFragment", "Nombre cargado: $nombre")
                    } else {
                        nombreTextView.text = "Nombre no disponible"
                        Log.d("PerfilFragment", "Nombre no encontrado en la base de datos")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("PerfilFragment", "Error al cargar el nombre: ${error.message}")
                    Toast.makeText(context, "Error al cargar el nombre", Toast.LENGTH_SHORT).show()
                    nombreTextView.text = "Error al cargar el nombre"
                }
            })
        } else {
            Log.e("PerfilFragment", "Usuario no autenticado")
            nombreTextView.text = "Usuario no autenticado"
        }
    }

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
                    imageButton.setImageBitmap(imageBitmap)
                    guardarImagenInternamente(imageBitmap)
                } else if (imageUri != null) {
                    imageButton.setImageURI(Uri.parse(imageUri))
                }
            }
        }
    }

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
