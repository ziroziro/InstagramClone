package com.example.kurikulumappinstagram.adapter

import android.content.Context
import android.content.Intent
import android.media.tv.TvContentRating
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kurikulumappinstagram.CommentActivity
import com.example.kurikulumappinstagram.MainActivity
import com.example.kurikulumappinstagram.R
import com.example.kurikulumappinstagram.model.Post
import com.example.kurikulumappinstagram.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter(private val mContext: Context,private val mPost : List<Post>) :
    RecyclerView.Adapter<PostViewHolder>() {

    private var firebaseUser : FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.list_post_layout,parent,false)
        return PostViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val myPost = mPost[position]
        Glide.with(mContext).load(myPost.getPostImage()).into(holder.postImage)
      //  Picasso.get().load(myPost.getPostImage()).into(holder.postImage)
        if (myPost.getDescription() == ""){
            holder.description.visibility = View.GONE
        }else{
            holder.description.visibility = View.VISIBLE
            holder.description.text = myPost.getDescription()
        }
        publisherInfo(holder.profileImage,holder.username,holder.publisher,myPost.getPublisher())

        ngelike(myPost.getPostId(), holder.likeButton)
        numberOfLikes(holder.likes, myPost.getPostId())
        getTotalComment(holder.comment,myPost.getPostId())

        holder.likeButton.setOnClickListener {
            if (holder.likeButton.tag == "Like"){

                FirebaseDatabase.getInstance().reference
                    .child("Likes").child(myPost.getPostId()).child(firebaseUser!!.uid)
                    .setValue(true)
            } else {
                FirebaseDatabase.getInstance().reference
                    .child("Likes").child(myPost.getPostId()).child(firebaseUser!!.uid)
                    .removeValue()

                val intent = Intent(mContext,MainActivity::class.java)
                mContext.startActivity(intent)
            }
        }

        holder.commentButton.setOnClickListener {
            val intentComment = Intent(mContext, CommentActivity::class.java)
            intentComment.putExtra("postId", myPost.getPostId())
            intentComment.putExtra("publisherId", myPost.getPublisher())
            mContext.startActivity(intentComment)
        }
    }

    private fun getTotalComment(comment: TextView, postId: String) {
        val commentRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postId)
    }

    private fun numberOfLikes(likes: TextView, postId: String) {
        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postId)

        likesRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    likes.text = snapshot.childrenCount.toString() + " Likes"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun ngelike(postId: String, likeButton: ImageView) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postId)

        likesRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(firebaseUser!!.uid).exists()){
                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag = "Liked"
                } else {
                    likeButton.setImageResource(R.drawable.heart_not_clicked)
                    likeButton.tag = "Like"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun publisherInfo(profileImage: CircleImageView, username: TextView, publisher: TextView, publisherID: String) {
        val usersRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(publisherID)

        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)
                    Glide.with(mContext).load(user?.getImage()).into(profileImage)
                   // Picasso.get().load(user?.getImage()).into(profileImage)
                    username.text = user?.getUsername()
                    publisher.text = user?.getUsername()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}

class PostViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
    var profileImage  : CircleImageView = itemView.findViewById(R.id.circleImage)
    var postImage     : ImageView = itemView.findViewById(R.id.image_postHome)
    var likeButton    : ImageView = itemView.findViewById(R.id.btnLikePost)
    var shareButton   : ImageView = itemView.findViewById(R.id.btnSharePost)
    var commentButton : ImageView = itemView.findViewById(R.id.btnCommentPost)
    var saveButton    : ImageView = itemView.findViewById(R.id.btnSavePost)
    var username      : TextView = itemView.findViewById(R.id.txt_usernamePost)
    var likes         : TextView = itemView.findViewById(R.id.tvPostLike)
    var publisher     : TextView = itemView.findViewById(R.id.tvPublisherPost)
    var comment       : TextView = itemView.findViewById(R.id.tvCommentPost)
    var description   : TextView = itemView.findViewById(R.id.tvDescriptionPost)
}
