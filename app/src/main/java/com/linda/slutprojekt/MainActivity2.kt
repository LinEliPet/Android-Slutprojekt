package com.linda.slutprojekt

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException


class MainActivity2 : AppCompatActivity() {

    private lateinit var textViewCurrencyValue: TextView
    private var mQueue: RequestQueue? = null

    private lateinit var textNumber: EditText
    private lateinit var buttonCalc: Button
    private var sekValue: Double = 0.0
    private var currencyValue: Double = 0.0

    val currency = ArrayList<String>()
    private lateinit var spinnerId: Spinner
    private lateinit var arrayAdp: ArrayAdapter<String>
    val rates = ArrayList<Double>()

    private lateinit var textCalcResult: TextView
    private lateinit var editor: SharedPreferences.Editor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val sharedPreferences =
            getSharedPreferences(java.lang.String.valueOf(R.string.preference), MODE_PRIVATE)

        textViewCurrencyValue = findViewById(R.id.chosenCurrency)
        textCalcResult = findViewById(R.id.calcResult)
        spinnerId = findViewById(R.id.spinId)

        mQueue = Volley.newRequestQueue(this)
        editor = sharedPreferences.edit()


        textViewCurrencyValue.text = (sharedPreferences.getString("Value", "Can't find value"))

        //Save last input for:
        //SEK amount
        val typedAmount = findViewById<EditText>(R.id.textNumber)
        typedAmount.setText(sharedPreferences.getString("Amount", ""))
        typedAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                Log.d("Linda typedAmount", "Before editing")
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                Log.d("Linda typedAmount", "During editing")
            }

            override fun afterTextChanged(editable: Editable) {
                var input = editable.toString()
                Log.d("Linda typedAmount", "Finished editing $input")
                editor.putString("Amount", input)
                editor.apply()
            }
        })

        //Code for comparison SEK to chosen currency
        textNumber = findViewById(R.id.textNumber)
        buttonCalc = findViewById(R.id.button_calculate)
        buttonCalc.setOnClickListener {

            if (sekValue != 0.0 && currencyValue != 0.0) {

                val diff = currencyValue / sekValue
                val result: Double = textNumber.text.toString().toDouble() * diff
                Log.d("Linda", result.toString())
                textCalcResult.text = result.toString()
            }

            Log.d("Linda", sekValue.toString())
            Log.d("Linda", currencyValue.toString())
            Log.d("Linda", textNumber.text.toString())
        }

        // Code for spinner:
        arrayAdp = ArrayAdapter(this@MainActivity2, android.R.layout.simple_spinner_dropdown_item, currency)
        spinnerId.adapter = arrayAdp

        // Show a loading indicator (e.g., ProgressBar) until the data is fetched
        val loadingIndicator = findViewById<ProgressBar>(R.id.loadingIndicator)
        loadingIndicator.visibility = View.VISIBLE

        // Call the jsonParse function to fetch the currency data
        val url = "https://api.currencyapi.com/v3/latest?apikey=vI1I5TjnI8Nwbdj5fdA1yiGLYA5jnVr8cSVh6Txp"
        jsonParse(url) { success ->
            // Hide the loading indicator when the data is fetched
            loadingIndicator.visibility = View.GONE

            if (success) {
                // Data fetching and update successful, enable user interaction with the spinner
                spinnerId.isEnabled = true
            } else {
                // Data fetching failed, handle the error condition
                Toast.makeText(this@MainActivity2, "Failed to fetch currency data", Toast.LENGTH_SHORT).show()
            }
        }

        spinnerId.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            @SuppressLint("SetTextI18n")
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Log.d("Linda itemSelected", p2.toString())

                if (p2 == 0) {
                    p0?.setSelection(sharedPreferences.getInt("SpinnerIndex", -1))
                    // If the user selects the default position (0), clear the chosenCurrency TextView
//                    textViewCurrencyValue?.text = ""
                } else {
                    val selectedCurrencyCode = currency[p2]
                    currencyValue = rates[p2 - 1]
                    Log.d("Linda p2", p2.toString())
                    editor.putInt("SpinnerIndex", p2).apply()

                    // Display the selected currency code and value in the chosenCurrency TextView
                    textViewCurrencyValue?.text = "$selectedCurrencyCode: $currencyValue"
                    Log.d("Linda textCurrency", currencyValue.toString())

//                    Toast.makeText(this@MainActivity2, "You've chosen $selectedCurrencyCode", Toast.LENGTH_LONG).show()
                }
            }


            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Clear the chosenCurrency TextView when nothing is selected
//                textViewCurrencyValue?.text = ""
            }
        }
        var spinnerPosition:Int = arrayAdp.getPosition("SEK");
        spinnerId.setSelection(spinnerPosition);
        Log.d("Linda spinner", spinnerPosition.toString())


    }

    override fun onStart() {
        super.onStart()
    }

    override fun onPause() {
        Log.d("Linda pause", "onPause")

        editor.putString("Value", currencyValue.toString())
        editor.apply()

        super.onPause()
    }

    //Currency compared to 1 US-dollar
    @SuppressLint("SetTextI18n")
    private fun jsonParse(url: String, callback: (Boolean) -> Unit) {
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d("Linda", response.toString())
                try {
                    // Retrieve the currency values using the selected currency code
                    val data = response.getJSONObject("data")
                    val currencyCodes = data.keys()

                    //Clear the previous values
                    currency.clear()
                    currency.add("Select a currency") // Add a default selection option
                    rates.clear()
                    Log.d("Linda currency", rates.toString())

                    while (currencyCodes.hasNext()) {
                        val currencyCode = currencyCodes.next()
                        val currencyObject = data.getJSONObject(currencyCode)
                        val sekObject = data.getJSONObject("SEK")

                        currencyValue = currencyObject.getDouble("value")
                        sekValue = sekObject.getDouble("value")


                        // Check if the current currency code matches the selected currency
                        if (currencyCode == spinnerId.selectedItem) {
                            // Display the currency code and value in the chosenCurrency TextView
                            Log.d("Linda currencyCode", spinnerId.selectedItem.toString())
                            textViewCurrencyValue?.text = "$currencyCode::: $currencyValue!"
                            Log.d("Linda currencyCode", "Selected Currency: $currencyCode, Value: $currencyValue")
                        }
                        currency.add(currencyCode)
                        rates.add(currencyValue)
                    }

                    // Update the adapter with new currency codes
                    arrayAdp.notifyDataSetChanged()

                    // Invoke the callback to indicate successful data fetching
                    callback(true)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    // Invoke the callback to indicate data fetching failure
                    callback(false)
                }
            },
            { error ->
                error.printStackTrace()
                Log.e("Linda", "API error: $error")
                // Invoke the callback to indicate data fetching failure
                callback(false)
            }
        )

        mQueue?.add(request)
    }

}