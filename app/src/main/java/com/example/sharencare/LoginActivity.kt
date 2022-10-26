package com.example.sharencare

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.AppCompatButton
import com.cometchat.pro.core.AppSettings
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.uikit.ui_components.calls.call_manager.listener.CometChatCallListener
import com.example.sharencare.constants.AppConfig
import com.example.sharencare.fragments.SignInFragment
import com.example.sharencare.fragments.SignUpFragment

class LoginActivity : AppCompatActivity() {

    private lateinit var signIn_btn_activity_login : AppCompatButton
    private lateinit var signUp_btn_activity_login : AppCompatButton
    private lateinit var forgetPassword_btn_activity_login : AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initComet()

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

    private fun initComet() {
        val appID = AppConfig.AppDetails.APP_ID
        val region = AppConfig.AppDetails.REGION
        val appSettings =
            AppSettings.AppSettingsBuilder().subscribePresenceForAllUsers().setRegion(region)
                .build()

        CometChat.init(this, appID, appSettings, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(successMessage: String) {
                Log.d(ContentValues.TAG, "Comet Initialization completed successfully")
            }

            override fun onError(e: CometChatException) {
                Log.d(ContentValues.TAG, "Comet Initialization failed with exception: " + e.message)
            }
        })
        CometChatCallListener.addCallListener(TAG, this)
    }


}