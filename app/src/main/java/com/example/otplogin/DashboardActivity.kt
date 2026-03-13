package com.example.otplogin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.otplogin.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PHONE = "extra_phone"
    }

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val phone = intent.getStringExtra(EXTRA_PHONE) ?: "Unknown"
        setupUI(phone)
    }

    private fun setupUI(phone: String) {
        binding.tvWelcome.text = "Welcome!"
        binding.tvPhoneNumber.text = "+91 $phone"

        val menuItems = listOf(
            Pair("My Profile", R.drawable.ic_profile),
            Pair("Transactions", R.drawable.ic_transactions),
            Pair("Settings", R.drawable.ic_settings),
            Pair("Help & Support", R.drawable.ic_help)
        )

        binding.cardProfile.setOnClickListener {
            showComingSoon("My Profile")
        }
        binding.cardTransactions.setOnClickListener {
            showComingSoon("Transactions")
        }
        binding.cardSettings.setOnClickListener {
            showComingSoon("Settings")
        }
        binding.cardHelp.setOnClickListener {
            showComingSoon("Help & Support")
        }

        binding.btnLogout.setOnClickListener {
            confirmLogout()
        }
    }

    private fun showComingSoon(feature: String) {
        AlertDialog.Builder(this)
            .setTitle(feature)
            .setMessage("$feature feature coming soon!")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
