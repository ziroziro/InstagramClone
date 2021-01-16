package com.example.kurikulumappinstagram

import android.content.Context
import android.os.Bundle
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kurikulumappinstagram.adapter.PostAdapter
import com.example.kurikulumappinstagram.model.Post
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class DetailPostFragment : Fragment() {

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post>? = null
    private lateinit var postId: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val mContext = inflater.inflate(R.layout.fragment_detail_post, container, false)

        var recyclerViewHome: RecyclerView? = null
        recyclerViewHome = mContext.findViewById(R.id.rv_detailpost)
        recyclerViewHome?.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerViewHome.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter =
            context.let { it?.let { it1 -> PostAdapter(it1, postList as ArrayList<Post>) } }
        recyclerViewHome.adapter = postAdapter

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.postId = pref.getString("profileId", "none")!!
        }

        getPost()
        return mContext
    }

    private fun getPost() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Post::class.java)
                postList!!.add(post!!)
                postAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

}