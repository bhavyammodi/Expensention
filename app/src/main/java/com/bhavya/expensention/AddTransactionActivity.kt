package com.bhavya.expensention

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.google.android.material.internal.ViewUtils.showKeyboard
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Date

class AddTransactionActivity : AppCompatActivity() {
    private val amountParam = "am"
    private val noteParam = "tn"

    private val scanQRCodeLauncher = registerForActivityResult(ScanQRCode()) { result ->
        when (result) {
            is QRResult.QRSuccess -> {
                val scannedContent = result.content.rawValue
                if (scannedContent != null) {
                    if (scannedContent.startsWith("upi://")) {
                        val amountInput = findViewById<EditText>(R.id.amountInput)
                        val labelInput = findViewById<EditText>(R.id.labelInput)
                        if (amountInput == null || amountInput.text.isEmpty() || amountInput.text == null || amountInput.text.toString().isEmpty()) {
                            amountInput.error = "Please Enter a valid amount"
                        } else if (labelInput == null || labelInput.text.isEmpty() || labelInput.text == null || labelInput.text.toString().isEmpty() || labelInput.text.toString().isBlank()) {
                            labelInput.error = "Please Enter a Label"
                        } else {
                            val amount = amountInput.text.toString().toDouble()
                            val label = labelInput.text.toString()
                            launchUPIUrl(scannedContent, amount, label)
                        }
                    } else {
                        Toast.makeText(this, "Not a UPI QR code: $scannedContent", Toast.LENGTH_LONG).show()
                    }
                }
            }
            is QRResult.QRUserCanceled -> {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
            is QRResult.QRMissingPermission -> {
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show()
            }
            is QRResult.QRError -> {
                Toast.makeText(this, "Error scanning QR code: ${result.exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_transaction)

        val amountEditText: EditText = findViewById(R.id.amountInput)
        amountEditText.requestFocus()
        showKeyboard(amountEditText)

        val scanQRButton: Button = findViewById(R.id.scanQRButton)
        val addTransaction: Button = findViewById(R.id.addTransaction)
        val typeSpinner: Spinner = findViewById(R.id.typeSpinner)

        typeSpinner.setSelection(0) // Set "Expense" as the default selection

        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedType = parent.getItemAtPosition(position).toString()
                if (selectedType == "Expense") {
                    scanQRButton.visibility = View.VISIBLE
                    addTransaction.text = getString(R.string.add_transaction_expense)
                } else if (selectedType == "Income") {
                    scanQRButton.visibility = View.GONE
                    addTransaction.text = getString(R.string.add_transaction_income)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        val close = findViewById<ImageButton>(R.id.close)
        close.setOnClickListener {
            finish()
        }

        addTransaction.setOnClickListener {
            addTransaction()
        }
        scanQRButton.setOnClickListener {
            val labelInput = findViewById<EditText>(R.id.labelInput)
            val amountInput = findViewById<EditText>(R.id.amountInput)
            if (amountInput == null || amountInput.text.isEmpty() || amountInput.text == null || amountInput.text.toString().isEmpty()) {
                amountInput.error = "Please Enter a valid amount"
            } else if (labelInput == null || labelInput.text.isEmpty() || labelInput.text == null || labelInput.text.toString().isEmpty() || labelInput.text.toString().isBlank()) {
                labelInput.error = "Please Enter a Label"
            } else {
                scanQRCodeLauncher.launch(null)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addTransaction(): Boolean {
        val labelInput = findViewById<EditText>(R.id.labelInput)
        val amountInput = findViewById<EditText>(R.id.amountInput)
        val label = labelInput.text.toString()
        if (amountInput == null || amountInput.text.isEmpty() || amountInput.text == null || amountInput.text.toString().isEmpty()) {
            amountInput.error = "Please Enter a valid amount"
        } else {
            val typeSpinner = findViewById<Spinner>(R.id.typeSpinner)
            val amount = amountInput.text.toString().toDouble()
            val type = typeSpinner.selectedItem.toString()
            if (label.isEmpty()) {
                labelInput.error = "Please Enter a Label"
            } else {
                val timeNow = System.currentTimeMillis()
                insert(Transaction(0, label, amount, type == "Expense", timeNow))
                return true
            }
        }
        return false
    }

    private fun hasParameter(url: String, param: String): Boolean {
        return url.contains("&$param=")
    }

    private fun extractAmountParameter(url: String): Double {
        val amountIndex = url.indexOf("&am=")
        var amountEndIndex = url.indexOf("&", amountIndex + 1)
        if (amountEndIndex == -1) amountEndIndex = url.length
        return url.substring(amountIndex + 4, amountEndIndex).toDouble()
    }

    private fun launchUPIUrl(url: String, amount: Double, label: String) {
        var upiUrl = url
        if (!hasParameter(upiUrl, amountParam)) {
            upiUrl = "$upiUrl&$amountParam=$amount"
        }
        if (!hasParameter(upiUrl, noteParam)) {
            upiUrl = "$upiUrl&$noteParam=$label"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(upiUrl))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            try {
                val extractedAmount = extractAmountParameter(upiUrl)
                val amountInput = findViewById<EditText>(R.id.amountInput)
                amountInput.setText(extractedAmount.toString())
            } catch (e: Exception) {
                Toast.makeText(this, "Error extracting amount: ${e.message}", Toast.LENGTH_LONG).show()
            }
            addTransaction()
        } else {
            Toast.makeText(this, "No UPI app found to handle the request", Toast.LENGTH_SHORT).show()
        }
    }

    private fun insert(transaction: Transaction) {
        val db = Room.databaseBuilder(this, AppDatabase::class.java, "transaction").build()

        GlobalScope.launch {
            db.transactionDao().insertAll(transaction)
            finish()
        }
    }
}