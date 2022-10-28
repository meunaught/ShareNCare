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
import com.example.sharencare.Model.Comment
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

class CommentsAdapter(private var mContext : Context,
                           private var mComments : MutableList<Comment>,
                           private var isFragment : Boolean = false) : RecyclerView.Adapter<CommentsAdapter.ViewHolder>()
{
    private var firebaseuser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comment_layout,parent,false)
        return CommentsAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mComments.size
    }

    @SuppressLint("CommitPrefEdits")
    override fun onBindViewHolder(holder: CommentsAdapter.ViewHolder, position: Int) {
        val comment = mComments[position]
        setPublisher(comment.getPublisher(),holder)
        holder.message.text = comment.getMessage()
    }

    private fun setPublisher(publisher: String,holder: ViewHolder) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(temp_snapshot in snapshot.children)
                {
                    val user = temp_snapshot.getValue(User :: class.java)
                    if(publisher == user?.getUid())
                    {
                        holder.usernameTextView.text = user.getUsername()
                        Glide.with(mContext).load(user.getImage()).fitCenter().diskCacheStrategy(
                            DiskCacheStrategy.ALL)
                            .error(R.drawable.profile)
                            .dontTransform().into(holder.profileImage)
                        break;
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
        var usernameTextView : TextView = itemView.findViewById(R.id.username_textview_comment_layout)
        var profileImage : CircleImageView = itemView.findViewById(R.id.user_profile_image_comment_layout)
        var message : TextView = itemView.findViewById(R.id.comment_textview_comment_layout)
    }
}

