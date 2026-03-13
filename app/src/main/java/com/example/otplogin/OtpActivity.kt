package com.example.otplogin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

class OtpActivity : Activity() {

    companion object {
        const val EXTRA_PHONE = "extra_phone"
        const val EXTRA_OTP   = "extra_otp"
    }

    private lateinit var tvHint: TextView
    private lateinit var tvError: TextView
    private lateinit var tvResend: TextView
    private lateinit var btnVerify: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var otpBoxes: List<EditText>
    private var timer: CountDownTimer? = null
    private lateinit var phone: String
    private lateinit var expectedOtp: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        phone       = intent.getStringExtra(EXTRA_PHONE) ?: ""
        expectedOtp = intent.getStringExtra(EXTRA_OTP)   ?: ""

        tvHint      = findViewById(R.id.tvPhoneHint)
        tvError     = findViewById(R.id.tvError)
        tvResend    = findViewById(R.id.tvResend)
        btnVerify   = findViewById(R.id.btnVerifyOtp)
        progressBar = findViewById(R.id.progressBar)

        otpBoxes = listOf(
            findViewById(R.id.etOtp1), findViewById(R.id.etOtp2),
            findViewById(R.id.etOtp3), findViewById(R.id.etOtp4),
            findViewById(R.id.etOtp5), findViewById(R.id.etOtp6)
        )

        tvHint.text = "OTP sent to +91 $phone"

        otpBoxes.forEachIndexed { i, et ->
            et.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, x: Int, y: Int, z: Int) {}
                override fun onTextChanged(s: CharSequence?, x: Int, y: Int, z: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < otpBoxes.size - 1) otpBoxes[i + 1].requestFocus()
                    btnVerify.isEnabled = otpBoxes.all { it.text.length == 1 }
                }
            })
            et.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_DEL && et.text.isNullOrEmpty() && i > 0) {
                    otpBoxes[i - 1].requestFocus(); true
                } else false
            }
        }

        btnVerify.setOnClickListener {
            verifyOtp(otpBoxes.joinToString("") { it.text.toString() })
        }
        tvResend.setOnClickListener { resendOtp() }
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
        startTimer()
    }

    override fun onDestroy() { super.onDestroy(); timer?.cancel() }

    private fun verifyOtp(entered: String) {
        btnVerify.isEnabled = false
        progressBar.visibility = View.VISIBLE
        btnVerify.postDelayed({
            progressBar.visibility = View.GONE
            if (entered == expectedOtp) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, DashboardActivity::class.java).apply {
                    putExtra(DashboardActivity.EXTRA_PHONE, phone)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            } else {
                tvError.visibility = View.VISIBLE
                tvError.text = "Incorrect OTP. Please try again."
                btnVerify.isEnabled = true
            }
        }, 900)
    }

    private fun resendOtp() {
        expectedOtp = (100000..999999).random().toString()
        tvError.visibility = View.GONE
        Toast.makeText(this, "New Demo OTP: $expectedOtp", Toast.LENGTH_LONG).show()
        startTimer()
    }

    private fun startTimer() {
        tvResend.isEnabled = false
        timer?.cancel()
        timer = object : CountDownTimer(30_000, 1000) {
            override fun onTick(ms: Long) { tvResend.text = "Resend in ${ms / 1000}s" }
            override fun onFinish()       { tvResend.text = "Resend OTP"; tvResend.isEnabled = true }
        }.start()
    }
}
