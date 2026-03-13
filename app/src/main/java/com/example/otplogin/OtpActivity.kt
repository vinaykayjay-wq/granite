package com.example.otplogin

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.otplogin.databinding.ActivityOtpBinding

class OtpActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PHONE = "extra_phone"
        const val EXTRA_OTP = "extra_otp"
        private const val RESEND_TIMEOUT_MS = 30_000L
    }

    private lateinit var binding: ActivityOtpBinding
    private lateinit var phone: String
    private lateinit var expectedOtp: String
    private var resendTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        phone = intent.getStringExtra(EXTRA_PHONE) ?: ""
        expectedOtp = intent.getStringExtra(EXTRA_OTP) ?: ""

        setupUI()
        startResendTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel()
    }

    private fun setupUI() {
        binding.tvPhoneHint.text = "Enter the OTP sent to +91 $phone"

        // Wire the 6 OTP boxes so focus moves automatically
        val otpBoxes = listOf(
            binding.etOtp1, binding.etOtp2, binding.etOtp3,
            binding.etOtp4, binding.etOtp5, binding.etOtp6
        )

        otpBoxes.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < otpBoxes.size - 1) {
                        otpBoxes[index + 1].requestFocus()
                    }
                    updateVerifyButtonState(otpBoxes)
                }
            })

            // Handle backspace to go to previous box
            editText.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL &&
                    editText.text.isNullOrEmpty() && index > 0
                ) {
                    otpBoxes[index - 1].requestFocus()
                    true
                } else {
                    false
                }
            }
        }

        binding.btnVerifyOtp.setOnClickListener {
            val enteredOtp = otpBoxes.joinToString("") { it.text.toString() }
            verifyOtp(enteredOtp)
        }

        binding.tvResend.setOnClickListener {
            resendOtp()
        }

        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun updateVerifyButtonState(
        otpBoxes: List<android.widget.EditText>
    ) {
        val allFilled = otpBoxes.all { it.text.length == 1 }
        binding.btnVerifyOtp.isEnabled = allFilled
    }

    private fun verifyOtp(enteredOtp: String) {
        binding.btnVerifyOtp.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        // Simulate verification delay
        binding.root.postDelayed({
            binding.progressBar.visibility = View.GONE

            if (enteredOtp == expectedOtp) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DashboardActivity::class.java).apply {
                    putExtra(DashboardActivity.EXTRA_PHONE, phone)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            } else {
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = "Invalid OTP. Please try again."
                binding.btnVerifyOtp.isEnabled = true
                shakeOtpBoxes()
            }
        }, 1000)
    }

    private fun shakeOtpBoxes() {
        val shake = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake)
        binding.otpContainer.startAnimation(shake)
    }

    private fun resendOtp() {
        // Generate a new OTP
        expectedOtp = (100000..999999).random().toString()
        binding.tvError.visibility = View.GONE

        Toast.makeText(
            this,
            "New Demo OTP: $expectedOtp",
            Toast.LENGTH_LONG
        ).show()

        startResendTimer()
    }

    private fun startResendTimer() {
        binding.tvResend.isEnabled = false
        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(RESEND_TIMEOUT_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvResend.text = "Resend OTP in ${seconds}s"
            }

            override fun onFinish() {
                binding.tvResend.text = "Resend OTP"
                binding.tvResend.isEnabled = true
            }
        }.start()
    }
}
