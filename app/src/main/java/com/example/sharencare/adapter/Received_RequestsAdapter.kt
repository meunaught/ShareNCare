package com.example.sharencare.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
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
import com.example.sharencare.FcmNotificationsSender
import com.example.sharencare.MainActivity
import com.example.sharencare.Model.User
import com.example.sharencare.R
import com.example.sharencare.async.friendApiCall
import com.example.sharencare.fragments.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class Received_RequestsAdapter(private var mContext : Context,
                               private var mUser : MutableList<User>,
                               private var isFragment : Boolean = false) : RecyclerView.Adapter<Received_RequestsAdapter.ViewHolder>()
{
    private var firebaseuser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Received_RequestsAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.received_requests_layout,parent,false)
        return Received_RequestsAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    override fun getItemId(position: Int): Long {
        return mUser[position].getUid().hashCode().toLong()
    }

    @SuppressLint("CommitPrefEdits")
    override fun onBindViewHolder(holder: Received_RequestsAdapter.ViewHolder, position: Int) {
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

        holder.acceptButton.setOnClickListener {
            if(position == 0 && mUser.size == 1)
            {
                System.out.println("Inside if")
                mUser.clear();
                this.notifyDataSetChanged()
            }
            if (holder.acceptButton.text.toString().lowercase() == "accept") {
                firebaseuser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference.child("Follow").child(it1.toString())
                        .child("Followers").child(user.getUid()).setValue(true)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseuser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference.child("Follow")
                                        .child(user.getUid()).child("Following")
                                        .child(it1.toString()).setValue(true)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful)
                                            {
                                                saveNotification("4","",user.getUid())
                                                if(user.getUid() != firebaseuser?.uid.toString())
                                                {
                                                    retrieveUser("has accepted your follow request",user.getUid())
                                                }
                                                val uid = firebaseuser?.uid?.lowercase()
                                                friendApiCall().execute(uid, user.getUid().lowercase())
                                            }
                                        }
                                }
                            }
                        }
                }
                firebaseuser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference.child("Follow").child(it1.toString())
                        .child("Received Requests").child(user.getUid()).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseuser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference.child("Follow")
                                        .child(user.getUid()).child("Sent Requests")
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

        holder.denyButton.setOnClickListener {
            if(position == 0 && mUser.size == 1)
            {
                System.out.println("Inside if")
                mUser.clear();
                this.notifyDataSetChanged()
            }
            if (holder.denyButton.text.toString().lowercase() == "deny") {
                firebaseuser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference.child("Follow").child(it1.toString())
                        .child("Received Requests").child(user.getUid()).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseuser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference.child("Follow")
                                        .child(user.getUid()).child("Sent Requests")
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

    private fun retrieveUser(message : String,receiver: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
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
                System.out.println("Received Request saved successfully")
            }
            else
            {
                System.out.println("Received Request didn't saved ")
            }
        }
    }

    class ViewHolder(@NonNull itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        var usernameTextView : TextView = itemView.findViewById(R.id.username_textview_received_requests_layout)
        var fullnameTextView : TextView = itemView.findViewById(R.id.fullname_textview_received_requests_layout)
        var profileImage : CircleImageView = itemView.findViewById(R.id.profileImage_received_requests_layout)
        var acceptButton : AppCompatButton = itemView.findViewById(R.id.accept_btn_received_requests_layout)
        var denyButton : AppCompatButton = itemView.findViewById(R.id.deny_btn_received_requests_layout)
    }
}

