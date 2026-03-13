package com.example.otplogin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast

class LoginActivity : Activity() {

    private lateinit var etPhone: EditText
    private lateinit var btnSend: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etPhone     = findViewById(R.id.etMobileNumber)
        btnSend     = findViewById(R.id.btnSendOtp)
        progressBar = findViewById(R.id.progressBar)

        etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnSend.isEnabled = isValidPhone(s?.toString()?.trim() ?: "")
            }
        })

        btnSend.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            if (isValidPhone(phone)) sendOtp(phone)
        }
    }

    private fun isValidPhone(p: String) = p.length >= 10 && p.all { it.isDigit() }

    private fun sendOtp(phone: String) {
        btnSend.isEnabled = false
        progressBar.visibility = View.VISIBLE
        btnSend.postDelayed({
            progressBar.visibility = View.GONE
            btnSend.isEnabled = true
            val otp = (100000..999999).random().toString()
            Toast.makeText(this, "Your Demo OTP: $otp", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, OtpActivity::class.java).apply {
                putExtra(OtpActivity.EXTRA_PHONE, phone)
                putExtra(OtpActivity.EXTRA_OTP, otp)
            })
        }, 1200)
    }
}
