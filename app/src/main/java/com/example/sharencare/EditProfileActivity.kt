package com.example.sharencare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.sharencare.fragments.EditPasswordFragment
import com.example.sharencare.fragments.editProfileFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_profile.*

class EditProfileActivity : AppCompatActivity() {

    private var selectedFragment : Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

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

        logout_btn_activity_edit_profile.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val i = Intent(this, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i)
            finish()
        }

        supportFragmentManager.beginTransaction().replace(R.id.frame_layout_activity_edit_profile,
            editProfileFragment()
        ).commit()

    }
}