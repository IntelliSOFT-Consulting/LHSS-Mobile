package com.intellisoft.lhss.screening

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss.R
import com.intellisoft.lhss.databinding.ActivityReferralScreenBinding
import com.intellisoft.lhss.fhir.data.DbObservation
import com.intellisoft.lhss.fhir.data.FormatterClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReferralScreen : AppCompatActivity() {

    val formatterClass = FormatterClass()
    var patientId : String? = ""
    var encounterId : String? = ""
    private val viewModel: ScreeningViewModel by viewModels()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var binding: ActivityReferralScreenBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReferralScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = findViewById<Toolbar>(R.id.toolbar) // Assuming you have a Toolbar with id 'toolbar' in your layout
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        encounterId = formatterClass.getSharedPref("encounterId", this)
        patientId = formatterClass.getSharedPref("patientId", this)

        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)

        binding.btnDoReferral.setOnClickListener {
            formatterClass.saveSharedPref("questionnaire", "referral.json", this)

            val intent = Intent(this, Screening::class.java)
            startActivity(intent)
        }

        val encounterList = viewModel.getCarePlan(patientId.toString(), encounterId.toString())

        binding.btnViewReferral.setOnClickListener {
            createDialog(encounterList)
        }

        if (encounterList.isNotEmpty()){
            binding.btnViewReferral.visibility = View.VISIBLE
            binding.btnDoReferral.visibility = View.GONE
        }else{
            binding.btnViewReferral.visibility = View.GONE
            binding.btnDoReferral.visibility = View.VISIBLE
        }


        getVaccinations()
    }
    private fun createDialog(info: ArrayList<DbObservation>) {

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage(info.toString())

        builder.setTitle("Referral Results")
        builder.setCancelable(false)
        builder.setPositiveButton("Close",
            DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                // When the user click yes button then app will close
//                finish()
            } as DialogInterface.OnClickListener)

        builder.setNegativeButton("Close",
            DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->
                dialog.cancel()
            } as DialogInterface.OnClickListener)

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }


    private fun getVaccinations() {

        CoroutineScope(Dispatchers.IO).launch {
            val encounterList = viewModel.getObservations(patientId.toString(), encounterId.toString())

            val vaccineAdapter = ScreeningAdapter(encounterList,this@ReferralScreen)
            CoroutineScope(Dispatchers.Main).launch {
                binding.recyclerView.adapter = vaccineAdapter
            }

        }

    }
}