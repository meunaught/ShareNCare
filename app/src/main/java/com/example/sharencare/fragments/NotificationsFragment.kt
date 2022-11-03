package com.example.sharencare.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharencare.MainActivity
import com.example.sharencare.Model.Notification
import com.example.sharencare.R
import com.example.sharencare.adapter.NotificationsAdapter
import com.faltenreich.skeletonlayout.Skeleton
import com.faltenreich.skeletonlayout.applySkeleton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NotificationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NotificationsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var recyclerview : RecyclerView
    private var notificationAdapter : NotificationsAdapter? = null
    private var mNotifications : MutableList<Notification>?= null
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var skeleton: Skeleton

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
        val view =  inflater.inflate(R.layout.fragment_notifications, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        recyclerview = view.findViewById(R.id.recycler_view_notifications_fragment)
        recyclerview?.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerview?.layoutManager = linearLayoutManager

        mNotifications = ArrayList()
        notificationAdapter = context?.let { NotificationsAdapter(it,mNotifications as ArrayList<Notification>,true) }
        notificationAdapter?.setHasStableIds(true)
        skeleton = recyclerview.applySkeleton(R.layout.notifications_layout)
        skeleton.showSkeleton()
        recyclerview?.setItemViewCacheSize(25)

        val navView = (activity as MainActivity).navView
        val menuItem = navView?.menu?.findItem(R.id.nav_notifications)
        menuItem?.isChecked = true
        val badge_notifications = navView?.getOrCreateBadge(R.id.nav_notifications)
        badge_notifications?.isVisible = false

        return view
    }

    override fun onResume() {
        super.onResume()
        getNotifications()
    }


    private fun getNotifications() {
        val notificationsRef = FirebaseDatabase.getInstance().reference.child("Notifications")

        notificationsRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                mNotifications?.clear()
                for(temp_snapshot in snapshot.children){
                    val notification = temp_snapshot.getValue(Notification :: class.java)
                    if(firebaseUser.uid == notification?.getReceiver())
                    {
                        notification.let { mNotifications?.add(it) }
                    }
                }
                skeleton.showOriginal()
                recyclerview.adapter = notificationAdapter
                setSeenNotifications()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


    private fun setSeenNotifications() {
        if (mNotifications != null) {
            for(notification in mNotifications!!)
            {
                updateIntoFirebase(notification)
            }
        }
    }

    private fun updateIntoFirebase(notification: Notification) {
        FirebaseDatabase.getInstance().reference.child("Notifications").child(notification.getNotificationID()).child("seen").setValue("true").addOnCompleteListener{task->
            if(task.isSuccessful)
            {
                System.out.println("Notifications seen")
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
         * @return A new instance of fragment NotificationsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NotificationsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}