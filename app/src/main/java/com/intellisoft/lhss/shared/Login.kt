package com.intellisoft.lhss.shared

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.intellisoft.lhss.MainActivity
import com.intellisoft.lhss.R
import com.intellisoft.lhss.fhir.data.DbSignIn
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.network_request.RetrofitCallsAuthentication

class Login : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private var retrofitCallsAuthentication = RetrofitCallsAuthentication()
    private var formatterClass = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)

        formatterClass.practionerInfoShared(this)

        /**
         * TODO: This is dummy login workflow
         */

        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {

            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {

                val dbSignIn = DbSignIn(username, password)
                retrofitCallsAuthentication.loginUser(this, dbSignIn)

            } else {
                etUsername.error = "Please Enter Username"
                etPassword.error = "Please Enter Password"
            }


        }
        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }
    }
}