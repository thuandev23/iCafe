package com.pro.shopfee.activity

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pro.shopfee.R
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PieChartActivity : BaseActivity() {
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private var countStatus4 = 0
    private var countStatus5 = 0
    private var totalStatus4 = 0.0
    private var totalStatus5 = 0.0
    private var totalRevenue = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pie_chart)
        initToolbar()
        pieChart = findViewById(R.id.pie_chart)
        barChart = findViewById(R.id.bar_chart)
        val tvTitleOrder = findViewById<TextView>(R.id.tv_title_order)
        val tvTitleDrink = findViewById<TextView>(R.id.tv_title_drink)
        val iVExportExcel = findViewById<ImageView>(R.id.img_export_excel)
        pieChart.visibility = View.GONE
        barChart.visibility = View.GONE

        tvTitleOrder.setOnClickListener {
            if (pieChart.visibility == View.GONE) {
                pieChart.visibility = View.VISIBLE
                barChart.visibility = View.GONE
            } else {
                pieChart.visibility = View.GONE
            }
        }
        tvTitleDrink.setOnClickListener {
            if (barChart.visibility == View.GONE) {
                barChart.visibility = View.VISIBLE
                pieChart.visibility = View.GONE
            } else {
                barChart.visibility = View.GONE
            }
        }
        getRevenue()
        loadStatisticsOrders()
        loadStatisticsDrinks()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            checkStoragePermission()
        }

        iVExportExcel.setOnClickListener {
            exportOrderDataToExcel()
        }
    }
    private fun initToolbar() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { finish() }
        tvToolbarTitle.text = getString(R.string.statistic)
    }

    private fun loadStatisticsOrders() {
        val ref = FirebaseDatabase.getInstance().getReference("order")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Reset counters and totals before calculation
                resetCounters()
                if (snapshot.exists()) {
                    for (orderSnapshot in snapshot.children) {
                        val status = orderSnapshot.child("status").getValue(Int::class.java) ?: continue
                        val total = orderSnapshot.child("total").getValue(Double::class.java) ?: 0.0

                        when (status) {
                            4 -> {
                                countStatus4++
                                totalStatus4 += total
                            }
                            5 -> {
                                countStatus5++
                                totalStatus5 += total
                            }
                        }
                    }
                    setupPieChart()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("Statistics", error.message)
            }
        })
    }

    private fun loadStatisticsDrinks() {
        val ref = FirebaseDatabase.getInstance().getReference("drink")
        // get 5 drinks with the highest sales
        ref.orderByChild("sale").limitToLast(5).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<String>()
                val listSales = mutableListOf<Int>()
                if (snapshot.exists()) {
                    for (drinkSnapshot in snapshot.children) {
                        val name = drinkSnapshot.child("name").getValue(String::class.java) ?: ""
                        val sales = drinkSnapshot.child("sale").getValue(Int::class.java) ?: 0
                        list.add(name)
                        listSales.add(sales)
                    }
                    setupBarChart(list, listSales)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("Statistics", error.message)
            }
        })
    }

    private fun getRevenue() {
        val ref = FirebaseDatabase.getInstance().getReference("order")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalRevenue = 0.0 // Reset revenue before calculating
                if (snapshot.exists()) {
                    for (orderSnapshot in snapshot.children) {
                        val status = orderSnapshot.child("status").getValue(Int::class.java) ?: continue
                        val total = orderSnapshot.child("total").getValue(Double::class.java) ?: 0.0

                        if (status == 3) {
                            totalRevenue += total
                        }
                    }
                }
                val tvRevenue = findViewById<TextView>(R.id.tv_title_revenue)
                tvRevenue.text = "Doanh thu: ${totalRevenue} VND"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Statistics", error.message)
            }
        })
    }

    private fun resetCounters() {
        countStatus4 = 0
        countStatus5 = 0
        totalStatus4 = 0.0
        totalStatus5 = 0.0
    }

    private fun setupPieChart() {
        val entries = listOf(
            PieEntry(countStatus4.toFloat(), "Đơn hàng hoàn thành"),
            PieEntry(countStatus5.toFloat(), "Đơn hàng đã hủy")
        )

        val dataSet = PieDataSet(entries, "Order Status")
        dataSet.colors =
            ColorTemplate.createColors(
                intArrayOf(Color.rgb(88, 214, 141), Color.rgb(174, 182, 191))
            )
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 20f

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.invalidate()
        pieChart.animateY(2000)

    }

    private fun setupBarChart(list: List<String>, listSales: List<Int>) {
        val entries = mutableListOf<BarEntry>()
        for (i in list.indices) {
            entries.add(BarEntry(i.toFloat(), listSales[i].toFloat()))
        }

        val dataSet = BarDataSet(entries, "Top 5 sản phẩm bán chạy nhất")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < list.size) list[index] else ""
            }
        }
        barChart.xAxis.granularity = 1f
        barChart.xAxis.isGranularityEnabled = true

        barChart.invalidate() // Refresh chart
        barChart.animateY(2000)
        barChart.setNoDataText("Không có dữ liệu")
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            exportOrderDataToExcel()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            exportOrderDataToExcel()
        } else {
            Toast.makeText(this, "Permission denied to write external storage", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportOrderDataToExcel() {
        val ref = FirebaseDatabase.getInstance().getReference("order")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val workbook: Workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Thống kê đơn hàng")

                // Header Row
                val headerRow = sheet.createRow(0)
                headerRow.createCell(0).setCellValue("ID đơn hàng")
                headerRow.createCell(1).setCellValue("Ngày tạo")
                headerRow.createCell(2).setCellValue("Người đặt")
                headerRow.createCell(3).setCellValue("Địa chỉ")
                headerRow.createCell(4).setCellValue("Số điện thoại")
                headerRow.createCell(5).setCellValue("Đồ uống")
                headerRow.createCell(6).setCellValue("Trạng thái đơn hàng")
                headerRow.createCell(7).setCellValue("Phương thức thanh toán")
                headerRow.createCell(8).setCellValue("Giá trị hóa đơn")

                var rowIndex = 1
                for (orderSnapshot in snapshot.children) {
                    val orderId = orderSnapshot.key ?: "Unknown"
                    val orderDate = orderSnapshot.child("dateTime").getValue(String::class.java) ?: "Unknown"
                    val customerName = orderSnapshot.child("userEmail").getValue(String::class.java) ?: "Unknown"
                    val address = orderSnapshot.child("address").child("address").getValue(String::class.java) ?: "Unknown"
                    val phoneNumber = orderSnapshot.child("address").child("phone").getValue(String::class.java) ?: "Unknown"
                    val drinks = orderSnapshot.child("drinks").children.joinToString(", ") {
                        val name = it.child("name").getValue(String::class.java) ?: "Unknown Drink"
                        val quantity = it.child("count").getValue(Int::class.java) ?: 0
                        "$name (số lượng $quantity)"
                    }
                    val status = orderSnapshot.child("status").getValue(Int::class.java) ?: 0
                    val statusName = when (status) {
                        0 -> "Cancel or Accept"
                        1 -> "New"
                        2 -> "Doing"
                        3 -> "Arrived"
                        4 -> "Complete"
                        5 -> "Cancelled"
                        else -> "Unknown"
                    }
                    val paymentMethod = orderSnapshot.child("paymentMethod").getValue(String::class.java) ?: "Unknown"
                    val total = orderSnapshot.child("total").getValue(Double::class.java) ?: 0.0

                    // Create a new row in the sheet for each order
                    val row = sheet.createRow(rowIndex++)
                    row.createCell(0).setCellValue(orderId)
                    row.createCell(1).setCellValue(orderDate)
                    row.createCell(2).setCellValue(customerName)
                    row.createCell(3).setCellValue(address)
                    row.createCell(4).setCellValue(phoneNumber)
                    row.createCell(5).setCellValue(drinks)
                    row.createCell(6).setCellValue(statusName)
                    row.createCell(7).setCellValue(paymentMethod)
                    row.createCell(8).setCellValue(total)
                }

                try {
                    val fileName = "iCafe_ThongKeDonHang.xlsx"
                    val file:File?
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Sử dụng MediaStore để lưu file vào Downloads trên Android 10 trở lên
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        }

                        val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                        if (uri != null) {
                            contentResolver.openOutputStream(uri).use { outputStream ->
                                workbook.write(outputStream)
                                Toast.makeText(this@PieChartActivity, "Data exported to Downloads", Toast.LENGTH_LONG).show()
                            }
                            file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                            openFile(file)
                        }
                    } else {
                        // Android 9 trở xuống: sử dụng phương pháp truyền thống
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val file = File(downloadsDir, fileName)
                        val fileOutputStream = FileOutputStream(file)
                        workbook.write(fileOutputStream)
                        fileOutputStream.close()
                        openFile(file)
                        Toast.makeText(this@PieChartActivity, "Data exported to ${file.absolutePath}", Toast.LENGTH_LONG).show()
                    }

                    workbook.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@PieChartActivity, "Error saving file", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ExportError", error.message)
            }
        })
    }

    private fun openFile(file: File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No app available to open this file", Toast.LENGTH_SHORT).show()
        }
    }
   }

