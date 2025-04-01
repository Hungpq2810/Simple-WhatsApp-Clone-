package com.example.messengerapp.adapter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.MessageChatActivity
import com.example.messengerapp.R
import com.example.messengerapp.VisitUserProfileActivity
import com.example.messengerapp.model.Chat
import com.example.messengerapp.model.Users
import com.google.firebase.database.DataSnapshot
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(mContext: Context, mUsers: List<Users>, isChatChecked: Boolean):
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private val mContext: Context
    private val mUsers: List<Users>
    private val isChatChecked: Boolean
    var lastMessage: String = ""

    init {
        this.mContext = mContext
        this.mUsers = mUsers
        this.isChatChecked = isChatChecked
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout, p0, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        Log.d("UserAdapter", "mUsers.size: ${mUsers.size}")
        return mUsers.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val user: Users = mUsers[p1]
        p0.username.text = user.getUsername()
        Picasso.get().load(user.getProfile()).placeholder(R.drawable.ic_profile).into(p0.profile_image)
        p0.img_on.visibility = if (user.getStatus() == "online") View.VISIBLE else View.GONE
        p0.img_off.visibility = if (user.getStatus() == "online") View.GONE else View.VISIBLE
        p0.last_msg.visibility = if (isChatChecked) View.VISIBLE else View.GONE

        if (isChatChecked) {
            retrieveLastMessage(user.getUid(), p0.last_msg)
        } else {
            p0.last_msg.visibility = View.GONE
        }

        if (isChatChecked) {
            if (user.getStatus() == "online") {
                p0.img_on.visibility = View.VISIBLE
                p0.img_off.visibility = View.GONE
            } else {
                p0.img_on.visibility = View.GONE
                p0.img_off.visibility = View.VISIBLE
            }
        } else {
            p0.img_on.visibility = View.GONE
            p0.img_off.visibility = View.GONE
        }

        p0.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Send Message",
                "Visit Profile"
            )
            val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
            builder.setTitle("What do you want?")
            builder.setItems(options, DialogInterface.OnClickListener { dialog, position ->
                if (position == 0) {
                    // send message
                    val intent = Intent(mContext, MessageChatActivity::class.java)
                    intent.putExtra("visit_id", user.getUid())
                    mContext.startActivity(intent)
                } else {
                    // visit profile
                    val intent = Intent(mContext, VisitUserProfileActivity::class.java)
                    intent.putExtra("visit_id", user.getUid())
                    mContext.startActivity(intent)
                }
            })
            builder.show()
        }


        Log.d("UserAdapter", "user: $user")
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var username: TextView
        var profile_image: CircleImageView
        var img_on: CircleImageView
        var img_off: CircleImageView
        var last_msg: TextView

        init {
            username = itemView.findViewById(R.id.username)
            profile_image = itemView.findViewById(R.id.profile_image)
            img_on = itemView.findViewById(R.id.image_online)
            img_off = itemView.findViewById(R.id.image_offline)
            last_msg = itemView.findViewById(R.id.last_message)
        }
    }

    private fun retrieveLastMessage(chatUserId: String, lastMessageTv: TextView) {
        // retrieve last message
        lastMessage = "defaultMsg"
        val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val reference = com.google.firebase.database.FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (dataSnapshot in p0.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && chat.getSender().equals(chatUserId) ||
                        chat.getReceiver().equals(chatUserId) && chat.getSender().equals(firebaseUser.uid)) {
                        lastMessage = chat.getMessage()
                    }
                }

                when (lastMessage) {
                    "defaultMsg" -> lastMessageTv.text = "No Message"
                    "sent you an image." -> lastMessageTv.text = "Image Sent"
                    else -> lastMessageTv.text = lastMessage
                }

                lastMessage = "defaultMsg"
            }

            override fun onCancelled(p0: com.google.firebase.database.DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}