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

package com.intellisoft.chanjoke.vaccine

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.chanjoke.utils.BlurBackgroundDialog
import com.intellisoft.chanjoke.utils.ProgressDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** A fragment class to show patient registration screen. */
class AdministerVaccineFragment : Fragment(R.layout.administer_vaccine) {

    private val viewModel: AdministerVaccineViewModel by viewModels()
    private val formatterClass = FormatterClass()
    private var patientId: String? = null
    private val progressDialogFragment = ProgressDialogFragment()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpActionBar()
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = FormatterClass().getSharedPref("title", requireContext())
                ?: getString(R.string.administer_vaccine)
            setDisplayHomeAsUpEnabled(true)
        }

        setHasOptionsMenu(true)
        updateArguments()
        onBackPressed()
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }


        patientId = formatterClass.getSharedPref("patientId", requireContext())
        observeResourcesSaveAction()

        childFragmentManager.setFragmentResultListener(
            QuestionnaireFragment.SUBMIT_REQUEST_KEY,
            viewLifecycleOwner,
        ) { _, _ ->
            onSubmitAction()
        }
    }

    private fun showCancelScreenerQuestionnaireAlertDialog() {
        val alertDialog: AlertDialog? =
            activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage(getString(R.string.cancel_questionnaire_message))
                    setPositiveButton(getString(android.R.string.yes)) { _, _ ->
                        NavHostFragment.findNavController(this@AdministerVaccineFragment)
                            .navigateUp()
                    }
                    setNegativeButton(getString(android.R.string.no)) { _, _ -> }
                }
                builder.create()
            }
        alertDialog?.show()
    }

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            showCancelScreenerQuestionnaireAlertDialog()
        }
    }

    private fun observeResourcesSaveAction() {

        CoroutineScope(Dispatchers.IO).launch {

            CoroutineScope(Dispatchers.Main).launch {
                viewModel.isResourcesSaved.observe(viewLifecycleOwner) {
                    if (progressDialogFragment.isVisible) {
                        progressDialogFragment.dismiss()
                    }
                    if (!it) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.inputs_missing),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return@observe
                    }
                    val blurBackgroundDialog =
                        BlurBackgroundDialog(this@AdministerVaccineFragment, requireContext())
                    blurBackgroundDialog.show()

                }
            }

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                showCancelScreenerQuestionnaireAlertDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setUpActionBar() {
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Administer Vaccine"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun updateArguments() {

        val questionnaireJson = formatterClass.getSharedPref("questionnaireJson", requireContext())

        requireArguments()
            .putString(QUESTIONNAIRE_FILE_PATH_KEY, questionnaireJson)
    }

    private fun addQuestionnaireFragment() {
        childFragmentManager.commit {
            replace(
                R.id.administerVaccine,
                QuestionnaireFragment.builder()
                    .setQuestionnaire(viewModel.questionnaire)
                    .showReviewPageBeforeSubmit(true) //Show preview page
                    .build(),
                QUESTIONNAIRE_FRAGMENT_TAG,
            )
        }
    }

    private fun onSubmitAction() {
        progressDialogFragment.show(requireFragmentManager(), "progressDialog")
        val questionnaireFragment =
            childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
        viewModel.saveScreenerEncounter(
            questionnaireFragment.getQuestionnaireResponse(),
            patientId.toString(),
        )

    }


    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }
}
