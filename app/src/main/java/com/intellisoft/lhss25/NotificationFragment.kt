package com.intellisoft.lhss25

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.fhir.FhirEngine
import com.intellisoft.lhss25.databinding.FragmentNotificationBinding
import com.intellisoft.lhss25.fhir.FhirApplication
import com.intellisoft.lhss25.shared.DbCommunicationData
import com.intellisoft.lhss25.shared.FormatterClass
import com.intellisoft.lhss25.shared.NotificationAdapter
import com.intellisoft.lhss25.shared.NotificationServiceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var fhirEngine: FhirEngine
    private lateinit var formatterClass: FormatterClass
    private val viewModel: NotificationServiceViewModel by viewModels()

    private lateinit var notificationList: ArrayList<DbCommunicationData>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        formatterClass = FormatterClass(requireContext())
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        CoroutineScope(Dispatchers.IO).launch {

            notificationList = viewModel.getCommunicationList()

            formatterClass.deleteSharedPref("","notificationBasedOn")
            formatterClass.deleteSharedPref("","communicationId")

            val formDataAdapter = NotificationAdapter(
                requireContext(),
                ArrayList(notificationList), this@NotificationFragment)

            CoroutineScope(Dispatchers.Main).launch {

                binding.recyclerView.layoutManager = LinearLayoutManager(context)
                binding.recyclerView.adapter = formDataAdapter
            }



        }

        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}