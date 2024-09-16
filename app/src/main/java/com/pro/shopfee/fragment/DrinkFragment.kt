package com.pro.shopfee.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.activity.DrinkDetailActivity
import com.pro.shopfee.adapter.DrinkAdapter
import com.pro.shopfee.adapter.FilterAdapter
import com.pro.shopfee.adapter.FilterAdapter.IClickFilterListener
import com.pro.shopfee.event.SearchKeywordEvent
import com.pro.shopfee.listener.IClickDrinkListener
import com.pro.shopfee.model.Drink
import com.pro.shopfee.model.Filter
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction.startActivity
import com.pro.shopfee.utils.StringUtil.isEmpty
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern

class DrinkFragment : Fragment() {

    private var mView: View? = null
    private var rcvFilter: RecyclerView? = null
    private var rcvDrink: RecyclerView? = null
    private var listDrink: MutableList<Drink>? = null
    private var listDrinkDisplay: MutableList<Drink>? = null
    private var listDrinkKeyWord: MutableList<Drink>? = null
    private var listFilter: ArrayList<Filter>? = null
    private var drinkAdapter: DrinkAdapter? = null
    private var filterAdapter: FilterAdapter? = null
    private var categoryId: Long = 0
    private var currentFilter: Filter? = null
    private var keyword = ""
    private var mValueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_drink, container, false)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        loadDataArguments()
        initUi()
        initListener()
        getListFilter()
        getListDrink()
        return mView
    }

    private fun loadDataArguments() {
        val bundle = arguments ?: return
        categoryId = bundle.getLong(Constant.CATEGORY_ID)
    }

    private fun initUi() {
        rcvFilter = mView!!.findViewById(R.id.rcv_filter)
        rcvDrink = mView!!.findViewById(R.id.rcv_drink)
        displayListDrink()
    }

    private fun initListener() {}
    private fun getListFilter() {
        listFilter = ArrayList()
        listFilter?.add(Filter(Filter.TYPE_FILTER_ALL, getString(R.string.filter_all)))
        listFilter?.add(Filter(Filter.TYPE_FILTER_RATE, getString(R.string.filter_rate)))
        listFilter?.add(Filter(Filter.TYPE_FILTER_PRICE, getString(R.string.filter_price)))
        listFilter?.add(Filter(Filter.TYPE_FILTER_PROMOTION, getString(R.string.filter_promotion)))
        val linearLayoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL, false
        )
        rcvFilter!!.layoutManager = linearLayoutManager
        currentFilter = listFilter?.get(0)
        currentFilter!!.isSelected = true
        filterAdapter = FilterAdapter(
            activity,
            listFilter,
            object : IClickFilterListener {
                override fun onClickFilterItem(filter: Filter) {
                    handleClickFilter(filter)
                }
            })
        rcvFilter!!.adapter = filterAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleClickFilter(filter: Filter) {
        for (filterEntity in listFilter!!) {
            if (filterEntity.id == filter.id) {
                filterEntity.isSelected = true
                setListDrinkDisplay(filterEntity, keyword)
                currentFilter = filterEntity
            } else {
                filterEntity.isSelected = false
            }
        }
        if (filterAdapter != null) filterAdapter!!.notifyDataSetChanged()
    }

    private fun getListDrink() {
        if (activity == null) return
        mValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (listDrink != null) {
                    listDrink!!.clear()
                } else {
                    listDrink = ArrayList()
                }
                for (dataSnapshot in snapshot.children) {
                    val drink = dataSnapshot.getValue(Drink::class.java)
                    if (drink != null) {
                        listDrink!!.add(0, drink)
                    }
                }
                setListDrinkDisplay(
                    Filter(Filter.TYPE_FILTER_ALL, getString(R.string.filter_all)),
                    keyword
                )
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        MyApplication[activity!!].getDrinkDatabaseReference()
            ?.orderByChild(Constant.CATEGORY_ID)
            ?.equalTo(categoryId.toDouble())
            ?.addValueEventListener(mValueEventListener!!)
    }

    private fun displayListDrink() {
        if (activity == null) return
        listDrinkDisplay = ArrayList()
        val linearLayoutManager = LinearLayoutManager(activity)
        rcvDrink!!.layoutManager = linearLayoutManager
        drinkAdapter = DrinkAdapter(listDrinkDisplay, object : IClickDrinkListener {
            override fun onClickDrinkItem(drink: Drink) {
                val bundle = Bundle()
                bundle.putLong(Constant.DRINK_ID, drink.id)
                startActivity(activity!!, DrinkDetailActivity::class.java, bundle)
            }
        })
        rcvDrink!!.adapter = drinkAdapter
    }

    private fun setListDrinkDisplay(filter: Filter, keyword: String?) {
        if (listDrink == null || listDrink!!.isEmpty()) return
        if (listDrinkKeyWord != null) {
            listDrinkKeyWord!!.clear()
        } else {
            listDrinkKeyWord = ArrayList()
        }
        if (listDrinkDisplay != null) {
            listDrinkDisplay!!.clear()
        } else {
            listDrinkDisplay = ArrayList()
        }
        if (!isEmpty(keyword)) {
            for (drink in listDrink!!) {
                if (getTextSearch(drink.name).toLowerCase(Locale.getDefault()).trim { it <= ' ' }
                        .contains(getTextSearch(keyword).toLowerCase(Locale.getDefault()).trim { it <= ' ' })) {
                    listDrinkKeyWord!!.add(drink)
                }
            }
            when (filter.id) {
                Filter.TYPE_FILTER_ALL -> listDrinkDisplay!!.addAll(
                    listDrinkKeyWord!!
                )
                Filter.TYPE_FILTER_RATE -> {
                    listDrinkDisplay!!.addAll(listDrinkKeyWord!!)
                    listDrinkDisplay!!.sortWith(Comparator { drink1: Drink, drink2: Drink ->
                        drink2.rate.compareTo(drink1.rate)
                    })
                }
                Filter.TYPE_FILTER_PRICE -> {
                    listDrinkDisplay!!.addAll(listDrinkKeyWord!!)
                    listDrinkDisplay!!.sortWith(Comparator { drink1: Drink, drink2: Drink ->
                        drink1.realPrice.compareTo(drink2.realPrice)
                    })
                }
                Filter.TYPE_FILTER_PROMOTION -> for (drink in listDrinkKeyWord!!) {
                    if (drink.sale > 0) listDrinkDisplay!!.add(drink)
                }
            }
        } else {
            when (filter.id) {
                Filter.TYPE_FILTER_ALL -> listDrinkDisplay!!.addAll(
                    listDrink!!
                )
                Filter.TYPE_FILTER_RATE -> {
                    listDrinkDisplay!!.addAll(listDrink!!)
                    listDrinkDisplay!!.sortWith(Comparator { drink1: Drink, drink2: Drink ->
                        drink2.rate.compareTo(drink1.rate)
                    })
                }
                Filter.TYPE_FILTER_PRICE -> {
                    listDrinkDisplay!!.addAll(listDrink!!)
                    listDrinkDisplay!!.sortWith(Comparator { drink1: Drink, drink2: Drink ->
                        drink1.realPrice.compareTo(drink2.realPrice)
                    })
                }
                Filter.TYPE_FILTER_PROMOTION -> for (drink in listDrink!!) {
                    if (drink.sale > 0) listDrinkDisplay!!.add(drink)
                }
            }
        }
        reloadListDrink()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun reloadListDrink() {
        if (drinkAdapter != null) drinkAdapter!!.notifyDataSetChanged()
    }

    private fun getTextSearch(input: String?): String {
        val nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfdNormalizedString).replaceAll("")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSearchKeywordEvent(event: SearchKeywordEvent) {
        keyword = event.keyword
        setListDrinkDisplay(currentFilter!!, keyword)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (filterAdapter != null) filterAdapter!!.release()
        if (activity != null && mValueEventListener != null) {
            MyApplication[activity!!].getDrinkDatabaseReference()
                ?.removeEventListener(mValueEventListener!!)
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    companion object {
        fun newInstance(categoryId: Long): DrinkFragment {
            val drinkFragment = DrinkFragment()
            val bundle = Bundle()
            bundle.putLong(Constant.CATEGORY_ID, categoryId)
            drinkFragment.arguments = bundle
            return drinkFragment
        }
    }
}