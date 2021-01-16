package com.example.kurikulumappinstagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kurikulumappinstagram.adapter.CommentAdapter
import com.example.kurikulumappinstagram.model.Comment
import com.example.kurikulumappinstagram.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.list_comment_layout.*
import kotlinx.android.synthetic.main.list_post_layout.*

class CommentActivity : AppCompatActivity() {

    private var postId = ""
    private var publisherid = ""
    private var firebaseUser : FirebaseUser? = null
    private var commentAdapter :CommentAdapter? =null
    private var commentlistdata :MutableList<Comment>? =null
    private var recyclerView :RecyclerView? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        val intentku = intent
        postId = intentku.getStringExtra("postId").toString()
        publisherid = intentku.getStringExtra("publisher").toString()

        firebaseUser =FirebaseAuth.getInstance().currentUser
        recyclerView =findViewById(R.id.recyclerview_comments)
        val linearLayoutManager =LinearLayoutManager(this)
        linearLayoutManager.reverseLayout =true
        recyclerView?.layoutManager =linearLayoutManager

        commentlistdata =ArrayList()
        commentAdapter = CommentAdapter(this ,commentlistdata as ArrayList<Comment>)
        recyclerView?.adapter =commentAdapter


        userInfo()
        readComment()
        getPostImage()
        txt_post_comment.setOnClickListener {
            if (edt_add_comment.text.toString() == ""){

                Toast.makeText(this, "Isi comment ", Toast.LENGTH_SHORT).show()
            } else {
                addComment()
            }
        }
    }

    private fun readComment() {
        val commentRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postId)

        commentRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    commentlistdata!!.clear()

                    for (s in snapshot.children){
                        val comment = s.getValue(Comment::class.java)
                        commentlistdata!!.add(comment!!)
                    }
                    commentAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun getPostImage() {
        val postref = FirebaseDatabase.getInstance().reference
            .child("Posts").child(postId).child("postimage")
        postref.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val image =snapshot.value.toString()

                    Picasso.get().load(image).into(image_post_user_comment)


                }
            }

        })

    }

    private fun addComment() {
        val commentRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postId)

        val commentsMap =  HashMap<String, Any>()
        commentsMap["comments"] = edt_add_comment.text.toString()
        commentsMap["publisher"] = firebaseUser!!.uid

        commentRef.push().setValue(commentsMap)
        edt_add_comment.text.clear()
    }

    private fun userInfo() {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(firebaseUser!!.uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)
                    Picasso.get().load(user!!.getImage()).into(image_post_comment)

                }
            }

        })
    }

}