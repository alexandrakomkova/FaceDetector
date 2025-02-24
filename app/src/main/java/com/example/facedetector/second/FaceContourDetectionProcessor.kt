package com.example.facedetector.second

import android.graphics.Rect
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.IOException

class FaceContourDetectionProcessor(
    //private val onSuccessCallback: ((FaceStatus) -> Unit)
) : BaseImageAnalyzer<List<Face>>() {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .build()

    private val detector = FaceDetection.getClient(options)

    override fun detectInImage(image: InputImage): Task<List<Face>> {
        return detector.process(image)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.d(TAG, "Exception thrown while trying to close Face Detector: $e")
        }
    }

    override fun onFailure(e: Exception) {
        Log.d(TAG, "Face Detector failed. $e")
    }

    override fun onSuccess(
        results: List<Face>,
        rect: Rect
    ) {
        Log.d(TAG, "Face Detector onSuccess.")
        if(results.isEmpty()) {
            Log.d(TAG, "Face Detector found 0 faces.")
        } else {
            Log.d(TAG, "Face Detector found faces.")
        }
    }

    companion object {
        private const val TAG = "FaceDetectorProcessor"
    }
}