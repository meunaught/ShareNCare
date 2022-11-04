package com.example.sharencare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth


class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var contactUs_btn_activity_forget_password : AppCompatButton
    private lateinit var verifyCode_btn_activity_forget_password: AppCompatButton
    private lateinit var email_editText_forgetPassword_activity : EditText
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        contactUs_btn_activity_forget_password = findViewById(R.id.contactUs_btn_activity_forget_password)
        verifyCode_btn_activity_forget_password = findViewById(R.id.verifyCode_btn_activity_forget_password)
        email_editText_forgetPassword_activity = findViewById(R.id.email_editText_forgetPassword_activity)
        auth = FirebaseAuth.getInstance()

        verifyCode_btn_activity_forget_password.setOnClickListener{
            val email = email_editText_forgetPassword_activity.text.toString()
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "please check your email", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener{
                    Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                }
        }

        contactUs_btn_activity_forget_password.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            val str = arrayOf<String>("shamik.shafkat@gmail.com")
            intent.putExtra(Intent.EXTRA_EMAIL,str)
            intent.putExtra(Intent.EXTRA_SUBJECT, "Mention Your Problem")
            intent.putExtra(Intent.EXTRA_TEXT,"Password verification not working")
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    }
}