package com.example.sharencare.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sharencare.FcmNotificationsSender
import com.example.sharencare.MainActivity
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

    override fun getItemId(position: Int): Long {
        return mUser[position].getUid().hashCode().toLong()
    }

    @SuppressLint("CommitPrefEdits")
    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val user = mUser[position]
        holder.usernameTextView.text = user.getUsername()
        holder.fullnameTextView.text = user.getFullname()
        Glide.with(mContext).load(user.getImage()).fitCenter().diskCacheStrategy(
            DiskCacheStrategy.ALL)
            .error(R.drawable.profile)
            .dontTransform().into(holder.profileImage)

        checkFollowingStatus(user.getUid(),holder.followButton)

        holder.itemView.setOnClickListener {
            val preference = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            preference.putString("profileId",user.getUid())
            preference.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                R.id.frame_layout_activity_main,ProfileFragment()).addToBackStack(null).commit()
        }


        holder.followButton.setOnClickListener {
            val currentTime = System.currentTimeMillis().toString()
            if (holder.followButton.text.toString().lowercase() == "follow") {
                firebaseuser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference.child("Follow").child(it1.toString())
                        .child("Sent Requests").child(user.getUid()).child("timeStamp").setValue(currentTime)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseuser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference.child("Follow")
                                        .child(user.getUid()).child("Received Requests")
                                        .child(it1.toString()).child("timeStamp").setValue(currentTime)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful)
                                            {
                                                saveNotification("3","",user.getUid())
                                                if(user.getUid() != firebaseuser?.uid.toString())
                                                {
                                                    retrieveUser("has sent you a follow request",user.getUid())
                                                }
                                            }
                                        }
                                }
                            }
                        }
                }
            }
            else if(holder.followButton.text.toString().lowercase() == "following")
            {
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
                                                checkFollowBack(user.getUid())
                                            }
                                        }
                                }
                            }
                        }
                }
            }
            else
            {
                firebaseuser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference.child("Follow").child(it1.toString())
                        .child("Sent Requests").child(user.getUid()).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                firebaseuser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference.child("Follow")
                                        .child(user.getUid()).child("Received Requests")
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

    private fun checkFollowBack(receiverUid: String) {
        val followingRef = FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseuser?.uid.toString())
            .child("Followers")
        followingRef.addListenerForSingleValueEvent(object : ValueEventListener{
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

    private fun retrieveUser(message : String,receiver: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(temp_snapshot in snapshot.children)
                {
                    val user = temp_snapshot.getValue(User::class.java)
                    if(user?.getUid() == firebaseuser?.uid.toString())
                    {
                        val token = "/topics/$receiver"
                        val sender = Html.fromHtml("<b>"+ user.getUsername() +"</b >" + "   "+ message)
                        val notificationsSender  =  FcmNotificationsSender(token,"ShareNCare"
                            ,sender.toString(),mContext, MainActivity()
                        )
                        notificationsSender.SendNotifications()
                        break;
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun saveNotification(type : String,postID: String,receiver: String) {
        if(firebaseuser?.uid.toString() == receiver)
        {
            return
        }
        val currentTime = System.currentTimeMillis().toString()

        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications")

        val notiMap = HashMap<String,Any>()
        notiMap["type"] = type
        notiMap["sender"] = firebaseuser?.uid.toString()
        notiMap["postID"] = postID
        notiMap["receiver"] = receiver
        notiMap["seen"] = "false"
        notiMap["notificationID"] = currentTime

        notiRef.child(currentTime).updateChildren(notiMap).addOnCompleteListener{ task->
            if(task.isSuccessful)
            {
                System.out.println("Sent Request saved successfully")
            }
            else
            {
                System.out.println("Sent Request didn't saved ")
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
                    val followingRef2 = firebaseuser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("Follow").child(it1.toString())
                            .child("Sent Requests")
                    }
                    followingRef2.addValueEventListener(object: ValueEventListener
                    {
                        override fun onDataChange(dataSnapshot: DataSnapshot)
                        {
                            if(dataSnapshot.child(uid).exists())
                            {
                                followButton.text = "Request Sent"
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

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}