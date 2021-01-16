package com.example.kurikulumappinstagram.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.kurikulumappinstagram.R
import com.example.kurikulumappinstagram.fragment.ProfileFragment
import com.example.kurikulumappinstagram.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class SearchUserAdapter(
    private var mContext: Context,
    private val mUser: List<User>,
    private var isFragment: Boolean = false
) : RecyclerView.Adapter<SearchUserAdapter.SearchUserViewHolder>() {

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUserViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.list_item_search, parent, false)
        return SearchUserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    override fun onBindViewHolder(holder: SearchUserViewHolder, position: Int) {
        val user = mUser[position]
        holder.userNametxtView.text = user.getUsername()
        holder.fullNametxtView.text = user.getFullname()
        Picasso.get().load(user.getImage()).into(holder.userProfileImage)

        cekFollowingStatus(user.getUID(),holder.followButton)

        holder.itemView.setOnClickListener {
            val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            pref.putString("profileId", user.getUID())
            pref.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frag_container, ProfileFragment()).commit()
        }
        holder.followButton.setOnClickListener {
            if (holder.followButton.text.toString() == "Follow") {

                firebaseUser?.uid.let { it ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it.toString())
                        .child("Following").child(user.getUID())
                        .setValue(true).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseUser?.uid.let { it ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUID())
                                        .child("Followers").child(it.toString())
                                        .setValue(true).addOnCompleteListener {
                                            if (task.isSuccessful){}

                                        }
                                }
                            }
                        }
                }

            } else {
                firebaseUser?.uid.let { it ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it.toString())
                        .child("Following").child(user.getUID())
                        .removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                firebaseUser?.uid.let { it ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUID())
                                        .child("Followers").child(it.toString())
                                        .removeValue().addOnCompleteListener { task ->
                                            if (task.isSuccessful){

                                            }
                                        }
                                }
                            }
                        }
                }
            }
        }
    }

    private fun cekFollowingStatus(uid: String, followButton: Button) {

        val followingRef = firebaseUser?.uid.let { it ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it.toString())
                .child("Following")
        }

        followingRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(uid).exists()){
                    followButton.text = "Following"
                }
                else{
                    followButton.text = "Follow"
                }
            }

        })
    }

    class SearchUserViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNametxtView: TextView = itemView.findViewById(R.id.txt_username)
        val fullNametxtView: TextView = itemView.findViewById(R.id.txt_fullname)
        val userProfileImage: ImageView = itemView.findViewById(R.id.img_search_profile)
        val followButton: Button = itemView.findViewById(R.id.btn_follow_search)

    }
}


