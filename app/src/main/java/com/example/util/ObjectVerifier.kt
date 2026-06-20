package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.example.data.model.ObjectPool
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs

object ObjectVerifier {

    sealed class VerificationResult {
        object Success : VerificationResult()
        data class Failure(val reason: String) : VerificationResult()
    }

    // Run anti-cheat analysis + ML Kit categorization
    suspend fun verifyImage(
        bitmap: Bitmap,
        targetObjectName: String,
        isCustomExtreme: Boolean = false,
        customLabel: String = "",
        minConfidence: Float = 0.80f
    ): VerificationResult = withContext(Dispatchers.Default) {
        
        // 1. Brightness Check (Anti-Cheat)
        val avgBrightness = calculateBrightness(bitmap)
        Log.d("ObjectVerifier", "Average Brightness: $avgBrightness")
        if (avgBrightness < 15f) {
            return@withContext VerificationResult.Failure("Image is too dark! Turn on a light and try again.")
        }

        // 2. Blur Check (Anti-Cheat)
        val blurScore = calculateVibrancyBlurScore(bitmap)
        Log.d("ObjectVerifier", "Blur/Vibrancy score: $blurScore")
        if (blurScore < 5f) {
            return@withContext VerificationResult.Failure("Image is too blurry! Keep the phone steady.")
        }

        // 3. ML Kit Recognition
        return@withContext suspendCoroutine<VerificationResult> { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)
            
            // Set threshold
            val options = ImageLabelerOptions.Builder()
                .setConfidenceThreshold(minConfidence)
                .build()
            
            val labeler = ImageLabeling.getClient(options)
            
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    val detectedList = labels.map { "${it.text.lowercase()} (${(it.confidence * 100).toInt()}%)" }
                    Log.d("ObjectVerifier", "Detected labels: $detectedList")

                    if (labels.isEmpty()) {
                        continuation.resume(VerificationResult.Failure("Couldn't recognize any item. Try adjusting your angle."))
                        return@addOnSuccessListener
                    }

                    val matched = if (isCustomExtreme) {
                        // Custom target object matching
                        labels.any { label ->
                            val text = label.text.lowercase()
                            val trg = customLabel.lowercase()
                            text.contains(trg) || trg.contains(text)
                        }
                    } else {
                        // Pool object matching
                        val detectedTexts = labels.map { it.text.lowercase() }
                        ObjectPool.matches(targetObjectName, detectedTexts)
                    }

                    if (matched) {
                        continuation.resume(VerificationResult.Success)
                    } else {
                        val mostConfident = labels.firstOrNull()?.text ?: "Unknown"
                        val progressMsg = "It looks like a $mostConfident. We need a $targetObjectName!"
                        continuation.resume(VerificationResult.Failure(progressMsg))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ObjectVerifier", "ML Kit execution failed", e)
                    continuation.resume(VerificationResult.Failure("Labeling error. Make sure Google Play Services is updated."))
                }
        }
    }

    // Calculates average luminance of pixels in Bitmap
    private fun calculateBrightness(bitmap: Bitmap): Float {
        var sum = 0L
        val width = bitmap.width
        val height = bitmap.height
        val totalCount = (width / 4) * (height / 4)
        if (totalCount == 0) return 100f

        for (x in 0 until width step 4) {
            for (y in 0 until height step 4) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                // Standard relative luminance calculations (0.299R + 0.587G + 0.114B)
                val luma = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                sum += luma
            }
        }
        return sum.toFloat() / totalCount
    }

    // Calculates horizontal adjacent variance to measure focus
    private fun calculateVibrancyBlurScore(bitmap: Bitmap): Float {
        var diffSum = 0.0
        val width = bitmap.width
        val height = bitmap.height
        val totalCount = (width / 4) * (height / 4)
        if (totalCount == 0) return 10f

        var lastLuma = 0
        for (y in 0 until height step 4) {
            for (x in 0 until width step 4) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                val luma = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                
                if (x > 0) {
                    diffSum += abs(luma - lastLuma)
                }
                lastLuma = luma
            }
        }
        return (diffSum / totalCount).toFloat()
    }
}
