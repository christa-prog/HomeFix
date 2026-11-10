package com.project.homefix.screens.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.project.homefix.models.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.homefix.navigation.ROUTE_BOOKINGS
import com.project.homefix.navigation.ROUTE_CUSTPROFILE
import com.project.homefix.navigation.ROUTE_CUSTDASHBOARD


@Composable
private fun BookingCard(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F3FF))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                booking.serviceName.ifBlank { "Booking" },
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF7C3AED)
            )
            Text("Date: ${booking.date}")
            Text("Time: ${booking.time}")
            Text("Location: ${booking.location}")
            if (booking.details.isNotBlank()) Text("Details: ${booking.details}")
            Text("Status: ${booking.status.replaceFirstChar { it.uppercase() }}")
        }
    }
}

@Composable
private fun rememberCustomerBookings(): List<Booking> {
    val bookings = remember { mutableStateListOf<Booking>() }
    val customerId = FirebaseAuth.getInstance().currentUser?.uid

    DisposableEffect(customerId) {
        if (customerId.isNullOrBlank()) {
            bookings.clear()
            onDispose { }
        } else {
            val query = FirebaseDatabase.getInstance().getReference("bookings")
                .orderByChild("customerId")
                .equalTo(customerId)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    bookings.clear()
                    snapshot.children.mapNotNull { child ->
                        child.getValue(Booking::class.java)?.copy(id = child.key.orEmpty())
                    }.forEach(bookings::add)
                }

                override fun onCancelled(error: DatabaseError) {
                    bookings.clear()
                }
            }
            query.addValueEventListener(listener)
            onDispose { query.removeEventListener(listener) }
        }
    }
    return bookings
}

@Composable
fun BookingsScreen(navController: NavHostController) {
    BookingsContent(navController, rememberCustomerBookings())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingsContent(navController: NavHostController, bookings: List<Booking>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("My Bookings", color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF49CBB)
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFF49CBB)) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(ROUTE_CUSTDASHBOARD) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("HOME") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { navController.navigate(ROUTE_BOOKINGS) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "My bookings") },
                    label = { Text("Bookings") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(ROUTE_CUSTPROFILE) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        if (bookings.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("No bookings yet")
                Text("Your confirmed bookings will appear here.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bookings, key = { it.id }) { booking -> BookingCard(booking) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookingsScreenPreview() {
    BookingsContent(
        navController = rememberNavController(),
        bookings = listOf(
            Booking(
                id = "preview-booking",
                serviceName = "Plumbing repair",
                date = "2026-11-05",
                time = "10:00 AM",
                location = "Nairobi",
                status = "pending"
            )
        )
    )
}
