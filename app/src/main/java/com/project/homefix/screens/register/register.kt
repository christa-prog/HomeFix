package com.project.homefix.screens.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.project.homefix.navigation.ROUTE_LOGIN
import com.project.homefix.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xffF49CBB))
            .padding(16.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(text = "Create Your Account",
            fontSize = 28.sp,
            color = Color((0xFF7C3AED)),
            fontWeight = FontWeight.Bold,

        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Join HomeFix and get started",
            fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))
        var fullname by remember{ mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmpass by remember { mutableStateOf("") }
        var selectedRole by remember { mutableStateOf("customer") }
        OutlinedTextField(
            value = fullname,
            onValueChange = {fullname=it},
            label={Text("Enter fullname")},
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "person icon"
                )
            }

        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {email=it},
            label = {Text("Enter Email")},
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "email icon"
                )
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {password=it},
            label = {Text("Enter password")},
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "email icon"
                )
            },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = confirmpass,
            onValueChange = {confirmpass=it},
            label = {Text("confirm password")},
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "email icon"
                )
            },
            visualTransformation = PasswordVisualTransformation(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Select role",fontSize = 20.sp,color=Color((0xFF7C3AED)))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            ) {
                TextButton(
                    onClick = { selectedRole = "customer" },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF7C3AED))
                ) {
                    Text(if (selectedRole == "customer") "✓ Customer" else "Customer", fontSize = 16.sp)
                }
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "person icon",
                    tint = Color(0XFFF49CBB)
                )
            }
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            ) {
                TextButton(
                    onClick = { selectedRole = "provider" },
                    colors = ButtonDefaults.textButtonColors(contentColor =Color(0xFF7C3AED))
                ) {
                    Text(if (selectedRole == "provider") "✓ Service Provider" else "Service Provider", fontSize = 16.sp)
                }
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "person icon",
                    tint = Color(0XFFF49CBB)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        val context= LocalContext.current
        val myAuth= AuthViewModel(navController,context)
        Button(
            onClick = { myAuth.register(fullname, email, password, confirmpass, selectedRole) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7C3AED),
                contentColor = Color.White
            )
        ){
            Text("REGISTER", fontSize = 24.sp)
        }
        //text button
        TextButton(
            onClick = { navController.navigate(ROUTE_LOGIN) },
            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF7C3AED))
        ) {
            Text("Already have an account? Log in here")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview(){
    RegisterScreen(rememberNavController())
}

