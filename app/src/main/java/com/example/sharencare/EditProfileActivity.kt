package com.example.sharencare

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.example.sharencare.fragments.EditPasswordFragment
import com.example.sharencare.fragments.editProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class EditProfileActivity : AppCompatActivity() {

    private var selectedFragment : Fragment? = null

    private lateinit var profileUpdate_btn_activity_edit_profile : AppCompatButton
    private lateinit var passwordUpdate_btn_activity_edit_password : AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        profileUpdate_btn_activity_edit_profile = findViewById(R.id.profileUpdate_btn_activity_edit_profile)
        passwordUpdate_btn_activity_edit_password = findViewById(R.id.passwordUpdate_btn_activity_edit_password)

        profileUpdate_btn_activity_edit_profile.setOnClickListener {
            selectedFragment = editProfileFragment()
            if(selectedFragment!=null)
            {
                supportFragmentManager.beginTransaction().replace(R.id.frame_layout_activity_edit_profile,selectedFragment!!).commit()
            }
        }

        passwordUpdate_btn_activity_edit_password.setOnClickListener {
            selectedFragment = EditPasswordFragment()
            if(selectedFragment!=null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_activity_edit_profile, selectedFragment!!).commit()
            }
        }


        supportFragmentManager.beginTransaction().replace(R.id.frame_layout_activity_edit_profile,
            editProfileFragment()
        ).commit()
    }

}