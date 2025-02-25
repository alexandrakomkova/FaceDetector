package com.example.facedetector

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.facedetector.ui.theme.FaceDetectorTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FaceDetectionActivity: ComponentActivity() {
    private lateinit var faceDetector: FaceDetector
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cameraExecutor = Executors.newSingleThreadExecutor()

        val realtimeOptions = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()
        faceDetector = FaceDetection.getClient(realtimeOptions)

        setContent {
            FaceDetectorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FaceDetectionScreen()
                    if (!allRuntimePermissionsGranted()) {
                        getRuntimePermissions()
                    }
                }
            }
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

    @OptIn(ExperimentalGetImage::class)
    @Composable
    fun FaceDetectionScreen() {
        var faces by remember { mutableStateOf(emptyList<Face>()) }

        CameraPreview { imageProxy ->
            Log.d("detectFaces", "0")
            val inputImage = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
            val detectedFaces = detectFaces(inputImage)
            faces = detectedFaces
            Log.d("detectFaces", "$faces")
            imageProxy.close()
        }

        Log.d("detectFaces", "1")
        DrawContours(faces = faces)
        Log.d("detectFaces", "3")
    }

    private fun detectFaces(image: InputImage): List<Face> {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()
        Log.d("detectFaces", "4")

        val detector = FaceDetection.getClient(options)
        val faces = detector.process(image)
            .addOnSuccessListener { faces ->
                Log.d("detectFaces", "success: $faces")
            }
            .addOnFailureListener { it ->
                Log.d("detectFaces", "fail")
                it.printStackTrace()
            }

        return faces.result
    }

    @Composable
    fun CameraPreview(onImageAvailable: (ImageProxy) -> Unit) {
        AndroidView(
            factory = { context ->
                val previewView = PreviewView(context)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener({
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, onImageAvailable)
                        }

                    // val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            // context as ComponentActivity, cameraSelector, preview
                            this@FaceDetectionActivity, cameraSelector, preview, imageAnalyzer
                        )
                        Log.d("CameraPreview", "success")

                    } catch (exc: Exception) {
                        Log.d("CameraPreview", "fail: ${exc.message}")
                    }
                }, ContextCompat.getMainExecutor(context))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
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



}
