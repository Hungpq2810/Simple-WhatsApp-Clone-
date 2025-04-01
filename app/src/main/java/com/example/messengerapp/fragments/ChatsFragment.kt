package com.example.messengerapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.R
import com.example.messengerapp.adapter.UserAdapter
import com.example.messengerapp.model.ChatList
import com.example.messengerapp.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatsFragment : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var userChatList : List<ChatList>? = null
    private var chatListRv: RecyclerView? = null
    private var firebaseUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chats, container, false)
        chatListRv = view.findViewById(R.id.chat_recycler_view)
        chatListRv!!.setHasFixedSize(true)
        chatListRv!!.layoutManager = LinearLayoutManager(requireContext())
        userChatList = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (userChatList as ArrayList).clear()

                for (dataSnapshot in p0.children) {
                    val chatList = dataSnapshot.getValue(ChatList::class.java)
                    (userChatList as ArrayList).add(chatList!!)
                }

                retriveChatList()
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


        return view
    }

    private fun retriveChatList() {
        mUsers = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("Users")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList).clear()

                for (dataSnapshot in p0.children) {
                    val user = dataSnapshot.getValue(Users::class.java)
                    Log.d("ChatsFragment", "username: ${user?.getUsername()}")

                    for (eachChatList in userChatList!!) {
                        if (user!!.getUid() == eachChatList.getId()) {
                            (mUsers as ArrayList).add(user)
                        }
                    }
                }

                if (isAdded) {  // Ensure the fragment is still attached before setting adapter
                    userAdapter = UserAdapter(requireContext(), mUsers!!, true)
                    chatListRv!!.adapter = userAdapter
                    Log.d("ChatsFragment", "adapter count: ${userAdapter!!.itemCount}")
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}