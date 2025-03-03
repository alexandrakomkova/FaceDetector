package com.example.facedetector

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.facedetector.camerax.CameraManager
import com.example.facedetector.ui.theme.FaceDetectorTheme

class MainActivity : ComponentActivity() {

    private lateinit var cameraManager: CameraManager
    private lateinit var graphicOverlay: GraphicOverlay


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FaceDetectorTheme {
                graphicOverlay = GraphicOverlay(this, null)

                Surface(modifier = Modifier.fillMaxSize()) {

                    createCameraManager()
                    if (!allRuntimePermissionsGranted()) {
                        getRuntimePermissions()
                    } else {
                        CameraPreview()
                    }

                }
            }
        }
    }

    @Composable
    private fun CameraPreview() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                AndroidView(
                    factory = {
                        cameraManager.startCamera()

                    },
                    modifier = Modifier.fillMaxSize()
                )
                AndroidView(factory = {
                    graphicOverlay
                }, modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { cameraManager.changeCameraSelector() }
            ) {
                Text(text = "Switch camera")
            }
        }


    }

    private fun createCameraManager() {
        cameraManager = CameraManager(
            context = this,
            lifecycleOwner = this,
            graphicOverlay = graphicOverlay
        )
    }

    private fun allRuntimePermissionsGranted(): Boolean {
        for (permission in REQUIRED_RUNTIME_PERMISSIONS) {
            permission.let {
                if (!isPermissionGranted(this, it)) {
                    return false
                }
            }
        }
        return true
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "Permission granted: $permission")
            return true
        }
        Log.i(TAG, "Permission NOT granted: $permission")
        return false
    }

    private fun getRuntimePermissions() {
        val permissionsToRequest = ArrayList<String>()
        for (permission in REQUIRED_RUNTIME_PERMISSIONS) {
            permission.let {
                if (!isPermissionGranted(this, it)) {
                    permissionsToRequest.add(permission)
                }
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUESTS
            )
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSION_REQUESTS = 1

        private val REQUIRED_RUNTIME_PERMISSIONS =
            arrayOf(
                android.Manifest.permission.CAMERA
            )
    }
}



