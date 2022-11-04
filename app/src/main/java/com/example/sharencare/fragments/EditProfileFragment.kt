package com.example.sharencare.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.example.sharencare.LoginActivity
import com.example.sharencare.Model.Comment
import com.example.sharencare.Model.Notification
import com.example.sharencare.Model.Post
import com.example.sharencare.Model.User
import com.example.sharencare.R
import com.example.sharencare.async.deleteApiCall
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import de.hdodenhof.circleimageview.CircleImageView


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [editProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class editProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private val pickImage = 100
    private var imageUri: Uri? = null
    private lateinit var firebaseUser : FirebaseUser
    private var imageUrl = ""
    private var checker = false
    private lateinit var storageReference : StorageReference
    private lateinit var image_btn_edit_profile_fragment : CircleImageView
    private lateinit var update_btn_edit_profile_fragment : AppCompatButton
    private lateinit var new_fullname_editText_edit_profile_fragment : EditText
    private lateinit var new_username_editText_edit_profile_fragment : EditText
    private lateinit var new_bio_editText_edit_profile_fragment : EditText
    private var progressDialog : ProgressDialog ?= null
    private lateinit var deleteBtn : AppCompatButton
    private var currentUser : User ?= null

    private var userID_List : MutableList<String> ? = null
    private var postID_List : MutableList<String> ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_edit_profile, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageReference = FirebaseStorage.getInstance().reference.child("Profile Pictures")
        image_btn_edit_profile_fragment = view.findViewById(R.id.image_btn_edit_profile_fragment)
        update_btn_edit_profile_fragment = view.findViewById(R.id.update_btn_edit_profile_fragment)
        new_fullname_editText_edit_profile_fragment = view.findViewById(R.id.new_fullname_editText_edit_profile_fragment)
        new_username_editText_edit_profile_fragment = view.findViewById(R.id.new_username_editText_edit_profile_fragment)
        new_bio_editText_edit_profile_fragment = view.findViewById(R.id.new_bio_editText_edit_profile_fragment)
        deleteBtn = view.findViewById(R.id.deleteAccount_btn_edit_profile_fragment)
        userID_List = ArrayList()
        postID_List = ArrayList()

        //Calling of userInfo method
        userInfo()

        image_btn_edit_profile_fragment.setOnClickListener{
            checker = true
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }
        deleteBtn.setOnClickListener {
            Toast.makeText(context, "Long press to confirm delete", Toast.LENGTH_SHORT).show()
        }
        deleteBtn.setOnLongClickListener(object : View.OnLongClickListener{
            override fun onLongClick(p0: View?): Boolean {
                progressDialog = ProgressDialog(context)
                progressDialog?.setTitle("Deleting Account")
                progressDialog?.setMessage("Please wait,this may take several minutes...")
                progressDialog?.setCanceledOnTouchOutside(false)
                progressDialog?.show()
                retrieveUsers()
                return true
            }
        })

        update_btn_edit_profile_fragment.setOnClickListener {
            val fullname = new_fullname_editText_edit_profile_fragment.text.toString()
            val username = new_username_editText_edit_profile_fragment.text.toString()
            val bio = new_bio_editText_edit_profile_fragment.text.toString()
            when{
                TextUtils.isEmpty(fullname)-> Toast.makeText(context,"Full Name is required", Toast.LENGTH_LONG).show()
                TextUtils.isEmpty(username)-> Toast.makeText(context,"Username is required", Toast.LENGTH_LONG).show()
                TextUtils.isEmpty(bio)-> Toast.makeText(context,"Email is required", Toast.LENGTH_LONG).show()
                (!checker)->{
                    progressDialog = ProgressDialog(context)
                    progressDialog?.setTitle("Updating Account")
                    progressDialog?.setMessage("Please wait,this may take a while...")
                    progressDialog?.setCanceledOnTouchOutside(false)
                    progressDialog?.show()
                    updateUserIntoFirebase(fullname,username,bio)
                }
                (imageUri == null) -> Toast.makeText(context,"Please select an image", Toast.LENGTH_LONG).show()
                else->{

                    progressDialog = ProgressDialog(context)
                    progressDialog?.setTitle("Updating Account")
                    progressDialog?.setMessage("Please wait,this may take a while...")
                    progressDialog?.setCanceledOnTouchOutside(false)
                    progressDialog?.show()
                    uploadImageIntoFirebase(fullname,username,bio)

                }
            }
        }
        return view
    }

    private fun retrievePosts() {
        println("Retrieve posts")
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(temp_Snapshot in snapshot.children)
                {
                    val post = temp_Snapshot.getValue(Post::class.java)
                    post?.getPostID()?.let { postID_List?.add(it) }
                }
                deleteComments()
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun retrieveUsers() {
        println("Retrieve Users")
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(temp_Snapshot in snapshot.children)
                {
                    val user = temp_Snapshot.getValue(User::class.java)
                    user?.getUid()?.let { userID_List?.add(it) }
                }
                retrievePosts()
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun transferPage() {
        println("Transfer Page")
        cometLogout()
        val i = Intent(activity, LoginActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
        activity?.finish()
        progressDialog?.dismiss()
    }

    private fun cometLogout() {
        CometChat.logout(object : CometChat.CallbackListener<String>() {
            override fun onSuccess(p0: String?) {
                Log.d(TAG, "Comet Logout EditProFrag completed successfully")
            }

            override fun onError(p0: CometChatException?) {
                Log.d(TAG, "Comet Logout EditProFrag failed with exception: " + p0?.message)
            }
        })
    }

    private fun deleteAuthentication() {
        println("Delete Auth")
        val topic = FirebaseAuth.getInstance().currentUser?.uid.toString()
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)

        val user = FirebaseAuth.getInstance().currentUser!!
        val credential : AuthCredential? = user.email?.let {
            currentUser?.getPassword()?.let { it1 ->
                EmailAuthProvider
                    .getCredential(it, it1) }
        }
        credential?.let {
            user.reauthenticate(it)
                .addOnCompleteListener { task->
                    if(task.isSuccessful) {
                        user.delete()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    println("User deleted successfully")
                                    Toast.makeText(context,"Deleted account successfully",Toast.LENGTH_LONG).show()
                                    transferPage()
                                }
                            }
                    } else {
                        Toast.makeText(context,"Your previous password doesn't match",Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun getCurrentUsers(){
        println("Current Users")
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(temp_Snapshot in snapshot.children)
                {
                    val temp_user = temp_Snapshot.getValue(User::class.java)
                    if(temp_user?.getUid() == firebaseUser.uid)
                    {
                        currentUser = temp_user
                        deleteUser()
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun deleteUser() {
        println("Delete user")
        val uid = firebaseUser.uid
        deleteApiCall().execute(uid)
        FirebaseDatabase.getInstance().reference.child("Users").child(uid).removeValue()
        deleteAuthentication()
    }

    private fun deleteNotifications() {
        println("Delete Notifications")
        val commentRef = FirebaseDatabase.getInstance().reference.child("Notifications")
        commentRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(temp_Snapshot in snapshot.children)
                {
                    val notification = temp_Snapshot.getValue(Notification::class.java)
                    if((notification?.getSender() == firebaseUser.uid)||
                        (notification?.getReceiver() == firebaseUser.uid))
                    {
                        FirebaseDatabase.getInstance().reference.child("Notifications").child(notification.getNotificationID()).removeValue()
                    }
                }
                getCurrentUsers()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun deleteFollow() {
        println("Delete Follow")
        FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid).removeValue()

        for(uid in userID_List!!)
        {
            FirebaseDatabase.getInstance().reference.child("Follow").child(uid).child("Following").
            child(firebaseUser.uid).removeValue()

            FirebaseDatabase.getInstance().reference.child("Follow").child(uid).child("Followers").
            child(firebaseUser.uid).removeValue()
        }
        deleteNotifications()
    }

    private fun deleteLikes() {
        println("Delete Likes")
        for(postID in postID_List!!)
        {
            FirebaseDatabase.getInstance().reference.child("Like").child(postID).child(firebaseUser.uid).removeValue()
        }
        deleteFollow()
    }

    private fun deletePosts() {
        println("Delete Posts")
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(temp_Snapshot in snapshot.children)
                {
                    val post = temp_Snapshot.getValue(Post::class.java)
                    if(post?.getPublisher() == firebaseUser.uid.toString())
                    {
                        FirebaseDatabase.getInstance().reference.child("Like").child(post.getPostID()).removeValue()
                        FirebaseDatabase.getInstance().reference.child("Posts").child(post.getPostID()).removeValue()
                    }
                }
                deleteLikes()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun deleteComments() {
        println("Delete Comments")
        val commentRef = FirebaseDatabase.getInstance().reference.child("Comments")
        commentRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(temp_Snapshot in snapshot.children)
                {
                    val comment = temp_Snapshot.getValue(Comment::class.java)
                    if(comment?.getPublisher() == firebaseUser.uid.toString())
                    {
                        FirebaseDatabase.getInstance().reference.child("Comments").child(comment.getCommentID()).removeValue()
                    }
                }
                deletePosts()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun uploadImageIntoFirebase(fullname : String,username: String,bio: String) {
        val fileReference = storageReference!!.child(firebaseUser.uid + ".jpg")
        var uploadTask : StorageTask<*>
        uploadTask = fileReference.putFile(imageUri!!)
        uploadTask.continueWithTask<Uri?>(Continuation <UploadTask.TaskSnapshot,Task<Uri>>{ task ->
            if(!task.isSuccessful)
            {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation fileReference.downloadUrl
        }).addOnCompleteListener (OnCompleteListener<Uri>{task->
            if(task.isSuccessful)
            {
                val downloadUrl = task.result
                imageUrl = downloadUrl.toString()
                updateUserIntoFirebase(fullname,username,bio)
            }
            else
            {
                Toast.makeText(context,"task not completed", Toast.LENGTH_LONG).show()
            }
        } )
    }

    private fun updateUserIntoFirebase(fullname: String, username: String,bio:String) {
        val currentUserID = firebaseUser.uid
        val usersRef : DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String,Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullname
        userMap["username"] = username
        userMap["bio"] = bio
        if(checker)
        {
            userMap["image"] = imageUrl
        }

        usersRef.child(currentUserID).updateChildren(userMap).addOnCompleteListener{ task->
            if(task.isSuccessful)
            {
                cometUserUpdate(fullname, imageUrl)
                Log.d("myTag", "inside task succesful");
                Toast.makeText(context,"Account has been updated successfully.",Toast.LENGTH_LONG).show()
            }
            else
            {
                val message = task.exception!!.toString()
                Toast.makeText(context,"Error : $message",Toast.LENGTH_LONG).show()
            }
        }
        progressDialog?.dismiss()
    }

    private fun userInfo()
    {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    val user = snapshot.getValue<User>(User :: class.java)
                    if (user != null) {
                        context?.let {
                            Glide.with(it).load(user.getImage()).fitCenter().diskCacheStrategy(
                                DiskCacheStrategy.ALL)
                                .error(R.drawable.profile)
                                .dontTransform().into(image_btn_edit_profile_fragment)
                        }
                    }
                    //Picasso.get().load(user?.getImage()).into(image_btn_edit_profile_fragment)
                    new_username_editText_edit_profile_fragment.setText(user?.getUsername())
                    new_fullname_editText_edit_profile_fragment.setText(user?.getFullname())
                    new_bio_editText_edit_profile_fragment.setText(user?.getBio())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            image_btn_edit_profile_fragment.setImageURI(imageUri)
        }
    }

    private fun cometUserUpdate(name : String, avatar : String) {
        val user = com.cometchat.pro.models.User()
        user.name = name
        user.avatar = avatar
        CometChat.updateCurrentUserDetails(user, object : CometChat.CallbackListener<com.cometchat.pro.models.User>() {
            override fun onSuccess(p0: com.cometchat.pro.models.User) {
                Log.d(TAG, "cometUser Updated" + p0.toString())
            }
            override fun onError(p0: CometChatException) {
                p0.message?.let { Log.d(TAG, it) }
            }
        })
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment editProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            editProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}