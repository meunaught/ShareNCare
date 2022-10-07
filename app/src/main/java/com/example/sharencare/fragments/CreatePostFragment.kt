package com.example.sharencare.fragments

import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.sharencare.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CreatePostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreatePostFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val pickImage = 100
    private val pickPdfDocument = 1
    private var imageUri: Uri? = null
    private var pdfUri : Uri ?= null
    private var imageUrl = ""
    private var pdfUrl = ""
    private var timeStamp = ""

    private lateinit var pdfTextView : TextView
    private lateinit var uploadImage_btn_create_post_fragment : AppCompatButton
    private lateinit var uploadPdf_btn_create_post_fragment : AppCompatButton
    private lateinit var post_btn_create_post_fragment : AppCompatButton
    private lateinit var image_view_create_post_fragment : ImageView
    private lateinit var editText_create_post_fragment : EditText
    private lateinit var storageReferenceImage : StorageReference
    private lateinit var storageReferencePdf : StorageReference
    private lateinit var firebaseUser : FirebaseUser

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
        val view =  inflater.inflate(R.layout.fragment_create_post, container, false)
        uploadImage_btn_create_post_fragment = view.findViewById(R.id.uploadImage_btn_create_post_fragment)
        uploadPdf_btn_create_post_fragment = view.findViewById(R.id.uploadPdf_btn_create_post_fragment)
        post_btn_create_post_fragment = view.findViewById(R.id.post_btn_create_post_fragment)
        image_view_create_post_fragment = view.findViewById(R.id.image_view_create_post_fragment)
        pdfTextView = view.findViewById(R.id.pdfTextView_create_post_fragment)
        editText_create_post_fragment = view.findViewById(R.id.editText_create_post_fragment)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageReferenceImage = FirebaseStorage.getInstance().reference.child("Post Images")
        storageReferencePdf = FirebaseStorage.getInstance().reference.child("Post Pdfs")

        uploadImage_btn_create_post_fragment.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        uploadPdf_btn_create_post_fragment.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "application/pdf"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, pickPdfDocument)
        }

        post_btn_create_post_fragment.setOnClickListener {
            val description = editText_create_post_fragment.text
            when{
                (description.isEmpty() && pdfUri == null && imageUri == null)->{
                    Toast.makeText(context,"Please add something for the post",Toast.LENGTH_LONG).show()
                }
                (imageUri == null && pdfUri == null)->{
                    timeStamp = System.currentTimeMillis().toString()
                    createPostIntoFirebase()
                }
                (imageUri == null)->{
                    timeStamp = System.currentTimeMillis().toString()
                    uploadPdfIntoFirebase()
                }
                (pdfUri== null)->{
                    timeStamp = System.currentTimeMillis().toString()
                    uploadImageIntoFirebase(false)
                }
                else->{
                    timeStamp = System.currentTimeMillis().toString()
                    uploadImageIntoFirebase(true)
                }
            }
        }

        return view
    }

    private fun uploadImageIntoFirebase( reference : Boolean) {
        val fileReference = storageReferenceImage.child(timeStamp + ".jpg")
        var uploadTask : StorageTask<*>
        uploadTask = fileReference.putFile(imageUri!!)
        uploadTask.continueWithTask<Uri?>(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
            if(!task.isSuccessful)
            {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation fileReference.downloadUrl
        }).addOnCompleteListener (OnCompleteListener<Uri>{ task->
            if(task.isSuccessful)
            {
                val downloadUrl = task.result
                imageUrl = downloadUrl.toString()
                Toast.makeText(context,"Image has been uploaded successfully", Toast.LENGTH_LONG).show()
                if(reference == true)
                {
                    uploadPdfIntoFirebase();
                }
                else{
                    createPostIntoFirebase()
                }
            }
            else
            {
                Toast.makeText(context,"task not completed", Toast.LENGTH_LONG).show()
            }
        } )
    }

    private fun uploadPdfIntoFirebase() {
        val fileReference = storageReferencePdf.child(timeStamp + ".pdf")
        var uploadTask : StorageTask<*>
        uploadTask = fileReference.putFile(pdfUri!!)
        uploadTask.continueWithTask<Uri?>(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
            if(!task.isSuccessful)
            {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation fileReference.downloadUrl
        }).addOnCompleteListener (OnCompleteListener<Uri>{ task->
            if(task.isSuccessful)
            {
                val downloadUrl = task.result
                pdfUrl = downloadUrl.toString()
                Toast.makeText(context,"Pdf has been uploaded successfully", Toast.LENGTH_LONG).show()
                createPostIntoFirebase()
            }
            else
            {
                Toast.makeText(context,"task not completed", Toast.LENGTH_LONG).show()
            }
        } )
    }

    private fun createPostIntoFirebase() {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef : DatabaseReference = FirebaseDatabase.getInstance().reference.child("Posts")
        val description = editText_create_post_fragment.text

        val userMap = HashMap<String,Any>()
        userMap["postID"] = timeStamp
        userMap["publisher"] = currentUserID
        userMap["description"] = description.toString()
        userMap["postImage"] = imageUrl
        userMap["postPdf"] = pdfUrl

        usersRef.child(timeStamp).updateChildren(userMap).addOnCompleteListener{ task->
            if(task.isSuccessful)
            {
                Toast.makeText(context,"Post has been created successfully.",Toast.LENGTH_LONG).show()
            }
            else
            {
                val message = task.exception!!.toString()
                Toast.makeText(context,"Error : $message",Toast.LENGTH_LONG).show()
            }
        }
    }


    @Deprecated("")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            image_view_create_post_fragment.setImageURI(imageUri)
        }
        else if(resultCode == RESULT_OK && requestCode == pickPdfDocument)
        {
            pdfUri = data?.data
            val uri : Uri? = data?.data
            val uriString : String = uri.toString()
            var pdfName : String?= null
            if(uriString.startsWith("content://")){
                var myCursor : Cursor?= null
                try{
                    myCursor = uri?.let { context?.contentResolver?.query(it,null,null,null,null) }
                    if (myCursor != null && myCursor.moveToFirst()) {
                        pdfName = myCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            .let { myCursor.getString(it) }
                        pdfTextView.text = pdfName
                    }
                }
                finally {
                    myCursor?.close()
                }
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
         * @return A new instance of fragment CreatePostFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreatePostFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
