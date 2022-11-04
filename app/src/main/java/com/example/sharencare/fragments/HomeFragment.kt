package com.example.sharencare.fragments

import android.Manifest
import android.app.DownloadManager
import android.app.ProgressDialog;
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.uikit.ui_components.cometchat_ui.CometChatUI
import com.cometchat.pro.uikit.ui_settings.UIKitSettings
import com.cometchat.pro.uikit.ui_settings.enum.UserMode
import com.example.sharencare.MainActivity
import com.example.sharencare.Model.Notification
import com.example.sharencare.Model.Post
import com.example.sharencare.Model.User
import com.example.sharencare.R
import com.example.sharencare.adapter.PostAdapter
import com.faltenreich.skeletonlayout.Skeleton
import com.faltenreich.skeletonlayout.SkeletonLayout
import com.faltenreich.skeletonlayout.applySkeleton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import org.webrtc.StatsReport


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var message_btn_home_fragment : ImageButton
    private lateinit var profileImage : CircleImageView
    private lateinit var usernameTextView: TextView
    private var postAdapter : PostAdapter ?= null
    private var postList : MutableList<Post> ?= null
    private var followingList : MutableList<Post>?= null
    private var firebaseUser : FirebaseUser ?= null
    private lateinit var skeleton: Skeleton
    private lateinit var recyclerView: RecyclerView

    private var progressDialog : ProgressDialog ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_home, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        profileImage = view.findViewById(R.id.profileImage_home_fragment)
        usernameTextView = view.findViewById(R.id.username_textview_home_fragment)

        recyclerView = view.findViewById(R.id.recycler_view_home_fragment)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager =linearLayoutManager

        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it,postList as ArrayList<Post>,(activity as MainActivity)) }
        postAdapter?.setHasStableIds(true)
        skeleton = recyclerView.applySkeleton(R.layout.post)
        skeleton.showSkeleton()
//        recyclerView.adapter = postAdapter
        recyclerView.setItemViewCacheSize(25)

        checkFollowings()
        message_btn_home_fragment = view.findViewById(R.id.message_btn_home_fragment)
        message_btn_home_fragment.setOnClickListener{
            modifyCometUI()
//            cometUserUpdate()
            startActivity(Intent(context, CometChatUI::class.java))
        }

        badgeSetForNotifications()
        userInfo()

        return view
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1 &&  grantResults[0] == PackageManager.PERMISSION_GRANTED){
            println("Request Granted")

            val fileName : String
            val url : String
            val preferences = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)
            if(preferences != null)
            {
                println("Preference is not null")
                fileName = preferences.getString("fileName","none").toString()
                url = preferences.getString("url","none").toString()
                println(fileName)
                val request : DownloadManager.Request = DownloadManager.Request(Uri.parse(url))
                request.setTitle(fileName)
                request.allowScanningByMediaScanner()
                request.setAllowedOverMetered(true)
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName)
                val dm : DownloadManager?= context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
                dm?.enqueue(request)
            }
        }
    }


    private fun badgeSetForNotifications() {
        var counter = 0
        val navView = (activity as MainActivity).navView
        val menuItem = navView?.menu?.findItem(R.id.nav_home)
        menuItem?.isChecked = true

        val badge_notifications = navView?.getOrCreateBadge(R.id.nav_notifications)

        val notificationRef = FirebaseDatabase.getInstance().reference.child("Notifications")
        notificationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                counter = 0
                for(temp_snapshot in snapshot.children)
                {
                    val notification = temp_snapshot.getValue(Notification::class.java)
                    if((notification?.getReceiver() == firebaseUser?.uid)
                        && notification?.getSeen().equals("false")){
                        counter++
                        println(counter)
                    }
                    else{

                    }
                }
                if(counter >0)
                {
                    println("Counter is more than 1")
                    badge_notifications?.isVisible = true
                    badge_notifications?.number = counter
                }
                else
                {
                    badge_notifications?.isVisible = false
                    println("Counter is 0")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    private fun userInfo(){
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser?.uid.toString())
        userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    val user = snapshot.getValue(User::class.java)
                    usernameTextView.text = user?.getUsername().toString()
                    context?.let {
                        Glide.with(it).load(user?.getImage()).fitCenter().diskCacheStrategy(
                            DiskCacheStrategy.ALL)
                            .error(R.drawable.profile)
                            .dontTransform().into(profileImage)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println(error)
            }

        })
    }

    private fun checkFollowings() {
        followingList = ArrayList()
        val followingRef = FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser?.uid.toString())
            .child("Following")
        followingRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    (followingList as ArrayList<String>).clear()
                    for(temp_snapshot in snapshot.children){
                        temp_snapshot.key?.let{(followingList as ArrayList<String>).add(it)}
                    }
                    System.out.println("Retrieve Posts called")
                    retrievePosts()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postsRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                postList?.clear()
                for(temp_snapshot in snapshot.children){
                    val post = temp_snapshot.getValue(Post :: class.java)
                    for(id in (followingList as ArrayList<*>)){
                        if(id == post?.getPublisher())
                        {
                            postList?.add(post!!)
                        }
                    }
                }
                skeleton.showOriginal()
                recyclerView.adapter = postAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    private fun modifyCometUI() {
        UIKitSettings.polls = false
        UIKitSettings.sendVoiceNotes = false
        UIKitSettings.blockUser = false
        UIKitSettings.userSettings = false
        UIKitSettings.color = "#5C32C7"
        UIKitSettings.userInMode = UserMode.FRIENDS
        UIKitSettings.passwordGroup = false
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}