package com.example.sharencare.fragments

import android.app.Activity
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
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.example.sharencare.Model.User
import com.example.sharencare.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso


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
    private lateinit var image_btn_edit_profile_fragment : ImageButton
    private lateinit var update_btn_edit_profile_fragment : AppCompatButton
    private lateinit var new_fullname_editText_edit_profile_fragment : EditText
    private lateinit var new_username_editText_edit_profile_fragment : EditText
    private lateinit var new_bio_editText_edit_profile_fragment : EditText

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

        //Calling of userInfo method
        userInfo()

        image_btn_edit_profile_fragment.setOnClickListener{
            checker = true
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }
        update_btn_edit_profile_fragment.setOnClickListener {
            val fullname = new_fullname_editText_edit_profile_fragment.text.toString()
            val username = new_username_editText_edit_profile_fragment.text.toString()
            val bio = new_bio_editText_edit_profile_fragment.text.toString()
            when{
                TextUtils.isEmpty(fullname)-> Toast.makeText(context,"Full Name is required", Toast.LENGTH_LONG).show()
                TextUtils.isEmpty(username)-> Toast.makeText(context,"Username is required", Toast.LENGTH_LONG).show()
                TextUtils.isEmpty(bio)-> Toast.makeText(context,"Email is required", Toast.LENGTH_LONG).show()
                (!checker)->{
                    updateUserIntoFirebase(fullname,username,bio)
                }
                (imageUri == null) -> Toast.makeText(context,"Please select an image", Toast.LENGTH_LONG).show()
                else->{
                    Log.d("myTag", "inside else of update btn edit profile");
                    uploadImageIntoFirebase(fullname,username,bio)
                }
            }
        }
        return view
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
                Toast.makeText(context,"task completed", Toast.LENGTH_LONG).show()
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
                Log.d("myTag", "inside task succesful");
                Toast.makeText(context,"Account has been updated successfully.",Toast.LENGTH_LONG).show()
            }
            else
            {
                val message = task.exception!!.toString()
                Toast.makeText(context,"Error : $message",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun userInfo()
    {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    val user = snapshot.getValue<User>(User :: class.java)
                    Picasso.get().load(user?.getImage()).into(image_btn_edit_profile_fragment)
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