package com.example.sharencare

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.cometchat.pro.core.AppSettings
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.example.sharencare.constants.AppConfig
import com.example.sharencare.fragments.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private var selectedFragment : Fragment? = null
    var x : Int = 10
    var navView : BottomNavigationView ?= null
    private var firebaseUser : FirebaseUser ?= null

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                selectedFragment = HomeFragment()
            }
            R.id.nav_search_button -> {
                selectedFragment = SearchFragment()
            }
            R.id.nav_add_post -> {
                selectedFragment = CreatePostFragment()
            }
            R.id.nav_notifications -> {
                selectedFragment = NotificationsFragment()
            }
            R.id.nav_profile -> {
                selectedFragment = ProfileFragment()
            }
        }

        if(selectedFragment!=null)
        {
            supportFragmentManager.beginTransaction().replace(R.id.frame_layout_activity_main,selectedFragment!!).addToBackStack(null).commit()
            return@OnNavigationItemSelectedListener true
        }

        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseUser = FirebaseAuth.getInstance().currentUser

        setTopic()

        navView  = findViewById(R.id.nav_view)
        navView?.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        supportFragmentManager.beginTransaction().replace(R.id.frame_layout_activity_main,HomeFragment()).commit()

    }

    override fun onBackPressed() {
        if(supportFragmentManager.backStackEntryCount> 0)
        {
            supportFragmentManager.popBackStackImmediate()
        }
        else
        {
            super.onBackPressed()
        }
    }

    private fun setTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(firebaseUser?.uid.toString())
    }

}