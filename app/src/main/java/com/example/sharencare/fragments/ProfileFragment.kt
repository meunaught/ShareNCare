package com.example.sharencare.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sharencare.EditProfileActivity
import com.example.sharencare.Model.User
import com.example.sharencare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*


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
    private var firebaseUser: FirebaseUser ?= FirebaseAuth.getInstance().currentUser

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
        val view =  inflater.inflate(R.layout.fragment_profile, container, false)

        val preferences = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)
        if(preferences != null)
        {
            this.profileId = preferences.getString("profileId","none").toString()
        }

        if(profileId == firebaseUser?.uid)
        {
            view.editProfile_btn_profile_fragment.text = "Edit Profile"
        }
        else if(profileId != firebaseUser?.uid)
        {
            checkIsFollowing()
        }

        view.editProfile_btn_profile_fragment.setOnClickListener{
            val edit_follow_btn = view?.editProfile_btn_profile_fragment?.text.toString()
            when{
                edit_follow_btn.lowercase() == "edit profile" ->{
                    startActivity(Intent(context,EditProfileActivity::class.java))
                }
                edit_follow_btn.lowercase()=="follow"->{
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(it1.toString())
                            .child("Following").child(profileId).setValue(true).addOnCompleteListener{task->
                                if(task.isSuccessful)
                                {
                                    view?.editProfile_btn_profile_fragment?.text = "Following"
                                }
                            }
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(profileId)
                            .child("Followers").child(it1.toString()).setValue(true)
                    }
                }
                edit_follow_btn.lowercase()=="following"->{
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(it1.toString())
                            .child("Following").child(profileId).removeValue().addOnCompleteListener{task->
                                if(task.isSuccessful)
                                {
                                    view?.editProfile_btn_profile_fragment?.text = "Follow"
                                }
                            }
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(profileId)
                            .child("Followers").child(it1.toString()).removeValue()
                    }
                }
            }
        }
        getFollowers()
        getFollowing()
        userInfo()

        return view
    }

    private fun checkIsFollowing(){
        val followRef = firebaseUser?.uid.let {
            FirebaseDatabase.getInstance().reference.child("Follow").child(it.toString()).child("Following")
        }

        followRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(profileId).exists())
                {
                    view?.editProfile_btn_profile_fragment?.text = "Following"
                }
                else
                {
                    view?.editProfile_btn_profile_fragment?.text = "Follow"
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
                    view?.followers_textView_profile_fragment?.text = snapshot.childrenCount.toString()
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
                    view?.following_textView_profile_fragment?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

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
                        view?.username_textView_profile_fragment?.text = user?.getUsername()
                        view?.fullName_textView_profile_fragment?.text = user?.getFullname()
                        view?.bio_textView_profile_fragment?.text = user?.getBio()
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
        preference?.putString("profileId",firebaseUser?.uid)
        preference?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val preference = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        preference?.putString("profileId",firebaseUser?.uid)
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