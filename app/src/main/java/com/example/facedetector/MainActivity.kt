package com.example.facedetector

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.facedetector.second.CameraManager
import com.example.facedetector.second.GraphicOverlay
import com.example.facedetector.ui.theme.FaceDetectorTheme
import com.google.mlkit.vision.face.Face

class MainActivity : ComponentActivity() {

    private lateinit var cameraManager: CameraManager
    private lateinit var graphicOverlay: GraphicOverlay


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FaceDetectorTheme {
                val faces by remember { mutableStateOf(emptyList<Face>()) }
                graphicOverlay = GraphicOverlay(this, null)

                Surface(modifier = Modifier.fillMaxSize()) {

                    createCameraManager(faces)
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
    fun DrawContours(faces: List<Face>) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            faces.forEach { face ->
                val bounds = face.boundingBox
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(bounds.left.toFloat(), bounds.top.toFloat()),
                    size = size.copy(width = bounds.width().toFloat(), height = bounds.height().toFloat()),
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }
    }

    @Composable
    private fun CameraPreview() {
        AndroidView(
            factory = { context ->

                cameraManager.startCamera()
           },
            modifier = Modifier.fillMaxSize()
        )

        AndroidView(factory = {
            graphicOverlay
        }, modifier = Modifier.fillMaxSize())
    }

    private fun createCameraManager(faces: List<Face>) {
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



