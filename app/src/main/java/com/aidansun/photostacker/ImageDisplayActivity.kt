package com.aidansun.photostacker

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.aidansun.photostacker.databinding.ActivityImageDisplayBinding
import com.ortiz.touchview.TouchImageView
import java.util.concurrent.Executors

class ImageDisplayActivity : AppCompatActivity() {
    private lateinit var preferences: SharedPreferences
    private lateinit var binding: ActivityImageDisplayBinding
    private lateinit var view: TouchImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Hide image view while processing
        view = binding.imageDisplay
        view.isVisible = false

        var tpe = Executors.newSingleThreadExecutor()
        tpe.submit(this::process)
    }

    fun process() {
        val enableAlignment = preferences.getBoolean("enableAlignment", true)
        val numFeatures = preferences.getInt("numFeatures", 5) * 100
        val retainPercent = preferences.getInt("retainPercent", 15).toDouble() / 100.0

        // Process list of images and reuse first image's memory
        val bitmap = BitmapHolder.bitmaps[0]
        processImage(
            BitmapHolder.bitmaps.toTypedArray(),
            bitmap,
            enableAlignment,
            numFeatures,
            retainPercent
        )

        GalleryManager.saveToGallery(bitmap, contentResolver)
        runOnUiThread {
            // Show image
            view.setImageBitmap(bitmap)
            view.isVisible = true
            binding.loadingDisplay.isVisible = false

            BitmapHolder.bitmaps.clear() // Also resets internal list of MainActivity
            Toast.makeText(
                baseContext,
                "Saved image to gallery",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @Suppress("unused")
    private external fun processImage(
        bitmaps: Array<Bitmap>,
        out: Bitmap,
        enableAlignment: Boolean,
        numFeatures: Int,
        retainPercent: Double
    )
}
