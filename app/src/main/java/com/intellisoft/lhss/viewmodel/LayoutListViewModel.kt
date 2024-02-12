package com.intellisoft.lhss.viewmodel


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
        SEARCH_PATIENT(R.drawable.search, "Search Patient"),
        REGISTER_CLIENT(R.drawable.register, "Register Client"),
        HEALTH_FACILITIES(R.drawable.ic_action_health, "Referrals"),
    }
}
