/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellisoft.lhss.add_patient

import android.app.Application
import android.app.ProgressDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.lhss.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.QuestionnaireResponse

/** A fragment class to show patient registration screen. */
class AddPatientFragment : Fragment(R.layout.add_patient_fragment) {

  private val viewModel: AddPatientViewModel by viewModels()
  private var progressDialog: ProgressDialog? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setUpActionBar()
    setUpActionBar()
    setHasOptionsMenu(true)
    updateArguments()
    if (savedInstanceState == null) {
      addQuestionnaireFragment()
    }
    observePatientSaveAction()

    /** Use the provided cancel|submit buttons from the sdc library */
    childFragmentManager.setFragmentResultListener(
      QuestionnaireFragment.SUBMIT_REQUEST_KEY,
      viewLifecycleOwner,
    ) { _, _ ->
      onSubmitAction()
    }
//    childFragmentManager.setFragmentResultListener(
//      QuestionnaireFragment.CANCEL_REQUEST_KEY,
//      viewLifecycleOwner,
//    ) { _, _ ->
//      NavHostFragment.findNavController(this).navigateUp()
//    }
  }

  private fun onSubmitAction() {
    val questionnaireFragment =
      childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
    savePatient(questionnaireFragment.getQuestionnaireResponse())
  }

  private fun savePatient(questionnaireResponse: QuestionnaireResponse) {
    viewModel.savePatient(questionnaireResponse)
  }

  private fun showProgressDialog() {
    progressDialog = ProgressDialog(context)
    progressDialog?.setMessage("Loading...")
    progressDialog?.setCancelable(false)
    progressDialog?.show()
  }

  private fun getStringFromAssets(fileName: String): String {
    return requireContext().assets.open(fileName).bufferedReader().use { it.readText() }
  }

  private fun addQuestionnaireFragment() {

    childFragmentManager.commit {
      add(
        R.id.add_patient_container,
        QuestionnaireFragment.builder()
          .setQuestionnaire(getStringFromAssets("new-patient-registration-paginated.json"))
//          .setShowCancelButton(true)
          .build(),
        QUESTIONNAIRE_FRAGMENT_TAG,
      )
    }
  }

  private fun observePatientSaveAction() {
    viewModel.isPatientSaved.observe(viewLifecycleOwner) {
      if (!it) {
        Toast.makeText(requireContext(), "Inputs are missing.", Toast.LENGTH_SHORT).show()
        return@observe
      }
      Toast.makeText(requireContext(), "Patient is saved.", Toast.LENGTH_SHORT).show()
      NavHostFragment.findNavController(this).navigateUp()
    }
  }

  private fun updateArguments() {
    requireArguments()
      .putString(QUESTIONNAIRE_FILE_PATH_KEY, "new-patient-registration-paginated.json")
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
        NavHostFragment.findNavController(this).navigateUp()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun setUpActionBar() {
    (requireActivity() as AppCompatActivity).supportActionBar?.apply {
      title = requireContext().getString(R.string.add_patient)
      setDisplayHomeAsUpEnabled(true)
    }
  }

  companion object {
    const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
    const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
  }
}
