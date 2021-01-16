package com.example.kurikulumappinstagram.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kurikulumappinstagram.EditProfile
import com.example.kurikulumappinstagram.R
import com.example.kurikulumappinstagram.adapter.MyImagesAdapter
import com.example.kurikulumappinstagram.model.Post
import com.example.kurikulumappinstagram.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*


class ProfileFragment : Fragment() {

    private lateinit var profilId: String
    private lateinit var firebaseUser: FirebaseUser

    private var postListGrid : MutableList<Post>? = null
    private var myImagesAdapter : MyImagesAdapter? = null
    private var myRecyclerImages : RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewProfile = inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        myRecyclerImages = viewProfile.findViewById(R.id.recycler_upload_picImage)
        myRecyclerImages!!.setHasFixedSize(true)
        val linearLayoutManager = GridLayoutManager(context,3)
        myRecyclerImages!!.layoutManager = linearLayoutManager

        postListGrid = ArrayList()
        myImagesAdapter = context?.let { MyImagesAdapter(it,postListGrid as ArrayList<Post>) }
        myRecyclerImages!!.adapter = myImagesAdapter

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profilId = pref.getString("profileId", "none")!!
        }
        if (profilId == firebaseUser.uid) {
            view?.btn_edit_profile?.text = "Edit Profile"
        } else if (profilId != firebaseUser.uid) {
            cekFollowandFollowingButtonStatus() //cek status button follow dan following
        }
        viewProfile.btn_edit_profile.setOnClickListener {
            val getButtonText = view?.btn_edit_profile?.text.toString()

            when {
                getButtonText == "Edit Profile" -> startActivity(
                    Intent(
                        context,
                        EditProfile::class.java
                    )
                )

                getButtonText == "Follow" -> {
                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profilId).setValue(true)
                    }
                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profilId)
                            .child("Followers").child(it1.toString()).setValue(true)
                    }
                }

                getButtonText == "Following" -> {
                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profilId).removeValue()
                    }

                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profilId)
                            .child("Followers").child(it1.toString()).removeValue()

                    }
                }
            }
        }

        getFollowers()
        getFollowing()
        userInfo()
        myPost()
        return viewProfile
    }

    private fun myPost() {
        var postRef = FirebaseDatabase.getInstance().reference
            .child("Posts")

        postRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(P0: DataSnapshot) {
                if (P0.exists()){
                    (postListGrid as ArrayList<Post>).clear()

                    for (snapshot in P0.children){
                        val post = snapshot.getValue(Post::class.java)
                        if (post!!.getPublisher() == profilId){
                            (postListGrid as ArrayList<Post>).add(post)
                        }
                        postListGrid!!.reverse()
                        myImagesAdapter!!.notifyDataSetChanged()
                    }
                }
            }
        })
    }

    private fun cekFollowandFollowingButtonStatus() {
        val followingRef = firebaseUser.uid.let { it ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it.toString())
                .child("Following")
        }
        if (followingRef != null) {
            followingRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        view?.btn_edit_profile?.text = "Following"
                    } else {
                        view?.btn_edit_profile?.text = "Follow"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profilId)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    view?.txt_total_followers?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getFollowing() {

        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profilId)
            .child("Following")
        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    view?.txt_total_following?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(profilId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get()
                        .load(user?.getImage())
                        .into(view?.profile_pic)

                    view?.profile_username?.text = user?.getUsername()
                    view?.txt_profile_fullname?.text = user?.getFullname()
                    view?.txt_user_bio_profile?.text = user?.getBio()

                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onStop() {
        super.onStop()
        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()
        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }
}