package com.example.messengerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)
        val toolbar: Toolbar = findViewById(R.id.toolbar_register)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
        val register_btn = findViewById<Button>(R.id.register_button)
        register_btn.setOnClickListener {
            registerUser()
        }
    }

    fun registerUser() {
        val username = findViewById<EditText>(R.id.username_register)
        val email = findViewById<EditText>(R.id.email_register)
        val password = findViewById<EditText>(R.id.password_register)

        val username_str = username.text.toString()
        val email_str = email.text.toString()
        val password_str = password.text.toString()

        if (username_str == "") {
            username.error = "Please enter username"
            return
        } else if (email_str == "") {
            email.error = "Please enter email"
            return
        } else if (password_str == "") {
            password.error = "Please enter password"
            return
        }

        mAuth.createUserWithEmailAndPassword(email_str, password_str)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firebaseUserId = mAuth.currentUser!!.uid
                    refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserId)
                    
                    val userHashMap = HashMap<String, Any>()
                    userHashMap["uid"] = firebaseUserId
                    userHashMap["username"] = username_str
                    userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/messenger-app-fb9e8.firebasestorage.app/o/315448958_858652371824208_3677039598187800485_n.jpg?alt=media&token=baa8f8a2-3756-4250-a109-a582cc0c337b"
                    userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/messenger-app-fb9e8.firebasestorage.app/o/canva-blue-and-pink-classy-photo-cherry-blossom-inspirational-quotes-facebook-cover-vpnA8PdWGCs.jpg?alt=media&token=59294d77-ca4b-4ccc-a2c5-be17972c2957"
                    userHashMap["status"] = "offline"
                    userHashMap["search"] = username_str.lowercase(Locale.getDefault())
                    userHashMap["facebook"] = "https://m.facebook.com"
                    userHashMap["instagram"] = "https://m.instagram.com"
                    userHashMap["website"] = "https://www.google.com"

                    refUsers.updateChildren(userHashMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val intent = Intent(this, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                        }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }

    }
}