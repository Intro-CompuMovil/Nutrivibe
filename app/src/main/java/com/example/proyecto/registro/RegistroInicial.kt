package com.example.proyecto.registro

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.R
import com.example.proyecto.preguntasRegistro.Pregunta1
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegistroInicial : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")
    private lateinit var progressDialog: ProgressDialog

    // UI Elements
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var birthDateEditText: EditText
    private lateinit var phoneEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_inicial)

        // Inicializa FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Inicializa UI elements
        initializeUIElements()
        setupProgressDialog()

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            if (validarCampos() && isInternetAvailable()) {
                registrarUsuario()
            }
        }
    }

    private fun initializeUIElements() {
        emailEditText = findViewById(R.id.mail)
        passwordEditText = findViewById(R.id.contraseña)
        nameEditText = findViewById(R.id.nombre)
        birthDateEditText = findViewById(R.id.fechanacimiento)
        phoneEditText = findViewById(R.id.celular)
    }

    private fun setupProgressDialog() {
        progressDialog = ProgressDialog(this).apply {
            setMessage("Procesando registro...")
            setCancelable(false)
        }
    }

    private fun validarCampos(): Boolean {
        var isValid = true

        if (nameEditText.text.toString().isEmpty()) {
            nameEditText.error = "Campo requerido"
            isValid = false
        }
        if (!isValidEmail(emailEditText.text.toString())) {
            emailEditText.error = "Email inválido"
            isValid = false
        }
        if (passwordEditText.text.toString().length < 6) {
            passwordEditText.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        }
        if (birthDateEditText.text.toString().isEmpty()) {
            birthDateEditText.error = "Campo requerido"
            isValid = false
        }
        if (phoneEditText.text.toString().isEmpty()) {
            phoneEditText.error = "Campo requerido"
            isValid = false
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            return true
        }
        Toast.makeText(this, "No hay conexión a internet", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun registrarUsuario() {
        progressDialog.show()

        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val name = nameEditText.text.toString().trim()
        val birthDate = birthDateEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        guardarDatosUsuario(userId, name, email, birthDate, phone)
                    }
                } else {
                    progressDialog.dismiss()
                    val errorMessage = when (task.exception?.message) {
                        "The email address is already in use by another account." ->
                            "Este correo ya está registrado"
                        "The email address is badly formatted." ->
                            "Formato de correo inválido"
                        else -> "Error en el registro: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun guardarDatosUsuario(userId: String, name: String, email: String, birthDate: String, phone: String) {
        val datosIniciales = mapOf(
            "name" to name,
            "email" to email,
            "birthDate" to birthDate,
            "phone" to phone,
            "disponible" to false
        )

        usersRef.child(userId).setValue(datosIniciales)
            .addOnCompleteListener { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registro inicial exitoso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Pregunta1::class.java).apply {
                        putExtra("userId", userId)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Error al guardar datos: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    companion object {
        const val PATH_USERS = "users"
    }
}