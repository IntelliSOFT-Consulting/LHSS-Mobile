package com.intellisoft.chanjoke.shared

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbSignIn
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.network_request.RetrofitCallsAuthentication

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

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

//            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
//
//                val dbSignIn = DbSignIn(username, password)
//                retrofitCallsAuthentication.loginUser(this, dbSignIn)
//
//            } else {
//                etUsername.error = "Please Enter Username"
//                etPassword.error = "Please Enter Password"
//            }


        }
        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }
    }
}