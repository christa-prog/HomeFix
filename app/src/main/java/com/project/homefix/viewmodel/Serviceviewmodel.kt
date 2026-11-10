package com.project.homefix.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.project.homefix.models.Service
import com.project.homefix.network.CloudinaryAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ServiceViewModel(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val servicesRef = FirebaseDatabase.getInstance().getReference("services")
    private val cloudinaryApi: CloudinaryAPI = Retrofit.Builder()
        .baseUrl("https://api.cloudinary.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CloudinaryAPI::class.java)

    private val uploadPreset = "products"

    fun addService(
        name: String,
        price: String,
        description: String,
        imageUri: Uri?
    ) {
        val providerId = FirebaseAuth.getInstance().currentUser?.uid
        if (providerId == null) {
            showMessage("Sign in as a service provider to add a service")
            return
        }
        if (name.isBlank() || description.isBlank() || price.toDoubleOrNull() == null) {
            showMessage("Enter a service name, description, and valid price")
            return
        }

        val serviceRef = servicesRef.push()
        val serviceId = serviceRef.key
        if (serviceId == null) {
            showMessage("Could not create service")
            return
        }

        scope.launch {
            try {
                val imageUrl = if (imageUri != null) uploadToCloudinary(imageUri) else ""
                val service = Service(
                    id = serviceId,
                    name = name.trim(),
                    price = price.trim(),
                    description = description.trim(),
                    providerId = providerId,
                    imageUrl = imageUrl
                )
                Tasks.await(serviceRef.setValue(service))
                showMessage("Service added successfully")
            } catch (error: Exception) {
                showMessage("Could not add service: ${error.localizedMessage ?: "unknown error"}")
            }
        }
    }

    fun updateService(
        serviceId: String,
        name: String,
        price: String,
        description: String,
        imageUri: Uri? = null
    ) {
        val providerId = FirebaseAuth.getInstance().currentUser?.uid
        if (providerId == null) {
            showMessage("Sign in as a service provider to update a service")
            return
        }
        if (serviceId.isBlank() || name.isBlank() || description.isBlank() || price.toDoubleOrNull() == null) {
            showMessage("Enter a service name, description, and valid price")
            return
        }

        scope.launch {
            try {
                val serviceRef = servicesRef.child(serviceId)
                val existing = Tasks.await(serviceRef.get())
                if (existing.child("providerId").getValue(String::class.java) != providerId) {
                    showMessage("You can only update your own services")
                    return@launch
                }

                val updates = mutableMapOf<String, Any>(
                    "name" to name.trim(),
                    "price" to price.trim(),
                    "description" to description.trim()
                )
                if (imageUri != null) {
                    updates["imageUrl"] = uploadToCloudinary(imageUri)
                }
                Tasks.await(serviceRef.updateChildren(updates))
                showMessage("Service updated successfully")
            } catch (error: Exception) {
                showMessage("Could not update service: ${error.localizedMessage ?: "unknown error"}")
            }
        }
    }

    fun deleteService(serviceId: String) {
        val providerId = FirebaseAuth.getInstance().currentUser?.uid
        if (providerId == null) {
            showMessage("Sign in as a service provider to delete a service")
            return
        }
        if (serviceId.isBlank()) {
            showMessage("Invalid service")
            return
        }

        scope.launch {
            try {
                val serviceRef = servicesRef.child(serviceId)
                val existing = Tasks.await(serviceRef.get())
                if (existing.child("providerId").getValue(String::class.java) != providerId) {
                    showMessage("You can only delete your own services")
                    return@launch
                }
                Tasks.await(serviceRef.removeValue())
                showMessage("Service deleted successfully")
            } catch (error: Exception) {
                showMessage("Could not delete service: ${error.localizedMessage ?: "unknown error"}")
            }
        }
    }

    private suspend fun uploadToCloudinary(uri: Uri): String {
        val contentResolver = appContext.contentResolver
        val imageBytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Could not read the selected image")
        val mediaType = (contentResolver.getType(uri) ?: "image/jpeg").toMediaTypeOrNull()
        val imageBody = imageBytes.toRequestBody(mediaType)
        val imagePart = MultipartBody.Part.createFormData("file", "service-image", imageBody)
        val presetBody = uploadPreset.toRequestBody("text/plain".toMediaTypeOrNull())

        val response = cloudinaryApi.uploadImage(imagePart, presetBody)

        return response.secure_url
            ?: throw IllegalStateException("Cloudinary did not return an image URL")
    }

    private fun showMessage(message: String) {
        scope.launch(Dispatchers.Main) {
            Toast.makeText(appContext, message, Toast.LENGTH_LONG).show()
        }
    }
}
