package com.project.homefix.screens.bookservice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.project.homefix.models.Booking
import com.project.homefix.models.BookingStore
import com.project.homefix.navigation.ROUTE_BOOKINGS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookServiceScreen(navController: NavHostController) {
    val selectedService = BookingStore.selectedService
    var dates by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "BOOK SERVICE",
                        fontSize = 20.sp,
                        color = Color(0xFF7C3AED)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFB385B2)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF49CBB)
                )
            )
        },
        bottomBar = {
            Button(
                enabled = !isSubmitting && selectedService != null,
                onClick = {
                    val user = FirebaseAuth.getInstance().currentUser
                    val service = selectedService
                    if (user == null || service == null) {
                        Toast.makeText(context, "Sign in and select a service to book", Toast.LENGTH_LONG).show()
                    } else {
                        isSubmitting = true
                        val bookingRef = FirebaseDatabase.getInstance()
                            .getReference("bookings")
                            .push()
                        val bookingId = bookingRef.key
                        if (bookingId == null) {
                            isSubmitting = false
                            Toast.makeText(context, "Could not create the booking", Toast.LENGTH_LONG).show()
                        } else {
                            val fallbackName = user.displayName?.takeIf { it.isNotBlank() }
                                ?: user.email.orEmpty()
                            val saveBooking: (String) -> Unit = { customerName ->
                                val booking = Booking(
                                    id = bookingId,
                                    date = dates,
                                    time = time,
                                    location = location,
                                    details = details,
                                    serviceName = service.name,
                                    serviceId = service.id,
                                    providerId = service.providerId,
                                    customerId = user.uid,
                                    customerName = customerName.ifBlank { fallbackName },
                                    status = "pending",
                                    createdAt = System.currentTimeMillis()
                                )
                                bookingRef.setValue(booking)
                                    .addOnSuccessListener {
                                        isSubmitting = false
                                        BookingStore.selectedService = null
                                        navController.navigate(ROUTE_BOOKINGS)
                                    }
                                    .addOnFailureListener { error ->
                                        isSubmitting = false
                                        Toast.makeText(
                                            context,
                                            error.localizedMessage ?: "Could not save booking",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                            FirebaseDatabase.getInstance().getReference("Users")
                                .child(user.uid)
                                .child("fullname")
                                .get()
                                .addOnSuccessListener { snapshot ->
                                    saveBooking(snapshot.getValue(String::class.java).orEmpty())
                                }
                                .addOnFailureListener { saveBooking(fallbackName) }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB385B2),
                    contentColor = Color.White
                )
            ){
           Text(text = "Confirm Booking",
                fontSize = 18.sp)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            selectedService?.let { service ->
                Text(
                    text = "${service.name} · KSh ${service.price}",
                    color = Color(0xFF5B21B6),
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                text = "Date",
                color = Color(0xFF7C3AED),
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(5.dp))
            OutlinedTextField(
                value = dates,
                onValueChange = {dates=it},
                label = {Text("Select Date")},
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "date icon"
                    )
                }
            )
            Spacer(modifier = Modifier.height(10.dp))


            Text(
                text = "Time",
                color = Color(0xFF7C3AED),
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(5.dp))
            OutlinedTextField(
                value = time,
                onValueChange = {time=it},
                label = {Text("Select time")},
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "time icon"
                    )
                }
            )
            Spacer(modifier = Modifier.height(10.dp))


            Text(
                text = "Location",
                color = Color(0xFF7C3AED),
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(5.dp))
            OutlinedTextField(
                value = location,
                onValueChange = {location=it},
                label = {Text("Enter your location")},
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "LOCATION icon"
                    )
                }
            )
            Spacer(modifier = Modifier.height(10.dp))


            Text(
                text = "Additional details (optional)",
                color = Color(0xFF7C3AED),
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(5.dp))
            OutlinedTextField(
                value = details,
                onValueChange = {details=it},
                label = {Text("Tell us more")},
                modifier = Modifier.fillMaxWidth()
            )

        }
        }
    }




@Preview(showBackground = true)
@Composable
fun BookServiceScreenPreview(){
    BookServiceScreen(rememberNavController())
}