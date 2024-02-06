/*
 * Copyright 2021-2023 Google LLC
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

package com.intellisoft.chanjoke.patient_list

import android.content.res.Resources
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.databinding.PatientListItemViewBinding
import timber.log.Timber
import java.time.LocalDate
import java.time.Period

class PatientItemViewHolder(binding: PatientListItemViewBinding) :
    RecyclerView.ViewHolder(binding.root) {
    //  private val statusView: ImageView = binding.status
    private val nameView: TextView = binding.name
    private val idNumber: TextView = binding.idNumber
    private val tvPhoneNumber: TextView = binding.tvPhoneNumber
    val btnView: Button = binding.btnView

    private val viewName: TextView = binding.viewName
    private val viewId: TextView = binding.viewId
    private val viewPhoneNumber: TextView = binding.viewPhoneNumber

    fun bindTo(
        patientItem: PatientListViewModel.PatientItem,
        onItemClicked: (PatientListViewModel.PatientItem) -> Unit,
    ) {
        this.nameView.text = patientItem.name
        this.idNumber.text =
            patientItem.identification//getFormattedAge(patientItem, idNumber.context.resources)
        this.tvPhoneNumber.text = patientItem.phone

        this.viewName.text = "Name:"
        this.viewId.text = "ID Number"
        this.viewPhoneNumber.text = "Phone number"
        this.itemView.setOnClickListener {
            onItemClicked(patientItem)

        }

    }




    /** The new ui just shows shortened id with just last 3 characters. */
    private fun getTruncatedId(patientItem: PatientListViewModel.PatientItem): String {
        return patientItem.resourceId.takeLast(3)
    }
}
