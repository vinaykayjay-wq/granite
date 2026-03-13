package com.example.otplogin

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class DashboardActivity : Activity() {

    companion object { const val EXTRA_PHONE = "extra_phone" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val phone = intent.getStringExtra(EXTRA_PHONE) ?: "Unknown"
        findViewById<TextView>(R.id.tvWelcome).text     = "Welcome!"
        findViewById<TextView>(R.id.tvPhoneNumber).text = "+91 $phone"

        listOf(
            R.id.cardProfile      to "My Profile",
            R.id.cardTransactions to "Transactions",
            R.id.cardSettings     to "Settings",
            R.id.cardHelp         to "Help & Support"
        ).forEach { (id, name) ->
            findViewById<android.view.View>(id).setOnClickListener { showComingSoon(name) }
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener { confirmLogout() }
    }

    private fun showComingSoon(f: String) =
        AlertDialog.Builder(this).setTitle(f).setMessage("$f coming soon!").setPositiveButton("OK", null).show()

    private fun confirmLogout() =
        AlertDialog.Builder(this).setTitle("Logout").setMessage("Logout?")
            .setPositiveButton("Yes") { _, _ ->
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
            .setNegativeButton("Cancel", null).show()
}
