package com.example.messengerapp.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.messengerapp.R
import com.example.messengerapp.model.Users
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {

    var usersReference: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null

    private val RequestCode = 438
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null
    private var coverChecker: String? = ""
    private var socialChecker: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val username: TextView = view.findViewById(R.id.username)
        val profile_image: CircleImageView = view.findViewById(R.id.profile_image)
        val cover_image: ImageView = view.findViewById(R.id.cover_image)
        val set_fb: ImageView = view.findViewById(R.id.set_fb)
        val set_insta: ImageView = view.findViewById(R.id.set_instagram)
        val set_website: ImageView = view.findViewById(R.id.set_website)


        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")

        usersReference!!.addValueEventListener(object : ValueEventListener {
            @SuppressLint("MissingInflatedId")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(Users::class.java)
                    username.text = user!!.getUsername()
                    Picasso.get().load(user.getProfile()).into(profile_image)
                    Picasso.get().load(user.getCover()).into(cover_image)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("SettingsFragment", "onCancelled: ${error.message}")
            }
        })

        profile_image.setOnClickListener {
            Log.d("SettingsFragment", "profile_image clicked")
            pickImage()
        }

        cover_image.setOnClickListener {
            Log.d("SettingsFragment", "cover_image clicked")
            coverChecker = "cover"
            pickImage()
        }

        set_fb.setOnClickListener {
            Log.d("SettingsFragment", "set_fb clicked")
            socialChecker = "facebook"
            setSocialLink()
        }

        set_insta.setOnClickListener {
            Log.d("SettingsFragment", "set_insta clicked")
            socialChecker = "instagram"
            setSocialLink()
        }

        set_website.setOnClickListener {
            Log.d("SettingsFragment", "set_website clicked")
            socialChecker = "website"
            setSocialLink()
        }


        return view
    }

    private fun pickImage() {
        Log.d("SettingsFragment", "pickImage")
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data != null) {
            val imageUri = data.data
            Toast.makeText(context, "Uploading...", Toast.LENGTH_SHORT).show()
            uploadImageToDatabase(imageUri!!)
            Log.d("SettingsFragment", "imageUri: $imageUri")
        }
    }

    private fun uploadImageToDatabase(uri: Uri) {
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("Image is uploading, please wait...")
        progressBar.show()

        val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")
        var uploadTask: StorageTask<*>
        uploadTask = fileRef.putFile(uri)
        uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if(!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation fileRef.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUrl = task.result
                val url = downloadUrl.toString()
                if (coverChecker == "cover") {
                    val map = HashMap<String, Any>()
                    map["cover"] = url
                    usersReference!!.updateChildren(map)
                    coverChecker = ""
                } else {
                    val map = HashMap<String, Any>()
                    map["profile"] = url
                    usersReference!!.updateChildren(map)
                    coverChecker = ""
                }
                progressBar.dismiss()
            }
        }
    }

    private fun setSocialLink() {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(context, androidx.appcompat.R.style.Theme_AppCompat_DayNight_Dialog_Alert)
        val editText = EditText(context)

        if (socialChecker == "website") {
            builder.setTitle("Write URL:")
            editText.hint = "e.g. www.google.com"
        } else {
            builder.setTitle("Write username:")
            editText.hint = "e.g. google"
        }
        builder.setView(editText)
        builder.setPositiveButton("Create") { dialog, which ->
            val str = editText.text.toString()
            if (str == "") {
                Toast.makeText(context, "Please write something...", Toast.LENGTH_SHORT).show()
            } else {
                saveSocialLink(str)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun saveSocialLink(str: String) {
        val map = HashMap<String, Any>()
        when (socialChecker) {
            "facebook" -> {
                map["facebook"] = "https://m.facebook.com/$str"
            }
            "instagram" -> {
                map["instagram"] = "https://m.instagram.com/$str"
            }
            "website" -> {
                if (str.substring(0, 4) != "http") {
                    map["website"] = "https://$str"
                } else {
                    map["website"] = str
                }
            }
        }
        usersReference!!.updateChildren(map).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Updated successfully.", Toast.LENGTH_SHORT).show()
            }
        }
        socialChecker = ""
    }
}