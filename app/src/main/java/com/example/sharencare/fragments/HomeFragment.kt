package com.example.sharencare.fragments

import android.app.ProgressDialog;
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.uikit.ui_components.cometchat_ui.CometChatUI
import com.example.sharencare.MainActivity
import com.example.sharencare.Model.Notification
import com.example.sharencare.Model.Post
import com.example.sharencare.R
import com.example.sharencare.adapter.PostAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


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
    private var postAdapter : PostAdapter ?= null
    private var postList : MutableList<Post> ?= null
    private var followingList : MutableList<Post>?= null
    private var firebaseUser : FirebaseUser ?= null

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

        var recyclerView : RecyclerView?= null
        recyclerView = view.findViewById(R.id.recycler_view_home_fragment)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager =linearLayoutManager

        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it,postList as ArrayList<Post>) }
        postAdapter?.setHasStableIds(true)
        recyclerView.adapter = postAdapter
        recyclerView.setItemViewCacheSize(25)

        checkFollowings()
        message_btn_home_fragment = view.findViewById(R.id.message_btn_home_fragment)
        message_btn_home_fragment.setOnClickListener{
            startActivity(Intent(context, CometChatUI::class.java))
        }

        badgeSetForNotifications()

        return view
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
        postsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                postList?.clear()
                var newList : MutableList<Post>? = null
                newList = ArrayList()
                for(temp_snapshot in snapshot.children){
                    val post = temp_snapshot.getValue(Post :: class.java)
                    for(id in (followingList as ArrayList<*>)){
                        if(id == post?.getPublisher())
                        {
                            postList?.add(post!!)
                        }
                    }
                    postAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

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