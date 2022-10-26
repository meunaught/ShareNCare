package com.example.sharencare

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cometchat.pro.core.AppSettings
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.example.sharencare.constants.AppConfig
import com.example.sharencare.fragments.*
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private var selectedFragment : Fragment? = null
    var x : Int = 10
    var navView : BottomNavigationView ?= null

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
            supportFragmentManager.beginTransaction().replace(R.id.frame_layout_activity_main,selectedFragment!!).commit()
            return@OnNavigationItemSelectedListener true
        }

        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseMessaging.getInstance().subscribeToTopic("all")

        navView  = findViewById(R.id.nav_view)
        navView?.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        supportFragmentManager.beginTransaction().replace(R.id.frame_layout_activity_main,HomeFragment()).commit()

    }

}