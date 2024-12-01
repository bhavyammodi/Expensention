package com.bhavya.expensention

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var deletedTransaction: Transaction
    private lateinit var transactions: List<Transaction>
    private lateinit var transactionsAdaptor: TransactionsAdaptor
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val intent = Intent(this, AddTransactionActivity::class.java)
        startActivity(intent)

        transactions = arrayListOf()
        transactionsAdaptor = TransactionsAdaptor(transactions)
        linearLayoutManager = LinearLayoutManager(this)

        db = Room.databaseBuilder(
            this, AppDatabase::class.java, "transaction"
        ).build()

        val recyclerView: RecyclerView = findViewById(R.id.recyclerview)
        recyclerView.apply {
            adapter = transactionsAdaptor
            layoutManager = linearLayoutManager
        }
        recyclerView.post {
            recyclerView.scrollToPosition(transactions.size - 1)
        }

        // swipe to delete
        val itemTouchHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteTransaction(transactions[viewHolder.adapterPosition])
            }

        }

        val swipeHelper = ItemTouchHelper(itemTouchHandler)
        swipeHelper.attachToRecyclerView(recyclerView)

        val addButton = findViewById<FloatingActionButton>(R.id.addButton)
        addButton.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
        val exportButton = findViewById<Button>(R.id.exportButton)
        exportButton.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1
                    )
                } else {
                    exportTransactionsToCSV()
                }
            } else {
                exportTransactionsToCSV()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            exportTransactionsToCSV()
        } else {
            Toast.makeText(
                this, "Permission denied to write to external storage", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun convertMillisToDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val date = Date(millis)
        return sdf.format(date)
    }

    private fun exportTransactionsToCSV() {
        val csvHeader = "Label,Amount,Type,Time\n"
        val csvData = StringBuilder(csvHeader)

        transactions.forEach { transaction ->
            val type = if (transaction.isExpense) "Expense" else "Income"
            csvData.append(
                "\"${transaction.label}\",${
                    (if (transaction.isExpense) -1 else 1) *
                            transaction.amount
                },${type},\"${convertMillisToDateTime(transaction.time)}\"\n"
            )
        }

        val fileName = "transactions-${
            convertMillisToDateTime(System.currentTimeMillis()).replace(
                ":",
                "_"
            )
        }.csv"
        val directoryPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + File.separator + "Expensention"
        val directory = File(directoryPath)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val filePath = directoryPath + File.separator + fileName
        val file = File(filePath)

        try {
            val fileWriter = FileWriter(file)
            fileWriter.write(csvData.toString())
            fileWriter.close()
            Toast.makeText(this, "CSV file saved to $filePath", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(this, "Error saving CSV file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchAll() {
        GlobalScope.launch {
            transactions = db.transactionDao().getAll()

            runOnUiThread {
                updateDashboard()
                transactionsAdaptor.setData(transactions)
            }
        }
    }

    private fun updateDashboard() {
        var expenseAmount = transactions.filter { it.isExpense }.map { it.amount }.sum()
        val incomeAmount = transactions.filter { !it.isExpense }.map { it.amount }.sum()
        val totalAmount = incomeAmount - expenseAmount

        val balance = findViewById<TextView>(R.id.balance)
        val income = findViewById<TextView>(R.id.income_amt)
        val expense = findViewById<TextView>(R.id.expense_amt)

        balance.text = "Rs %.2f".format(totalAmount)
        income.text = "Rs %.2f".format(incomeAmount)
        if (expenseAmount == 0.0) expenseAmount *= -1
        expense.text = "Rs %.2f".format(-expenseAmount)
    }

    private fun undoDelete() {
        GlobalScope.launch {
            db.transactionDao().insertAll(deletedTransaction)
            fetchAll()
        }
    }

    private fun showSnackbar() {
        val view = findViewById<View>(R.id.coordinator)
        val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_LONG)

        // Inflate custom view
        val customView = layoutInflater.inflate(R.layout.snackbar, null)
        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
        snackbarLayout.setPadding(0, 0, 0, 0)

        // Set text and action
        val snackbarText = customView.findViewById<TextView>(R.id.snackbar_text)
        snackbarText.text = "Transaction Deleted!"
        val snackbarAction = customView.findViewById<Button>(R.id.snackbar_action)
        snackbarAction.text = "Undo"
        snackbarAction.setOnClickListener {
            undoDelete()
            snackbar.dismiss()
        }

        // Add custom view to Snackbar layout
        snackbarLayout.addView(customView, 0)

        // Adjust Snackbar position and width
        val addButton = findViewById<FloatingActionButton>(R.id.addButton)
        addButton.post {
            val addButtonLeft = addButton.left
            val snackbarWidth = addButtonLeft - view.left - 50
            val params = snackbar.view.layoutParams as CoordinatorLayout.LayoutParams
            params.width = snackbarWidth
            params.height = addButton.height - 20
            params.gravity = Gravity.BOTTOM or Gravity.START
            snackbar.view.layoutParams = params
            snackbar.show()
        }
    }

    private fun deleteTransaction(transaction: Transaction) {
        deletedTransaction = transaction

        GlobalScope.launch {
            db.transactionDao().delete(transaction)
            fetchAll()
            showSnackbar()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}