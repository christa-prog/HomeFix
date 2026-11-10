package com.project.homefix.screens.providerDS

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.project.homefix.models.Booking
import com.project.homefix.models.BookingStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderBookingSummaryScreen(navController: NavHostController) {
    val providerId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    ProviderBookingSummaryContent(
        navController = navController,
        providerId = providerId,
        bookings = rememberProviderBookings(providerId)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderBookingSummaryContent(
    navController: NavHostController,
    providerId: String,
    bookings: List<Booking>
) {
    val pagerState = rememberPagerState(
        initialPage = BookingStore.providerSummaryPage.coerceIn(0, 1),
        pageCount = { 2 }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking summaries", color = Color(0xFF7C3AED)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFFB385B2))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF49CBB)
                )
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) { page ->
            val isPending = page == 0
            val title = if (isPending) "Pending services" else "Completed services"
            val targetStatus = if (isPending) "pending" else "completed"
            val filteredBookings = bookings.filter {
                if (isPending) {
                    it.status.equals("pending", ignoreCase = true) ||
                        it.status.equals("accepted", ignoreCase = true)
                } else {
                    it.status.equals(targetStatus, ignoreCase = true)
                }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                Text("${filteredBookings.size} booking(s)", color = Color.Gray)
                if (filteredBookings.isEmpty()) {
                    Text("No ${targetStatus} bookings yet.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filteredBookings, key = { it.id }) { booking ->
                            SummaryBookingCard(
                                booking = booking,
                                showActions = isPending,
                                onAccept = { respondToBooking(booking, providerId, "accepted") },
                                onReject = { respondToBooking(booking, providerId, "rejected") },
                                onDone = { markBookingCompleted(booking, providerId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryBookingCard(
    booking: Booking,
    showActions: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onDone: () -> Unit
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
            if (showActions) {
                when (booking.status.lowercase()) {
                    "pending" -> androidx.compose.foundation.layout.Row(
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
                        onClick = onDone,
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
}

@Preview(showBackground = true)
@Composable
private fun ProviderBookingSummaryPreview() {
    ProviderBookingSummaryContent(
        navController = rememberNavController(),
        providerId = "preview-provider",
        bookings = listOf(
            Booking(
                id = "preview-pending",
                serviceName = "Plumbing repair",
                customerName = "Alex Mwangi",
                date = "2026-11-05",
                time = "10:00 AM",
                location = "Nairobi",
                status = "pending"
            )
        )
    )
}
