package com.aidansun.photostacker

import android.graphics.Bitmap
import java.util.ArrayList

// Singleton object to share bitmaps between views.
object BitmapHolder {
    var bitmaps = ArrayList<Bitmap>()
}
