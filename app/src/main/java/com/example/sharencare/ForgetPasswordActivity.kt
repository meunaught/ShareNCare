package com.example.sharencare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton


class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var contactUs_btn_activity_forget_password : AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        contactUs_btn_activity_forget_password = findViewById(R.id.contactUs_btn_activity_forget_password)

        contactUs_btn_activity_forget_password.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            val str = arrayOf<String>("shamik.shafkat@gmail.com")
            intent.putExtra(Intent.EXTRA_EMAIL,str)
            intent.putExtra(Intent.EXTRA_SUBJECT, "Mention Your Problem")
            intent.putExtra(Intent.EXTRA_TEXT,"Password verification not working")
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    }
}