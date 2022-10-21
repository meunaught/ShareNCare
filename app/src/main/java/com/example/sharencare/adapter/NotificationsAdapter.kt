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
import com.example.sharencare.Model.Notification
import com.example.sharencare.Model.User
import com.example.sharencare.R
import com.example.sharencare.fragments.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class NotificationsAdapter(private var mContext : Context,
                           private var mNoti : MutableList<Notification>,
                           private var isFragment : Boolean = false) : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>()
{
    private var firebaseuser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.notifications_layout,parent,false)
        return NotificationsAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mNoti.size
    }

    @SuppressLint("CommitPrefEdits")
    override fun onBindViewHolder(holder: NotificationsAdapter.ViewHolder, position: Int) {
        val notification = mNoti[position]
        setSender(holder,notification)

        holder.itemView.setOnClickListener {

        }
        setDescription(holder,notification)
    }

    private fun setDescription(holder:ViewHolder,notification: Notification) {
        if(notification.getType().equals("1"))
        {
            holder.description.text = "Has liked your post"
        }
        else if(notification.getType().equals("2"))
        {
            holder.description.text = "Has commented your post"
        }
        else if(notification.getType().equals("3"))
        {
            holder.description.text = "Has sent you a follow request"
        }
        else if(notification.getType().equals("4"))
        {
            holder.description.text = "Has accepted your follow request"
        }
    }

    private fun setSender(holder: ViewHolder,sender : Notification)
    {
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users")
        userRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(snapshot in dataSnapshot.children)
                {
                    val user = snapshot.getValue(User::class.java)
                    if(user?.getUid() == sender.getSender())
                    {
                        holder.usernameTextView.text = user.getUsername()
                        Glide.with(mContext).load(user.getImage()).fitCenter().diskCacheStrategy(
                            DiskCacheStrategy.ALL)
                            .error(R.drawable.profile)
                            .dontTransform().into(holder.profileImage)
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
        var usernameTextView : TextView = itemView.findViewById(R.id.username_textview_notifications_layout)
        var profileImage : CircleImageView = itemView.findViewById(R.id.profileImage_notifications_layout)
        var description : TextView = itemView.findViewById(R.id.description_textview_notifications_layout)
    }
}

