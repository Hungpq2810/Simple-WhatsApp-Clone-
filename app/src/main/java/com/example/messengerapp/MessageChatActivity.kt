package com.example.messengerapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.Notifications.Data
import com.example.messengerapp.Notifications.MyResponse
import com.example.messengerapp.Notifications.Sender
import com.example.messengerapp.Notifications.Token
import com.example.messengerapp.adapter.ChatsAdapter
import com.example.messengerapp.fragments.APIService
import com.example.messengerapp.fragments.AccessToken
import com.example.messengerapp.model.Chat
import com.example.messengerapp.model.Users
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Response

class MessageChatActivity : AppCompatActivity() {
    var userIdVisit: String = ""
    var firebaseUser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    lateinit var chatAdapter: ChatsAdapter
    var mChatList: List<Chat>? = null
    lateinit var recycler_view_message_chat: RecyclerView

    var notify = false
    var apiService: APIService? = null
    lateinit var accessToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

//        accessToken = AccessToken().getAccessToken()

        val toolbar: Toolbar = findViewById(R.id.toolbar_mc)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@MessageChatActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

//        apiService = Client.getClient("https://fcm.googleapis.com/", accessToken).create(APIService::class.java)
        // add headers
//        val headers = HashMap<String, String>()
//        headers["Content-Type"] = "application/json"
//        headers["Authorization"] = "key=$accessToken"
//        apiService!!.setHeader(headers)

        intent = intent
        userIdVisit = intent.getStringExtra("visit_id").toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().reference.child("Users").child(userIdVisit)

        val sendMsgBtn = findViewById<ImageView>(R.id.send_msg_btn)
        val messageEt = findViewById<EditText>(R.id.text_msg)
        val attachFileBtn = findViewById<ImageView>(R.id.attach_file_btn)
        val username_mc = findViewById<TextView>(R.id.username_mc)
        val profile_image_mc = findViewById<ImageView>(R.id.profile_image_mc)

        recycler_view_message_chat = findViewById<RecyclerView>(R.id.recycler_view_message_chat)
        recycler_view_message_chat.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recycler_view_message_chat.layoutManager = linearLayoutManager

        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val user: Users? = p0.getValue(Users::class.java)
                username_mc.text = user!!.getUsername()
                Picasso.get().load(user.getProfile()).into(profile_image_mc)

                retriveMessages(firebaseUser!!.uid, userIdVisit, user.getProfile())
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        attachFileBtn.setOnClickListener {
            notify = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), 438)

        }

        sendMsgBtn.setOnClickListener {
            // send message
            val message = messageEt.text.toString()
            if (message.isEmpty()) {
                // empty, don't send
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            } else {
                // send message
                sendMessage(firebaseUser!!.uid, userIdVisit, message)
            }
            messageEt.setText("")
        }

        seenMessage(userIdVisit)
    }

    var seenListener: ValueEventListener? = null

    private fun seenMessage(userId: String) {
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        seenListener = reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (dataSnapshot in p0.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && chat.getSender().equals(userId)) {
                        val hashMap = HashMap<String, Any>()
                        hashMap["isSeen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun sendMessage(uid: String, receiverId: String?, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key
        val messageHashMap = HashMap<String, Any>()
        messageHashMap["sender"] = uid
        messageHashMap["receiver"] = receiverId!!
        messageHashMap["message"] = message
        messageHashMap["isSeen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey!!
        reference.child("Chats").child(messageKey).setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chatListRef =
                        FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
                            .child(userIdVisit)
                    chatListRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()) {
                                chatListRef.child("id").setValue(userIdVisit)
                            }
                            val receiverRef =
                                FirebaseDatabase.getInstance().reference.child("ChatList").child(userIdVisit)
                                    .child(firebaseUser!!.uid)
                            receiverRef.child("id").setValue(firebaseUser!!.uid)
                        }

                        override fun onCancelled(p0: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })

                    val ref = FirebaseDatabase.getInstance().reference
                        .child("Users").child(firebaseUser!!.uid)
                    ref.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            val user = p0.getValue(Users::class.java)
                            if (notify) {
                                sendNotification(receiverId, user!!.getUsername(), message)
                            }
                            notify = false
                        }

                        override fun onCancelled(p0: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.data != null) {
            val progressBar = ProgressDialog(this)
            progressBar.setMessage("Image is being sent, please wait...")
            progressBar.show()

            val fireUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            var uploadTask = filePath.putFile(fireUri!!)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val messageHashMap = HashMap<String, Any>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["message"] = "sent you an image."
                    messageHashMap["url"] = url
                    messageHashMap["isSeen"] = false
                    messageHashMap["messageId"] = messageId!!

                    ref.child("Chats").child(messageId).setValue(messageHashMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                progressBar.dismiss()
                                val ref = FirebaseDatabase.getInstance().reference.child("Users")
                                    .child(firebaseUser!!.uid)
                                ref.addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(p0: DataSnapshot) {
                                        val user = p0.getValue(Users::class.java)
                                        if (notify) {
                                            sendNotification(userIdVisit, user!!.getUsername(), "sent you an image.")
                                        }
                                        notify = false
                                    }

                                    override fun onCancelled(p0: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }
                                })
                            }
                        }
                }

            }
        }
    }

    private fun retriveMessages(senderId: String, receiverId: String, receiverImageUrl: String) {
        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for (snapshot in p0.children) {
                    val chat = snapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId) ||
                        chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId)
                    ) {
                        (mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatAdapter = ChatsAdapter(this@MessageChatActivity, mChatList as ArrayList<Chat>, receiverImageUrl)
                    recycler_view_message_chat.adapter = chatAdapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onPause() {
        super.onPause()

        reference!!.removeEventListener(seenListener!!)
    }

    private fun sendNotification(receiverId: String?, username: String?, message: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")

        val query = ref.orderByKey().equalTo(receiverId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (dataSnapshot in p0.children) {
                    val token: Token? = dataSnapshot.getValue(Token::class.java)
                    val data = Data(
                        firebaseUser!!.uid,
                        R.mipmap.ic_launcher,
                        "$username: $message",
                        "New Message",
                        userIdVisit
                    )
                    val sender = Sender(data, token!!.getToken())
                    apiService?.sendNotification(sender)
                        ?.enqueue(object : retrofit2.Callback<MyResponse> {
                            override fun onResponse(
                                call: Call<MyResponse>,
                                response: Response<MyResponse>
                            ) {
                                if (response.code() == 200) {
                                    if (response.body()!!.success !== 1) {
                                        Toast.makeText(this@MessageChatActivity, "Failed, Nothing happened.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                                println(t.message)
                            }
                        })
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}