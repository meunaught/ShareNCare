package com.example.sharencare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sharencare.fragments.SignInFragment

import com.example.sharencare.fragments.SignUpFragment
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportFragmentManager.beginTransaction().replace(R.id.frame_layout_activity_login,
            SignInFragment()
        ).commit()

        signIn_btn_activity_login.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.frame_layout_activity_login,
                SignInFragment()
            ).commit()
        }

        signUp_btn_activity_login.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.frame_layout_activity_login,
                SignUpFragment()
            ).commit()
        }

        forgetPassword_btn_activity_login.setOnClickListener {
            startActivity(Intent(this,ForgetPasswordActivity::class.java))
        }

    }
}