package com.example.sharencare.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharencare.MainActivity
import com.example.sharencare.Model.Notification
import com.example.sharencare.Model.User
import com.example.sharencare.R
import com.example.sharencare.adapter.UserAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var recyclerview : RecyclerView ?= null
    private var userAdapter : UserAdapter? = null
    private var mUser : MutableList<User>?= null
    private var sUser : MutableList<User> = ArrayList()
    private lateinit var firebaseUser : FirebaseUser

    private lateinit var search_editText_search_fragment : EditText

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
        val view =  inflater.inflate(R.layout.fragment_search, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        search_editText_search_fragment = view.findViewById(R.id.search_editText_search_fragment)
        recyclerview = view.findViewById(R.id.recycler_view_search_fragment)
        recyclerview?.setHasFixedSize(true)
        recyclerview?.layoutManager = LinearLayoutManager(context)

        mUser = ArrayList()
        userAdapter = context?.let { UserAdapter(it,mUser as ArrayList<User>,true) }
        userAdapter?.setHasStableIds(true)
        recyclerview?.adapter = userAdapter
        recyclerview?.setItemViewCacheSize(15)
        retriveUsers()

        search_editText_search_fragment.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(search_editText_search_fragment.text.toString()== "")
                {
                    recyclerview?.visibility = View.INVISIBLE
                }
                else
                {
                    searchUsers(p0.toString())
                    recyclerview?.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        badgeSetForNotifications()

        return view
    }

    private fun badgeSetForNotifications() {
        var counter = 0
        val navView = (activity as MainActivity).navView
        val menuItem = navView?.menu?.findItem(R.id.nav_search_button)
        menuItem?.isChecked = true

        var badge_notifications = navView?.getOrCreateBadge(R.id.nav_notifications)

        val notificationRef = FirebaseDatabase.getInstance().reference.child("Notifications")
        notificationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                counter = 0
                for(temp_snapshot in snapshot.children)
                {
                    val notification = temp_snapshot.getValue(Notification::class.java)
                    if((notification?.getReceiver() == firebaseUser.uid)
                        && notification.getSeen().equals("false")){
                        counter++
                        println(counter)
                    }
                    else{
                        println("Same")
                    }
                }
                if(counter >0)
                {
                    println("Counter is more than 1")
                    badge_notifications?.isVisible = true
                    badge_notifications?.number = counter
                }
                else
                {
                    badge_notifications?.isVisible = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    private fun searchUsers( input :String) {
        mUser?.clear()
        for(user in sUser)
        {
            if(KMP(user.getFullname().lowercase(),input.lowercase(),user))
            {

            }
            else if(KMP(user.getUsername().lowercase(),input.lowercase(),user))
            {

            }
        }

        userAdapter?.notifyDataSetChanged()
    }


    private fun KMP(txt : String ,pattern : String,user : User) : Boolean
    {
        val pat_len  = pattern.length
        val txt_len  = txt.length

        var lps : ArrayList<Int> = arrayListOf()
        lps = computeLPSArray(pattern,pat_len,lps)

        var i = 0 // index for txt[]
        var j = 0 // index for pat[]

        while ((txt_len - i) >= (pat_len - j)) {
            if (pattern[j] == txt[i]) {
                j++
                i++
            }
            if (j == pat_len) {
                if(pat_len>=6)
                {
                    if((i-j)==0)
                    {
                        mUser?.add(0,user)
                    }
                    else{
                        mUser?.add(user)
                    }
                    return true
                }
                else{
                    if((i-j)==0)
                    {
                        mUser?.add(0,user)
                        return true;
                    }
                    else if(txt[i - j - 1] == ' '){
                        mUser?.add(user)
                        return true
                    }
                    else{
                        j = lps[j - 1];
                    }
                }
            }
            else if (i < txt_len && pattern[j] != txt[i]) {

                if(j != 0)
                {
                    j = lps [j - 1]
                }
                else
                {
                    i = i + 1
                }
            }
        }
        return false
    }

    private fun computeLPSArray(pattern : String ,length : Int ,lps : ArrayList<Int>) : ArrayList<Int>
    {
        var len = 0
        lps.add(0,0)
        var i  = 1

        while(i<length)
        {
            if(pattern[i] == pattern[len])
            {
                len++
                lps.add(i,len)
                i++

            }
            else
            {
                if (len != 0) {
                    len = lps[len-1]
                }
                else
                {
                    lps.add(i,0)
                    i++;
                }
            }
        }
        return lps
    }

    private fun retriveUsers() {
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users")
        userRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                sUser.clear()
                for(snapshot in dataSnapshot.children)
                {
                    val user = snapshot.getValue(User::class.java)
                    if(user!=null)
                    {
                        sUser.add(user)
                    }
                }
                userAdapter?.notifyDataSetChanged()
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
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}