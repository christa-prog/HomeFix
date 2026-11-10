package com.project.homefix.screens.custdashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.project.homefix.R
import com.project.homefix.models.BookingStore
import com.project.homefix.models.Service
import com.project.homefix.navigation.ROUTE_BOOKINGS
import com.project.homefix.navigation.ROUTE_SERVICEDETAILS
import com.project.homefix.navigation.ROUTE_CUSTPROFILE
import com.project.homefix.navigation.ROUTE_LOGIN
import com.project.homefix.navigation.ROUTE_SPLASH

@Composable
private fun rememberCustomerName(): String {
    var name by remember { mutableStateOf("there") }
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    DisposableEffect(userId) {
        if (userId == null) {
            name = "there"
            onDispose { }
        } else {
            val nameRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId)
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
private fun rememberAvailableServices(): List<Service> {
    val services = remember { mutableStateListOf<Service>() }
    DisposableEffect(Unit) {
        val servicesRef = FirebaseDatabase.getInstance().getReference("services")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                services.clear()
                snapshot.children.mapNotNull { it.getValue(Service::class.java) }
                    .forEach(services::add)
            }

            override fun onCancelled(error: DatabaseError) {
                services.clear()
            }
        }
        servicesRef.addValueEventListener(listener)
        onDispose { servicesRef.removeEventListener(listener) }
    }
    return services
}

@Composable
fun CustDashboardScreen(navController: NavHostController) {
    CustDashboardContent(
        navController = navController,
        firstName = rememberCustomerName(),
        services = rememberAvailableServices()
    )
}

@Composable
private fun CustDashboardContent(
    navController: NavHostController,
    firstName: String,
    services: List<Service>
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = Color(0xFFB385B2),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.AddAlert, contentDescription = "Notifications")
            }
        },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
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
                text = "Hello, $firstName",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7C3AED)
            )
            Text(
                text = "What service do you need today?",
                fontSize = 20.sp,
                color = Color(0xFF7C3AED)
            )
            Text(
                text = "Popular Services",
                color = Color(0xFF7C3AED),
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                ServiceCategoryCard(
                    title = "Plumbing",
                    startingPrice = "From Ksh 1,500",
                    imageResource = R.drawable.plumbing,
                    onClick = { selectedCategory = "Plumbing" }
                )
                ServiceCategoryCard(
                    title = "Painting",
                    startingPrice = "From Ksh 2,000",
                    imageResource = R.drawable.painter,
                    onClick = { selectedCategory = "Painting" }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                ServiceCategoryCard(
                    title = "Electrician",
                    startingPrice = "From Ksh 1,500",
                    imageResource = R.drawable.electricity,
                    onClick = { selectedCategory = "Electrician" }
                )
                ServiceCategoryCard(
                    title = "Cleaning",
                    startingPrice = "From Ksh 1,600",
                    imageResource = R.drawable.cleaning,
                    onClick = { selectedCategory = "Cleaning" }
                )
            }
        }
    }

    selectedCategory?.let { category ->
        val matchingServices = services.filter { service ->
            service.name.contains(categoryKeyword(category), ignoreCase = true)
        }
        AlertDialog(
            onDismissRequest = { selectedCategory = null },
            title = { Text("$category services") },
            text = {
                if (matchingServices.isEmpty()) {
                    Text("No $category services are available yet.")
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(matchingServices, key = { it.id }) { service ->
                            ElevatedCard(
                                onClick = {
                                    BookingStore.selectedService = service
                                    selectedCategory = null
                                    navController.navigate(ROUTE_SERVICEDETAILS)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (service.imageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = service.imageUrl,
                                            contentDescription = service.name,
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(RoundedCornerShape(10.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(service.name, fontWeight = FontWeight.SemiBold)
                                        Text("Price: KSh ${service.price}", color = Color(0xFF5B21B6))
                                        Text(service.description)
                                        Text("Tap to view details", color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { selectedCategory = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB385B2))
                ) {
                    Text("Close")
                }
            }
        )
    }
}

private fun categoryKeyword(category: String): String = when (category) {
    "Plumbing" -> "plumb"
    "Painting" -> "paint"
    "Electrician" -> "electric"
    "Cleaning" -> "clean"
    else -> category
}

@Composable
private fun ServiceCategoryCard(
    title: String,
    startingPrice: String,
    imageResource: Int,
    onClick: () -> Unit
) {
    ElevatedCard(onClick = onClick) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .size(150.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(imageResource),
                contentDescription = title,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .size(100.dp)
            )
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(startingPrice, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustDashboardScreenPreview() {
    CustDashboardContent(
        navController = rememberNavController(),
        firstName = "Alex",
        services = listOf(
            Service(
                id = "preview-plumbing",
                name = "Plumbing repair",
                price = "1,500",
                description = "Leak repairs and fixture installation."
            )
        )
    )
}
