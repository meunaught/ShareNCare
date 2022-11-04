package com.example.sharencare.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sharencare.CommentActivity
import com.example.sharencare.Model.Notification
import com.example.sharencare.Model.User
import com.example.sharencare.R
import com.example.sharencare.fragments.ProfileFragment
import com.example.sharencare.fragments.ReceivedRequestsFragment
import com.github.marlonlom.utilities.timeago.TimeAgo
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

    override fun getItemId(position: Int): Long {
        return mNoti[position].getNotificationID().hashCode().toLong()
    }

    @SuppressLint("CommitPrefEdits")
    override fun onBindViewHolder(holder: NotificationsAdapter.ViewHolder, position: Int) {
        val notification = mNoti[position]
        setSender(holder,notification)

        holder.itemView.setOnClickListener {

        }

        holder.linear_layout.setOnClickListener {
            if(notification.getType() == "1")
            {
                val preference = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
                preference.putString("postID",notification.getPostID())
                preference.apply()

                mContext.startActivity(Intent(mContext, CommentActivity::class.java))
            }
            else if(notification.getType() == "2")
            {
                val preference = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
                preference.putString("postID",notification.getPostID())
                preference.apply()

                mContext.startActivity(Intent(mContext, CommentActivity::class.java))
            }
            else if(notification.getType() == "3")
            {
                (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                    R.id.frame_layout_activity_main, ReceivedRequestsFragment()
                ).addToBackStack(null).commit()
            }
            else if(notification.getType() == "4")
            {
                val preference = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
                preference.putString("profileId",notification.getSender())
                preference.apply()

                (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                    R.id.frame_layout_activity_main,ProfileFragment()).addToBackStack(null).commit()
            }
        }
        //setDescription(holder,notification)
    }

    private fun setDescription(holder:ViewHolder,notification: Notification) {
        if(notification.getType().equals("1"))
        {
            holder.messageTextView.text = Html.fromHtml("<b>"+ holder.username +"</b >" + "   "+ "has liked your post")
            val text = TimeAgo.using(notification.getNotificationID().toLong())
            holder.timeAgo.text = text.toString()
        }
        else if(notification.getType().equals("2"))
        {
            holder.messageTextView.text = Html.fromHtml("<b>"+ holder.username +"</b >" + "   "+ "has commented on your post")
            val text = TimeAgo.using(notification.getNotificationID().toLong())
            holder.timeAgo.text = text.toString()
        }
        else if(notification.getType().equals("3"))
        {
            holder.messageTextView.text = Html.fromHtml("<b>"+ holder.username +"</b >" + "   "+ "has sent you a follow request")
            val text = TimeAgo.using(notification.getNotificationID().toLong())
            holder.timeAgo.text = text.toString()
        }
        else if(notification.getType().equals("4"))
        {
            holder.messageTextView.text = Html.fromHtml("<b>"+ holder.username +"</b >" + "   "+ "has accepted your follow request")
            val text = TimeAgo.using(notification.getNotificationID().toLong())
            holder.timeAgo.text = text.toString()
        }
    }

    private fun setSender(holder: ViewHolder,sender : Notification)
    {
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(snapshot in dataSnapshot.children)
                {
                    val user = snapshot.getValue(User::class.java)
                    if(user?.getUid() == sender.getSender())
                    {
                        holder.username = user.getUsername()
                        Glide.with(mContext).load(user.getImage()).fitCenter().diskCacheStrategy(
                            DiskCacheStrategy.ALL)
                            .error(R.drawable.profile)
                            .dontTransform().into(holder.profileImage)
                        setDescription(holder,sender)
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
        var messageTextView : TextView = itemView.findViewById(R.id.message_textview_notifications_layout)
        var profileImage : CircleImageView = itemView.findViewById(R.id.profileImage_notifications_layout)
        var timeAgo : TextView = itemView.findViewById(R.id.time_textview_notifications_layout)
        var username : String = ""
        var linear_layout : LinearLayout = itemView.findViewById(R.id.linear_layout2_notifications_layout)
    }
}