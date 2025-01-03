package com.intellisoft.lhss25.clinical_info.viewmodel

import android.app.Application
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.intellisoft.lhss25.R

class ClinicalLayoutListViewModel(application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {

    fun getLayoutList(): List<Layout> {
        return Layout.values().toList()
    }

    enum class Layout(
        @DrawableRes val iconId: Int,
        val textId: String,
    ) {
        TB_TREATMENT(R.drawable.ic_action_next_kin, "TB Treatment"),
        HIV_STATUS_TREATMENT(R.drawable.ic_action_next_kin, "HIV Status and Treatment"),
        LABORATORY_RESULTS(R.drawable.ic_action_next_kin, "Laboratory Results"),
        DST(R.drawable.ic_action_next_kin, "DST/Cultures/LPA"),
        DR_TB_FOLLOW_UP_TEST(R.drawable.ic_action_next_kin, "DR TB Follow Up Test"),
    }
}