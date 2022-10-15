package com.example.sharencare.fragments

import android.app.ProgressDialog;
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.FragmentTransaction
import com.example.sharencare.R
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
                        saveUserIntoFirebase(fullname,username,email,password)
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
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/sharencare-54c4e.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=ca02e7f0-b372-40c0-bc63-cfb0d38a2ae0"

        usersRef.child(currentUserID).setValue(userMap).addOnCompleteListener{ task->
            if(task.isSuccessful)
            {
                FirebaseDatabase.getInstance().reference.child("Follow").child(currentUserID)
                    .child("Following").child(currentUserID).setValue(true)

                FirebaseDatabase.getInstance().reference.child("Follow").child(currentUserID)
                    .child("Followers").child(currentUserID).setValue(true)

                Toast.makeText(context,"Account has been created successfully.",Toast.LENGTH_LONG).show()
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