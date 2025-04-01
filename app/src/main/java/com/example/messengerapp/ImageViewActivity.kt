package com.example.messengerapp

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso

class ImageViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        val intent = intent
        val imageUrl = intent.getStringExtra("url")

        val imageView = findViewById<ImageView>(R.id.image_view)
        Picasso.get().load(imageUrl).into(imageView)

        imageView.setOnClickListener {
            finish()
        }
    }
}
