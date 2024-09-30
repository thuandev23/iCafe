package com.pro.shopfee.fragment

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.activity.DrinkDetailActivity
import com.pro.shopfee.activity.NotificationActivity
import com.pro.shopfee.adapter.BannerViewPagerAdapter
import com.pro.shopfee.adapter.CategoryPagerAdapter
import com.pro.shopfee.adapter.NewsAdapter
import com.pro.shopfee.event.SearchKeywordEvent
import com.pro.shopfee.listener.IClickDrinkListener
import com.pro.shopfee.model.Category
import com.pro.shopfee.model.Drink
import com.pro.shopfee.model.News
import com.pro.shopfee.model.Notification
import com.pro.shopfee.model.User
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction.startActivity
import com.pro.shopfee.utils.Utils
import me.relex.circleindicator.CircleIndicator3
import org.greenrobot.eventbus.EventBus
import java.util.*

class HomeFragment : Fragment() {
    private lateinit var originalUser: User

    private var mView: View? = null
    private var viewPagerDrinkFeatured: ViewPager2? = null
    private var indicatorDrinkFeatured: CircleIndicator3? = null
    private var viewPagerCategory: ViewPager2? = null
    private var tabCategory: TabLayout? = null
    private var edtSearchName: EditText? = null
    private var imgSearch: ImageView? = null
    private var imgNotification: ImageView? = null
    private var countNotification: TextView? = null
    private var listDrinkFeatured: MutableList<Drink>? = null
    private var listCategory: MutableList<Category>? = null
    private var mCategoryValueEventListener: ValueEventListener? = null
    private var mDrinkValueEventListener: ValueEventListener? = null
    private val mHandlerBanner = Handler(Looper.getMainLooper())
    private val mRunnableBanner = Runnable {
        if (viewPagerDrinkFeatured == null || listDrinkFeatured == null || listDrinkFeatured!!.isEmpty()) {
            return@Runnable
        }
        if (viewPagerDrinkFeatured!!.currentItem == listDrinkFeatured!!.size - 1) {
            viewPagerDrinkFeatured!!.currentItem = 0
            return@Runnable
        }
        viewPagerDrinkFeatured!!.currentItem = viewPagerDrinkFeatured!!.currentItem + 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_home, container, false)
        initUi()
        initListener()
        getUnreadNotificationCount()
        loadListDrinkBanner()
        getListCategory()
        getListNews()
        return mView
    }

    private fun initUi() {
        val tvUsername = mView!!.findViewById<TextView>(R.id.tv_user_name)
        val imageUser = mView!!.findViewById<ImageView>(R.id.img_user_profile)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userReference =
                MyApplication[this.requireContext()].getUserDatabaseReference()?.child(userId)
            userReference?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    originalUser = snapshot.getValue(User::class.java) ?: return
                    tvUsername.text = originalUser.username
                    loadImageFromFirebaseStorage("user_images/$userId.jpg", imageUser)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("InfoUserActivity", "Failed to read user data: ${error.message}")
                }
            })
        }
        viewPagerDrinkFeatured = mView!!.findViewById(R.id.view_pager_drink_featured)
        indicatorDrinkFeatured = mView!!.findViewById(R.id.indicator_drink_featured)
        viewPagerCategory = mView!!.findViewById(R.id.view_pager_category)
        viewPagerCategory?.isUserInputEnabled = false
        tabCategory = mView!!.findViewById(R.id.tab_category)
        edtSearchName = mView!!.findViewById(R.id.edt_search_name)
        imgSearch = mView!!.findViewById(R.id.img_search)
        imgNotification = mView!!.findViewById(R.id.img_notification)
        //countNotification = mView!!.findViewById(R.id.tv_notification_count)
    }

    private fun initListener() {
        edtSearchName!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val strKey = s.toString().trim { it <= ' ' }
                if (strKey == "" || strKey.isEmpty()) {
                    searchDrink()
                }
            }
        })
        imgSearch!!.setOnClickListener { searchDrink() }
        edtSearchName!!.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchDrink()
                return@setOnEditorActionListener true
            }
            false
        }
        imgNotification!!.setOnClickListener {
            startActivity(requireActivity(), NotificationActivity::class.java)
        }
    }

    private fun getUnreadNotificationCount() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val databaseReference = FirebaseDatabase.getInstance()
            .getReference("notifications/tokens/$currentUserId/notification")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var unreadCount = 0
                val notifications = mutableListOf<Notification>()
                for (dataSnapshot in snapshot.children) {
                    val notification = dataSnapshot.getValue(Notification::class.java)
                    notification?.let {
                        notifications.add(it)
                        if (it.isRead == false) {
                            unreadCount++
                        }
                    }
                }
                countNotification?.text = unreadCount.toString()

            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })
    }

    private fun loadImageFromFirebaseStorage(imagePath: String, imageUser: ImageView) {
        val storageRef = FirebaseStorage.getInstance().getReference(imagePath)
        storageRef.downloadUrl.addOnSuccessListener { uri ->

            Glide.with(this).load(uri).circleCrop().into(imageUser)

        }.addOnFailureListener { exception ->
            Log.e("InfoUserActivity", "Failed to download image: ${exception.message}")
        }
    }

    private fun getListNews() {
        // example data
        val listNews = mutableListOf<News>()
        listNews.add(
            News(
                "1", "Title 1", "Description 1", "https://picsum.photos/200/300", "2021-09-01"
            )
        )
        listNews.add(
            News(
                "2", "Title 2", "Description 2", "https://picsum.photos/200/300", "2021-09-02"
            )
        )
        listNews.add(
            News(
                "3", "Title 3", "Description 3", "https://picsum.photos/200/300", "2021-09-03"
            )
        )
        listNews.add(
            News(
                "4", "Title 4", "Description 4", "https://picsum.photos/200/300", "2021-09-04"
            )
        )
        listNews.add(
            News(
                "5", "Title 5", "Description 5", "https://picsum.photos/200/300", "2021-09-05"
            )
        )

        // show data
        val recyclerView = mView?.findViewById<RecyclerView>(R.id.rv_news)
        recyclerView?.layoutManager = GridLayoutManager(activity, 1)
        recyclerView?.adapter = NewsAdapter(listNews)

    }

    private fun loadListDrinkBanner() {
        if (activity == null) return
        mDrinkValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (listDrinkFeatured != null) {
                    listDrinkFeatured!!.clear()
                } else {
                    listDrinkFeatured = ArrayList()
                }
                for (dataSnapshot in snapshot.children) {
                    val drink = dataSnapshot.getValue(Drink::class.java)
                    if (drink != null && drink.isFeatured) {
                        listDrinkFeatured!!.add(drink)
                    }
                }
                displayListBanner()
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        MyApplication[requireActivity()].getDrinkDatabaseReference()
            ?.addValueEventListener(mDrinkValueEventListener!!)
    }

    private fun displayListBanner() {
        val adapter = BannerViewPagerAdapter(listDrinkFeatured, object : IClickDrinkListener {
            override fun onClickDrinkItem(drink: Drink) {
                val bundle = Bundle()
                bundle.putLong(Constant.DRINK_ID, drink.id)
                startActivity(activity!!, DrinkDetailActivity::class.java, bundle)
            }
        })
        viewPagerDrinkFeatured!!.adapter = adapter
        indicatorDrinkFeatured!!.setViewPager(viewPagerDrinkFeatured)
        viewPagerDrinkFeatured!!.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mHandlerBanner.removeCallbacks(mRunnableBanner)
                mHandlerBanner.postDelayed(mRunnableBanner, 3000)
            }
        })
    }

    private fun getListCategory() {
        if (activity == null) return
        mCategoryValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (listCategory != null) {
                    listCategory!!.clear()
                } else {
                    listCategory = ArrayList()
                }
                for (dataSnapshot in snapshot.children) {
                    val category = dataSnapshot.getValue(
                        Category::class.java
                    )
                    if (category != null) {
                        listCategory!!.add(category)
                    }
                }
                displayTabsCategory()
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        MyApplication[requireActivity()].getCategoryDatabaseReference()
            ?.addValueEventListener(mCategoryValueEventListener!!)
    }

    private fun displayTabsCategory() {
        if (activity == null || listCategory == null || listCategory!!.isEmpty()) return
        viewPagerCategory!!.offscreenPageLimit = listCategory!!.size
        val adapter = CategoryPagerAdapter(requireActivity(), listCategory)
        viewPagerCategory!!.adapter = adapter
        TabLayoutMediator(
            tabCategory!!, viewPagerCategory!!
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = listCategory!![position].name!!.toLowerCase(Locale.getDefault())
        }.attach()
    }

    private fun searchDrink() {
        val strKey = edtSearchName!!.text.toString().trim { it <= ' ' }
        EventBus.getDefault().post(SearchKeywordEvent(strKey))
        Utils.hideSoftKeyboard(requireActivity())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (activity != null && mCategoryValueEventListener != null) {
            MyApplication[requireActivity()].getCategoryDatabaseReference()
                ?.removeEventListener(mCategoryValueEventListener!!)
        }
        if (activity != null && mDrinkValueEventListener != null) {
            MyApplication[requireActivity()].getDrinkDatabaseReference()
                ?.removeEventListener(mDrinkValueEventListener!!)
        }
    }
}