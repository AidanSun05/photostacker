package com.aidansun.photostacker

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.OutputStream

object GalleryManager {
    // Saves a bitmap image to the device's gallery.
    fun saveToGallery(bitmap: Bitmap, contentResolver: ContentResolver) {
        val filename = "Image_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        val resolver = contentResolver
        imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        fos = imageUri?.let { resolver.openOutputStream(it) }

        fos?.use { outputStream ->
            // Compress and write the bitmap data into the output stream
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
    }
}
