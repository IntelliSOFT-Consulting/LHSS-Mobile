package com.intellisoft.lhss.screening

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.lhss.MainActivity
import com.intellisoft.lhss.R
import com.intellisoft.lhss.fhir.data.FormatterClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Screening : AppCompatActivity() {

    var questionnaireJsonString: String? = null
    private val viewModel: ScreeningViewModel by viewModels()
    val formatterClass = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screening)

        setSupportActionBar(findViewById(R.id.my_toolbar))

        val questionnaire = formatterClass.getSharedPref("questionnaire", this)

        // Perform the time-consuming operation in the background using lifecycleScope
        lifecycleScope.launch(Dispatchers.IO) {
            val questionnaireJsonString = getStringFromAssets(questionnaire.toString())

            // Use lifecycleScope to execute the UI-related code on the main thread
            launch(Dispatchers.Main) {
                val questionnaireFragment = QuestionnaireFragment.builder()
                    .setQuestionnaire(questionnaireJsonString!!)
                    .build()

                addQuestionnaireFragment(questionnaireFragment)
            }
        }


    }

    private fun addQuestionnaireFragment(questionnaireFragment: QuestionnaireFragment) {
        if (!isFinishing) {
            // Check if the activity is not finishing before making UI changes
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container_view, questionnaireFragment)
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.submit_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.submit -> {
            submitQuestionnaire()
            true
        }

        else -> {
            // The user's action isn't recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }




    private fun submitQuestionnaire() {

        // Get a questionnaire response
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
                as QuestionnaireFragment
        val questionnaireResponse = fragment.getQuestionnaireResponse()

        // Print the response to the log
        val jsonParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
        val questionnaireResponseString =
            jsonParser.encodeResourceToString(questionnaireResponse)

        val patientId = formatterClass.getSharedPref("patientId", this)

        viewModel.saveScreenerEncounter(questionnaireResponse, patientId.toString())

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun getStringFromAssets(fileName: String): String {
        return assets.open(fileName).bufferedReader().use { it.readText() }
    }
}