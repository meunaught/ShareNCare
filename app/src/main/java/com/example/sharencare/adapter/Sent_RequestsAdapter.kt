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
import com.example.sharencare.fragments.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView

class Sent_RequestsAdapter(private var mContext : Context,
                           private var mUser : MutableList<User>,
                           private var isFragment : Boolean = false) : RecyclerView.Adapter<Sent_RequestsAdapter.ViewHolder>()
{
    private var firebaseuser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Sent_RequestsAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.sent_requests_layout,parent,false)
        return Sent_RequestsAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    override fun getItemId(position: Int): Long {
        return mUser[position].getUid().hashCode().toLong()
    }

    @SuppressLint("CommitPrefEdits")
    override fun onBindViewHolder(holder: Sent_RequestsAdapter.ViewHolder, position: Int) {
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
            if (holder.followButton.text.toString().lowercase() == "remove request") {
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

    class ViewHolder(@NonNull itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        var usernameTextView : TextView = itemView.findViewById(R.id.username_textview_sent_requests_layout)
        var fullnameTextView : TextView = itemView.findViewById(R.id.fullname_textview_sent_requests_layout)
        var profileImage : CircleImageView = itemView.findViewById(R.id.profileImage_sent_requests_layout)
        var followButton : AppCompatButton = itemView.findViewById(R.id.follow_btn_sent_requests_layout)
    }
}

