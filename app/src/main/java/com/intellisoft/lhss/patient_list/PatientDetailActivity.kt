package com.intellisoft.lhss.patient_list

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss.screening.ScreeningAdapter
import com.intellisoft.lhss.screening.ScreeningViewModel
import com.google.android.fhir.FhirEngine
import com.intellisoft.lhss.R
import com.intellisoft.lhss.databinding.ActivityPatientDetailBinding
import com.intellisoft.lhss.fhir.FhirApplication
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.screening.Screening
import com.intellisoft.lhss.utils.AppUtils
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModel
import com.intellisoft.lhss.viewmodel.PatientDetailsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PatientDetailActivity : AppCompatActivity() {

    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
//    private val args: PatientDetailActivityArgs by navArgs()
    private lateinit var binding: ActivityPatientDetailBinding
    val formatterClass = FormatterClass()
    private val viewModel: ScreeningViewModel by viewModels()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var patientId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_detail)

        patientId = formatterClass.getSharedPref("patientId", this).toString()


        binding = ActivityPatientDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar) // Assuming you have a Toolbar with id 'toolbar' in your layout
        setSupportActionBar(toolbar)

        fhirEngine = FhirApplication.fhirEngine(this)
        patientDetailsViewModel =
            ViewModelProvider(
                this,
                PatientDetailsViewModelFactory(this.application, fhirEngine, patientId),
            )
                .get(PatientDetailsViewModel::class.java)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        patientDetailsViewModel.livePatientData.observe(this) {
            binding.apply {
                tvName.text = it.name
                tvGender.text = AppUtils().capitalizeFirstLetter(it.gender)
                tvDob.text = it.dob
                tvContact.text = it.contact_name
                tvPhone.text = it.contact_phone
                tvContactGender.text = it.contact_gender
            }
        }
        patientDetailsViewModel.getPatientDetailData()

        binding.btnScreening.setOnClickListener {

            formatterClass.saveSharedPref("patientId", patientId, this)
            formatterClass.saveSharedPref("questionnaire", "screening.json", this)

            val intent = Intent(this, Screening::class.java)
            startActivity(intent)
        }

        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)

        getVaccinations()
    }

    private fun getVaccinations() {

        CoroutineScope(Dispatchers.IO).launch {
            val encounterList = viewModel.getEncounterList(patientId)
            val vaccineAdapter = ScreeningAdapter(encounterList,this@PatientDetailActivity)
            CoroutineScope(Dispatchers.Main).launch {
                binding.recyclerView.adapter = vaccineAdapter
            }

        }




    }
}