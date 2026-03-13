package com.example.otplogin

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.otplogin.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        // Enable button only when valid phone number entered
        binding.etMobileNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val phone = s?.toString()?.trim() ?: ""
                binding.btnSendOtp.isEnabled = isValidPhone(phone)
            }
        })

        binding.btnSendOtp.setOnClickListener {
            val phone = binding.etMobileNumber.text.toString().trim()
            if (isValidPhone(phone)) {
                sendOtp(phone)
            }
        }
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.length >= 10 && phone.all { it.isDigit() }
    }

    private fun sendOtp(phone: String) {
        binding.btnSendOtp.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        // Simulate network delay then navigate to OTP screen
        binding.root.postDelayed({
            binding.progressBar.visibility = View.GONE
            binding.btnSendOtp.isEnabled = true

            // Generate a demo OTP (in a real app this comes from your backend)
            val demoOtp = (100000..999999).random().toString()

            Toast.makeText(
                this,
                "Demo OTP: $demoOtp (use this to login)",
                Toast.LENGTH_LONG
            ).show()

            val intent = Intent(this, OtpActivity::class.java).apply {
                putExtra(OtpActivity.EXTRA_PHONE, phone)
                putExtra(OtpActivity.EXTRA_OTP, demoOtp)
            }
            startActivity(intent)
        }, 1500)
    }
}
