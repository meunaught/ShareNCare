package com.example.sharencare.fragments

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.FragmentTransaction
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.example.sharencare.EditProfileActivity
import com.example.sharencare.R
import com.example.sharencare.constants.AppConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SignUpFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignUpFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var signUp_btn_sign_up_fragment : AppCompatButton
    private lateinit var fullname_editText_signUp_fragment : EditText
    private lateinit var username_editText_signUp_fragment : EditText
    private lateinit var password_editText_signUp_fragment : EditText
    private lateinit var email_editText_signUp_fragment : EditText

    private var progressDialog : ProgressDialog?= null


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
        val view =  inflater.inflate(R.layout.fragment_sign_up, container, false)
        signUp_btn_sign_up_fragment = view.findViewById(R.id.signUp_btn_sign_up_fragment)
        fullname_editText_signUp_fragment = view.findViewById(R.id.fullname_editText_signUp_fragment)
        username_editText_signUp_fragment = view.findViewById(R.id.username_editText_signUp_fragment)
        password_editText_signUp_fragment = view.findViewById(R.id.password_editText_signUp_fragment)
        email_editText_signUp_fragment = view.findViewById(R.id.email_editText_signUp_fragment)

        signUp_btn_sign_up_fragment.setOnClickListener {
            createUser()
        }
        return view
    }

    private fun createUser() {
        val fullname = fullname_editText_signUp_fragment.text.toString()
        val username = username_editText_signUp_fragment.text.toString()
        val email = email_editText_signUp_fragment.text.toString()
        val password = password_editText_signUp_fragment.text.toString()

        when{
            TextUtils.isEmpty(fullname)->{
                Toast.makeText(context,"Full Name is required",Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(username)->{
                Toast.makeText(context,"Username is required",Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(email)->{
                Toast.makeText(context,"Email is required",Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(password)->{
                Toast.makeText(context,"Password Name is required",Toast.LENGTH_LONG).show()
            }

            else->{
                progressDialog = ProgressDialog(context)
                progressDialog?.setTitle("Sign Up")
                progressDialog?.setMessage("Please wait,this may take a while...")
                progressDialog?.setCanceledOnTouchOutside(false)
                progressDialog?.show()

                val userAuth : FirebaseAuth = FirebaseAuth.getInstance()
                userAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{ task->
                    if(task.isSuccessful)
                    {
                        userAuth.currentUser?.sendEmailVerification()?.addOnSuccessListener{
                            saveUserIntoFirebase(fullname,username,email,password)
                            userAuth.uid?.let { createCometuser(it, fullname) }
                        }?.addOnFailureListener{
                            val message = it.toString()
                            Toast.makeText(context,"Error : $message",Toast.LENGTH_LONG).show()
                            userAuth.signOut()
                            progressDialog?.dismiss()
                        }
                    }
                    else
                    {
                        val message = task.exception!!.toString()
                        Toast.makeText(context,"Error : $message",Toast.LENGTH_LONG).show()
                        userAuth.signOut()
                        progressDialog?.dismiss()
                    }
                }

            }
        }
    }

    private fun saveUserIntoFirebase(fullname: String, username: String, email: String, password: String) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef : DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
        val userMap = HashMap<String,Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullname
        userMap["username"] = username
        userMap["email"] = email
        userMap["password"] = password
        userMap["bio"] = "Hi, I am using shareNcare app"
        userMap["image"] = AppConfig.AppDetails.demoAvatar

        usersRef.child(currentUserID).setValue(userMap).addOnCompleteListener{ task->
            if(task.isSuccessful)
            {
                FirebaseDatabase.getInstance().reference.child("Follow").child(currentUserID)
                    .child("Following").child(currentUserID).setValue(true)

                FirebaseDatabase.getInstance().reference.child("Follow").child(currentUserID)
                    .child("Followers").child(currentUserID).setValue(true)

                Toast.makeText(context,"Account has been created successfully.Please verify it before you sign in",Toast.LENGTH_LONG).show()
                FirebaseAuth.getInstance().signOut()
                val newFragment : Fragment = SignInFragment()
                val transaction : FragmentTransaction = requireFragmentManager().beginTransaction()
                transaction.replace(R.id.frame_layout_activity_login,newFragment)
                transaction.addToBackStack(null)
                transaction.commit()
                progressDialog?.dismiss()

            }
            else
            {
                val message = task.exception!!.toString()
                Toast.makeText(context,"Error : $message",Toast.LENGTH_LONG).show()
                FirebaseAuth.getInstance().signOut()
                progressDialog?.dismiss()
            }
        }
    }

    private fun createCometuser (uid : String, name : String) {
        val AUTH_KEY = AppConfig.AppDetails.AUTH_KEY
        val user = User()
        user.uid = uid
        user.name = name
        user.avatar = AppConfig.AppDetails.demoAvatar
        CometChat.createUser(user, AUTH_KEY, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(user: User) {
                Log.d("createUser", user.toString())
            }

            override fun onError(e: CometChatException) {
                Log.e("createUser", e.message.toString())
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
         * @return A new instance of fragment SignUpFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignUpFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}