package com.example.mishappawarenessapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Name required"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                etEmail.error = "Email required"
                return@setOnClickListener
            }

            if (pass.length < 6) {
                etPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }


            if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user == null) {
                                Toast.makeText(this, "User creation failed", Toast.LENGTH_SHORT).show()
                                return@addOnCompleteListener
                            }

                            val uid = user.uid

                            val userMap = hashMapOf(
                                "uid" to uid,
                                "name" to name,
                                "email" to email
                            )

                            db.collection("users").document(uid).set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Error saving user", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            val errorMsg = when {
                                task.exception?.message?.contains("email address is already in use") == true ->
                                    "Email already registered"
                                task.exception?.message?.contains("password") == true ->
                                    "Password must be at least 6 characters"
                                else ->
                                    task.exception?.message ?: "Registration failed"
                            }

                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()

                        }
                    }
            } else {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
