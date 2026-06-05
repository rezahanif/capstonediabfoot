package com.project.insole

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.project.insole.core.ble.BleConnectionManager
import com.project.insole.core.ble.BleViewModel
import com.project.insole.core.theme.InsoleTheme
import com.project.insole.core.ble.BleDevicePairingScreen
import com.project.insole.features.auth.presentation.AuthViewModel
import com.project.insole.features.auth.presentation.screens.LandingScreen
import com.project.insole.features.auth.presentation.screens.LoginScreen
import com.project.insole.features.auth.presentation.screens.SignUpScreen
import com.project.insole.features.main.presentation.MainScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single activity hosting Jetpack Compose Navigation for the entire app.
 * All screens are defined in the NavHost with their respective routes.
 * Handles Bluetooth permissions at runtime (Android 6.0+).
 * Initializes BLE connection on app startup.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        private const val REQUEST_CODE_BLE_PERMISSIONS = 100
    }

    @Inject
    lateinit var bleConnectionManager: BleConnectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestBluetoothPermissions()
        // Initialize BLE connection after permissions are granted
        bleConnectionManager.initializeBleConnection()
        setContent {
            InsoleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val sharedBleViewModel: BleViewModel = hiltViewModel()

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
                                    navController.navigate("main") {
                                        popUpTo("landing") { inclusive = true }
                                    }
                                },
                                onNavigateToSignUp = { navController.navigate("signup") },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("signup") {
                            SignUpScreen(
                                viewModel = hiltViewModel(),
                                onNavigateToHome = {
                                    navController.navigate("main") {
                                        popUpTo("landing") { inclusive = true }
                                    }
                                },
                                onNavigateToSignIn = { navController.navigate("login") },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("main") {
                            val authViewModel: AuthViewModel = hiltViewModel()
                            MainScreen(
                                onNavigateToPairing = { navController.navigate("pairing") },
                                onLogout = {
                                    authViewModel.logout()
                                    navController.navigate("landing") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                },
                                bleViewModel = sharedBleViewModel
                            )
                        }
                        composable("pairing") {
                            BleDevicePairingScreen(
                                viewModel = sharedBleViewModel,
                                onConnected = { navController.popBackStack() },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Request Bluetooth permissions at runtime (Android 6.0+).
     * Checks if permissions are already granted before requesting.
     */
    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            // Android 11 and below: only need location permission
            if (!hasLocationPermission()) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_BLE_PERMISSIONS
                )
            }
            return
        }

        // Android 12+: need BLUETOOTH_SCAN and BLUETOOTH_CONNECT
        val permissionsNeeded = mutableListOf<String>()
        if (!hasBluetoothScanPermission()) {
            permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (!hasBluetoothConnectPermission()) {
            permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                REQUEST_CODE_BLE_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_BLE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // All permissions granted - BLE connection can proceed
                    android.util.Log.d("MainActivity", "Bluetooth permissions granted")
                } else {
                    // Permissions denied - show warning or handle gracefully
                    android.util.Log.w("MainActivity", "Bluetooth permissions denied")
                }
            }
        }
    }

    private fun hasBluetoothScanPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED

    private fun hasBluetoothConnectPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}
