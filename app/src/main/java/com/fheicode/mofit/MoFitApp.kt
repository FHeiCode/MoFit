package com.fheicode.mofit

import android.app.Application
import com.fheicode.mofit.data.MoFitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File

class MoFitApp : Application() {
    lateinit var repository: MoFitRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = MoFitRepository(
            file = File(filesDir, "mofit-data.json"),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
    }
}
