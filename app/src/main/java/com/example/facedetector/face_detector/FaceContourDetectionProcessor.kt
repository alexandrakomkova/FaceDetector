package com.example.facedetector.face_detector

import android.graphics.Rect
import android.util.Log
import com.example.facedetector.camerax.BaseImageAnalyzer
import com.example.facedetector.GraphicOverlay
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.IOException

class FaceContourDetectionProcessor(
    override val graphicOverlay: GraphicOverlay,
    //private var faces: List<Face>
    //private val view: GraphicOverlay,
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

    override fun onSuccess(results: List<Face>, graphicOverlay: GraphicOverlay, rect: Rect) {
       // Log.d(TAG, "Face Detector onSuccess.")
        if(results.isEmpty()) {
            // Log.d(TAG, "Face Detector found 0 faces.")
        } else {
            // Log.d(TAG, "Face Detector found faces.")
            graphicOverlay.clear()
            if (results.isNotEmpty()){
                results.forEach {
                    val faceGraphic = FaceGraphic(
                        graphicOverlay,
                        it,
                        rect,
                        //onSuccessCallback
                        )
                    graphicOverlay.add(faceGraphic)
                }
                graphicOverlay.postInvalidate()
            }
        }
    }

    override fun onFailure(e: Exception) {
        Log.d(TAG, "Face Detector failed. $e")
    }


    companion object {
        private const val TAG = "FaceDetectorProcessor"
    }
}