package com.intellisoft.chanjoke.vaccine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intellisoft.chanjoke.R

class BottomSheetDialog : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_dialog, container, false)

        val btnAdministerVaccine = view.findViewById<Button>(R.id.btnAdministerVaccine)
        val btnContraindications = view.findViewById<Button>(R.id.btnContraindications)

        btnContraindications.setOnClickListener {
            Toast.makeText(activity, "Contra Shared", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        btnAdministerVaccine.setOnClickListener {
            Toast.makeText(activity, "Administer Shared", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        return view
    }
}