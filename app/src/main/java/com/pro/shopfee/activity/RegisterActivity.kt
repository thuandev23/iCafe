package com.pro.shopfee.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.widget.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pro.shopfee.R
import com.pro.shopfee.activity.admin.AdminMainActivity
import com.pro.shopfee.model.User
import com.pro.shopfee.prefs.DataStoreManager
import com.pro.shopfee.prefs.DataStoreManager.Companion.user
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.Constant.REQUEST_CODE_ADDRESS
import com.pro.shopfee.utils.GlobalFunction.startActivity
import com.pro.shopfee.utils.StringUtil.isEmpty
import com.pro.shopfee.utils.StringUtil.isValidEmail

class RegisterActivity : BaseActivity() {
    private var edtUserName: EditText? = null
    private var edtEmail: EditText? = null
    private var edtPassword: EditText? = null
    private var edtConfirmPassword: EditText? = null
    private var editAddress: EditText? = null
    private var btnRegister: Button? = null
    private var layoutLogin: LinearLayout? = null
    private var rdbAdmin: RadioButton? = null
    private var rdbUser: RadioButton? = null
    private var isEnableButtonRegister = false
    private var isPasswordVisible = false

    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0
    private var address: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        initUi()
        initListener()
    }

    private fun initUi() {
        edtUserName = findViewById(R.id.edt_username)
        edtEmail = findViewById(R.id.edt_email)
        edtPassword = findViewById(R.id.edt_password)
        editAddress = findViewById(R.id.edt_address)
        edtConfirmPassword = findViewById(R.id.edt_confirm_password)
        btnRegister = findViewById(R.id.btn_register)
        layoutLogin = findViewById(R.id.layout_login)
        rdbAdmin = findViewById(R.id.rdb_admin)
        rdbUser = findViewById(R.id.rdb_user)
    }

    private fun initListener() {
        rdbUser!!.isChecked = true
        edtUserName!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (!isEmpty(s.toString())) {
                    edtUserName!!.setBackgroundResource(R.drawable.bg_white_corner_16_border_main)
                } else {
                    edtUserName!!.setBackgroundResource(R.drawable.bg_white_corner_16_border_gray)
                }
                val strEmail = edtEmail!!.text.toString().trim { it <= ' ' }
                if (!isEmpty(s.toString()) && !isEmpty(strEmail)) {
                    isEnableButtonRegister = true
                    btnRegister!!.setBackgroundResource(R.drawable.bg_button_enable_corner_16)
                } else {
                    isEnableButtonRegister = false
                    btnRegister!!.setBackgroundResource(R.drawable.bg_button_disable_corner_16)
                }
            }
        })
        edtEmail!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (!isEmpty(s.toString())) {
                    edtEmail!!.setBackgroundResource(R.drawable.bg_white_corner_16_border_main)
                } else {
                    edtEmail!!.setBackgroundResource(R.drawable.bg_white_corner_16_border_gray)
                }
                val strPassword = edtPassword!!.text.toString().trim { it <= ' ' }
                if (!isEmpty(s.toString()) && !isEmpty(strPassword)) {
                    isEnableButtonRegister = true
                    btnRegister!!.setBackgroundResource(R.drawable.bg_button_enable_corner_16)
                } else {
                    isEnableButtonRegister = false
                    btnRegister!!.setBackgroundResource(R.drawable.bg_button_disable_corner_16)
                }
            }
        })
        edtPassword!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (!isEmpty(s.toString())) {
                    edtPassword!!.setBackgroundResource(R.drawable.bg_white_corner_16_border_main)
                } else {
                    edtPassword!!.setBackgroundResource(R.drawable.bg_white_corner_16_border_gray)
                }
                val strConfirmPassword = edtConfirmPassword!!.text.toString().trim { it <= ' ' }
                if (!isEmpty(s.toString()) && !isEmpty(strConfirmPassword)) {
                    isEnableButtonRegister = true
                    btnRegister!!.setBackgroundResource(R.drawable.bg_button_enable_corner_16)
                } else {
                    isEnableButtonRegister = false
                    btnRegister!!.setBackgroundResource(R.drawable.bg_button_disable_corner_16)
                }
            }
        })
        edtConfirmPassword!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (!isEmpty(s.toString())) {
                    edtConfirmPassword!!.setBackgroundResource(R.drawable.bg_white_corner_16_border_main)
                } else {
                    edtConfirmPassword!!.setBackgroundResource(R.drawable.bg_white_corner_16_border_gray)
                }
                val strAddress = editAddress!!.text.toString().trim { it <= ' ' }
                if (!isEmpty(s.toString()) && !isEmpty(strAddress)) {
                    isEnableButtonRegister = true
                    btnRegister!!.setBackgroundResource(R.drawable.bg_button_enable_corner_16)
                } else {
                    isEnableButtonRegister = false
                    btnRegister!!.setBackgroundResource(R.drawable.bg_button_disable_corner_16)
                }
            }
        })
        editAddress!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (!isEmpty(s.toString())) {
                    editAddress!!.setBackgroundResource(R.drawable.bg_white_corner_16_border_main)
                } else {
                    editAddress!!.setBackgroundResource(R.drawable.bg_white_corner_16_border_gray)
                }
                val strPassword = edtPassword!!.text.toString().trim { it <= ' ' }
                if (!isEmpty(s.toString()) && !isEmpty(strPassword)) {
                    isEnableButtonRegister = true
                    btnRegister!!.setBackgroundResource(R.drawable.bg_button_enable_corner_16)
                } else {
                    isEnableButtonRegister = false
                    btnRegister!!.setBackgroundResource(R.drawable.bg_button_disable_corner_16)
                }
            }
        })
        layoutLogin!!.setOnClickListener { finish() }
        btnRegister!!.setOnClickListener { onClickValidateRegister() }
        togglePasswordVisibility(edtPassword!!)
        togglePasswordVisibility(edtConfirmPassword!!)
        editAddress!!.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = editAddress!!.compoundDrawablesRelative[2]
                if (drawableEnd != null && event.rawX >= (editAddress!!.right - drawableEnd.bounds.width())) {
                    val intent = Intent(this, MapsActivity::class.java)
                    startActivityForResult(intent, REQUEST_CODE_ADDRESS)
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }

    }

    private fun isPasswordStrong(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=!])(?=\\S+$).{8,}$"
        return password.matches(passwordPattern.toRegex())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADDRESS && resultCode == Activity.RESULT_OK) {
            data?.let {
                latitude = it.getDoubleExtra("latitude", 0.0)
                longitude = it.getDoubleExtra("longitude", 0.0)
                address = it.getStringExtra("address") ?: ""
                Log.d(
                    "RegisterActivity",
                    "Latitude: $latitude, Longitude: $longitude, Address: $address"
                )
                editAddress!!.setText(address)
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun togglePasswordVisibility(editText: EditText) {
        editText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = editText.compoundDrawablesRelative[2]
                if (drawableEnd != null && event.rawX >= (editText.right - drawableEnd.bounds.width())) {
                    isPasswordVisible = !isPasswordVisible
                    if (isPasswordVisible) {
                        editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0, 0, R.drawable.ic_hide, 0
                        )
                    } else {
                        editText.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0, 0, R.drawable.ic_view, 0
                        )
                    }
                    editText.setSelection(editText.text.length)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }

    private fun onClickValidateRegister() {
        if (!isEnableButtonRegister) return
        val strUserName = edtUserName!!.text.toString().trim { it <= ' ' }
        val strEmail = edtEmail!!.text.toString().trim { it <= ' ' }
        val strPassword = edtPassword!!.text.toString().trim { it <= ' ' }
        val strConfirmPassword = edtConfirmPassword!!.text.toString().trim { it <= ' ' }
        val strAddress = editAddress!!.text.toString()
        if (isEmpty(strEmail)) {
            showToastMessage(getString(R.string.msg_email_require))
        } else if (isEmpty(strUserName)) {
            showToastMessage(getString(R.string.msg_userName_require))
        } else if (isEmpty(strPassword)) {
            showToastMessage(getString(R.string.msg_password_require))
        } else if (isEmpty(strConfirmPassword)) {
            showToastMessage(getString(R.string.msg_confirm_password_require))
        } else if (!strPassword.equals(strConfirmPassword)) {
            showToastMessage(getString(R.string.msg_password_mismatch))
        } else if (!isPasswordStrong(strPassword)) {
            showToastMessage(getString(R.string.msg_password_form))
        } else if (!isValidEmail(strEmail)) {
            showToastMessage(getString(R.string.msg_email_invalid))
        } else if (isEmpty(strAddress)) {
            showToastMessage(getString(R.string.msg_address_require))
        } else {
            if (rdbAdmin!!.isChecked) {
                if (!strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                    Toast.makeText(
                        this, getString(R.string.msg_email_invalid_admin), Toast.LENGTH_SHORT
                    ).show()
                } else {
                    registerUserFirebase(strEmail, strPassword, strUserName, strAddress, latitude, longitude)
                }
            } else {
                if (strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                    Toast.makeText(
                        this, getString(R.string.msg_email_invalid_user), Toast.LENGTH_SHORT
                    ).show()
                } else {
                    registerUserFirebase(strEmail, strPassword, strUserName, strAddress, latitude, longitude)
                }
            }
        }
    }


    private fun registerUserFirebase(
        email: String, password: String, userName: String, address: String, latitude: Double?, longitude: Double?
    ) {
        showProgressDialog(true)
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                showProgressDialog(false)
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        val userObjectSavePass = User( email= user.email, password =  password)
                        val userObjectNotSavePass = User(email = user.email, username =  userName, password = password, address =  address,
                            latitude = latitude, longitude =  longitude
                        )

                        if (user.email != null && user.email!!.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                            userObjectSavePass.isAdmin = true
                            userObjectNotSavePass.isAdmin = true
                        }
                        DataStoreManager.user = userObjectSavePass
                        val database = FirebaseDatabase.getInstance()
                        val usersRef = database.getReference("users")
                        usersRef.child(user.uid).setValue(userObjectNotSavePass)
                            .addOnSuccessListener {
                                goToMainActivity()
                            }.addOnFailureListener { e ->
                                showToastMessage("Lỗi khi thêm user vào database: ${e.message}")
                            }
                    }
                } else {
                    showToastMessage(getString(R.string.msg_register_error))
                }
            }
    }

    private fun goToMainActivity() {
        if (user!!.isAdmin) {
            startActivity(this, AdminMainActivity::class.java)
        } else {
            startActivity(this, LoginActivity::class.java)
        }
        finishAffinity()
    }
}