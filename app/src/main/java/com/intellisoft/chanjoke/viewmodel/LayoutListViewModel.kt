package com.intellisoft.chanjoke.viewmodel


import android.app.Application
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.intellisoft.chanjoke.R

class LayoutListViewModel(application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {

    fun getLayoutList(): List<Layout> {
        return Layout.values().toList()
    }

    enum class Layout(
        @DrawableRes val iconId: Int,
        val textId: String,
    ) {
        HEALTH_FACILITIES(R.drawable.ic_action_health, "Health Facilities"),
        REGISTER_CLIENT(R.drawable.register, "Register Client"),
    }
}
