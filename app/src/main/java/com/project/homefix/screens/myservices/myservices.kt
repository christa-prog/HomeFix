package com.project.homefix.screens.myservices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

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
import com.project.homefix.models.Service
import com.project.homefix.viewmodel.ServiceViewModel
import com.project.homefix.navigation.ROUTE_MYSERVICES
import com.project.homefix.navigation.ROUTE_PROVIDERPROFILE
import com.project.homefix.navigation.ROUTE_PROADDSERVICE
import com.project.homefix.navigation.ROUTE_PROVIDERDS

@Composable
fun rememberProviderServices(): List<Service> {
    val services = remember { mutableStateListOf<Service>() }
    val providerId = FirebaseAuth.getInstance().currentUser?.uid

    DisposableEffect(providerId) {
        if (providerId == null) {
            services.clear()
            onDispose { }
        } else {
            val query = FirebaseDatabase.getInstance().getReference("services")
                .orderByChild("providerId")
                .equalTo(providerId)
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
            query.addValueEventListener(listener)
            onDispose { query.removeEventListener(listener) }
        }
    }
    return services
}

@Composable
fun ProviderServiceCard(
    service: Service,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F3FF))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (service.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = service.imageUrl,
                        contentDescription = service.name,
                        modifier = Modifier.size(76.dp).clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(service.name, fontWeight = FontWeight.SemiBold)
                    Text(service.description)
                    Text(
                        "Price: ${service.price}",
                        color = Color(0xFF5B21B6),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.width(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32),
                        contentColor = Color.White
                    )
                ) {
                    Text("Edit")
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.width(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC62828),
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun MyServicesScreen(navController: NavHostController) {
    val context = LocalContext.current
    val serviceViewModel = remember(context) { ServiceViewModel(context) }
    MyServicesContent(
        navController = navController,
        services = rememberProviderServices(),
        onUpdateService = { service, name, description, price ->
            serviceViewModel.updateService(service.id, name, price, description)
        },
        onDeleteService = { service -> serviceViewModel.deleteService(service.id) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyServicesContent(
    navController: NavHostController,
    services: List<Service>,
    onUpdateService: (Service, String, String, String) -> Unit = { _, _, _, _ -> },
    onDeleteService: (Service) -> Unit = {}
) {
    var editingService by remember { mutableStateOf<Service?>(null) }
    var deletingService by remember { mutableStateOf<Service?>(null) }
    var editName by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editPrice by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Services", fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF49CBB)
                )
            )
        },
        bottomBar = {
            Column {
                Button(
                    onClick = { navController.navigate(ROUTE_PROADDSERVICE) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    contentPadding = PaddingValues(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB385B2),
                        contentColor = Color.White
                    )
                ) {
                    Text("Add a service")
                }
                NavigationBar(containerColor = Color(0xFFF49CBB)) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate(ROUTE_PROVIDERDS) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("HOME") }
                    )
                    NavigationBarItem(
                        selected = true,
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
        }
    ) { padding ->
        if (services.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("No services yet")
                Text("Add your first service to show customers what you offer.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(services, key = { it.id }) { service ->
                    ProviderServiceCard(
                        service = service,
                        onEdit = {
                            editingService = service
                            editName = service.name
                            editDescription = service.description
                            editPrice = service.price
                        },
                        onDelete = { deletingService = service }
                    )
                }
            }
        }
    }

    editingService?.let { service ->
        AlertDialog(
            onDismissRequest = { editingService = null },
            title = { Text("Edit service") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Service name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Description") }
                    )
                    OutlinedTextField(
                        value = editPrice,
                        onValueChange = { editPrice = it },
                        label = { Text("Price") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateService(service, editName, editDescription, editPrice)
                        editingService = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB385B2))
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(
                    onClick = { editingService = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB385B2))
                ) { Text("Cancel") }
            }
        )
    }

    deletingService?.let { service ->
        AlertDialog(
            onDismissRequest = { deletingService = null },
            title = { Text("Delete service?") },
            text = { Text("${service.name} will be removed from your services.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteService(service)
                        deletingService = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC62828))
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(
                    onClick = { deletingService = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB385B2))
                ) { Text("Cancel") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProviderServiceCardPreview() {
    MyServicesContent(
        navController = rememberNavController(),
        services = listOf(
            Service(
                id = "preview-service",
                name = "Plumbing repair",
                price = "KSh 2,500",
                description = "Pipe repairs, leak detection, and fixture installation."
            )
        )
    )
}
