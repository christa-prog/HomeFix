

package com.project.homefix.viewmodel

import android.content.Context
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.project.homefix.models.User
import com.project.homefix.navigation.ROUTE_CUSTDASHBOARD
import com.project.homefix.navigation.ROUTE_LOGIN
import com.project.homefix.navigation.ROUTE_REGISTER
import com.project.homefix.navigation.ROUTE_PROVIDERDS

class AuthViewModel(var navController: NavHostController, var context: Context) {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun register(
        fullname: String,
        email: String,
        password: String,
        confirmpass: String,
        role: String
    ) {
        if (fullname.isBlank() || email.isBlank() || password.isBlank() || confirmpass.isBlank()) {
            makeText(context, "Please complete all fields", Toast.LENGTH_LONG).show()
            return
        }
        if (password != confirmpass) {
            makeText(context, "Passwords do not match", Toast.LENGTH_LONG).show()
            return
        }

        mAuth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    makeText(
                        context,
                        task.exception?.localizedMessage ?: "Registration failed",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnCompleteListener
                }

                val userId = mAuth.currentUser?.uid
                if (userId == null) {
                    makeText(context, "Could not retrieve the new account", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                val userData = User(fullname.trim(), email.trim(), "", userId, role)
                FirebaseDatabase.getInstance().getReference("Users").child(userId)
                    .setValue(userData)
                    .addOnCompleteListener { saveTask ->
                        if (saveTask.isSuccessful) {
                            val destination = if (role == "customer") {
                                ROUTE_CUSTDASHBOARD
                            } else {
                                ROUTE_PROVIDERDS
                            }
                            makeText(context, "Account created successfully", Toast.LENGTH_LONG).show()
                            navController.navigate(destination) {
                                popUpTo(ROUTE_REGISTER) { inclusive = true }
                            }
                        } else {
                            makeText(
                                context,
                                saveTask.exception?.localizedMessage ?: "Could not save user profile",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            makeText(context, "Email and password can't be blank", Toast.LENGTH_LONG).show()
            return
        }

        mAuth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    makeText(
                        context,
                        task.exception?.localizedMessage ?: "Sign in failed",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnCompleteListener
                }

                val userId = mAuth.currentUser?.uid
                if (userId == null) {
                    makeText(context, "Could not retrieve the signed-in account", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                FirebaseDatabase.getInstance().getReference("Users").child(userId).child("role")
                    .get()
                    .addOnSuccessListener { roleSnapshot ->
                        val role = roleSnapshot.getValue(String::class.java)
                        val destination = when (role?.lowercase()) {
                            "provider" -> ROUTE_PROVIDERDS
                            "customer", "user" -> ROUTE_CUSTDASHBOARD
                            else -> {
                                makeText(context, "Your account role is missing or invalid", Toast.LENGTH_LONG).show()
                                return@addOnSuccessListener
                            }
                        }

                        navController.navigate(destination) {
                            popUpTo(ROUTE_LOGIN) { inclusive = true }
                        }
                    }
                    .addOnFailureListener { error ->
                        makeText(
                            context,
                            error.localizedMessage ?: "Could not load your account role",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
    }
}