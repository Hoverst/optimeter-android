package com.optimeter.app.presentation.scan

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextAnalyzer(
    private val onDigitsDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            try {
                val originalBitmap = imageProxy.toBitmap()
                val matrix = android.graphics.Matrix().apply {
                    postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                }
                val rotatedBitmap = android.graphics.Bitmap.createBitmap(
                    originalBitmap,
                    0, 0,
                    originalBitmap.width, originalBitmap.height,
                    matrix,
                    true
                )

                val cropWidth = (rotatedBitmap.width * 0.8f).toInt()
                val cropHeight = (cropWidth / 2.66f).toInt()
                val startX = (rotatedBitmap.width - cropWidth) / 2
                val startY = (rotatedBitmap.height - cropHeight) / 2

                val croppedBitmap = android.graphics.Bitmap.createBitmap(
                    rotatedBitmap,
                    startX, startY,
                    cropWidth, cropHeight
                )

                val image = InputImage.fromBitmap(croppedBitmap, 0)

                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text
                        val digits = text.replace(kotlin.text.Regex("[^0-9]"), "")
                        if (digits.isNotEmpty()) {
                            onDigitsDetected(digits)
                        }
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } catch (e: Exception) {
                imageProxy.close()
            }
        } else {
            imageProxy.close()
        }
    }
}
