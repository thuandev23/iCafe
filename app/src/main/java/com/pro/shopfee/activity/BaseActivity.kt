package com.pro.shopfee.activity

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.messaging.FirebaseMessaging
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.pro.shopfee.R

abstract class BaseActivity : AppCompatActivity() {

    private var progressDialog: MaterialDialog? = null
    private var alertDialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createProgressDialog()
        createAlertDialog()
        requestNotificationPermission()
        configureStrictMode()
        subscribeToFirebaseTopic("test")
    }

    private fun createProgressDialog() {
        progressDialog = MaterialDialog.Builder(this)
            .content(R.string.msg_waiting_message)
            .progress(true, 0)
            .build()
    }

    fun showProgressDialog(value: Boolean) {
        if (value) {
            if (progressDialog != null && !progressDialog!!.isShowing) {
                progressDialog!!.show()
                progressDialog!!.setCancelable(false)
            }
        } else {
            if (progressDialog != null && progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
            }
        }
    }

    fun dismissProgressDialog() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
        if (alertDialog != null && alertDialog!!.isShowing) {
            alertDialog!!.dismiss()
        }
    }

    private fun createAlertDialog() {
        alertDialog = MaterialDialog.Builder(this)
            .title(R.string.app_name)
            .positiveText(R.string.action_ok)
            .cancelable(false)
            .build()
    }

    fun showAlertDialog(errorMessage: String?) {
        alertDialog!!.setContent(errorMessage)
        alertDialog!!.show()
    }

    fun showAlertDialog(@StringRes resourceId: Int) {
        alertDialog!!.setContent(resourceId)
        alertDialog!!.show()
    }

    fun setCancelProgress(isCancel: Boolean) {
        if (progressDialog != null) {
            progressDialog!!.setCancelable(isCancel)
        }
    }

    fun showToastMessage(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
        if (alertDialog != null && alertDialog!!.isShowing) {
            alertDialog!!.dismiss()
        }
        super.onDestroy()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Dexter.withContext(this)
                .withPermissions(Manifest.permission.POST_NOTIFICATIONS)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report != null && report.areAllPermissionsGranted()) {
                            Toast.makeText(this@BaseActivity, "Permission granted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@BaseActivity, "Permission denied", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                        token?.continuePermissionRequest()
                    }
                }).check()
        }
    }

    private fun configureStrictMode() {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    private fun subscribeToFirebaseTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
    }
}