package com.pro.shopfee.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.model.User
import kotlinx.android.synthetic.main.activity_info_user.btn_update_info_user

class InfoUserActivity : BaseActivity() {
    private lateinit var tvUsername: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvUserAddress: TextView
    private lateinit var tvUserGender: TextView
    private lateinit var tvUserBirthday: TextView
    private lateinit var imageUser: ImageView
    private var imageUri: Uri? = null
    private val auth = FirebaseAuth.getInstance()
    private lateinit var originalUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_user)
        initToolbar()
        initUi()
        initData()
    }

    private fun initToolbar() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { finish() }
        tvToolbarTitle.text = getString(R.string.info_user)
    }

    private fun initUi() {
        imageUser = findViewById(R.id.img_user)
        tvUsername = findViewById(R.id.tv_username_info_user)
        tvUserEmail = findViewById(R.id.tv_email_info_user)
        tvUserPhone = findViewById(R.id.tv_phone_info_user)
        tvUserAddress = findViewById(R.id.tv_address_info_user)
        tvUserGender = findViewById(R.id.tv_gender_info_user)
        tvUserBirthday = findViewById(R.id.tv_birthday_info_user)

        // Set click listener for the image
        imageUser.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }

        btn_update_info_user.setOnClickListener {
            val updatedUser = User(
                image = imageUri?.toString() ?: originalUser.image,
                email = originalUser.email,
                username = originalUser.username,
                phone = tvUserPhone.text.toString(),
                address = tvUserAddress.text.toString(),
                gender = tvUserGender.text.toString(),
                birthday = tvUserBirthday.text.toString(),
                password = originalUser.password

            )
                updateUserData(updatedUser)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            imageUser.setImageURI(imageUri)
        }
    }

    private fun initData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = MyApplication[this].getUserDatabaseReference()?.child(userId)
            userReference?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    originalUser = snapshot.getValue(User::class.java) ?: return
                    tvUsername.text = originalUser.username
                    tvUserEmail.text = originalUser.email
                    tvUserPhone.text = originalUser.phone
                    tvUserAddress.text = originalUser.address
                    tvUserGender.text = originalUser.gender
                    tvUserBirthday.text = originalUser.birthday
                    loadImageFromFirebaseStorage("user_images/$userId.jpg")
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("InfoUserActivity", "Failed to read user data: ${error.message}")
                }
            })
        }
    }

    private fun loadImageFromFirebaseStorage(imagePath: String) {
        val storageRef = FirebaseStorage.getInstance().getReference(imagePath)
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            if (!isDestroyed && !isFinishing) {
                Glide.with(this).load(uri).circleCrop().into(imageUser)
            }
        }.addOnFailureListener { exception ->
            Log.e("InfoUserActivity", "Failed to download image: ${exception.message}")
        }
    }


    private fun updateUserData(updatedUser: User) {
        val userId = auth.currentUser?.uid ?: return
        imageUri?.let { uri ->
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("user_images/$userId.jpg")
            imageRef.putFile(uri).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    updatedUser.image = imageUrl.toString()
                    saveUserToFirebase(userId, updatedUser)
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to get image URL!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Image upload failed!", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            updatedUser.image = originalUser.image
            saveUserToFirebase(userId, updatedUser)
        }
    }

    private fun saveUserToFirebase(userId: String, updatedUser: User) {
        val userReference = MyApplication[this].getUserDatabaseReference()?.child(userId)
        userReference?.setValue(updatedUser)?.addOnSuccessListener {
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            finish()  // Navigate back to the previous activity
        }?.addOnFailureListener {
            Toast.makeText(this, "Profile update failed!", Toast.LENGTH_SHORT).show()
        }
    }
}
