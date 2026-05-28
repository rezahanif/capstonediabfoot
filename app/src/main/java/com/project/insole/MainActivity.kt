package com.project.insole

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.project.insole.core.theme.InsoleTheme
import com.project.insole.features.auth.presentation.screens.LandingScreen
import com.project.insole.features.auth.presentation.screens.LoginScreen
import com.project.insole.features.auth.presentation.screens.SignUpScreen
import com.project.insole.features.sensor.presentation.screens.DashboardScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single activity hosting Jetpack Compose Navigation for the entire app.
 * All screens are defined in the NavHost with their respective routes.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InsoleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "landing") {
                        composable("landing") {
                            LandingScreen(
                                onNavigateToLogin = { navController.navigate("login") },
                                onNavigateToSignUp = { navController.navigate("signup") }
                            )
                        }
                        composable("login") {
                            LoginScreen(
                                viewModel = hiltViewModel(),
                                onNavigateToHome = { 
                                    navController.navigate("dashboard") {
                                        popUpTo("landing") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("signup") {
                            SignUpScreen(
                                viewModel = hiltViewModel(),
                                onNavigateToHome = {
                                    navController.navigate("dashboard") {
                                        popUpTo("landing") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("dashboard") {
                            DashboardScreen(viewModel = hiltViewModel())
                        }
                    }
                }
            }
        }
    }
}
