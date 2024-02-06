package com.intellisoft.chanjoke.patient_list

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.add_patient.AddPatientFragment.Companion.QUESTIONNAIRE_FILE_PATH_KEY
import com.intellisoft.chanjoke.databinding.FragmentPatientListBinding
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.viewmodel.MainActivityViewModel
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.sync.SyncJobStatus
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt

class PatientListFragment : Fragment() {

    private var formatterClass = FormatterClass()
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientListViewModel: PatientListViewModel
    private lateinit var searchView: SearchView
    private lateinit var topBanner: LinearLayout
    private lateinit var syncStatus: TextView
    private lateinit var syncPercent: TextView
    private lateinit var syncProgress: ProgressBar
    private var _binding: FragmentPatientListBinding? = null
    private val binding
        get() = _binding!!

    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    private var isSearched = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPatientListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isSearched = false

        val patientListAction = formatterClass.getSharedPref("patientListAction", requireContext())
        if (patientListAction != null && patientListAction == NavigationDetails.ADMINISTER_VACCINE.name) {
            binding.patientListContainer.linearVaccinationVenue.visibility = View.VISIBLE
            formatterClass.saveSharedPref("isSelectedVaccinationVenue", "true", requireContext())
            formatterClass.deleteSharedPref("patientListAction", requireContext())

        }

        clearSharedPref()


        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
//            title = resources.getString(R.string.title_patient_list)
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
            setHomeAsUpIndicator(null)
        }
        fhirEngine = FhirApplication.fhirEngine(requireContext())
        patientListViewModel =
            ViewModelProvider(
                this,
                PatientListViewModel.PatientListViewModelFactory(
                    requireActivity().application,
                    fhirEngine
                ),
            )[PatientListViewModel::class.java]
        val recyclerView: RecyclerView = binding.patientListContainer.patientList
//        val adapter = PatientItemRecyclerViewAdapter(this::onPatientItemClicked)
//        recyclerView.adapter = adapter
//        recyclerView.addItemDecoration(
//            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL).apply {
//                setDrawable(ColorDrawable(Color.LTGRAY))
//            },
//        )
        patientListViewModel.liveSearchedPatients.observe(viewLifecycleOwner) {

            Timber.d("Submitting ${it.count()} patient records")
            val patientList = ArrayList(it)

            if (patientList.isEmpty() && isSearched) {
                //Display client not found
                val noPatientDialog = NoPatientDialog(requireContext())
                noPatientDialog.show()
            }

            val patientAdapter = PatientAdapter(patientList, requireContext())
            recyclerView.adapter = patientAdapter


        }

        patientListViewModel.patientCount.observe(viewLifecycleOwner) {
            binding.patientListContainer.patientCount.text = "$it Patient(s)"
        }
        binding.patientListContainer.radioGrpVenue.setOnCheckedChangeListener { group, checkedId ->
            // checkedId is the RadioButton ID that is checked
            if (checkedId != -1) {
                val selectedRadioButton = view.findViewById<RadioButton>(checkedId)
                val selectedText: String = selectedRadioButton.text.toString()
                formatterClass.saveSharedPref("selectedVaccinationVenue", selectedText, requireContext())
                // Do something with the selectedRadioButton or selectedText
            } else {
                // No radio button is selected
                // Handle this case as needed
            }
        }


        searchView = binding.patientListContainer.search
        topBanner = binding.syncStatusContainer.linearLayoutSyncStatus
        syncStatus = binding.syncStatusContainer.tvSyncingStatus
        syncPercent = binding.syncStatusContainer.tvSyncingPercent
        syncProgress = binding.syncStatusContainer.progressSyncing
        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    isSearched = true
                    patientListViewModel.searchPatientsByName(newText)
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    isSearched = true
                    patientListViewModel.searchPatientsByName(query)
                    return true
                }
            },
        )
        searchView.setOnQueryTextFocusChangeListener { view, focused ->
            if (!focused) {
                // hide soft keyboard
                (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (searchView.query.isNotEmpty()) {
                            searchView.setQuery("", true)
                        } else {
                            isEnabled = false
                            activity?.onBackPressed()
                        }
                    }
                },
            )

        binding.apply {
            addPatient.setOnClickListener { onAddPatientClick() }
            addPatient.setColorFilter(Color.WHITE)
        }
        setHasOptionsMenu(true)

        lifecycleScope.launch {
            mainActivityViewModel.pollState.collect {
                Timber.d("onViewCreated: pollState Got status $it")
                when (it) {
                    is SyncJobStatus.Started -> {
                        Timber.i("Sync: ${it::class.java.simpleName}")
//                        fadeInTopBanner(it)
                    }

                    is SyncJobStatus.InProgress -> {
                        Timber.i("Sync: ${it::class.java.simpleName} with data $it")
//                        fadeInTopBanner(it)
                    }

                    is SyncJobStatus.Finished -> {
                        Timber.i("Sync: ${it::class.java.simpleName} at ${it.timestamp}")
                        patientListViewModel.searchPatientsByName(
                            searchView.query.toString().trim()
                        )
                        mainActivityViewModel.updateLastSyncTimestamp()

                    }

                    is SyncJobStatus.Failed -> {
                        Timber.i("Sync: ${it::class.java.simpleName} at ${it.timestamp}")
                        patientListViewModel.searchPatientsByName(
                            searchView.query.toString().trim()
                        )
                        mainActivityViewModel.updateLastSyncTimestamp()

                    }

                    else -> {
                        Timber.i("Sync: Unknown state.")
                        patientListViewModel.searchPatientsByName(
                            searchView.query.toString().trim()
                        )
                        mainActivityViewModel.updateLastSyncTimestamp()

                    }
                }
            }
        }

//        val locations = patientListViewModel.retrieveLocations()
//        Timber.e("Location::::::")
//        locations.forEach {
//            Timber.e("Location:::::: -> ${it.name}")
//        }

    }

    private fun clearSharedPref() {
        //Clear the vaccines
        formatterClass.clearVaccineShared(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()

        formatterClass.deleteSharedPref("selectedVaccinationVenue", requireContext())
        formatterClass.deleteSharedPref("isSelectedVaccinationVenue", requireContext())

        _binding = null
    }

    private fun onPatientItemClicked(patientItem: PatientListViewModel.PatientItem) {

        formatterClass.saveSharedPref("patientId", patientItem.resourceId, requireContext())
        formatterClass.saveSharedPref("patientDob", patientItem.dob.toString(), requireContext())
        val action =
            PatientListFragmentDirections.actionPatientListToPatientDetailActivity(patientItem.resourceId)

        findNavController().navigate(action)
    }

    private fun onAddPatientClick() {

        val bundle = Bundle()
        bundle.putString(QUESTIONNAIRE_FILE_PATH_KEY, "new-patient-registration-paginated.json")
        findNavController().navigate(R.id.addPatientFragment, bundle)

    }

}