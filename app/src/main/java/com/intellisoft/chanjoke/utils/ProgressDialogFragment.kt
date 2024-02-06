package com.intellisoft.chanjoke.utils

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment


class ProgressDialogFragment : DialogFragment() {

    private lateinit var progressDialog: ProgressDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Please wait...")
        progressDialog.isIndeterminate = false
        progressDialog.max = 100
        return progressDialog
    }
}