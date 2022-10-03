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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.sharencare.Model.User
import com.example.sharencare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_create_post.view.*
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.fragment_sign_up.view.*


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
    private var firebaseUser : FirebaseUser = FirebaseAuth.getInstance().currentUser!!

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
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_edit_profile, container, false)

        userInfo()

        view.image_btn_edit_profile_fragment.setOnClickListener{
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }
        view.update_btn_edit_profile_fragment.setOnClickListener {
            val fullname = view?.new_fullname_editText_edit_profile_fragment?.text.toString()
            val username = view?.new_username_editText_edit_profile_fragment?.text.toString()
            val bio = view?.new_bio_editText_edit_profile_fragment?.text.toString()
            when{
                TextUtils.isEmpty(fullname)-> Toast.makeText(context,"Full Name is required", Toast.LENGTH_LONG).show()
                TextUtils.isEmpty(username)-> Toast.makeText(context,"Username is required", Toast.LENGTH_LONG).show()
                TextUtils.isEmpty(bio)-> Toast.makeText(context,"Email is required", Toast.LENGTH_LONG).show()
                else->{
                    Log.d("myTag", "inside else of update btn edit profile");
                    updateUserIntoFirebase(fullname,username,bio)
                }

            }

        }
        return view
    }

    private fun updateUserIntoFirebase(fullname: String, username: String,bio:String) {

        val currentUserID = firebaseUser.uid
        val usersRef : DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String,Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullname
        userMap["username"] = username
        userMap["bio"] = bio

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
                    view?.new_username_editText_edit_profile_fragment?.setText(user?.getUsername())
                    view?.new_fullname_editText_edit_profile_fragment?.setText(user?.getFullname())
                    view?.new_bio_editText_edit_profile_fragment?.setText(user?.getBio())
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
            requireView().image_btn_edit_profile_fragment.setImageURI(imageUri)
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