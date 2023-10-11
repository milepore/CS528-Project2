package com.bignerdranch.android.criminalintent

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.CheckBox
import com.bignerdranch.android.criminalintent.databinding.FragmentCrimeDetailBinding

class ChooseCheckBox(checkboxes : Array<CheckBox>)  {

    val allCheckBoxes : Array<CheckBox>

    init {
        allCheckBoxes = checkboxes

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
}
