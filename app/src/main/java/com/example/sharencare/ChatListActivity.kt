package com.example.sharencare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.uikit.ui_components.cometchat_ui.CometChatUI
import com.example.sharencare.Model.User
import com.example.sharencare.adapter.ChatUserAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class ChatListActivity : AppCompatActivity() {

//    private lateinit var userRecyclerview : RecyclerView
//    private lateinit var userList : ArrayList<User>
//    private lateinit var adapter: ChatUserAdapter
//    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(this@ChatListActivity, CometChatUI::class.java))
//        setContentView(R.layout.activity_chat_list)
//        userList = ArrayList()
//        adapter = ChatUserAdapter(this, userList)
//        mAuth = FirebaseAuth.getInstance()
//
//        userRecyclerview = findViewById(R.id.userRecyclerView)
//        userRecyclerview.layoutManager = LinearLayoutManager(this)
//        userRecyclerview.adapter = adapter
//
//        val userRef = FirebaseDatabase.getInstance().getReference().child("Users")
//        userRef.addValueEventListener(object : ValueEventListener
//        {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//
//                    userList?.clear()
//
//                    for(snapshot in dataSnapshot.children)
//                    {
//                        val user = snapshot.getValue(User::class.java)
//                        if(user!=null)
//                        {
//                            userList?.add(user)
//                        }
//                    }
//                    adapter?.notifyDataSetChanged()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//        })
        
    }
}