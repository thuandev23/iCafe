package com.pro.shopfee.activity

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.pro.shopfee.R
import java.util.Locale

class ChangeLanguageActivity : AppCompatActivity() {
    private var btnChangeLanguageVn: LinearLayout? = null
    private var btnChangeLanguageEn: LinearLayout? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_language)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)

        initToolbar()
        initListener()
    }

    private fun initToolbar() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { finish() }
        tvToolbarTitle.text = getString(R.string.label_change_language)
    }

    private fun initListener() {
        btnChangeLanguageVn = findViewById<LinearLayout>(R.id.btn_change_language_vn)
        btnChangeLanguageEn = findViewById<LinearLayout>(R.id.btn_change_language_en)

        btnChangeLanguageVn!!.setOnClickListener {
            confirmLanguageChange("vi")
        }
        btnChangeLanguageEn!!.setOnClickListener {
            confirmLanguageChange("en")
        }
    }

    private fun confirmLanguageChange(language: String) {
        // Create a confirmation dialog
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(getString(R.string.txt_change_language))
        alertDialog.setMessage(getString(R.string.txt_app_restart))
        alertDialog.setPositiveButton(getString(R.string.yes)) { _, _ ->
            // If user agrees, change language and restart app
            setLanguage(language)
        }
        alertDialog.setNegativeButton(getString(R.string.no), null)
        alertDialog.show()
    }

    private fun setLanguage(language: String) {
        // Save the selected language to SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("app_language", language)
        editor.apply()

        // Set the new locale
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Restart the application to apply the changes
        restartApp()
    }

    private fun restartApp() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
