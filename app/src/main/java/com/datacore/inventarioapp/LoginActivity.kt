package com.datacore.inventarioapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var btnIngresar: Button
    private lateinit var btnRegistrar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // Si ya hay sesión iniciada, pasa directo al inventario
        if (auth.currentUser != null) {
            irAlInventario()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById<View>(android.R.id.content)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        btnIngresar = findViewById(R.id.btnIngresar)
        btnRegistrar = findViewById(R.id.btnRegistrar)

        btnIngresar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            if (!datosValidos(email, password)) return@setOnClickListener

            cargando(true)
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { irAlInventario() }
                .addOnFailureListener { error ->
                    cargando(false)
                    Toast.makeText(this, "No se pudo iniciar sesión: ${error.message}", Toast.LENGTH_LONG).show()
                }
        }

        btnRegistrar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            if (!datosValidos(email, password)) return@setOnClickListener

            cargando(true)
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { irAlInventario() }
                .addOnFailureListener { error ->
                    cargando(false)
                    Toast.makeText(this, "No se pudo crear la cuenta: ${error.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun datosValidos(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun cargando(enCurso: Boolean) {
        btnIngresar.isEnabled = !enCurso
        btnRegistrar.isEnabled = !enCurso
    }

    private fun irAlInventario() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}