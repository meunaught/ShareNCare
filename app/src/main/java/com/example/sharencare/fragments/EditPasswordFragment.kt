package com.example.sharencare.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.example.sharencare.Model.User
import com.example.sharencare.R
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditPasswordFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditPasswordFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var firebaseUser : FirebaseUser
    private var user : User?=null
    private lateinit var password_btn_edit_password_fragment : AppCompatButton
    private lateinit var previousPassword : EditText
    private lateinit var newPassword : EditText
    private lateinit var confirmPassword : EditText

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
        val view = inflater.inflate(R.layout.fragment_edit_password, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        password_btn_edit_password_fragment = view?.findViewById(R.id.password_btn_edit_password_fragment)!!
        previousPassword = view.findViewById(R.id.previous_password_editText_edit_password_fragment)
        newPassword = view.findViewById(R.id.new_password_editText_edit_password_fragment)
        confirmPassword = view.findViewById(R.id.confirm_password_editText_edit_password_fragment)

        userInfo()
        password_btn_edit_password_fragment.setOnClickListener {
            changePassword()
        }

        return view
    }

    private fun changePassword()
    {
        when{
            (previousPassword.text.isEmpty())->{
                Toast.makeText(context,"Previous password is required",Toast.LENGTH_SHORT).show()
            }
            (newPassword.text.isEmpty())->{
                Toast.makeText(context,"Password is required",Toast.LENGTH_SHORT).show()
            }
            (confirmPassword.text.isEmpty())->{
                Toast.makeText(context,"Confirm password is required",Toast.LENGTH_SHORT).show()
            }
            (!newPassword.text.toString().equals(confirmPassword.text.toString()))->{
                Toast.makeText(context,"Password confirmation doesn't match",Toast.LENGTH_SHORT).show()
            }
            else->{
                val user = FirebaseAuth.getInstance().currentUser!!
                val credential : AuthCredential = user.email?.let {
                    EmailAuthProvider
                        .getCredential(it, previousPassword.text.toString())
                }!!
                user.reauthenticate(credential)
                    .addOnCompleteListener { task->
                        if(task.isSuccessful)
                        {
                            user.updatePassword(newPassword.text.toString())
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        updatePasswordIntoFirebase(newPassword.text.toString())
                                    }
                                }
                        }
                        else
                        {
                            Toast.makeText(context,"Your previous password doesn't match",Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun updatePasswordIntoFirebase(password: String) {

        val currentUserID = firebaseUser.uid
        val usersRef : DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String,Any>()
        userMap["uid"] = currentUserID
        userMap["password"] = password

        usersRef.child(currentUserID).updateChildren(userMap).addOnCompleteListener{ task->
            if(task.isSuccessful)
            {
                Toast.makeText(context,"Password has been updated successfully.",Toast.LENGTH_LONG).show()
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
                    user = snapshot.getValue<User>(User :: class.java)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
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
         * @return A new instance of fragment EditPasswordFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditPasswordFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}