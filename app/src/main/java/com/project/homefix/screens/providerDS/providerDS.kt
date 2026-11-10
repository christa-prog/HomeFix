package com.project.homefix.screens.providerDS

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.homefix.models.Booking
import com.project.homefix.models.BookingStore
import com.project.homefix.models.Service
import com.project.homefix.navigation.ROUTE_LOGIN
import com.project.homefix.navigation.ROUTE_SPLASH
import com.project.homefix.navigation.ROUTE_MYSERVICES
import com.project.homefix.navigation.ROUTE_PROVIDERPROFILE
import com.project.homefix.navigation.ROUTE_PROVIDER_BOOKING_SUMMARY
import com.project.homefix.screens.myservices.ProviderServiceCard
import com.project.homefix.screens.myservices.rememberProviderServices

private enum class BookingSortOrder(val label: String) {
    NEWEST_FIRST("Newest to oldest"),
    OLDEST_FIRST("Oldest to newest")
}

@Composable
fun rememberProviderBookings(providerId: String): List<Booking> {
    val bookings = remember(providerId) { mutableStateListOf<Booking>() }

    DisposableEffect(providerId) {
        if (providerId.isBlank()) {
            bookings.clear()
            onDispose { }
        } else {
            val query = FirebaseDatabase.getInstance().getReference("bookings")
                .orderByChild("providerId")
                .equalTo(providerId)
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

fun respondToBooking(booking: Booking, providerId: String, status: String) {
    if (booking.id.isBlank() || providerId.isBlank()) return
    if (status !in setOf("accepted", "rejected", "completed")) return

    val bookingRef = FirebaseDatabase.getInstance().getReference("bookings").child(booking.id)
    bookingRef.get().addOnSuccessListener { snapshot ->
        val belongsToProvider = snapshot.child("providerId").getValue(String::class.java) == providerId
        val currentStatus = snapshot.child("status").getValue(String::class.java).orEmpty()
        val canUpdate = when (status) {
            "accepted", "rejected" -> currentStatus.equals("pending", ignoreCase = true)
            "completed" -> currentStatus.equals("accepted", ignoreCase = true)
            else -> false
        }
        if (belongsToProvider && canUpdate) {
            bookingRef.child("status").setValue(status)
        }
    }
}

fun markBookingCompleted(booking: Booking, providerId: String) {
    respondToBooking(booking, providerId, "completed")
}

@Composable
private fun rememberProviderName(): String {
    var name by remember { mutableStateOf("there") }
    val providerId = FirebaseAuth.getInstance().currentUser?.uid

    DisposableEffect(providerId) {
        if (providerId.isNullOrBlank()) {
            name = "there"
            onDispose { }
        } else {
            val nameRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(providerId)
                .child("fullname")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    name = snapshot.getValue(String::class.java)
                        ?.trim()
                        ?.split(" ")
                        ?.firstOrNull()
                        ?.takeIf { it.isNotBlank() }
                        ?: "there"
                }

                override fun onCancelled(error: DatabaseError) {
                    name = "there"
                }
            }
            nameRef.addValueEventListener(listener)
            onDispose { nameRef.removeEventListener(listener) }
        }
    }
    return name
}

@Composable
fun ProviderDashboardScreen(navController: NavHostController) {
    val services = rememberProviderServices()
    val providerId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val providerBookings = rememberProviderBookings(providerId)
    ProviderDashboardContent(
        navController = navController,
        providerName = rememberProviderName(),
        services = services,
        bookings = providerBookings,
        onUpdateBooking = { booking, status ->
            respondToBooking(booking, providerId, status)
        }
    )
}

@Composable
private fun ProviderDashboardContent(
    navController: NavHostController,
    providerName: String,
    services: List<Service>,
    bookings: List<Booking>,
    onUpdateBooking: (Booking, String) -> Unit
) {
    var sortOrder by remember { mutableStateOf(BookingSortOrder.NEWEST_FIRST) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val pendingBookings = bookings.filter {
        it.status.equals("pending", ignoreCase = true) ||
            it.status.equals("accepted", ignoreCase = true)
    }
    val completedBookings = bookings.filter { it.status.equals("completed", ignoreCase = true) }
    val sortedBookings = when (sortOrder) {
        BookingSortOrder.NEWEST_FIRST -> bookings.sortedByDescending { it.createdAt }
        BookingSortOrder.OLDEST_FIRST -> bookings.sortedBy { it.createdAt }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFF49CBB)) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("HOME") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(ROUTE_MYSERVICES) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "My services") },
                    label = { Text("Services") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(ROUTE_PROVIDERPROFILE) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(
                            onClick = {
                                navController.navigate(ROUTE_LOGIN) {
                                    popUpTo(ROUTE_SPLASH) { inclusive = true }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to login",
                                tint = Color(0xFFB385B2)
                            )
                        }
                    }
                    Text(
                        "Hello, $providerName",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED),
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                    Text(
                        "Here's your business overview",
                        fontSize = 20.sp,
                        color = Color(0xFF7C3AED),
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                    Text("Your services", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7C3AED))
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BookingSummaryCard(
                        title = "Pending services",
                        count = pendingBookings.size,
                        containerColor = Color(0xFFFFF3E0),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            BookingStore.providerSummaryPage = 0
                            navController.navigate(ROUTE_PROVIDER_BOOKING_SUMMARY)
                        }
                    )
                    BookingSummaryCard(
                        title = "Completed services",
                        count = completedBookings.size,
                        containerColor = Color(0xFFE8F5E9),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            BookingStore.providerSummaryPage = 1
                            navController.navigate(ROUTE_PROVIDER_BOOKING_SUMMARY)
                        }
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Incoming bookings", fontSize = 20.sp, fontWeight = FontWeight.SemiBold,color = Color(0xFF7C3AED))
                    TextButton(
                        onClick = { sortMenuExpanded = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB385B2))
                    ) {
                        Text("Sort: ${sortOrder.label}")
                    }
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        BookingSortOrder.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    sortOrder = option
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            if (sortedBookings.isEmpty()) {
                item { Text("No customer bookings yet.", color = Color.Gray) }
            } else {
                items(sortedBookings, key = { it.id }) { booking ->
                    ProviderBookingCard(
                        booking = booking,
                        onAccept = { onUpdateBooking(booking, "accepted") },
                        onReject = { onUpdateBooking(booking, "rejected") },
                        onMarkDone = { onUpdateBooking(booking, "completed") }
                    )
                }
            }
            item {
                Text("Your services", fontSize = 20.sp, fontWeight = FontWeight.SemiBold,color = Color(0xFF7C3AED))
            }
            if (services.isEmpty()) {
                item { Text("You haven't added any services yet.", modifier = Modifier.padding(vertical = 8.dp)) }
            } else {
                items(services, key = { "service-${it.id}" }) { service ->
                    ProviderServiceCard(service)
                }
            }
        }
    }
}

@Composable
private fun BookingSummaryCard(
    title: String,
    count: Int,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, fontWeight = FontWeight.SemiBold, color = Color(0xFF5B21B6))
            Text(count.toString(), fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Tap to view", color = Color.Gray)
        }
    }
}

@Composable
private fun ProviderBookingCard(
    booking: Booking,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onMarkDone: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F3FF))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(booking.serviceName.ifBlank { "Service booking" }, fontWeight = FontWeight.SemiBold)
            Text("Customer: ${booking.customerName.ifBlank { "Customer" }}")
            Text("Date: ${booking.date} · Time: ${booking.time}")
            Text("Location: ${booking.location}")
            if (booking.details.isNotBlank()) Text("Details: ${booking.details}")
            Text("Status: ${booking.status.replaceFirstChar { it.uppercase() }}")
            when (booking.status.lowercase()) {
                "pending" -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB385B2),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Accept")
                    }
                    Button(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB385B2),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Reject")
                    }
                }
                "accepted" -> Button(
                    onClick = onMarkDone,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB385B2),
                        contentColor = Color.White
                    )
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProviderDashboardScreenPreview() {
    ProviderDashboardContent(
        navController = rememberNavController(),
        providerName = "Alex",
        services = emptyList(),
        bookings = listOf(
            Booking(
                id = "preview-booking",
                date = "2026-11-05",
                time = "10:00 AM",
                location = "Nairobi",
                serviceName = "Plumbing repair",
                customerName = "Alex Mwangi",
                createdAt = 1L
            )
        ),
        onUpdateBooking = { _, _ -> }
    )
}
