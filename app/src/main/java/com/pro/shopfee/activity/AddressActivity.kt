package com.pro.shopfee.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.adapter.AddressAdapter
import com.pro.shopfee.adapter.AddressAdapter.IClickAddressListener
import com.pro.shopfee.event.AddressSelectedEvent
import com.pro.shopfee.model.Address
import com.pro.shopfee.prefs.DataStoreManager.Companion.user
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction
import com.pro.shopfee.utils.GlobalFunction.showToastMessage
import com.pro.shopfee.utils.StringUtil.isEmpty
import org.greenrobot.eventbus.EventBus

class AddressActivity : BaseActivity() {

    private var listAddress: MutableList<Address>? = null
    private var addressAdapter: AddressAdapter? = null
    private var addressSelectedId: Long = 0
    private var mValueEventListener: ValueEventListener? = null
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0
    private var address: String = ""
    private var edtAddress: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)
        loadDataIntent()
        initToolbar()
        initUi()
        loadListAddressFromFirebase()
        initSwipeToDelete()
    }

    private fun loadDataIntent() {
        val bundle = intent.extras ?: return
        addressSelectedId = bundle.getLong(Constant.ADDRESS_ID, 0)
    }

    private fun initToolbar() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { onBackPressed() }
        tvToolbarTitle.text = getString(R.string.address_title)
    }

    private fun initUi() {
        val rcvAddress = findViewById<RecyclerView>(R.id.rcv_address)
        val linearLayoutManager = LinearLayoutManager(this)
        rcvAddress.layoutManager = linearLayoutManager
        listAddress = ArrayList()
        addressAdapter = AddressAdapter(
            listAddress,
            object : IClickAddressListener {
                override fun onClickAddressItem(address: Address) {
                    handleClickAddress(address, address.latitude!!, address.longitude!!)
                }
            })
        rcvAddress.adapter = addressAdapter
        val btnAddAddress = findViewById<Button>(R.id.btn_add_address)
        btnAddAddress.setOnClickListener { onClickAddAddress() }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadListAddressFromFirebase() {
        showProgressDialog(true)
        mValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                showProgressDialog(false)
                resetListAddress()
                for (dataSnapshot in snapshot.children) {
                    val address = dataSnapshot.getValue(
                        Address::class.java
                    )
                    if (address != null) {
                        listAddress!!.add(0, address)
                    }
                }
                if (addressSelectedId > 0 && listAddress != null && listAddress!!.isNotEmpty()) {
                    for (address in listAddress!!) {
                        if (address.id == addressSelectedId) {
                            address.isSelected = true
                            break
                        }
                    }
                }
                if (addressAdapter != null) addressAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                showProgressDialog(false)
                showToastMessage(getString(R.string.msg_get_date_error))
            }
        }
        MyApplication[this].getAddressDatabaseReference()
            ?.orderByChild("userEmail")
            ?.equalTo(user!!.email)
            ?.addValueEventListener(mValueEventListener!!)
    }

    private fun resetListAddress() {
        if (listAddress != null) {
            listAddress!!.clear()
        } else {
            listAddress = ArrayList()
        }
    }

    private fun handleClickAddress(address: Address, lat: Double, lng: Double) {
        EventBus.getDefault().post(AddressSelectedEvent(address, lat, lng))
        finish()
    }
    private fun initSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val address = listAddress?.get(position)
                // Remove from Firebase
                address?.let {
                    showProgressDialog(true)
                    MyApplication[this@AddressActivity].getAddressDatabaseReference()
                        ?.child(it.id.toString())?.removeValue()
                }
                // Remove from the list and notify adapter
                listAddress?.removeAt(position)
                addressAdapter?.notifyItemRemoved(position)
                showProgressDialog(false)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val background = ColorDrawable(Color.RED)

                // Set the delete icon and scale it
                val deleteIcon = ContextCompat.getDrawable(this@AddressActivity, R.drawable.ic_delete)!!

                // Increase icon size by multiplying the original size (e.g., 1.5 times larger)
                val scaleFactor = 1.5f
                val intrinsicWidth = (deleteIcon.intrinsicWidth * scaleFactor).toInt()
                val intrinsicHeight = (deleteIcon.intrinsicHeight * scaleFactor).toInt()

                val iconMargin = (itemView.height - intrinsicHeight) / 2
                val iconTop = itemView.top + (itemView.height - intrinsicHeight) / 2
                val iconBottom = iconTop + intrinsicHeight

                // Draw red background
                if (dX > 0) { // Swiping to the right
                    val iconLeft = itemView.left + iconMargin
                    val iconRight = iconLeft + intrinsicWidth
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                } else if (dX < 0) { // Swiping to the left
                    val iconLeft = itemView.right - iconMargin - intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                } else { // No swipe
                    background.setBounds(0, 0, 0, 0)
                }

                background.draw(c)
                deleteIcon.draw(c)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(findViewById(R.id.rcv_address))
    }
    @SuppressLint("InflateParams, MissingInflatedId")
    fun onClickAddAddress() {
        val viewDialog = layoutInflater.inflate(R.layout.layout_bottom_sheet_add_address, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(viewDialog)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // init ui
        val edtName = viewDialog.findViewById<TextView>(R.id.edt_name)
        val edtPhone = viewDialog.findViewById<TextView>(R.id.edt_phone)
        edtAddress = viewDialog.findViewById<TextView>(R.id.edt_address)
        val tvCancel = viewDialog.findViewById<TextView>(R.id.tv_cancel)
        val tvAdd = viewDialog.findViewById<TextView>(R.id.tv_add)

        // Set listener
        tvCancel.setOnClickListener { bottomSheetDialog.dismiss() }
        tvAdd.setOnClickListener {
            val strName = edtName.text.toString().trim { it <= ' ' }
            val strPhone = edtPhone.text.toString().trim { it <= ' ' }
            val strAddress = edtAddress!!.text.toString().trim { it <= ' ' }
            if (isEmpty(strName) || isEmpty(strPhone) || isEmpty(strAddress)) {
                showToastMessage(this, getString(R.string.message_enter_infor))
            } else {
                val id = System.currentTimeMillis()
                val address = Address(id, strName, strPhone, strAddress, user!!.email, latitude, longitude)
                MyApplication[this].getAddressDatabaseReference()
                    ?.child(id.toString())
                    ?.setValue(address) { _: DatabaseError?, _: DatabaseReference? ->
                        showToastMessage(
                            this,
                            getString(R.string.msg_add_address_success)
                        )
                        GlobalFunction.hideSoftKeyboard(this)
                        bottomSheetDialog.dismiss()
                    }
            }
        }
        edtAddress!!.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = edtAddress!!.compoundDrawablesRelative[2]
                if (drawableEnd != null && event.rawX >= (edtAddress!!.right - drawableEnd.bounds.width())) {
                    val intent = Intent(this, MapsActivity::class.java)
                    startActivityForResult(intent, Constant.REQUEST_CODE_ADDRESS)
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }
        edtAddress!!.text = address
        bottomSheetDialog.show()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.REQUEST_CODE_ADDRESS && resultCode == Activity.RESULT_OK) {
            data?.let {
                latitude = it.getDoubleExtra("latitude", 0.0)
                longitude = it.getDoubleExtra("longitude", 0.0)
                address = it.getStringExtra("address") ?: ""
                Log.d(
                    "RegisterActivity",
                    "Latitude: $latitude, Longitude: $longitude, Address: $address"
                )
                edtAddress!!.text= address
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        mValueEventListener?.let {
            MyApplication[this].getAddressDatabaseReference()?.removeEventListener(it)
        }
    }
}