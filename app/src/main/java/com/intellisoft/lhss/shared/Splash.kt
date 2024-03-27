package com.intellisoft.lhss.shared

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import com.google.android.fhir.sync.Sync
import com.intellisoft.lhss.MainActivity
import com.intellisoft.lhss.databinding.ActivitySplashBinding
import com.intellisoft.lhss.fhir.data.FhirSyncWorker
import com.intellisoft.lhss.fhir.data.FormatterClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Splash : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        val fullName = "Mark N Anthony "
        val parts = fullName.trim().split(" ")
        if (parts.isNotEmpty()){
            val firstName = parts.getOrNull(0) ?: ""
            val middleNameParts = if (parts.size > 1){
                parts.subList(1, parts.size - 1)
            }else{
                parts.subList(1, parts.size)
            }

            val middleName = if (middleNameParts.isNotEmpty()) middleNameParts.joinToString(" ") else ""
            val lastName = parts.last()

        }

        Handler().postDelayed({
            if (FormatterClass().getSharedPref("isLoggedIn", this@Splash) == "true") {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish()
            }

        }, 1000)
    }
}