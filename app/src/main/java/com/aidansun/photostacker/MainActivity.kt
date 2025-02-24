package com.aidansun.photostacker

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.aidansun.photostacker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var preferences: SharedPreferences

    private lateinit var binding: ActivityMainBinding
    private lateinit var controller: LifecycleCameraController
    private val picturesHandler = Handler(Looper.getMainLooper())
    private lateinit var picturesAction: Runnable

    private var currentLensFacing = CameraSelector.LENS_FACING_BACK // Default to back camera
    private var images = ArrayList<Bitmap>()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted; open the camera.
                startCamera()
            } else {
                // Permission denied.
                Toast.makeText(
                    this,
                    "Camera permission is required to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        requestCameraPermission()

        picturesAction = object : Runnable {
            override fun run() {
                val numPictures = preferences.getInt("numPictures", 5)
                val delay = preferences.getInt("delay", 5).toLong() * 1000

                if (images.size < numPictures) {
                    takePicture()
                    picturesHandler.postDelayed(this, delay)
                } else {
                    switchToImageView()
                }
            }
        }

        setCameraEnabled(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        picturesHandler.removeCallbacks(picturesAction)
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted.
                startCamera()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // Provide an explanation to the user *asynchronously*.
                Toast.makeText(this, "Camera access is needed to take pictures", Toast.LENGTH_LONG)
                    .show()
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }

            else -> {
                // Directly request the permission.
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        controller = LifecycleCameraController(this)
        controller.bindToLifecycle(this)

        // Default to the back camera
        controller.setCameraSelector(
            CameraSelector.Builder()
                .requireLensFacing(currentLensFacing)
                .build()
        )

        // Preview
        val previewView: PreviewView = findViewById(R.id.textureView)
        previewView.setController(controller)
        controller.isPinchToZoomEnabled = true
        controller.isTapToFocusEnabled = true

        binding.shutterView.alpha = 0.0f
    }

    private fun takePicture() {
        val callback = object : ImageCapture.OnImageCapturedCallback() {
            override fun onError(exc: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
            }

            override fun onCaptureSuccess(image: ImageProxy) {
                capturedImage(image)
            }
        }
        controller.takePicture(ContextCompat.getMainExecutor(this), callback)
    }

    private fun capturedImage(image: ImageProxy) {
        // Rotate bitmap to match device and camera rotation
        val bitmap = imageProxyToBitmap(image)
        val matrix = Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) }
        val correctedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )

        runOnUiThread {
            shutterEffect()
            images.add(correctedBitmap)
        }

        val saveToGallery = preferences.getBoolean("saveToGallery", false)
        if (saveToGallery) {
            GalleryManager.saveToGallery(correctedBitmap, contentResolver)
        }

        image.close() // Close after processing
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun switchToImageView() {
        setCameraEnabled(true)
        BitmapHolder.bitmaps = images
        startActivity(Intent(this, ImageDisplayActivity::class.java))
    }

    private fun setCameraEnabled(enabled: Boolean) {
        binding.flipButton.isVisible = enabled
        binding.cameraButton.isVisible = enabled
        binding.stopButton.isVisible = !enabled
        binding.settingsButton.isVisible = enabled
    }

    private fun shutterEffect() {
        // Set the overlay to visible and fully opaque
        binding.shutterView.apply {
            alpha = 1f
            visibility = View.VISIBLE
            // Animate the alpha to 0 (fade out) quickly to simulate a shutter effect
            animate()
                .alpha(0f)
                .setDuration(250)
                .withEndAction { visibility = View.GONE }
                .start()
        }
    }

    fun flipButtonClick(@Suppress("unused") view: View) {
        currentLensFacing = if (currentLensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }

        // Update camera selector
        controller.setCameraSelector(
            CameraSelector.Builder()
                .requireLensFacing(currentLensFacing)
                .build()
        )
    }

    fun cameraButtonClick(@Suppress("unused") view: View) {
        setCameraEnabled(false)
        picturesHandler.post(picturesAction)
    }

    fun stopButtonClick(@Suppress("unused") view: View) {
        setCameraEnabled(true)
        picturesHandler.removeCallbacks(picturesAction)
        images.clear()
    }

    fun settingsButtonClick(@Suppress("unused") view: View) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    companion object {
        // Used to load the 'photostacker' library on application startup.
        init {
            System.loadLibrary("photostacker")
        }

        private const val TAG = "PhotoStacker"
    }
}
