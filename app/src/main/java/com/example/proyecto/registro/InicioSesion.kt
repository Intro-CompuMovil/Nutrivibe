package com.example.proyecto.registro

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.MainActivity
import com.example.proyecto.R
import com.example.proyecto.UserData
import com.example.proyecto.preguntasRegistro.PreguntaInicial
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class InicioSesion : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var userRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciosesion)

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        // Inicializar ProgressDialog
        setupProgressDialog()

        val emailEditText = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword)
        val loginButton = findViewById<Button>(R.id.button)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateForm(email, password)) {
                // Autenticación en Firebase
                signInUser(email, password)
            }
        }
        val buttonGoogle = findViewById<Button>(R.id.buttonGoogle)
        buttonGoogle.setOnClickListener {
            val intent = Intent(this, PreguntaInicial::class.java)
            startActivity(intent)
        }
    }

    private fun setupProgressDialog() {
        progressDialog = ProgressDialog(this).apply {
            setMessage("Iniciando sesión...")
            setCancelable(false)
        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        var valid = true

        if (TextUtils.isEmpty(email)) {
            showToast("El correo es requerido")
            valid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("El correo no es válido")
            valid = false
        }

        if (TextUtils.isEmpty(password)) {
            showToast("La contraseña es requerida")
            valid = false
        } else if (password.length < 6) {
            showToast("La contraseña debe tener al menos 6 caracteres")
            valid = false
        }

        return valid
    }

    private fun signInUser(email: String, password: String) {
        progressDialog.show()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    updateUI(auth.currentUser)
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    showToast("Error de autenticación.")
                }
            }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            // Actualizar estado en Firebase
            userRef = FirebaseDatabase.getInstance().getReference("users/${currentUser.uid}")
            userRef.child("disponible").setValue(true)
                .addOnSuccessListener {
                    Log.d(TAG, "Estado de disponible actualizado exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al actualizar estado de disponible: ${e.message}")
                }

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        } else {
            showToast("Usuario no encontrado.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "InicioSesion"
    }
}
