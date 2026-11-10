package com.project.homefix.screens.splashscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.project.homefix.R
import com.project.homefix.navigation.ROUTE_REGISTER
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    LaunchedEffect(key1 = true) {
        delay(3000)//3 second delay
        navController.navigate(ROUTE_REGISTER)

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF5B21B6)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        //LOGO
        Image(
            painter = painterResource(id = R.drawable.home),
            contentDescription = "logo",
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))
        //APP NAME
        Text(text = "HomeFix",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold

        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Your Home,our priority",
            fontSize = 20.sp,
            fontStyle = FontStyle.Italic,)
    }

}
@Preview(showBackground = true)
@Composable
fun SplashScreenPreview(){
    SplashScreen(rememberNavController())
}