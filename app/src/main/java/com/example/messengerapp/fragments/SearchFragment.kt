package com.example.messengerapp.fragments

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
import com.example.messengerapp.R
import com.example.messengerapp.adapter.UserAdapter
import com.example.messengerapp.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class SearchFragment : Fragment() {
    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var usersRv: RecyclerView? = null
    private var searchUsersET: EditText? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        mUsers = ArrayList()

        searchUsersET = view.findViewById(R.id.searchUsersET)
        usersRv = view.findViewById(R.id.searchList)

        searchUsersET!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchForUsers(p0.toString().lowercase(Locale.getDefault()))
                Log.d("SearchFragment", "searchForUsers: ${p0.toString()}")
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        userAdapter = UserAdapter(requireContext(), mUsers!!, false)
        usersRv?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }
        retrieveAllUsers()


        return view
    }

    private fun retrieveAllUsers() {
        val firebaseUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val refUsers = FirebaseDatabase.getInstance().reference.child("Users")

        refUsers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (!isAdded) return

                (mUsers as ArrayList<Users>).clear()
                if (searchUsersET?.text.toString() == "") {
                    for (snapshot in p0.children) {
                        val user = snapshot.getValue(Users::class.java)
                        if (user != null && user.getUid() != firebaseUserId) {
                            (mUsers as ArrayList<Users>).add(user)
                        }
                    }
                }
                userAdapter = UserAdapter(requireContext(), mUsers!!, false)
                usersRv?.adapter = userAdapter
                Log.d("SearchFragment", "mUsers: ${(mUsers as ArrayList<Users>).size}")
                Log.d("SearchFragment", "adapter count: ${userAdapter!!.itemCount}")
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun searchForUsers(str: String) {
        val firebaseUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val queryUsers = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("search")
            .startAt(str)
            .endAt(str + "\uf8ff")

        queryUsers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
                for (snapshot in p0.children) {
                    val user = snapshot.getValue(Users::class.java)
                    if (user != null && user.getUid() != firebaseUserId) {
                        (mUsers as ArrayList<Users>).add(user)
                    }
                }
                if (isAdded) {  // Ensure the fragment is still attached before setting adapter
                    userAdapter = UserAdapter(requireContext(), mUsers!!, true)
                    usersRv!!.adapter = userAdapter
                    Log.d("ChatsFragment", "adapter count: ${userAdapter!!.itemCount}")
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}