package com.bignerdranch.android.criminalintent

import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity

class ChooseCheckBox : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_crime_detail)

        val enableFaceDetection: CheckBox = findViewById(R.id.enableFaceDetection)
        val enableContourDetection: CheckBox = findViewById(R.id.enableContourDetection)
        val enableMeshDetection: CheckBox = findViewById(R.id.enableMeshDetection)
        val enableSelfieSegmentation: CheckBox = findViewById(R.id.enableSelfieSegmentation)

        enableFaceDetection.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableContourDetection.isChecked = false
                enableMeshDetection.isChecked = false
                enableSelfieSegmentation.isChecked = false
            }
        }

        enableContourDetection.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableFaceDetection.isChecked = false
                enableMeshDetection.isChecked = false
                enableSelfieSegmentation.isChecked = false
            }
        }

        enableMeshDetection.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableFaceDetection.isChecked = false
                enableContourDetection.isChecked = false
                enableSelfieSegmentation.isChecked = false
            }
        }

        enableSelfieSegmentation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableFaceDetection.isChecked = false
                enableContourDetection.isChecked = false
                enableMeshDetection.isChecked = false
            }
        }
    }
}
