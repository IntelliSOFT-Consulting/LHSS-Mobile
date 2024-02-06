package com.intellisoft.chanjoke.vaccine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.ActivityAdministerVaccineBinding
import com.intellisoft.chanjoke.fhir.data.FormatterClass

class AdministerVaccine : AppCompatActivity() {

    private val formatterClass = FormatterClass()
    private lateinit var binding: ActivityAdministerVaccineBinding
    private var patientId :String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdministerVaccineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar) // Assuming you have a Toolbar with id 'toolbar' in your layout
        setSupportActionBar(toolbar)

        patientId = formatterClass.getSharedPref("patientId",this)

        val bundle = Bundle()
        bundle.putString(
            AdministerVaccineFragment.QUESTIONNAIRE_FILE_PATH_KEY,
            "vaccine-administration.json")

        val vaccine = AdministerVaccineFragment()
        vaccine.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_administer_vaccine, vaccine)
            .commit()

        println(patientId)

    }


}