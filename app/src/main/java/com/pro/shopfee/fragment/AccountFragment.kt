package com.pro.shopfee.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.activity.ChangeLanguageActivity
import com.pro.shopfee.activity.ChangePasswordActivity
import com.pro.shopfee.activity.ContactActivity
import com.pro.shopfee.activity.FeedbackActivity
import com.pro.shopfee.activity.InfoUserActivity
import com.pro.shopfee.activity.LoginActivity
import com.pro.shopfee.activity.MainActivity
import com.pro.shopfee.activity.ChatActivity
import com.pro.shopfee.model.User
import com.pro.shopfee.prefs.DataStoreManager.Companion.user
import com.pro.shopfee.utils.GlobalFunction.startActivity

class AccountFragment : Fragment() {

    private var mView: View? = null
    private var layoutChat: LinearLayout? = null
    private var layoutInfoUser: LinearLayout? = null
    private var layoutChangeLanguage: LinearLayout? = null
    private var layoutFeedback: LinearLayout? = null
    private var layoutContact: LinearLayout? = null
    private var layoutChangePassword: LinearLayout? = null
    private var layoutSignOut: LinearLayout? = null
    private lateinit var originalUser: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_account, container, false)
        initToolbar()
        initUi()
        initListener()
        return mView
    }

    private fun initToolbar() {
        val imgToolbarBack = mView!!.findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = mView!!.findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { backToHomeScreen() }
        tvToolbarTitle.text = getString(R.string.nav_account)
    }

    private fun backToHomeScreen() {
        val mainActivity = activity as MainActivity? ?: return
        mainActivity.viewPager2?.currentItem = 0
    }

    private fun initUi() {
        val tvUsername = mView!!.findViewById<TextView>(R.id.tv_username)
        val tvEmail = mView!!.findViewById<TextView>(R.id.tv_email)
        val imageUser = mView!!.findViewById<ImageView>(R.id.image_user)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userReference =
                MyApplication[this.requireContext()].getUserDatabaseReference()?.child(userId)
            userReference?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    originalUser = snapshot.getValue(User::class.java) ?: return
                    tvUsername.text = originalUser.username
                    tvEmail.text = originalUser.email
                    loadImageFromFirebaseStorage("user_images/$userId.jpg", imageUser)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("InfoUserActivity", "Failed to read user data: ${error.message}")
                }
            })
        }
        layoutChat = mView!!.findViewById(R.id.layout_chat)
        layoutChangeLanguage = mView!!.findViewById(R.id.layout_change_language)
        layoutFeedback = mView!!.findViewById(R.id.layout_feedback)
        layoutContact = mView!!.findViewById(R.id.layout_contact)
        layoutChangePassword = mView!!.findViewById(R.id.layout_change_password)
        layoutSignOut = mView!!.findViewById(R.id.layout_sign_out)
        layoutInfoUser = mView!!.findViewById(R.id.layout_user_info)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun initListener() {

        layoutChat!!.setOnClickListener {
            val bundle = Bundle()
            val adminUid = "8MlQiotY10QoKR8bpBAoqW2rgBq1"

                bundle.putString("receiptUid", adminUid)

            startActivity(requireActivity(), ChatActivity::class.java, bundle)
        }

        layoutChangeLanguage!!.setOnClickListener {
            startActivity(
                requireActivity(), ChangeLanguageActivity::class.java
            )
        }
        layoutInfoUser!!.setOnClickListener {
            startActivity(
                requireActivity(), InfoUserActivity::class.java
            )
        }
        layoutFeedback!!.setOnClickListener {
            startActivity(
                requireActivity(), FeedbackActivity::class.java
            )
        }
        layoutContact!!.setOnClickListener {
            startActivity(
                requireActivity(), ContactActivity::class.java
            )
        }
        layoutChangePassword!!.setOnClickListener {
            startActivity(
                requireActivity(), ChangePasswordActivity::class.java
            )
        }
        layoutSignOut!!.setOnClickListener { onClickSignOut() }
    }

    private fun loadImageFromFirebaseStorage(imagePath: String, imageUser: ImageView) {
        val storageRef = FirebaseStorage.getInstance().getReference(imagePath)
        storageRef.downloadUrl.addOnSuccessListener { uri ->

            Glide.with(this).load(uri).circleCrop().into(imageUser)

        }.addOnFailureListener { exception ->
            Log.e("InfoUserActivity", "Failed to download image: ${exception.message}")
        }
    }

    private fun onClickSignOut() {
        if (activity == null) return
        FirebaseAuth.getInstance().signOut()
        user = null
        startActivity(requireActivity(), LoginActivity::class.java)
        requireActivity().finishAffinity()
    }
}