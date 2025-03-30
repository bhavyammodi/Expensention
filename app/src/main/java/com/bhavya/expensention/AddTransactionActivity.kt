package com.bhavya.expensention

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {
    private val amountParam = "am"
    private val noteParam = "tn"

    @RequiresApi(Build.VERSION_CODES.O)
    private val scanQRCodeLauncher = registerForActivityResult(ScanQRCode()) { result ->
        when (result) {
            is QRResult.QRSuccess -> {
                val scannedContent = result.content.rawValue
                if (scannedContent != null) {
                    if (scannedContent.startsWith("upi://")) {
                        val amountInput = findViewById<EditText>(R.id.amountInput)
                        val labelInput = findViewById<EditText>(R.id.labelInput)
                        if (amountInput == null || amountInput.text.isEmpty() || amountInput.text == null || amountInput.text.toString()
                                .isEmpty()
                        ) {
                            amountInput.error = "Please Enter a valid amount"
                        } else if (labelInput == null || labelInput.text.isEmpty() || labelInput.text == null || labelInput.text.toString()
                                .isEmpty() || labelInput.text.toString().isBlank()
                        ) {
                            labelInput.error = "Please Enter a Label"
                        } else {
                            val amount = amountInput.text.toString().toDouble()
                            val label = labelInput.text.toString()
                            launchUPIUrl(scannedContent, amount, label)
                        }
                    } else {
                        Toast.makeText(
                            this, "Not a UPI QR code: $scannedContent", Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            is QRResult.QRUserCanceled -> {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }

            is QRResult.QRMissingPermission -> {
                Toast.makeText(
                    this, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG
                ).show()
            }

            is QRResult.QRError -> {
                Toast.makeText(
                    this, "Error scanning QR code: ${result.exception.message}", Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private lateinit var dateTimeTextView: TextView
    private var selectedDateTime: Calendar = Calendar.getInstance()

    @RequiresApi(Build.VERSION_CODES.O)
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
        val typeRadioGroup: RadioGroup = findViewById(R.id.typeRadioGroup)
        val expenseRadioButton: RadioButton = findViewById(R.id.expenseRadioButton)
        val classificationSpinner: Spinner = findViewById(R.id.classificationSpinner)
        val customCategoryInput: EditText = findViewById(R.id.customCategoryInput)

        // Set "Expense" as the default selection
        expenseRadioButton.isChecked = true

        typeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.expenseRadioButton -> {
                    scanQRButton.visibility = View.VISIBLE
                    addTransaction.text = getString(R.string.add_transaction_expense)
                }

                R.id.incomeRadioButton -> {
                    scanQRButton.visibility = View.GONE
                    addTransaction.text = getString(R.string.add_transaction_income)
                }
            }
        }

        val close = findViewById<ImageButton>(R.id.close)
        close.setOnClickListener {
            finish()
        }

        classificationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                if (parent.getItemAtPosition(position) == "Others") {
                    customCategoryInput.visibility = View.VISIBLE
                } else {
                    customCategoryInput.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        dateTimeTextView = findViewById(R.id.dateTimeTextView)
        updateDateTimeTextView()

        dateTimeTextView.setOnClickListener {
            showDatePickerDialog()
        }

        addTransaction.setOnClickListener {
            addTransaction()
        }
        scanQRButton.setOnClickListener {
            val labelInput = findViewById<EditText>(R.id.labelInput)
            val amountInput = findViewById<EditText>(R.id.amountInput)
            if (amountInput == null || amountInput.text.isEmpty() || amountInput.text == null || amountInput.text.toString()
                    .isEmpty()
            ) {
                amountInput.error = "Please Enter a valid amount"
            } else if (labelInput == null || labelInput.text.isEmpty() || labelInput.text == null || labelInput.text.toString()
                    .isEmpty() || labelInput.text.toString().isBlank()
            ) {
                labelInput.error = "Please Enter a Label"
            } else {
                scanQRCodeLauncher.launch(null)
            }
        }
    }

    private fun updateDateTimeTextView() {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateTimeTextView.text = sdf.format(selectedDateTime.time)
    }

    private fun showDatePickerDialog() {
        val year = selectedDateTime.get(Calendar.YEAR)
        val month = selectedDateTime.get(Calendar.MONTH)
        val day = selectedDateTime.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                selectedDateTime.set(Calendar.YEAR, selectedYear)
                selectedDateTime.set(Calendar.MONTH, selectedMonth)
                selectedDateTime.set(Calendar.DAY_OF_MONTH, selectedDay)
                updateDateTimeTextView()
                showTimePickerDialog()
            }, year, month, day)

        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val hour = selectedDateTime.get(Calendar.HOUR_OF_DAY)
        val minute = selectedDateTime.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            selectedDateTime.set(Calendar.HOUR_OF_DAY, selectedHour)
            selectedDateTime.set(Calendar.MINUTE, selectedMinute)
            updateDateTimeTextView()
        }, hour, minute, true)

        timePickerDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addTransaction(): Boolean {
        val labelInput = findViewById<EditText>(R.id.labelInput)
        val amountInput = findViewById<EditText>(R.id.amountInput)
        val classificationSpinner = findViewById<Spinner>(R.id.classificationSpinner)
        val customCategoryInput = findViewById<EditText>(R.id.customCategoryInput)
        val label = labelInput.text.toString()
        if (amountInput == null || amountInput.text.isEmpty() || amountInput.text == null || amountInput.text.toString()
                .isEmpty()
        ) {
            amountInput.error = "Please Enter a valid amount"
        } else {
            val typeRadioGroup = findViewById<RadioGroup>(R.id.typeRadioGroup)
            val amount = amountInput.text.toString().toDouble()
            val isExpense = typeRadioGroup.checkedRadioButtonId == R.id.expenseRadioButton
            val classification = if (classificationSpinner.selectedItem == "Others") {
                customCategoryInput.text.toString()
            } else {
                classificationSpinner.selectedItem.toString()
            }
            if (label.isEmpty()) {
                labelInput.error = "Please Enter a Label"
            } else {
                val time = selectedDateTime.timeInMillis
                insert(Transaction(0, label, amount, isExpense, time, classification))
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

    @RequiresApi(Build.VERSION_CODES.O)
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
                Toast.makeText(this, "Error extracting amount: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
            addTransaction()
        } else {
            Toast.makeText(this, "No UPI app found to handle the request", Toast.LENGTH_SHORT)
                .show()
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