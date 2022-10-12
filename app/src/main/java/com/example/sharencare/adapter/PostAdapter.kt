package com.example.sharencare.adapter

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.sharencare.Model.Post
import com.example.sharencare.Model.User
import com.example.sharencare.R
import com.example.sharencare.fragments.ProfileFragment
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception

class PostAdapter(private var mContext : Context,
                  private var mPost : List<Post>) : RecyclerView.Adapter<PostAdapter.ViewHolder>()
{
    private var firebaseuser: FirebaseUser? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.post,parent,false)
        return PostAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    @SuppressLint("CommitPrefEdits")
    override fun onBindViewHolder(holder: PostAdapter.ViewHolder, position: Int) {
        firebaseuser = FirebaseAuth.getInstance().currentUser

        val post = mPost[position]
        if(!post.getPostImage().isEmpty())
        {
            Picasso.get().load(post.getPostImage()).into(holder.postImage)
        }
        else
        {
            holder.postImage.layoutParams.height = 0
        }
        holder.description.text = post.getDescription()
        if(post.getPostPdfName().isEmpty())
        {
            holder.postPdf.layoutParams.height = 0
        }
        else{
            holder.postPdf.text = post.getPostPdfName()
        }
        holder.likeBtn.setOnClickListener {

        }
        holder.commentBtn.setOnClickListener {

        }

        holder.postPdf.setOnClickListener {
            if(!post.getPostPdfName().isEmpty()){
                Toast.makeText(mContext,"Long click to download the pdf file",Toast.LENGTH_SHORT).show()
            }
        }
        holder.postPdf.setOnLongClickListener ( object : View.OnLongClickListener{
            override fun onLongClick(p0: View?): Boolean {
                downloadPdfFile(mContext,post.getPostPdfName(),post.getPostPdf())
                return true
            }
        } )

        holder.postImage.setOnClickListener{
            if(!post.getPostImage().isEmpty()){
                Toast.makeText(mContext,"Long click to download the image file",Toast.LENGTH_SHORT).show()
            }
        }

        holder.postImage.setOnLongClickListener(object : View.OnLongClickListener{
            override fun onLongClick(p0: View?): Boolean {
                downloadImageFile(mContext,post.getPostImage(),post.getPostImage())
                return true
            }
        })

        holder.username.setOnClickListener {
            val preference = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            preference.putString("profileId",post.getPublisher())
            preference.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                R.id.frame_layout_activity_main,ProfileFragment()).commit()
        }

        holder.profileImage.setOnClickListener {
            val preference = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            preference.putString("profileId",post.getPublisher())
            preference.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                R.id.frame_layout_activity_main,ProfileFragment()).commit()
        }

        publisherInfo(holder.username,holder.profileImage,post.getPublisher())
    }

    private fun downloadImageFile(context: Context,fileName: String,url: String?){
        val request : DownloadManager.Request = DownloadManager.Request(Uri.parse(url))
        request.setTitle(fileName)
        request.allowScanningByMediaScanner()
        request.setAllowedOverMetered(true)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName)
        val dm : DownloadManager?= context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        dm?.enqueue(request)
    }

    private fun downloadPdfFile(context: Context, fileName: String, url: String?)
    {
        val request : DownloadManager.Request = DownloadManager.Request(Uri.parse(url))
        request.setTitle(fileName)
        request.setMimeType("application/pdf")
        request.allowScanningByMediaScanner()
        request.setAllowedOverMetered(true)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName)
        val dm : DownloadManager?= context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        dm?.enqueue(request)
    }

    private fun publisherInfo(username: TextView, profileImage: CircleImageView, publisher: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisher)
        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    val user = snapshot.getValue<User>(User :: class.java)
                    Picasso.get().load(user?.getImage()).into(profileImage)
                    username.text = user?.getUsername()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    class ViewHolder(@NonNull itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        var username : TextView = itemView.findViewById(R.id.username_postLayout)
        var profileImage : CircleImageView = itemView.findViewById(R.id.user_profile_image_postLayout)
        var description : TextView = itemView.findViewById(R.id.postDescriptionTextView_postLayout)
        var postImage : ImageView = itemView.findViewById(R.id.post_image_postLayout)
        var postPdf : TextView = itemView.findViewById(R.id.pdfTextView_postLayout)
        var likeNumber : TextView = itemView.findViewById(R.id.likeNumberTextView_postLayout)
        var likeBtn : ImageButton = itemView.findViewById(R.id.post_image_like_btn)
        var commentNumber : TextView = itemView.findViewById(R.id.commentNumberTextView_postLayout)
        var commentBtn : ImageButton = itemView.findViewById(R.id.post_image_comment_btn)
        var comments : TextView = itemView.findViewById(R.id.comments_postLayout)
    }
}