package com.intellisoft.lhss25

import android.app.Application
import android.text.format.DateFormat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import com.google.android.fhir.sync.CurrentSyncJobStatus
import com.google.android.fhir.sync.PeriodicSyncConfiguration
import com.google.android.fhir.sync.PeriodicSyncJobStatus
import com.google.android.fhir.sync.RepeatInterval
import com.google.android.fhir.sync.Sync
import com.intellisoft.lhss25.fhir.FhirSyncWorker
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class SplashViewModel(application: Application) : AndroidViewModel(application){

    private val _lastSyncTimestampLiveData = MutableLiveData<String>()
    val lastSyncTimestampLiveData: LiveData<String>
        get() = _lastSyncTimestampLiveData

    private val _oneTimeSyncTrigger =
        MutableSharedFlow<Boolean>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    val pollPeriodicSyncJobStatus: SharedFlow<PeriodicSyncJobStatus> =
        Sync.periodicSync<FhirSyncWorker>(
            application.applicationContext,
            periodicSyncConfiguration =
            PeriodicSyncConfiguration(
                syncConstraints = Constraints.Builder().build(),
                repeat = RepeatInterval(interval = 15, timeUnit = TimeUnit.MINUTES),
            ),
        )
            .shareIn(viewModelScope, SharingStarted.Eagerly, 10)

    val pollState: SharedFlow<CurrentSyncJobStatus> =
        _oneTimeSyncTrigger
            .flatMapLatest {
                Sync.oneTimeSync<FhirSyncWorker>(context = application.applicationContext)
            }
            .map { it }
            .shareIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun triggerOneTimeSync() {
        viewModelScope.launch { _oneTimeSyncTrigger.emit(true) }
    }

    /** Emits last sync time. */
    fun updateLastSyncTimestamp(lastSync: OffsetDateTime? = null) {
        val formatter =
            DateTimeFormatter.ofPattern(
                if (DateFormat.is24HourFormat(getApplication())) formatString24 else formatString12,
            )
        _lastSyncTimestampLiveData.value =
            lastSync?.let { it.toLocalDateTime()?.format(formatter) ?: "" }
                ?: Sync.getLastSyncTimestamp(getApplication())?.toLocalDateTime()?.format(formatter) ?: ""
    }

    companion object {
        private const val formatString24 = "yyyy-MM-dd HH:mm:ss"
        private const val formatString12 = "yyyy-MM-dd hh:mm:ss a"
    }


}