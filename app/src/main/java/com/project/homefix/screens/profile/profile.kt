package com.project.homefix.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.homefix.models.User
import com.project.homefix.navigation.ROUTE_BOOKINGS
import com.project.homefix.navigation.ROUTE_CUSTDASHBOARD
import com.project.homefix.navigation.ROUTE_MYSERVICES
import com.project.homefix.navigation.ROUTE_PROVIDERDS

@Composable
private fun rememberProfileUser(): User {
    val authUser = FirebaseAuth.getInstance().currentUser
    var profile by remember {
        mutableStateOf(
            User(
                fullname = authUser?.displayName.orEmpty(),
                email = authUser?.email.orEmpty()
            )
        )
    }

    DisposableEffect(authUser?.uid) {
        val userId = authUser?.uid
        if (userId == null) {
            onDispose { }
        } else {
            val profileRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    profile = User(
                        fullname = snapshot.child("fullname").getValue(String::class.java).orEmpty(),
                        email = snapshot.child("email").getValue(String::class.java)
                            .orEmpty()
                            .ifBlank { authUser.email.orEmpty() },
                        role = snapshot.child("role").getValue(String::class.java).orEmpty()
                    )
                }

                override fun onCancelled(error: DatabaseError) = Unit
            }
            profileRef.addValueEventListener(listener)
            onDispose { profileRef.removeEventListener(listener) }
        }
    }
    return profile
}

@Composable
fun ProfileScreen(navController: NavHostController, isProvider: Boolean) {
    ProfileContent(
        navController = navController,
        isProvider = isProvider,
        user = rememberProfileUser()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    navController: NavHostController,
    isProvider: Boolean,
    user: User
) {
    val homeRoute = if (isProvider) ROUTE_PROVIDERDS else ROUTE_CUSTDASHBOARD

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Profile", color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold)
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
                    onClick = { navController.navigate(homeRoute) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("HOME") }
                )
                if (isProvider) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate(ROUTE_MYSERVICES) },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "My services") },
                        label = { Text("Services") }
                    )
                } else {
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate(ROUTE_BOOKINGS) },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Bookings") },
                        label = { Text("Bookings") }
                    )
                }
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isProvider) "Service provider account" else "Customer account",
                color = Color(0xFF7C3AED),
                fontWeight = FontWeight.SemiBold
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F3FF))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileInfoRow("Full name", user.fullname.ifBlank { "Not provided" })
                    ProfileInfoRow("Email", user.email.ifBlank { "Not provided" })
                    ProfileInfoRow(
                        "Account type",
                        if (isProvider) "Service provider" else "Customer"
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomerProfilePreview() {
    ProfileContent(
        navController = rememberNavController(),
        isProvider = false,
        user = User(fullname = "Alex Mwangi", email = "alex@example.com", role = "customer")
    )
}
