package com.intellisoft.lhss.utils

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.button.MaterialButton
import com.intellisoft.lhss.R
import com.intellisoft.lhss.detail.PatientDetailActivity
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.fhir.data.NavigationDetails
import com.intellisoft.lhss.vaccine.stock_management.VaccineStockManagement

class BlurBackgroundDialog(
    private val fragment: Fragment, context: Context
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_blur_background)
        // Set window attributes to cover the entire screen
        window?.apply {
            attributes?.width = WindowManager.LayoutParams.MATCH_PARENT
            attributes?.height = WindowManager.LayoutParams.MATCH_PARENT

            // Make the dialog cover the status bar and navigation bar
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )

            setBackgroundDrawableResource(android.R.color.transparent) // Set a transparent background
        }
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        window?.setBackgroundDrawableResource(R.color.colorPrimary)
        var valueText = when (FormatterClass().getSharedPref("vaccinationFlow", context)) {
            "addAefi" -> {
                "The AEFI details have been recorded successfully."
            }

            "createVaccineDetails" -> {
                "The vaccine details have been recorded successfully."
            }

            "updateVaccineDetails" -> {
                "Record has been updated successfully!"
            }

            else -> {
                "Record has been captured successfully!"
            }
        }
        if (FormatterClass().getSharedPref("isRegistration", context) == "true") {
            valueText = "Patient added successfully!"
        }

        findViewById<TextView>(R.id.info_textview).apply {
            text = valueText
        }
        val closeMaterialButton = findViewById<MaterialButton>(R.id.closeMaterialButton)
        closeMaterialButton.setOnClickListener {
            dismiss()
            val patientId = FormatterClass().getSharedPref("patientId", context)

            val isRegistration = FormatterClass().getSharedPref("isRegistration", context)
            if (isRegistration != null) {
                if (isRegistration == "true") {
//                    val intent = Intent(context, PatientDetailActivity::class.java)
//                    intent.putExtra("patientId", patientId)
//                    context.startActivity(intent)
                    FormatterClass().deleteSharedPref("isRegistration", context)
                    NavHostFragment.findNavController(fragment).navigateUp()
                }
            } else {
                val vaccinationFlow =
                    FormatterClass().getSharedPref("isVaccineAdministered", context)
                if (vaccinationFlow == "stockManagement") {
                    val intent = Intent(context, VaccineStockManagement::class.java)
                    intent.putExtra("functionToCall", NavigationDetails.ADMINISTER_VACCINE.name)
                    intent.putExtra("patientId", patientId)
                    context.startActivity(intent)
                    FormatterClass().deleteSharedPref("isVaccineAdministered", context)
                } else {
                    val intent = Intent(context, PatientDetailActivity::class.java)
                    intent.putExtra("patientId", patientId)
                    context.startActivity(intent)
                }

            }
        }
    }
}