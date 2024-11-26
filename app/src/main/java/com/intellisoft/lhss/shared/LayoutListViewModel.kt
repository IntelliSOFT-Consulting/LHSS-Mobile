package com.intellisoft.lhss.shared

import android.app.Application
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.intellisoft.lhss.R

class LayoutListViewModel(application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {

    fun getLayoutList(): List<Layout> {
        return Layout.values().toList()
    }

    enum class Layout(
        @DrawableRes val iconId: Int,
        val textId: String,
    ) {
        SEARCH_PATIENT(R.drawable.ic_search_patient, "Search Patient"),
        REGISTER_CLIENT(R.drawable.ic_register_patient, "Register Patient"),
        HEALTH_FACILITIES(R.drawable.ic_referrals, "Referrals"),
        NOTIFICATION(R.drawable.ic_action_notifications, "Notifications"),
    }
}