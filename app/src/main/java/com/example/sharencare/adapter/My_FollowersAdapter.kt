package com.example.sharencare.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sharencare.Model.User
import com.example.sharencare.R
import com.example.sharencare.async.deleteFriendApiCall
import com.example.sharencare.fragments.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class My_FollowersAdapter(private var mContext : Context,
                          private var mUser : MutableList<User>,
                          private var isFragment : Boolean = false) : RecyclerView.Adapter<My_FollowersAdapter.ViewHolder>()
{
    private var firebaseuser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): My_FollowersAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.my_followers_layout,parent,false)
        return My_FollowersAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    override fun getItemId(position: Int): Long {
        return mUser[position].getUid().hashCode().toLong()
    }

    @SuppressLint("CommitPrefEdits")
    override fun onBindViewHolder(holder: My_FollowersAdapter.ViewHolder, position: Int) {
        val user = mUser[position]
        holder.usernameTextView.text = user.getUsername()
        holder.fullnameTextView.text = user.getFullname()
        Glide.with(mContext).load(user.getImage()).fitCenter().diskCacheStrategy(
            DiskCacheStrategy.ALL)
            .error(R.drawable.profile)
            .dontTransform().into(holder.profileImage)

        holder.itemView.setOnClickListener {
            val preference = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            preference.putString("profileId",user.getUid())
            preference.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                R.id.frame_layout_activity_main,ProfileFragment()).commit()
        }

        holder.followButton.setOnClickListener {
            if(position == 0 && mUser.size == 1)
            {
                System.out.println("Inside if")
                mUser.clear();
                this.notifyDataSetChanged()
            }
            if (holder.followButton.text.toString().lowercase() == "remove follower") {
                firebaseuser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference.child("Follow").child(it1.toString())
                        .child("Followers").child(user.getUid()).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseuser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference.child("Follow")
                                        .child(user.getUid()).child("Following")
                                        .child(it1.toString()).removeValue()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful)
                                            {
                                                checkFollowBack(user.getUid())
                                                holder.followButton.text = "Follower Removed"
                                            }
                                        }
                                }
                            }
                        }
                }
            }
        }
    }

    private fun checkFollowBack(receiverUid: String) {
        val followingRef = FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseuser?.uid.toString())
            .child("Following")
        followingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var now = false
                if(snapshot.exists())
                {
                    for(temp_snapshot in snapshot.children){
                        if (receiverUid == temp_snapshot.key) {
                            now = true
                            break
                        }
                    }
                    if(!now) {
                        val uid = firebaseuser?.uid?.lowercase()
                        deleteFriendApiCall().execute(uid, receiverUid.lowercase())
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    class ViewHolder(@NonNull itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        var usernameTextView : TextView = itemView.findViewById(R.id.username_textview_my_followers_layout)
        var fullnameTextView : TextView = itemView.findViewById(R.id.fullname_textview_my_followers_layout)
        var profileImage : CircleImageView = itemView.findViewById(R.id.profileImage_my_followers_layout)
        var followButton : AppCompatButton = itemView.findViewById(R.id.follow_btn_my_followers_layout)
    }
}

