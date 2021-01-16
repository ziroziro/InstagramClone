package com.example.kurikulumappinstagram.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kurikulumappinstagram.R
import com.example.kurikulumappinstagram.adapter.SearchUserAdapter
import com.example.kurikulumappinstagram.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*


class SearchFragment : Fragment() {

    private var recycler: RecyclerView? = null
    private var searchAdapter: SearchUserAdapter? = null
    private var mUser: MutableList<User>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        recycler = view.findViewById(R.id.recycler_search)

        recycler?.setHasFixedSize(true)
        recycler?.layoutManager = GridLayoutManager(context, 2)

        mUser = ArrayList()
        searchAdapter = context.let {
            it?.let { it1 -> SearchUserAdapter(it1, mUser as ArrayList<User>) }
        }
        recycler?.adapter = searchAdapter

        view.search_editText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (view.search_editText.toString() == "") {

                } else {
                    recycler?.visibility = View.VISIBLE
                    getUser()
                    searchUser(s.toString().toLowerCase())
                }
            }
        })
        return view
    }

    private fun searchUser(input: String) {
        val query = FirebaseDatabase.getInstance().getReference()
            .child("Users")
            .orderByChild("fullname")
            .startAt(input).endAt(input + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                mUser?.clear()

                for (s in snapshot.children) {
                    val user = s.getValue(User::class.java)
                    if (user != null) {
                        mUser?.add(user)
                    }
                }
                searchAdapter?.notifyDataSetChanged()
            }

        })
    }

    private fun getUser() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (view?.search_editText?.text.toString() == "") {
                    mUser?.clear()

                    for (s in snapshot.children) {
                        val user = s.getValue(User::class.java)
                        if (user != null) {
                            mUser?.add(user)
                        }
                    }
                    searchAdapter?.notifyDataSetChanged()
                }
            }

        })
    }
}