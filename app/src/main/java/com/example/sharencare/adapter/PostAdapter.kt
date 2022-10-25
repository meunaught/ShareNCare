package com.example.sharencare.adapter

import kotlinx.coroutines.*
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
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
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sharencare.CommentActivity
import com.example.sharencare.EditProfileActivity
import com.example.sharencare.Model.Post
import com.example.sharencare.Model.User
import com.example.sharencare.R
import com.example.sharencare.fragments.ProfileFragment
import com.example.sharencare.fragments.SignInFragment
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.Exception
import java.net.MalformedURLException
import java.net.URL
import java.net.UnknownServiceException
import kotlin.math.sign

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

    fun getItems() : List<Post>{
        return this.mPost
    }

    fun setItems(posts : List<Post>){
        mPost = posts
    }

    override fun getItemId(position: Int): Long {
        return mPost[position].getPostID().hashCode().toLong()
    }

    @SuppressLint("CommitPrefEdits")
    override fun onBindViewHolder(holder: PostAdapter.ViewHolder, position: Int) {
        firebaseuser = FirebaseAuth.getInstance().currentUser
        val post = mPost[position]
        if(!post.getPostImage().isEmpty())
        {
            holder.postImage.layout(0,0,0,0)
            Glide.with(mContext).load(post.getPostImage()).fitCenter().diskCacheStrategy(
                DiskCacheStrategy.ALL)
                .error(R.drawable.profile)
                .dontTransform().into(holder.postImage)
        }
        else
        {
            holder.postImage.layoutParams.height = 0
        }
        if(post.getDescription().isEmpty())
        {
            holder.description.layoutParams.height = 0
        }
        else
        {
            holder.description.text = post.getDescription()
        }
        if(post.getPostPdfName().isEmpty())
        {
            holder.postPdf.layoutParams.height = 0
        }
        else{
            holder.postPdf.text = post.getPostPdfName()
        }

        holder.likeBtn.setOnClickListener {
            if(holder.likeBtnState)
            {
                Picasso.get().load(R.drawable.heart_not_clicked).fit().centerInside().into(holder.likeBtn)
                removeLikeFromFirebase(post.getPostID())
                if(holder.likeNumber.text.toString() == "1")
                {
                    holder.likeNumber.text = ""
                }
            }
            else {
                Picasso.get().load(R.drawable.heart_clicked).fit().centerInside().into(holder.likeBtn)
                saveLikeIntoFirebase(post.getPostID())
                saveNotification("1",post.getPostID(),post.getPublisher())
            }
        }
        holder.commentBtn.setOnClickListener {
            val preference = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            preference.putString("postID",post.getPostID())
            preference.apply()

            mContext.startActivity(Intent(mContext,CommentActivity::class.java))
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
        likeInfoLoading(post.getPostID(),holder)
        getLike(post.getPostID(),holder)
    }

    private fun saveNotification(type : String,postID: String,publisher: String) {
        val currentTime = System.currentTimeMillis().toString()

        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(currentTime)

        val notiMap = HashMap<String,Any>()
        notiMap["type"] = type
        notiMap["sender"] = firebaseuser?.uid.toString()
        notiMap["postID"] = postID
        notiMap["receiver"] = publisher
        notiMap["seen"] = "false"
        notiMap["notificationID"] = currentTime

        notiRef.updateChildren(notiMap).addOnCompleteListener{ task->
            if(task.isSuccessful)
            {
                System.out.println("Like saved successfully")
            }
            else
            {
                System.out.println("Like didn't saved ")
            }
        }
    }

    private fun getLike(postID: String,holder: ViewHolder) {
        val likeRef = FirebaseDatabase.getInstance().reference.child("Like").child(postID)

        likeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    holder.likeNumber.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun likeInfoLoading(postID: String,holder: ViewHolder) {
        val userRef =  firebaseuser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference.child("Like")
                .child(postID).child(it1.toString())
        }

        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    holder.likeBtnState = true
                    Picasso.get().load(R.drawable.heart_clicked).fit().centerInside().into(holder.likeBtn)
                }
                else
                {
                    holder.likeBtnState = false
                    Picasso.get().load(R.drawable.heart_not_clicked).fit().centerInside().into(holder.likeBtn)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun saveLikeIntoFirebase(postID : String) {
        firebaseuser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference.child("Like")
                .child(postID).child(it1.toString()).setValue(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {
                        System.out.println("Added to firebase")
                    }
                }
        }
    }


    private fun removeLikeFromFirebase(postID : String) {
        firebaseuser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference.child("Like")
                .child(postID)
                .child(it1.toString()).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {
                        System.out.println("Removed to firebase")
                    }
                }
        }
    }


    private fun loadImage(postImageUrl: String, postImage: ImageView) {
        runBlocking {
            launch {
                val result = async(Dispatchers.IO) {
                    val url = URL(postImageUrl)

                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }

                    BitmapFactory.decodeStream(url.openStream(),null,options)
                    val inSampleSizeVal = calculateInSampleSize(options,100,300)

                    val finalOptions = BitmapFactory.Options().apply {
                        inJustDecodeBounds  = true
                        inSampleSize = inSampleSizeVal
                    }

                    return@async BitmapFactory.decodeStream(url.openStream(),null,finalOptions)
                }
                try {
                    val bitmap = result.await()
                    postImage.setImageBitmap(bitmap)
                }
                catch(e : IOException)
                {
                    System.out.println("Error")
                }
                catch (e: UnknownServiceException)
                {
                    System.out.println("Error2")
                }
                catch(e : MalformedURLException){
                    System.out.println("Error")
                }
            }
        }
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
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
                    Glide.with(mContext.applicationContext).load(user?.getImage()).fitCenter().diskCacheStrategy(
                        DiskCacheStrategy.ALL)
                        .dontTransform().into(profileImage)
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
        var likeBtnState : Boolean = false
        var username : TextView = itemView.findViewById(R.id.username_postLayout)
        var profileImage : CircleImageView = itemView.findViewById(R.id.user_profile_image_postLayout)
        var description : TextView = itemView.findViewById(R.id.postDescriptionTextView_postLayout)
        var postImage : ImageView = itemView.findViewById(R.id.post_image_postLayout)
        var postPdf : TextView = itemView.findViewById(R.id.pdfTextView_postLayout)
        var likeNumber : TextView = itemView.findViewById(R.id.likeNumberTextView_postLayout)
        var likeBtn : ImageButton = itemView.findViewById(R.id.post_image_like_btn)
        var commentNumber : TextView = itemView.findViewById(R.id.commentNumberTextView_postLayout)
        var commentBtn : ImageButton = itemView.findViewById(R.id.post_image_comment_btn)
    }
}