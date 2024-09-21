package com.bhavya.expensention

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.google.android.material.internal.ViewUtils.showKeyboard
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddTransactionActivity : AppCompatActivity() {
    private val amountParam = "am"
    private val noteParam = "tn"

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_transaction)

        val amountEditText: EditText = findViewById(R.id.amountInput)
        amountEditText.requestFocus()
        showKeyboard(amountEditText)

        val scanQRButton: Button = findViewById(R.id.scanQRButton)
        val addTransaction: Button = findViewById(R.id.addTransaction);
        val typeSpinner: Spinner = findViewById(R.id.typeSpinner)

        typeSpinner.setSelection(0) // Set "Expense" as the default selection

        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
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
            val labelInput = findViewById<EditText>(R.id.labelInput);
            val amountInput = findViewById<EditText>(R.id.amountInput);
            if (amountInput == null || amountInput.text.isEmpty() || amountInput.text == null || amountInput.text.toString()
                    .isEmpty()
            ) {
                amountInput.error = "Please Enter a valid amount"
            } else if (labelInput == null || labelInput.text.isEmpty() || labelInput.text == null || labelInput.text.toString()
                    .isEmpty() || labelInput.text.toString().isBlank()
            ) {
                labelInput.error = "Please Enter a Label"
            } else {
                val integrator = IntentIntegrator(this)
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                integrator.setPrompt("Scan a UPI QR code")
                integrator.setCameraId(0) // Use a specific camera of the device
                integrator.setBeepEnabled(true)
                integrator.setOrientationLocked(true)
                integrator.setBarcodeImageEnabled(true)
                integrator.initiateScan()
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                val scannedContent = result.contents
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
                        launchUPIUrl(scannedContent, amount)
                    }
                } else {
                    Toast.makeText(this, "Not a UPI QR code: $scannedContent", Toast.LENGTH_LONG)
                        .show()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun addTransaction(): Boolean {
        val labelInput = findViewById<EditText>(R.id.labelInput);
        val amountInput = findViewById<EditText>(R.id.amountInput);
        val label = labelInput.text.toString()
        if (amountInput == null || amountInput.text.isEmpty() || amountInput.text == null || amountInput.text.toString()
                .isEmpty()
        ) {
            amountInput.error = "Please Enter a valid amount"
        } else {
            val typeSpinner = findViewById<Spinner>(R.id.typeSpinner);
            val amount = amountInput.text.toString().toDouble();
            val type = typeSpinner.selectedItem.toString();
            if (label.isEmpty()) {
                labelInput.error = "Please Enter a Label"
            } else {
                insert(Transaction(0, label, amount, type == "Expense"))
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

    private fun launchUPIUrl(url: String, amount: Double) {
        var upiUrl = url;
        if (!hasParameter(upiUrl, amountParam)) {
            upiUrl = "$upiUrl&am=$amount"
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
        var db = Room.databaseBuilder(
            this, AppDatabase::class.java, "transaction"
        ).build()

        GlobalScope.launch {
            db.transactionDao().insertAll(transaction)
            finish()
        }
    }
}