package com.intellisoft.chanjoke.network_request

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.fhir.data.DbSignIn
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.UrlData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class RetrofitCallsAuthentication {


    fun loginUser(context: Context, dbSignIn: DbSignIn){

        CoroutineScope(Dispatchers.Main).launch {

            val job = Job()
            CoroutineScope(Dispatchers.IO + job).launch {
                starLogin(context, dbSignIn)
            }.join()
        }

    }
    private suspend fun starLogin(context: Context, dbSignIn: DbSignIn) {


        val job1 = Job()
        CoroutineScope(Dispatchers.Main + job1).launch {

            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait..")
            progressDialog.setMessage("Authentication in progress..")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            var messageToast = ""
            val job = Job()
            CoroutineScope(Dispatchers.IO + job).launch {

                val formatter = FormatterClass()
                val baseUrl = context.getString(UrlData.BASE_URL.message)
                val apiService = RetrofitBuilder.getRetrofit(baseUrl).create(Interface::class.java)
                try {
                    

                    val apiInterface = apiService.signInUser(dbSignIn)
                    if (apiInterface.isSuccessful){

                        val statusCode = apiInterface.code()
                        val body = apiInterface.body()

                        if (statusCode == 200 || statusCode == 201){

                            if (body != null){

                                val access_token = body.access_token
                                val expires_in = body.expires_in
                                val refresh_expires_in = body.refresh_expires_in
                                val refresh_token = body.refresh_token

                                formatter.saveSharedPref("access_token", access_token, context)
                                formatter.saveSharedPref("expires_in", expires_in.toString(), context)
                                formatter.saveSharedPref("refresh_expires_in", refresh_expires_in, context)
                                formatter.saveSharedPref("refresh_token", refresh_token, context)
                                formatter.saveSharedPref("isLoggedIn", "true", context)

                                getUserDetails(context)

                                messageToast = "Login successful.."

                                val intent = Intent(context, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                context.startActivity(intent)
                                if (context is Activity) {
                                    context.finish()
                                }

                            }else{
                                messageToast = "Error: Body is null"
                            }
                        }else{
                            messageToast = "Error: The request was not successful"
                        }
                    }else{
                        apiInterface.errorBody()?.let {
                            val errorBody = JSONObject(it.string())
                            messageToast = errorBody.getString("error")
                        }
                    }


                }catch (e: Exception){

                    Log.e("******","")
                    Log.e("******",e.toString())
                    Log.e("******","")


                    messageToast = "Cannot login user.."
                }


            }.join()
            CoroutineScope(Dispatchers.Main).launch{

                progressDialog.dismiss()
                Toast.makeText(context, messageToast, Toast.LENGTH_LONG).show()

            }

        }

    }

    fun getUser(context: Context, dbSignIn: DbSignIn){

        CoroutineScope(Dispatchers.Main).launch {

            val job = Job()
            CoroutineScope(Dispatchers.IO + job).launch {
                getUserDetails(context)
            }.join()
        }

    }
    private suspend fun getUserDetails(context: Context) {


        CoroutineScope(Dispatchers.IO).launch {

            val formatter = FormatterClass()
            val baseUrl = context.getString(UrlData.BASE_URL.message)
            val apiService = RetrofitBuilder.getRetrofit(baseUrl).create(Interface::class.java)
            try {

                val token = formatter.getSharedPref("access_token", context)
                if (token != null){
                    val apiInterface = apiService.getUserInfo("Bearer $token")
                    if (apiInterface.isSuccessful){

                        val statusCode = apiInterface.code()
                        val body = apiInterface.body()

                        if (statusCode == 200 || statusCode == 201){

                            if (body != null){

                                val user = body.user
                                if (user != null){
                                    val fullNames = user.fullNames
                                    val idNumber = user.idNumber
                                    val practitionerRole = user.practitionerRole
                                    val fhirPractitionerId = user.fhirPractitionerId
                                    val email = user.email
                                    val phone = user.phone
                                    val id = user.id

                                    formatter.saveSharedPref("practitionerFullNames", fullNames, context)
                                    formatter.saveSharedPref("practitionerIdNumber", idNumber, context)
                                    formatter.saveSharedPref("practitionerRole", practitionerRole, context)
                                    formatter.saveSharedPref("fhirPractitionerId", fhirPractitionerId, context)
                                    formatter.saveSharedPref("practitionerId", id, context)
                                    formatter.saveSharedPref("practitionerEmail", email, context)
                                    formatter.saveSharedPref("practitionerPhone", phone ?: "", context)

                                }
                            }
                        }
                    }
                }
            }catch (e: Exception){

                Log.e("******","")
                Log.e("******",e.toString())
                Log.e("******","")
            }


        }


    }



}

