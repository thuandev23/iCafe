package com.pro.shopfee.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pro.shopfee.R
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.StringUtil.isEmpty
import com.pro.shopfee.utils.StringUtil.isValidEmail

class ForgotPasswordActivity : BaseActivity() {

    private var edtEmail: EditText? = null
    private var btnResetPassword: Button? = null
    private var isEnableButtonResetPassword = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        initToolbar()
        initUi()
        initListener()
    }

    private fun initToolbar() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { finish() }
        tvToolbarTitle.text = getString(R.string.reset_password)
    }

    private fun initUi() {
        edtEmail = findViewById(R.id.edt_email)
        btnResetPassword = findViewById(R.id.btn_reset_password)
    }

    private fun initListener() {
        edtEmail!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (!isEmpty(s.toString())) {
                    edtEmail!!.setBackgroundResource(R.drawable.bg_white_corner_16_border_main)
                } else {
                    edtEmail!!.setBackgroundResource(R.drawable.bg_white_corner_16_border_gray)
                }
                if (!isEmpty(s.toString())) {
                    isEnableButtonResetPassword = true
                    btnResetPassword!!.setBackgroundResource(R.drawable.bg_button_enable_corner_16)
                } else {
                    isEnableButtonResetPassword = false
                    btnResetPassword!!.setBackgroundResource(R.drawable.bg_button_disable_corner_16)
                }
            }
        })
        btnResetPassword!!.setOnClickListener { onClickValidateResetPassword() }
    }

    private fun onClickValidateResetPassword() {
        if (!isEnableButtonResetPassword) return
        val strEmail = edtEmail!!.text.toString().trim { it <= ' ' }
        if (isEmpty(strEmail)) {
            Toast.makeText(
                this@ForgotPasswordActivity,
                getString(R.string.msg_email_require), Toast.LENGTH_SHORT
            ).show()
        } else if (!isValidEmail(strEmail)) {
            Toast.makeText(
                this@ForgotPasswordActivity,
                getString(R.string.msg_email_invalid), Toast.LENGTH_SHORT
            ).show()
        } else {
            fetchAllEmail(strEmail)
        }
    }
    private fun fetchAllEmail(inputEmail: String) {
        val database = FirebaseDatabase.getInstance().getReference("users")
        database.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                var emailFound = false
                for (userSnapshot in snapshot.children) {
                    val email = userSnapshot.child("email").getValue(String::class.java)
                    if (email != null && email == inputEmail) {
                        emailFound = true
                        break
                    }
                }

                if (emailFound) {
                    resetPassword(inputEmail)
                } else {
                    Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Task failed
                Toast.makeText(this, "Error retrieving data", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun resetPassword(email: String) {
        showProgressDialog(true)
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task: Task<Void?> ->
                showProgressDialog(false)
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        getString(R.string.msg_reset_password_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    edtEmail!!.setText("")
                }
            }
    }
}