package com.opera.firstapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.project.homefix.navigation.ROUTE_BOOKINGS
import com.project.homefix.navigation.ROUTE_BOOKSERVICE
import com.project.homefix.navigation.ROUTE_CUSTDASHBOARD
import com.project.homefix.navigation.ROUTE_CUSTPROFILE
import com.project.homefix.navigation.ROUTE_LOGIN
import com.project.homefix.navigation.ROUTE_MYSERVICES
import com.project.homefix.navigation.ROUTE_PROADDSERVICE
import com.project.homefix.navigation.ROUTE_PROVIDERDS
import com.project.homefix.navigation.ROUTE_PROVIDERPROFILE
import com.project.homefix.navigation.ROUTE_PROVIDER_BOOKING_SUMMARY
import com.project.homefix.navigation.ROUTE_REGISTER
import com.project.homefix.navigation.ROUTE_SPLASH
import com.project.homefix.navigation.ROUTE_SERVICEDETAILS
import com.project.homefix.screens.booking.BookingsScreen
import com.project.homefix.screens.bookservice.BookServiceScreen
import com.project.homefix.screens.custdashboard.CustDashboardScreen
import com.project.homefix.screens.login.LoginScreen
import com.project.homefix.screens.myservices.MyServicesScreen
import com.project.homefix.screens.proaddservice.ProAddServicescreen
import com.project.homefix.screens.profile.ProfileScreen
import com.project.homefix.screens.providerDS.ProviderDashboardScreen
import com.project.homefix.screens.providerDS.ProviderBookingSummaryScreen
import com.project.homefix.screens.register.RegisterScreen
import com.project.homefix.screens.splashscreen.SplashScreen
import com.project.homefix.screens.servicedetails.ServiceDetailsScreen

@Composable
fun AppNavHost(
    modifier: Modifier=Modifier,
    navController: NavHostController= rememberNavController(),
    startDestination: String=ROUTE_SPLASH
) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = startDestination
    ) {
        composable(ROUTE_SPLASH) {
            SplashScreen(navController)
        }
        composable(ROUTE_LOGIN) {
            LoginScreen(navController)
        }
        composable(ROUTE_REGISTER) {
            RegisterScreen(navController)
        }
       composable(ROUTE_CUSTDASHBOARD) {
           CustDashboardScreen(navController)
       }
        composable(ROUTE_BOOKINGS) {
            BookingsScreen(navController)
        }
        composable(ROUTE_PROVIDERDS) {
            ProviderDashboardScreen(navController)
        }
        composable(ROUTE_PROVIDER_BOOKING_SUMMARY) {
            ProviderBookingSummaryScreen(navController)
        }
        composable(ROUTE_BOOKSERVICE){
            BookServiceScreen(navController)
        }
        composable(ROUTE_SERVICEDETAILS) {
            ServiceDetailsScreen(navController)
        }
        composable ( ROUTE_PROADDSERVICE ){
            ProAddServicescreen(navController)
        }
        composable(ROUTE_MYSERVICES) {
            MyServicesScreen(navController)
        }
        composable(ROUTE_CUSTPROFILE) {
            ProfileScreen(navController, isProvider = false)
        }
        composable(ROUTE_PROVIDERPROFILE) {
            ProfileScreen(navController, isProvider = true)
        }


    }
}