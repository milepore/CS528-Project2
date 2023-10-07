package com.bignerdranch.android.criminalintent

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.bignerdranch.android.criminalintent.databinding.FragmentCrimeDetailBinding

class ChooseCheckBox : Fragment(R.layout.fragment_crime_detail) {

    private var _binding: FragmentCrimeDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCrimeDetailBinding.bind(view)

        val allCheckBoxes = arrayOf(
            binding.enableFaceDetection,
            binding.enableContourDetection,
            binding.enableMeshDetection,
            binding.enableSelfieSegmentation
        )

        for (checkBox in allCheckBoxes) {
            checkBox.setOnCheckedChangeListener { currentCheckBox, isChecked ->
                if (isChecked) {
                    for (otherCheckBox in allCheckBoxes) {
                        if (otherCheckBox != currentCheckBox) {
                            otherCheckBox.isChecked = false
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
