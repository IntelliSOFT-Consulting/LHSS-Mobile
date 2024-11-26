package com.intellisoft.lhss.network_request

import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellisoft.lhss.R
import com.intellisoft.lhss.fhir.Constants
import com.intellisoft.lhss.shared.DbResponseError
import com.intellisoft.lhss.shared.DbSignIn
import com.intellisoft.lhss.shared.FormatterClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import timber.log.Timber

class RetrofitCallsAuthentication {


    fun loginUser(context: Context, dbSignIn: DbSignIn, fragment:Fragment) {

        CoroutineScope(Dispatchers.Main).launch {

            val job = Job()
            CoroutineScope(Dispatchers.IO + job).launch {
                starLogin(context, dbSignIn, fragment)
            }.join()
        }

    }

    private suspend fun starLogin(context: Context, dbSignIn: DbSignIn, fragment:Fragment) {


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

                val formatter = FormatterClass(context)
                val apiService = RetrofitBuilder.getRetrofit(Constants.BASE_URL).create(Interface::class.java)
                try {

                    val apiInterface = apiService.signInUser(dbSignIn)
                    if (apiInterface.isSuccessful) {

                        val statusCode = apiInterface.code()
                        val body = apiInterface.body()

                        if (statusCode == 200 || statusCode == 201) {

                            if (body != null) {

                                val accessToken = body.access_token
                                val expiresIn = body.expires_in
                                val refreshExpiresIn = body.refresh_expires_in
                                val refreshToken = body.refresh_token

                                Timber.e("User Information ${Gson().toJson(body)}")

                                formatter.saveSharedPref("","access_token", accessToken)
                                formatter.saveSharedPref("", "expires_in", expiresIn)
                                formatter.saveSharedPref("", "refresh_expires_in", refreshExpiresIn)
                                formatter.saveSharedPref("","refresh_token", refreshToken)
                                formatter.saveSharedPref("","isLoggedIn", "true")

//                                getUserDetails(context)

                                messageToast = "Login successful.."

                                findNavController(fragment).navigate(R.id.landingPageFragment)

                            } else {
                                messageToast = "Error: Body is null"
                            }
                        } else {
                            messageToast = "Error: The request was not successful"
                        }
                    } else {
                        apiInterface.errorBody()?.let {
                            val errorBody = JSONObject(it.string())
                            messageToast = errorBody.getString("error")
                        }
                    }


                } catch (e: Exception) {

                    Log.e("******", "")
                    Log.e("******", e.toString())
                    Log.e("******", "")


                    messageToast = "Cannot login user.."
                }


            }.join()
            CoroutineScope(Dispatchers.Main).launch {

                progressDialog.dismiss()
                Toast.makeText(context, messageToast, Toast.LENGTH_LONG).show()

            }

        }

    }

    fun getUser(context: Context, dbSignIn: DbSignIn) {

        CoroutineScope(Dispatchers.Main).launch {

            val job = Job()
            CoroutineScope(Dispatchers.IO + job).launch {
//                getUserDetails(context)
            }.join()
        }

    }

//    private suspend fun getUserDetails(context: Context) {
//
//
//        CoroutineScope(Dispatchers.IO).launch {
//
//            val formatter = FormatterClass()
//            val baseUrl = context.getString(UrlData.BASE_URL.message)
//            val apiService = RetrofitBuilder.getRetrofit(baseUrl).create(Interface::class.java)
//            try {
//
//                val token = formatter.getSharedPref("access_token", context)
//                if (token != null) {
//                    val apiInterface = apiService.getUserInfo("Bearer $token")
//                    if (apiInterface.isSuccessful) {
//
//                        val statusCode = apiInterface.code()
//                        val body = apiInterface.body()
//
//                        if (statusCode == 200 || statusCode == 201) {
//
//                            if (body != null) {
//                                Timber.e("User Information ${Gson().toJson(body)}")
//
//                                val user = body.user
//                                if (user != null) {
//                                    saveUserInformation(user, context)
////                                    val countyName = user.countyName
////                                    val fullNames = user.fullNames
////                                    val idNumber = user.idNumber
////                                    val practitionerRole = user.practitionerRole
////                                    val fhirPractitionerId = user.fhirPractitionerId
////                                    val email = user.email
////                                    val phone = user.phone
////                                    val id = user.id
////                                    val facility = user.facility
////                                    val facilityName = user.facilityName
////
////
////                                    val subCountyName = user.subCountyName
////                                    val wardName = user.wardName
//
////                                    formatter.saveSharedPref(
////                                        "practitionerFullNames",
////                                        fullNames,
////                                        context
////                                    )
////                                    formatter.saveSharedPref(
////                                        "practitionerIdNumber",
////                                        idNumber,
////                                        context
////                                    )
////                                    formatter.saveSharedPref(
////                                        "practitionerRole",
////                                        practitionerRole,
////                                        context
////                                    )
////                                    formatter.saveSharedPref(
////                                        "fhirPractitionerId",
////                                        fhirPractitionerId,
////                                        context
////                                    )
////                                    formatter.saveSharedPref("practitionerId", id, context)
////                                    formatter.saveSharedPref("practitionerEmail", email, context)
////                                    formatter.saveSharedPref(
////                                        "practitionerFacility",
////                                        facility,
////                                        context
////                                    )
////                                    formatter.saveSharedPref(
////                                        "practitionerFacilityName",
////                                        facilityName,
////                                        context
////                                    )
////                                    formatter.saveSharedPref(
////                                        "practitionerPhone",
////                                        phone ?: "",
////                                        context
////                                    )
////
////                                    formatter.saveSharedPref(
////                                        "countyName",
////                                        countyName ?: "",
////                                        context
////                                    )
////                                    formatter.saveSharedPref(
////                                        "subCountyName",
////                                        subCountyName ?: "",
////                                        context
////                                    )
////                                    formatter.saveSharedPref("wardName", wardName ?: "", context)
//
//                                }
//                            }
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//
//                Log.e("******", "")
//                Log.e("******", e.toString())
//                Log.e("******", "")
//            }
//
//
//        }
//
//
//    }
//
//    private fun saveUserInformation(user: DbUser, context: Context) {
//        val formatter = FormatterClass()
//        formatter.saveSharedPref(
//            "countyName ", user.countyName.toString(), context
//        )
//        formatter.saveSharedPref(
//            "practitionerFacility", user.facility, context
//        )
//        formatter.saveSharedPref(
//            "practitionerFacilityName", user.facilityName, context
//        )
//        formatter.saveSharedPref(
//            "fhirPractitionerId", user.fhirPractitionerId, context
//        )
//        formatter.saveSharedPref(
//            "id", user.id, context
//        )
//        formatter.saveSharedPref(
//            "idNumber", user.idNumber, context
//        )
//        formatter.saveSharedPref(
//            "phone", user.phone.toString(), context
//        )
//        formatter.saveSharedPref(
//            "practitionerRole", user.practitionerRole, context
//        )
//        formatter.saveSharedPref(
//            "subCountyName", user.subCountyName.toString(), context
//        )
//        formatter.saveSharedPref(
//            "wardName", user.wardName.toString(), context
//        )
//
//    }


    // Parse error response using Gson
    private fun parseError(response: Response<*>): DbResponseError? {
        return try {
            response.errorBody()?.let {
                val gson = Gson()
                val type = object : TypeToken<DbResponseError>() {}.type
                gson.fromJson(it.charStream(), type)
            }
        } catch (e: Exception) {
            null
        }
    }


}

