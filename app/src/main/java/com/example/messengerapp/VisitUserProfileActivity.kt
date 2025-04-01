package com.example.messengerapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.messengerapp.model.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class VisitUserProfileActivity : AppCompatActivity() {

    private var userVisitId: String = ""
    private var user: Users? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_user_profile)
        val username_tv = findViewById<TextView>(R.id.username)
        val avatar_iv = findViewById<ImageView>(R.id.profile_image)
        val cover_iv = findViewById<ImageView>(R.id.cover_image)
        val fb_btn = findViewById<ImageView>(R.id.fb_display)
        val insta_btn = findViewById<ImageView>(R.id.instagram_display)
        val website_btn = findViewById<ImageView>(R.id.website_display)
        val send_msg_btn = findViewById<Button>(R.id.send_msg_btn)

        userVisitId = intent.getStringExtra("visit_id")!!

        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(userVisitId)
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    user = p0.getValue(Users::class.java)
                    username_tv.text = user!!.getUsername()
                    Picasso.get().load(user!!.getProfile()).into(avatar_iv)
                    Picasso.get().load(user!!.getCover()).into(cover_iv)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        fb_btn.setOnClickListener {
            val uri = Uri.parse(user!!.getFacebook())
            startActivity(Intent(Intent.ACTION_VIEW, uri))

        }

        insta_btn.setOnClickListener {
            // open instagram
            val uri = Uri.parse(user!!.getInstagram())
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        website_btn.setOnClickListener {
            // open website
            val uri = Uri.parse(user!!.getWebsite())
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        send_msg_btn.setOnClickListener {
            val intent = Intent(this@VisitUserProfileActivity, MessageChatActivity::class.java)
            intent.putExtra("visit_id", user!!.getUid())
            startActivity(intent)
        }
    }
}