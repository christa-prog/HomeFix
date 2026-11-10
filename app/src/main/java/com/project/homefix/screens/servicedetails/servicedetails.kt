package com.project.homefix.screens.servicedetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.project.homefix.models.BookingStore
import com.project.homefix.navigation.ROUTE_BOOKSERVICE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailsScreen(navController: NavHostController) {
    val service = BookingStore.selectedService

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Service details", color = Color(0xFF7C3AED)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
                onClick = { if (service != null) navController.navigate(ROUTE_BOOKSERVICE) },
                enabled = service != null,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                colors = ButtonDefaults.buttonColors
                    (containerColor = Color(0xFFB385B2), contentColor = Color.White)
            ) {
                Text("BOOK", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    ) { innerPadding ->
        if (service == null) {
            Text(
                text = "No service was selected.",
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = service.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C3AED)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F3FF))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Service description", fontWeight = FontWeight.SemiBold)
                        Text(service.description, fontSize = 16.sp)
                        Text(
                            text = "Price: KSh ${service.price}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5B21B6)
                        )
                    }
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ServiceDetailsScreenPreview() {
    ServiceDetailsScreen(rememberNavController())
}
