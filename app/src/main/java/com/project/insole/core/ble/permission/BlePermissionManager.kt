package com.project.insole.core.ble.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat

/**
 * Manages Bluetooth Low Energy permissions for Android devices.
 * Handles runtime permission requests for both pre and post Android 12 devices.
 */
class BlePermissionManager(private val context: Context) {

    /**
     * List of required BLE permissions for Android 12+
     */
    private val android12PlusPermissions = listOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE
    )

    /**
     * List of required permissions for pre-Android 12 devices
     */
    private val preAndroid12Permissions = listOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    /**
     * Checks if all required BLE permissions are granted.
     */
    fun hasAllPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android12PlusPermissions.all { permission ->
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            preAndroid12Permissions.all { permission ->
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    /**
     * Returns list of permissions that need to be requested.
     */
    fun getRequiredPermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android12PlusPermissions.filter { permission ->
                ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
            }
        } else {
            preAndroid12Permissions.filter { permission ->
                ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
            }
        }
    }

    /**
     * Checks specific permission status.
     */
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns the appropriate permissions based on Android version.
     */
    fun getPermissionsForVersion(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android12PlusPermissions.toTypedArray()
        } else {
            preAndroid12Permissions.toTypedArray()
        }
    }
}
