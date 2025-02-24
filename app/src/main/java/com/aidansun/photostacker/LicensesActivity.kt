package com.aidansun.photostacker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aidansun.photostacker.databinding.ActivityLicensesBinding

class LicensesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLicensesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val textView = binding.licensesText
        textView.text =
            resources.openRawResource(R.raw.licenses).bufferedReader().use { it.readText() }
    }
}
