package com.example.sharencare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton
import com.example.sharencare.fragments.SignInFragment
import com.example.sharencare.fragments.SignUpFragment

class LoginActivity : AppCompatActivity() {

    private lateinit var signIn_btn_activity_login : AppCompatButton
    private lateinit var signUp_btn_activity_login : AppCompatButton
    private lateinit var forgetPassword_btn_activity_login : AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        signIn_btn_activity_login = findViewById(R.id.signIn_btn_activity_login)
        signUp_btn_activity_login = findViewById(R.id.signUp_btn_activity_login)
        forgetPassword_btn_activity_login = findViewById(R.id.forgetPassword_btn_activity_login)

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