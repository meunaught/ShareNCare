package com.example.sharencare.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sharencare.*
import com.example.sharencare.Model.Comment
import com.example.sharencare.Model.Post
import com.example.sharencare.Model.User
import com.example.sharencare.R
import com.example.sharencare.fragments.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.*


class PostAdapter(private var mContext : Context,
                  private var mPost : List<Post>,private var mainActivity: MainActivity) : RecyclerView.Adapter<PostAdapter.ViewHolder>()
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
                val browserIntent = Intent(Intent.ACTION_VIEW)
                browserIntent.setDataAndType(Uri.parse(post.getPostPdf()),"application/pdf")
                println(post.getPostPdf())
                val chooser = Intent.createChooser(browserIntent,post.getPostPdfName())
                chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK // optional
                mContext.startActivity(chooser)
            }
        }

        holder.postImage.setOnClickListener{
            if(!post.getPostImage().isEmpty()){
                val browserIntent = Intent(Intent.ACTION_VIEW)
                browserIntent.setDataAndType(Uri.parse(post.getPostImage()),
                    "image/*"
                )
                println(post.getPostImage())
                val chooser = Intent.createChooser(browserIntent,post.getPostImage())
                chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK // optional
                mContext.startActivity(chooser)
            }
        }

        holder.username.setOnClickListener {
            val preference = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            preference.putString("profileId",post.getPublisher())
            preference.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                R.id.frame_layout_activity_main,ProfileFragment()).addToBackStack(null).commit()
        }

        holder.profileImage.setOnClickListener {
            val preference = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            preference.putString("profileId",post.getPublisher())
            preference.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                R.id.frame_layout_activity_main,ProfileFragment()).addToBackStack(null).commit()
        }

        publisherInfo(holder.username,holder.profileImage,post.getPublisher())
        likeInfoLoading(post.getPostID(),holder)
        getLike(post.getPostID(),holder)
        getComment(post.getPostID(),holder)
    }

    private fun saveNotification(type : String,postID: String,publisher: String) {
        if(firebaseuser?.uid.toString() == publisher)
        {
            return
        }
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
                sendNotification(publisher)
            }
            else
            {
                System.out.println("Like didn't saved ")
            }
        }
    }

    private fun sendNotification(publisher: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(temp_snapshot in snapshot.children)
                {
                    val user = temp_snapshot.getValue(User::class.java)
                    if(user?.getUid() == firebaseuser?.uid.toString())
                    {
                        val token = "/topics/$publisher"
                        println(token)
                        val sender = Html.fromHtml("<b>"+ user.getUsername() +"</b >" + "   "+ "has liked your post")
                        val notificationsSender  =  FcmNotificationsSender(token,"ShareNCare"
                            ,sender.toString(),mContext,MainActivity())
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

    private fun getComment(postID: String,holder: ViewHolder)
    {
        val commentRef = FirebaseDatabase.getInstance().reference.child("Comments")

        commentRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var counter = 0
                for(temp_Snapshot in snapshot.children)
                {
                    val comment = temp_Snapshot.getValue(Comment::class.java)
                    if(comment?.getPostID() == postID)
                    {
                        counter++
                    }
                }
                if(counter>0)
                {
                    holder.commentNumber.text = counter.toString()
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


    private fun downloadImageFile(context: Context,fileName: String,url: String?){
        /*val homeFragment : HomeFragment = HomeFragment()
        homeFragment.givePermissions(context,mainActivity,fileName,url)*/
        val permissionArray = arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission
                (context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            val preference = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            preference.putString("fileName",fileName)
            preference.putString("url",url)
            preference.apply()

            ActivityCompat.requestPermissions(mainActivity, permissionArray, 1)
            println("Request wanted")
        }
        else{
            val request : DownloadManager.Request = DownloadManager.Request(Uri.parse(url))
            request.setTitle(fileName)
            request.allowScanningByMediaScanner()
            request.setAllowedOverMetered(true)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName)
            val dm : DownloadManager?= context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
            dm?.enqueue(request)
        }
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