package com.example.messengerapp.adapter

import android.content.Context
import android.content.Intent
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.ImageViewActivity
import com.example.messengerapp.R
import com.example.messengerapp.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatsAdapter(
    mContext: Context,
    mChatList: List<Chat>,
    imageUrl: String
) : RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage = itemView.findViewById<CircleImageView>(R.id.profile_image) ?: null
        var showTextMsg = itemView.findViewById<TextView>(R.id.show_text_msg)
        var textSeen = itemView.findViewById<TextView>(R.id.text_seen)
        var leftImageView = itemView.findViewById<ImageView>(R.id.left_image_view)
        var rightImageView = itemView.findViewById<ImageView>(R.id.right_image_view)
    }

    private val mContext: Context = mContext
    private val mChatList: List<Chat> = mChatList
    private val imageUrl: String = imageUrl
    var firebaseUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return if (position == 1) {
            val view = LayoutInflater.from(mContext).inflate(R.layout.message_item_right, parent, false)
            ViewHolder(view)
        } else {
            val view = LayoutInflater.from(mContext).inflate(R.layout.message_item_left, parent, false)
            ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return mChatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat: Chat = mChatList[position]

        if (holder.profileImage != null){
            Picasso.get().load(imageUrl).into(holder.profileImage)
        }

        if (chat.getMessage().equals("sent you an image.") && !chat.getUrl().equals("")) {
            if (chat.getSender().equals(firebaseUser!!.uid)) {
                holder.showTextMsg.visibility = View.GONE
                holder.rightImageView.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.rightImageView)

                holder.rightImageView.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View Full Image",
                        "Delete Image",
                        "Cancel"
                    )
                    var builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("What do you want?")
                    builder.setItems(options) { dialog, which ->
                        if (which == 0) {
                            val intent = Intent(mContext, ImageViewActivity::class.java)
                            intent.putExtra("url", chat.getUrl())
                            mContext.startActivity(intent)
                        } else
                        if (which == 1) {
                            deleteSentMessage(position, holder)
                        }
                    }
                    builder.show()

                }
            } else if (!chat.getSender().equals(firebaseUser!!.uid)) {
                holder.showTextMsg.visibility = View.GONE
                holder.leftImageView.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.leftImageView)

                holder.leftImageView.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View Full Image",
                        "Cancel"
                    )
                    var builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("What do you want?")
                    builder.setItems(options) { dialog, which ->
                        if (which == 0) {
                            val intent = Intent(mContext, ImageViewActivity::class.java)
                            intent.putExtra("url", chat.getUrl())
                            mContext.startActivity(intent)
                        }
                    }
                    builder.show()
                }
            }
        } else {
            holder.showTextMsg.text = chat.getMessage()

            if (firebaseUser!!.uid == chat.getSender()) {
                holder.showTextMsg.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "Delete Message",
                        "Cancel"
                    )
                    var builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("What do you want?")
                    builder.setItems(options) { dialog, which ->
                        if (which == 0) {
                            deleteSentMessage(position, holder)
                        }
                    }
                    builder.show()
                }
            }
        }

        if (firebaseUser!!.uid == chat.getSender()) {
            holder.textSeen.visibility = View.VISIBLE
            if (chat.getIsSeen()) {
                holder.textSeen.text = "Seen"
            } else {
                holder.textSeen.text = "Sent"
            }
        } else {
            holder.textSeen.visibility = View.GONE
        }


        if (position == mChatList.size - 1) {
            Log.d("ChatsAdapter", "chat: ${chat.getIsSeen()}")
            Log.d("ChatsAdapter", "chat: ${chat.getMessageId()}")
            if (chat.getIsSeen()) {
                holder.textSeen.text = "Seen"
                if (chat.getMessage().equals("sent you an image.") && !chat.getUrl().equals("")) {
                    val lp: ViewGroup.MarginLayoutParams =
                        holder.showTextMsg.layoutParams as ViewGroup.MarginLayoutParams
                    lp.setMargins(0, 245, 10, 0)
                    holder.textSeen.layoutParams = lp
                }
            } else {
                holder.textSeen.text = "Sent"
                if (chat.getMessage().equals("sent you an image.") && !chat.getUrl().equals("")) {
                    val lp: ViewGroup.MarginLayoutParams =
                        holder.showTextMsg.layoutParams as ViewGroup.MarginLayoutParams
                    lp.setMargins(0, 245, 10, 0)
                    holder.textSeen.layoutParams = lp
                }

            }
        } else {
            holder.textSeen.visibility = View.GONE
        }
    }

    override fun getItemViewType(position: Int): Int {

        firebaseUser = FirebaseAuth.getInstance().currentUser
        return if (mChatList[position].getSender().equals(firebaseUser!!.uid)) {
            1
        } else {
            0
        }
    }

    private fun deleteSentMessage(position: Int, holder: ViewHolder) {
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
            .child(mChatList[position].getMessageId())
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(holder.itemView.context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(holder.itemView.context, "Failed, Not deleted", Toast.LENGTH_SHORT).show()
                }
            }
    }
}