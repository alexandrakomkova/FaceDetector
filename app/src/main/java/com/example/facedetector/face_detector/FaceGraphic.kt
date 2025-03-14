package com.example.facedetector.face_detector

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import com.example.facedetector.GraphicOverlay
import com.google.mlkit.vision.face.Face


class FaceGraphic(
    overlay: GraphicOverlay,
    private val face: Face,
    private val imageRect: Rect,
): GraphicOverlay.Graphic(overlay) {
    private val facePositionPaint: Paint
    private val idPaint: Paint
    private val boxPaint: Paint

    init {
        val selectedColor = Color.WHITE

        facePositionPaint = Paint()
        facePositionPaint.color = selectedColor
        idPaint = Paint()
        idPaint.color = selectedColor

        boxPaint = Paint()
        boxPaint.color = selectedColor
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH
    }
    private val greenBoxPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = BOX_STROKE_WIDTH
    }

    private val redBoxPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = BOX_STROKE_WIDTH
    }

    override fun draw(canvas: Canvas?) {
        Log.d(TAG, "draw")
        val rect = calculateRect(
            imageRect.height().toFloat(),
            imageRect.width().toFloat(),
            face.boundingBox
        )
        val faceDimensions = getFaceDimensions()
        when {
            checkIsToFar(faceDimensions) -> {
                canvas?.drawRect(rect,redBoxPaint)
            }
            checkIsNoCentered(faceDimensions) -> {
                canvas?.drawRect(rect,redBoxPaint)
            }
            else -> {
                canvas?.drawRect(rect,greenBoxPaint)
            }
        }
    }

    private fun checkIsNoCentered(
        faceDimensions: FaceDimensions
    ): Boolean {
        val width =  imageRect.width()
        val height =  imageRect.height()

        return faceDimensions.left < 0 || faceDimensions.right > width || faceDimensions.top < 0 || faceDimensions.bottom > height
    }

    private fun checkIsToFar(
        faceDimensions: FaceDimensions
    ): Boolean {
        val screenPercentage = 0.4f
        val width =  imageRect.width()
        val height =  imageRect.height()

        return (
                faceDimensions.bottom - faceDimensions.top <= height * screenPercentage ||
                        faceDimensions.right - faceDimensions.left <= width * screenPercentage
                )
    }

    private fun getFaceDimensions(): FaceDimensions = run {
        val x = face.boundingBox.centerX().toFloat()
        val y = face.boundingBox.centerY().toFloat()
        FaceDimensions(
            x = x,
            y = y,
            left = x - face.boundingBox.width() / 2.0f,
            top = y - face.boundingBox.height() / 2.0f,
            right = x + face.boundingBox.width() / 2.0f,
            bottom = y + face.boundingBox.height() / 2.0f,
        )
    }

    companion object {
        private const val BOX_STROKE_WIDTH = 5.0f
        private const val TAG = "FaceGraphic"
    }
}

data class FaceDimensions(
    val x: Float,
    val y: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)