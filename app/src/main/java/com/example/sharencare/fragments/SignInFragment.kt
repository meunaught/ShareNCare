package com.example.sharencare.fragments

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.cometchat.pro.core.AppSettings
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.User
import com.example.sharencare.MainActivity
import com.example.sharencare.R
import com.example.sharencare.constants.AppConfig
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SignInFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignInFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var signIn_btn_sign_in_fragment :AppCompatButton
    private lateinit var email_editText_signIn_fragment : EditText
    private lateinit var password_editText_signIn_fragment : EditText
    private var progressDialog : ProgressDialog ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser!=null)
        {
            startActivity(Intent(context,MainActivity::class.java))
            this.activity?.finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)
        signIn_btn_sign_in_fragment = view.findViewById(R.id.signIn_btn_sign_in_fragment)
        email_editText_signIn_fragment = view.findViewById(R.id.email_editText_signIn_fragment)
        password_editText_signIn_fragment = view.findViewById(R.id.password_editText_signIn_fragment)

        signIn_btn_sign_in_fragment.setOnClickListener {
            val email = email_editText_signIn_fragment.text.toString()
            val password = password_editText_signIn_fragment.text.toString()
            when{
                TextUtils.isEmpty(email)-> Toast.makeText(context,"Email is required", Toast.LENGTH_LONG).show()
                TextUtils.isEmpty(password)-> Toast.makeText(context,"Password Name is required",
                    Toast.LENGTH_LONG).show()

                else->{
                    progressDialog = ProgressDialog(context)
                    progressDialog?.setTitle("Signing In ")
                    progressDialog?.setMessage("Please wait,this may take a while...")
                    progressDialog?.setCanceledOnTouchOutside(false)
                    progressDialog?.show()
                    val userAuth : FirebaseAuth = FirebaseAuth.getInstance()
                    userAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener{ task->
                        if(task.isSuccessful)
                        {
                            val verification = userAuth.currentUser?.isEmailVerified
                            if(verification == true)
                            {
                                userAuth.uid?.let { it1 -> loginComet(it1) }
                                progressDialog?.dismiss()
                                startActivity(Intent(context,MainActivity::class.java))
                                this.activity?.finish()
                            }
                            else
                            {
                                userAuth.currentUser?.sendEmailVerification()?.addOnSuccessListener{
                                    Toast.makeText(context,"Please verify your email before you sign in",Toast.LENGTH_LONG).show()
                                    userAuth.signOut()
                                    progressDialog?.dismiss()
                                }?.addOnFailureListener{
                                    val message = it.toString()
                                    Toast.makeText(context,"Error : $message",Toast.LENGTH_LONG).show()
                                    userAuth.signOut()
                                    progressDialog?.dismiss()
                                }
                            }
                        }
                        else
                        {
                            progressDialog?.dismiss()
                            val message = task.exception!!.toString()
                            Toast.makeText(context,"Error : $message", Toast.LENGTH_LONG).show()
                            userAuth.signOut()
                        }
                    }
                }
            }
        }

        return view
    }

    private fun loginComet(uid: String) {
        val AUTH_KEY = AppConfig.AppDetails.AUTH_KEY
        if(CometChat.getLoggedInUser() != null) {
            cometLogout()
        }
        CometChat.login(uid, AUTH_KEY, object : CometChat.CallbackListener<User?>() {
            override fun onSuccess(user: User?) {
                    Log.d(TAG, "Login Successful : "+user.toString())
            }
            override fun onError(e: CometChatException) {
                    Log.d(TAG, "Login failed with exception: " + e.message);
                }
            })
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SignInFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignInFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}