package com.optimeter.app.presentation.scan

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextAnalyzer(
    private val onDigitsDetected: (String) -> Unit,
    private val onImageProcessed: ((android.graphics.Bitmap) -> Unit)? = null
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private fun preProcessBitmapForOCR(src: android.graphics.Bitmap): android.graphics.Bitmap {
        val width = src.width
        val height = src.height
        val pixels = IntArray(width * height)
        
        // Load pixels into array for fast processing
        src.getPixels(pixels, 0, width, 0, 0, width, height)

        // Threshold value: Any pixel where R, G, and B are all below this value is considered "Black".
        // 100 is a good starting point for dark grey/black digits.
        val darkThreshold = 100 

        for (i in pixels.indices) {
            val pixelColor = pixels[i]
            val r = android.graphics.Color.red(pixelColor)
            val g = android.graphics.Color.green(pixelColor)
            val b = android.graphics.Color.blue(pixelColor)

            // If the pixel is dark in all channels, it's our black digit.
            // Otherwise (if it's bright, or strongly red/colored), turn it white.
            if (r < darkThreshold && g < darkThreshold && b < darkThreshold) {
                pixels[i] = android.graphics.Color.BLACK
            } else {
                pixels[i] = android.graphics.Color.WHITE
            }
        }

        val dest = android.graphics.Bitmap.createBitmap(width, height, src.config ?: android.graphics.Bitmap.Config.ARGB_8888)
        dest.setPixels(pixels, 0, width, 0, 0, width, height)
        return dest
    }

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

                val padding = (cropHeight * 0.15).toInt()
                val safeStartX = kotlin.math.max(0, startX - padding)
                val safeStartY = kotlin.math.max(0, startY - padding)
                val safeEndX = kotlin.math.min(rotatedBitmap.width, startX + cropWidth + padding)
                val safeEndY = kotlin.math.min(rotatedBitmap.height, startY + cropHeight + padding)
                val safeCropWidth = safeEndX - safeStartX
                val safeCropHeight = safeEndY - safeStartY

                val croppedBitmap = android.graphics.Bitmap.createBitmap(
                    rotatedBitmap,
                    safeStartX, safeStartY,
                    safeCropWidth, safeCropHeight
                )

                val processedBitmap = preProcessBitmapForOCR(croppedBitmap)
                onImageProcessed?.invoke(processedBitmap)

                val image = InputImage.fromBitmap(processedBitmap, 0)

                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val regex = kotlin.text.Regex("^\\d{5,8}$")
                        val matches = mutableListOf<Pair<String, Int>>()
                        val imageCenterY = processedBitmap.height / 2

                        for (block in visionText.textBlocks) {
                            for (line in block.lines) {
                                val cleaned = line.text.replace(kotlin.text.Regex("[^0-9]"), "")
                                if (cleaned.matches(regex)) {
                                    val box = line.boundingBox
                                    val distance = if (box != null) {
                                        kotlin.math.abs(box.centerY() - imageCenterY)
                                    } else {
                                        Int.MAX_VALUE
                                    }
                                    matches.add(Pair(cleaned, distance))
                                }
                            }
                        }

                        val bestMatch = matches.minByOrNull { it.second }
                        if (bestMatch != null) {
                            onDigitsDetected(bestMatch.first)
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
