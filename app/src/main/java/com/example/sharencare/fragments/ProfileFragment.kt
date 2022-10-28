package com.example.sharencare.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sharencare.EditProfileActivity
import com.example.sharencare.FcmNotificationsSender
import com.example.sharencare.MainActivity
import com.example.sharencare.Model.MyDiffCallBack
import com.example.sharencare.Model.Notification
import com.example.sharencare.Model.Post
import com.example.sharencare.Model.User
import com.example.sharencare.R
import com.example.sharencare.adapter.PostAdapter
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var profileId : String
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var editProfile_btn_profile_fragment : AppCompatButton
    private lateinit var followers_textView_profile_fragment : TextView
    private lateinit var following_textView_profile_fragment : TextView
    private lateinit var username_textView_profile_fragment : TextView
    private lateinit var fullName_textView_profile_fragment : TextView
    private lateinit var bio_textView_profile_fragment : TextView
    private lateinit var profile_picture_profile_fragment : ImageView
    private lateinit var scrollView : NestedScrollView
    private lateinit var toolbar: Toolbar

    private var postAdapter : PostAdapter?= null
    private var postList : MutableList<Post> ?= null
    private var recyclerView : RecyclerView?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_profile, container, false)
        firebaseUser =  FirebaseAuth.getInstance().currentUser!!
        editProfile_btn_profile_fragment = view.findViewById(R.id.editProfile_btn_profile_fragment)
        followers_textView_profile_fragment = view.findViewById(R.id.followers_textView_profile_fragment)
        following_textView_profile_fragment = view.findViewById(R.id.following_textView_profile_fragment)
        username_textView_profile_fragment = view.findViewById(R.id.username_textView_profile_fragment)
        fullName_textView_profile_fragment = view.findViewById(R.id.fullName_textView_profile_fragment)
        bio_textView_profile_fragment = view.findViewById(R.id.bio_textView_profile_fragment)
        profile_picture_profile_fragment = view.findViewById(R.id.profile_picture_profile_fragment)
        scrollView = view.findViewById(R.id.scrollView_profile_fragment)
        toolbar = view.findViewById(R.id.toolbar_profile_fragment)

        toolbar.inflateMenu(R.menu.options_profile_fragment)
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.options_followers) {
                (context as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                    R.id.frame_layout_activity_main,My_followersFragment()).commit()
                return@setOnMenuItemClickListener true
            }
            else if(item.itemId == R.id.options_following)
            {
                (context as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                    R.id.frame_layout_activity_main,My_FollowingsFragment()).commit()
                return@setOnMenuItemClickListener true
            }

            else if(item.itemId == R.id.options_request_sent)
            {
                (context as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                    R.id.frame_layout_activity_main,SentRequests()).commit()
                return@setOnMenuItemClickListener true
            }
            else if(item.itemId == R.id.options_request_receive)
            {
                (context as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                    R.id.frame_layout_activity_main,ReceivedRequestsFragment()).commit()
                return@setOnMenuItemClickListener true
            }
            false
        }

        recyclerView = view.findViewById(R.id.recycler_view_profile_fragment)
        val linearLayoutManager = LinearLayoutManager(context)

        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView?.layoutManager =linearLayoutManager
        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it,postList as ArrayList<Post>) }
        postAdapter?.setHasStableIds(true)
        recyclerView?.adapter = postAdapter
        recyclerView?.setItemViewCacheSize(25)


        val preferences = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)
        if(preferences != null)
        {
            this.profileId = preferences.getString("profileId","none").toString()
        }
        else
        {
            this.profileId = firebaseUser.uid
        }

        if(profileId == firebaseUser.uid)
        {
            editProfile_btn_profile_fragment.text = "Edit Profile"
            recyclerView?.suppressLayout(false)
        }
        else if(profileId == "Logout Has Been Done")
        {
            editProfile_btn_profile_fragment.text = "Edit Profile"
            profileId = firebaseUser.uid
            recyclerView?.suppressLayout(false)
        }
        else if(profileId != firebaseUser.uid)
        {
            checkIsFollowing()
        }

        editProfile_btn_profile_fragment.setOnClickListener{
            val edit_follow_btn = editProfile_btn_profile_fragment.text.toString()
            when{
                edit_follow_btn.lowercase() == "edit profile" ->{
                    startActivity(Intent(context,EditProfileActivity::class.java))
                }
                edit_follow_btn.lowercase() == "request sent" ->{
                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(it1.toString())
                            .child("Sent Requests").child(profileId).removeValue().addOnCompleteListener{task->
                                if(task.isSuccessful)
                                {
                                    editProfile_btn_profile_fragment.text = "Follow"
                                }
                            }
                    }

                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(profileId)
                            .child("Received Requests").child(it1.toString()).removeValue()
                    }
                }
                edit_follow_btn.lowercase()=="follow"->{
                    val currentTime = System.currentTimeMillis().toString()
                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(it1.toString())
                            .child("Sent Requests").child(profileId).child("timeStamp").setValue(currentTime).addOnCompleteListener{task->
                                if(task.isSuccessful)
                                {
                                    editProfile_btn_profile_fragment.text = "Request Sent"
                                    saveNotification("3","",profileId)
                                    sendNotification("has sent you a follow request",profileId)
                                }
                            }
                    }
                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(profileId)
                            .child("Received Requests").child(it1.toString()).child("timeStamp").setValue(currentTime)
                    }
                }
                edit_follow_btn.lowercase()=="following"->{
                    recyclerView?.suppressLayout(true)
                    scrollView.isNestedScrollingEnabled = false
                    recyclerView?.isNestedScrollingEnabled = false

                    firebaseUser = FirebaseAuth.getInstance().currentUser!!
                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(it1.toString())
                            .child("Following").child(profileId).removeValue().addOnCompleteListener{task->
                                if(task.isSuccessful)
                                {
                                    editProfile_btn_profile_fragment.text = "Follow"
                                }
                            }
                    }

                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(profileId)
                            .child("Followers").child(it1.toString()).removeValue()
                    }
                }
            }
        }

        retrievePosts()

        badgeSetForNotifications()

        return view
    }

    private fun sendNotification(message: String,receiver: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(temp_snapshot in snapshot.children)
                {
                    val user = temp_snapshot.getValue(User::class.java)
                    if(user?.getUid() == firebaseUser.uid.toString())
                    {
                        val token = "/topics/$receiver"
                        val sender = Html.fromHtml("<b>"+ user.getUsername() +"</b >" + "   "+ message)
                        val notificationsSender  = context?.let {
                            FcmNotificationsSender(
                                token, "ShareNCare", sender.toString(), it, MainActivity()
                            )
                        }
                        notificationsSender?.SendNotifications()
                        break;
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun badgeSetForNotifications() {
        var counter = 0
        val navView = (activity as MainActivity).navView
        var badge_notifications = navView?.getOrCreateBadge(R.id.nav_notifications)

        val notificationRef = FirebaseDatabase.getInstance().reference.child("Notifications")
        notificationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                counter = 0
                for(temp_snapshot in snapshot.children)
                {
                    val notification = temp_snapshot.getValue(Notification::class.java)
                    if((notification?.getReceiver() == firebaseUser.uid)
                        && notification.getSeen().equals("false")){
                        counter++
                        println(counter)
                    }
                    else{
                        println("Same")
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
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }


    private fun saveNotification(type : String,postID: String,receiver: String) {
        val currentTime = System.currentTimeMillis().toString()

        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications")

        val notiMap = HashMap<String,Any>()
        notiMap["type"] = type
        notiMap["sender"] = firebaseUser.uid.toString()
        notiMap["postID"] = postID
        notiMap["receiver"] = receiver
        notiMap["seen"] = "false"
        notiMap["notificationID"] = currentTime

        notiRef.child(currentTime).updateChildren(notiMap).addOnCompleteListener{ task->
            if(task.isSuccessful)
            {
                System.out.println("Received Request saved successfully")
            }
            else
            {
                System.out.println("Received Request didn't saved ")
            }
        }
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var newList : MutableList<Post>? = null
                newList = ArrayList()
                for(temp_snapshot in snapshot.children){
                    val post = temp_snapshot.getValue(Post :: class.java)
                    val id = profileId
                    if(id == post?.getPublisher()) {
                        newList.add(post)
                    }
                }
                val oldList = postAdapter?.getItems()
                val result = oldList?.let { MyDiffCallBack(it, newList) }
                    ?.let { DiffUtil.calculateDiff(it) }
                postAdapter?.setItems(newList)
                postAdapter?.let { result?.dispatchUpdatesTo(it) }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun checkIsFollowing(){
        val followRef = firebaseUser.uid.let {
            FirebaseDatabase.getInstance().reference.child("Follow").child(it.toString()).child("Following")
        }

        followRef.addValueEventListener(object : ValueEventListener{
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(profileId).exists())
                {
                    editProfile_btn_profile_fragment.text = "Following"
                    recyclerView?.visibility = View.VISIBLE
                    recyclerView?.suppressLayout(false)
                    scrollView.isNestedScrollingEnabled = true
                    recyclerView?.isNestedScrollingEnabled = true
                }
                else
                {
                    val requestRef = firebaseUser.uid.let {
                        FirebaseDatabase.getInstance().reference.child("Follow").child(it.toString()).child("Sent Requests")
                    }
                    requestRef.addValueEventListener(object : ValueEventListener{
                        override fun onDataChange(snapshot2: DataSnapshot) {
                            if(snapshot2.child(profileId).exists())
                            {
                                editProfile_btn_profile_fragment.text = "Request Sent"
                                recyclerView?.visibility = View.INVISIBLE
                                recyclerView?.suppressLayout(true)
                                scrollView.isNestedScrollingEnabled = false
                                recyclerView?.isNestedScrollingEnabled = false
                            }
                            else
                            {
                                editProfile_btn_profile_fragment.text = "Follow"
                                recyclerView?.visibility = View.INVISIBLE
                                recyclerView?.suppressLayout(true)
                                scrollView.isNestedScrollingEnabled = false
                                recyclerView?.isNestedScrollingEnabled = false
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference.child("Follow").child(profileId)
            .child("Followers")


        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    followers_textView_profile_fragment.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun getFollowing() {
        val followingRef = FirebaseDatabase.getInstance().reference.child("Follow").child(profileId)
            .child("Following")


        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    following_textView_profile_fragment.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    //called in onStart method
    private fun userInfo()
    {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(profileId)

        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    val user = snapshot.getValue<User>(User :: class.java)
                    if(user == null)
                    {
                        Log.d("myTag", "onDataChange: ")
                    }
                    else
                    {
                        username_textView_profile_fragment.text = user.getUsername()
                        fullName_textView_profile_fragment.text = user.getFullname()
                        bio_textView_profile_fragment.text = user.getBio()

                        val storageRef : StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(user.getImage())
                        storageRef.downloadUrl.addOnSuccessListener ( object : OnSuccessListener<Uri> {
                            override fun onSuccess(p0: Uri?) {
                                context?.let {
                                    Glide.with(it).load(user.getImage()).fitCenter().diskCacheStrategy(
                                        DiskCacheStrategy.ALL)
                                        .error(R.drawable.profile)
                                        .dontTransform().into(profile_picture_profile_fragment)
                                }
                                //Picasso.get().load(user.getImage()).into(profile_picture_profile_fragment)
                            }
                        }).addOnFailureListener(object : OnFailureListener{
                            override fun onFailure(p0: Exception) {
                                Toast.makeText(context,"Exception Found while loading image in profile fragment", Toast.LENGTH_LONG).show()
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onStop() {
        super.onStop()
        val preference = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        preference?.putString("profileId",firebaseUser?.uid)
        preference?.apply()
    }

    override fun onStart() {
        super.onStart()
        val preference = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        preference?.putString("profileId",firebaseUser.uid)
        preference?.apply()

        getFollowers()
        getFollowing()
        userInfo()
    }

    override fun onDestroy() {
        super.onDestroy()
        val preference = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        preference?.putString("profileId","Logout Has Been Done")
        preference?.putString("publisherId","Logout Has Been Done")
        preference?.apply()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}