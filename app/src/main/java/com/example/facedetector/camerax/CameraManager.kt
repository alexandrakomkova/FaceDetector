package com.example.facedetector.camerax

import android.content.Context
import android.util.Log
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.facedetector.GraphicOverlay
import com.example.facedetector.face_detector.FaceContourDetectionProcessor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val graphicOverlay: GraphicOverlay,
) {
    private lateinit var cameraExecutor: ExecutorService
    private var cameraSelectorOption = CameraSelector.LENS_FACING_FRONT
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null

    init {
        createNewExecutor()
    }

    private fun createNewExecutor() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        Log.d(TAG, "createNewExecutor success")
    }

    fun startCamera(): View {
        val previewView = PreviewView(context)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener(
            {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                preview = Preview.Builder()
                    .build()
                    .also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraSelectorOption)
                    .build()

                imageAnalyzer = ImageAnalysis.Builder()
                    //.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, selectAnalyzer())
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner = lifecycleOwner,
                        cameraSelector = cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                    Log.d(TAG, "success")

                } catch (e: Exception) {
                    Log.d(TAG, "fail: ${e.message}")
                }
            }, ContextCompat.getMainExecutor(context)
        )

        return previewView
    }

    private fun selectAnalyzer(): ImageAnalysis.Analyzer {
        return FaceContourDetectionProcessor(
            graphicOverlay = graphicOverlay
        )
    }

    fun changeCameraSelector() {

        cameraSelectorOption =
            if (cameraSelectorOption == CameraSelector.LENS_FACING_BACK) {
                Log.d(TAG, "front")
                CameraSelector.LENS_FACING_FRONT

                }
            else {
                Log.d(TAG, "back")
                CameraSelector.LENS_FACING_BACK
            }

        // graphicOverlay.toggleSelector()
        // startCamera()

        try {
            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                lifecycleOwner = lifecycleOwner,
                cameraSelector = CameraSelector.Builder().requireLensFacing(cameraSelectorOption).build(),
                preview,
                imageAnalyzer
            )
            Log.d(TAG, "success")

        } catch (e: Exception) {
            Log.d(TAG, "fail: ${e.message}")
        }

    }

    companion object {
        private const val TAG = "CameraManager"
    }

}