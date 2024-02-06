package com.intellisoft.chanjoke.vaccine.stock_management

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.detail.PatientDetailActivity
import com.intellisoft.chanjoke.fhir.data.DbVaccineStockDetails
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.vaccine.AdministerVaccineViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Arrays
import java.util.Locale

class VaccineStockManagement : AppCompatActivity() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerView: RecyclerView
    private var patientId = ""
    private var formatterClass = FormatterClass()
    private val administerVaccineViewModel: AdministerVaccineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vaccine_stock_management)

        patientId = formatterClass.getSharedPref("patientId", this).toString()
        val toolBar=findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolBar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        recyclerView = findViewById(R.id.recyclerView)
        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        findViewById<MaterialButton>(R.id.btnNext).setOnClickListener {

            createImmunizationRecommendation()

            val intent = Intent(this, PatientDetailActivity::class.java)
            intent.putExtra("patientId", patientId)
            startActivity(intent)
        }
        findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {

            val intent = Intent(this, PatientDetailActivity::class.java)
            intent.putExtra("patientId", patientId)
            startActivity(intent)
        }

        getStockManagement()

    }

    private fun createImmunizationRecommendation() {

        administerVaccineViewModel.createImmunizationRecommendation(this)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun getStockManagement() {

        CoroutineScope(Dispatchers.IO).launch {

            val administeredProduct =
                formatterClass.getSharedPref("administeredProduct", this@VaccineStockManagement)
            val vaccinationTargetDisease = formatterClass.getSharedPref(
                "vaccinationTargetDisease",
                this@VaccineStockManagement
            )

            if (administeredProduct != null && vaccinationTargetDisease != null) {
                val stockList = formatterClass.saveStockValue(
                    administeredProduct,
                    vaccinationTargetDisease,
                    this@VaccineStockManagement
                )
                val dbVaccineStockDetailsList = ArrayList<DbVaccineStockDetails>()
                for (i in stockList) {
                    val dbVaccineStockDetails = DbVaccineStockDetails(i.value, i.name)
                    dbVaccineStockDetailsList.add(dbVaccineStockDetails)
                }
                val vaccineStockAdapter =
                    VaccineStockAdapter(dbVaccineStockDetailsList, this@VaccineStockManagement)
                CoroutineScope(Dispatchers.Main).launch {
                    recyclerView.adapter = vaccineStockAdapter
                }
            }

        }

    }

}