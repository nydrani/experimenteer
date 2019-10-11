package xyz.velvetmilk.testingtool.tools

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager

class PermissionsHelper {

    companion object {
        val cameraPermissions = arrayOf(Manifest.permission.CAMERA)
        val infoPermissions = arrayOf(Manifest.permission.READ_PHONE_STATE)
        val openSlesPermissions = arrayOf(Manifest.permission.RECORD_AUDIO)
        val telephonyPermissions = arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION)
        val telecomPermissions = arrayOf(Manifest.permission.READ_PHONE_STATE)
        val subscriptionPermissions = arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION)
        val gpsPermissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

        private fun checkPermissions(activity: Activity, permissions: Array<String>): Boolean {
            for (permission in permissions) {
                if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }

            return true
        }

        fun requestPermissions(activity: Activity, permissions: Array<String>) {
            activity.requestPermissions(permissions, 0)
        }

        fun checkAndRequestPermissions(activity: Activity, permissions: Array<String>): Boolean {
            if (!checkPermissions(activity, permissions)) {
                requestPermissions(activity, permissions)
                return false
            }

            return true
        }
    }
}
