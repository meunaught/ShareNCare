package com.example.sharencare.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.example.sharencare.Model.User
import com.example.sharencare.R
import com.example.sharencare.fragments.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(private var mContext : Context,
                    private var mUser : List<User>,
                    private var isFragment : Boolean = false) : RecyclerView.Adapter<UserAdapter.ViewHolder>()
{
    private var firebaseuser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_info_layout,parent,false)
        return UserAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    @SuppressLint("CommitPrefEdits")
    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val user = mUser[position]
        holder.usernameTextView.text = user.getUsername()
        holder.fullnameTextView.text = user.getFullname()
        Picasso.get().load(user.getImage()).into(holder.profileImage)
        checkFollowingStatus(user.getUid(),holder.followButton)

        holder.itemView.setOnClickListener {
            val preference = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            preference.putString("profileId",user.getUid())
            preference.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                R.id.frame_layout_activity_main,ProfileFragment()).commit()
        }


        holder.followButton.setOnClickListener {
            if (holder.followButton.text.toString().lowercase() == "follow") {
                Log.d("myTag", "BindViewHolder inside if");
                firebaseuser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference.child("Follow").child(it1.toString())
                        .child("Following").child(user.getUid()).setValue(true)
                        .addOnCompleteListener { task ->

                            if (task.isSuccessful) {

                                firebaseuser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference.child("Follow")
                                        .child(user.getUid()).child("Followers")
                                        .child(it1.toString()).setValue(true)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful)
                                            { }
                                        }
                                }
                            }
                        }
                }
            }
            else
            {
                Log.d("myTag", "BindViewHolder");
                firebaseuser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference.child("Follow").child(it1.toString())
                        .child("Following").child(user.getUid()).removeValue()
                        .addOnCompleteListener { task ->

                            if (task.isSuccessful) {

                                firebaseuser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference.child("Follow")
                                        .child(user.getUid()).child("Followers")
                                        .child(it1.toString()).removeValue()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful)
                                            {

                                            }
                                        }
                                }
                            }
                        }
                }
            }
        }

    }

    class ViewHolder(@NonNull itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        var usernameTextView : TextView = itemView.findViewById(R.id.username_textview_user_info_layout)
        var fullnameTextView : TextView = itemView.findViewById(R.id.fullname_textview_user_info_layout)
        var profileImage : CircleImageView = itemView.findViewById(R.id.profileImage_user_info_layout)
        var followButton : AppCompatButton = itemView.findViewById(R.id.follow_btn_user_info_layout)
    }

    private fun checkFollowingStatus(uid: String, followButton: AppCompatButton) {
        val followingRef = firebaseuser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference.child("Follow").child(it1.toString())
                .child("Following")
        }
        followingRef.addValueEventListener(object: ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                if(dataSnapshot.child(uid).exists())
                {
                    followButton.text = "Following"
                }
                else
                {
                    followButton.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}