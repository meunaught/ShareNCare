package com.example.sharencare

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sharencare.Model.Comment
import com.example.sharencare.Model.Notification
import com.example.sharencare.Model.Post
import com.example.sharencare.Model.User
import com.example.sharencare.adapter.CommentsAdapter
import com.example.sharencare.adapter.NotificationsAdapter
import com.example.sharencare.adapter.PostAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentActivity : AppCompatActivity() {
    private var likeBtnState : Boolean = false
    private var username : TextView ?= null
    private var profileImage : CircleImageView ? = null
    private var description : TextView ? = null
    private var postImage : ImageView ? = null
    private var postPdf : TextView ? = null
    private var likeNumber : TextView ? = null
    private var likeBtn : ImageButton ? = null
    private var commentNumber : TextView ? = null
    private var postID : String = ""
    private var postCreator : Post ?= null
    private var postPdfUrl : String = ""
    private var firebaseUser : FirebaseUser ?= null
    private var comment : EditText?= null
    private var sendBtn : ImageButton ?= null

    private var recyclerview : RecyclerView?= null
    private var commentAdapter : CommentsAdapter? = null
    private var mComments : MutableList<Comment>?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        username = findViewById(R.id.username_comment_activity)
        profileImage = findViewById(R.id.user_profile_image_comment_activity)
        description = findViewById(R.id.postDescriptionTextView_comment_activity)
        postImage = findViewById(R.id.post_image_comment_activity)
        postPdf = findViewById(R.id.pdfTextView_comment_activity)
        likeNumber = findViewById(R.id.likeNumberTextView_comment_activity)
        likeBtn = findViewById(R.id.post_image_like_btn_comment_activity)
        commentNumber = findViewById(R.id.commentNumberTextView_comment_activity)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        comment = findViewById(R.id.editText_comment_activity)
        sendBtn = findViewById(R.id.sendImageBtn_comment_activity)


        recyclerview = findViewById(R.id.recycler_view_comment_activity)
        recyclerview?.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerview?.layoutManager = linearLayoutManager

        mComments = ArrayList()
        commentAdapter = CommentsAdapter(this,mComments as ArrayList<Comment>,true)
        commentAdapter?.setHasStableIds(true)
        recyclerview?.adapter = commentAdapter
        recyclerview?.setItemViewCacheSize(15)

        getComments()

        val preferences = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if(preferences != null)
        {
            this.postID = preferences.getString("postID","none").toString()
            setUpPost()
        }
        else
        {
            this.postID = ""
        }
        likeBtn?.setOnClickListener {
            if(likeBtnState)
            {
                Picasso.get().load(R.drawable.heart_not_clicked).fit().centerInside().into(likeBtn)
                removeLikeFromFirebase(postID)
                if(likeNumber?.text.toString() == "1")
                {
                    likeNumber?.text = ""
                }
            }
            else {
                Picasso.get().load(R.drawable.heart_clicked).fit().centerInside().into(likeBtn)
                saveLikeIntoFirebase(postID)
                postCreator?.getPublisher()?.let { it1 -> saveNotification("1",postID, it1) }
                if(postCreator?.getPublisher() != firebaseUser?.uid.toString())
                {
                    retrieveUser("has liked your post")
                }
            }
        }
        sendBtn?.setOnClickListener {
            if(!comment?.text.toString().isEmpty())
            {
                saveCommentIntoFirebase()
                postCreator?.getPublisher()?.let { it1 -> saveNotification("2",postID, it1) }
                if(postCreator?.getPublisher() != firebaseUser?.uid.toString())
                {
                    retrieveUser("has commented on your post")
                }
                comment?.onEditorAction(EditorInfo.IME_ACTION_DONE)
                comment?.text?.clear()
            }
        }
    }

    private fun retrieveUser(message : String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(temp_snapshot in snapshot.children)
                {
                    val user = temp_snapshot.getValue(User::class.java)
                    if(user?.getUid() == firebaseUser?.uid.toString())
                    {
                        val token = "/topics/" + postCreator?.getPublisher()
                        val sender = Html.fromHtml("<b>"+ user.getUsername() +"</b >" + "   "+ message)
                        val notificationsSender  =  FcmNotificationsSender(token,"ShareNCare"
                            ,sender.toString(),application.applicationContext,MainActivity())
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

    private fun getComments() {
        val commentRef = FirebaseDatabase.getInstance().reference.child("Comments")

        commentRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var counter = 0
                mComments?.clear()
                for(temp_snapshot in snapshot.children){
                    val temp_comment = temp_snapshot.getValue(Comment :: class.java)
                    if( postID == temp_comment?.getPostID())
                    {
                        mComments?.add(0,temp_comment)
                        counter++
                    }
                    if(counter>0)
                    {
                        commentNumber?.text = counter.toString()
                    }
                    commentAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun saveCommentIntoFirebase() {
        val currentTime = System.currentTimeMillis().toString()
        val commentRef = FirebaseDatabase.getInstance().reference.child("Comments")

        val commentMap = HashMap<String,Any>()
        commentMap["commentID"] = currentTime
        commentMap["postID"] = postID
        commentMap["publisher"] = firebaseUser?.uid.toString()
        commentMap["message"] = comment?.text.toString()

        commentRef.child(currentTime).updateChildren(commentMap).addOnCompleteListener{ task->
            if(task.isSuccessful)
            {
                System.out.println("Comment Stored Successfully")
            }
            else
            {

            }
        }
    }

    private fun saveNotification(type : String,postID: String,publisher: String) {
        if(firebaseUser?.uid.toString() == publisher)
        {
            return
        }
        val currentTime = System.currentTimeMillis().toString()

        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications")

        val notiMap = HashMap<String,Any>()
        notiMap["type"] = type
        notiMap["sender"] = firebaseUser?.uid.toString()
        notiMap["postID"] = postID
        notiMap["receiver"] = publisher
        notiMap["seen"] = "false"
        notiMap["notificationID"] = currentTime

        notiRef.child(currentTime).updateChildren(notiMap).addOnCompleteListener{ task->
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

    private fun saveLikeIntoFirebase(postID : String) {
        firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference.child("Like")
                .child(postID).child(it1.toString()).setValue(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {

                    }
                }
        }
    }


    private fun removeLikeFromFirebase(postID : String) {
        firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference.child("Like")
                .child(postID)
                .child(it1.toString()).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {

                    }
                }
        }
    }

    private fun setUpPost()
    {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(temp_snapshot in snapshot.children){
                    val post = temp_snapshot.getValue(Post :: class.java)
                    if(postID == post?.getPostID())
                    {
                        postCreator = post
                        setUpUser(post.getPublisher())
                        if(post.getDescription().isEmpty())
                        {
                            description?.layoutParams?.height = 0
                        }
                        else
                        {
                            description?.text = post.getDescription()
                        }
                        if(post.getPostImage().isEmpty())
                        {
                            postImage?.layoutParams?.height = 0
                        }
                        else
                        {
                            postImage?.let {
                                Glide.with(application).load(post.getPostImage()).fitCenter().diskCacheStrategy(
                                    DiskCacheStrategy.ALL)
                                    .error(R.drawable.profile)
                                    .dontTransform().into(it)
                            }
                        }
                        if(post.getPostPdf().isEmpty())
                        {
                            postPdf?.layoutParams?.height = 0
                        }
                        else
                        {
                            postPdf?.text = post.getPostPdfName()
                            postPdfUrl = post.getPostPdf()
                        }
                        getLike()
                        likeInfoLoading()
                        break;
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun getLike() {
        val likeRef = FirebaseDatabase.getInstance().reference.child("Like").child(postID)

        likeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    likeNumber?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun likeInfoLoading() {
        val userRef =  firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference.child("Like")
                .child(postID).child(it1.toString())
        }

        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    System.out.println("Clicked")
                    likeBtnState = true
                    Picasso.get().load(R.drawable.heart_clicked).fit().centerInside().into(likeBtn)
                }
                else
                {
                    System.out.println("Not Clicked")
                    likeBtnState = false
                    Picasso.get().load(R.drawable.heart_not_clicked).fit().centerInside().into(likeBtn)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun setUpUser(publisher : String)
    {
        System.out.println("Yess")
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(temp_snapshot in snapshot.children){
                    val user = temp_snapshot.getValue(User :: class.java)
                    if(publisher == user?.getUid())
                    {
                        System.out.println("Yess")
                        username?.text = user.getUsername()
                        profileImage?.let {
                            Glide.with(application).load(user.getImage()).fitCenter().diskCacheStrategy(
                                DiskCacheStrategy.ALL)
                                .error(R.drawable.profile)
                                .dontTransform().into(it)
                        }
                        break;
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}