package com.linda.slutprojekt

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class MainActivity : AppCompatActivity() {
    private var mTextViewResult: TextView? = null
    private var mQueue: RequestQueue? = null
//    private var frameLayout: FrameLayout = findViewById(R.id.frameLayout)
    private var secondFragment: SecondFragment = SecondFragment()

    private lateinit var btnCurSur: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, FirstFragment()).commit()

        mTextViewResult = findViewById(R.id.lastUpdated)
        val buttonParse = findViewById<Button>(R.id.button_parse)
        mQueue = Volley.newRequestQueue(this)
        buttonParse.setOnClickListener { jsonParse() ; changeFragment() } // ; if several (or ,)

        //Button to MainActivity2
        btnCurSur = findViewById(R.id.button_search_currency)
        btnCurSur.setOnClickListener{val intent = Intent(this, MainActivity2::class.java)
                startActivity(intent)}
    }

    //Currency compared to 1 US-dollar
    @SuppressLint("SetTextI18n")
    private fun jsonParse() {
        val url = "https://api.currencyapi.com/v3/latest?apikey=vI1I5TjnI8Nwbdj5fdA1yiGLYA5jnVr8cSVh6Txp"
        Log.d("Linda", "test")

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d("Linda", response.toString())
                val text: String?
                val text2: String?
                val text3: String?
                val text4: String?
                val text5: String?
                val text6: String?
                try {
                    text = response.getJSONObject("meta").getString("last_updated_at")
                    text2 = response.getJSONObject("data").getJSONObject("SEK").getString("value")
                    text3 = response.getJSONObject("data").getJSONObject("EUR").getString("value")
                    text4 = response.getJSONObject("data").getJSONObject("JPY").getString("value")
                    text5 = response.getJSONObject("data").getJSONObject("GBP").getString("value")
                    text6 = response.getJSONObject("data").getJSONObject("AUD").getString("value")
//                    response.getString("last_updated_at")
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                Log.d("Linda", text)
                text2?.let { Log.d("Linda", it) }
                mTextViewResult!!.text = "Last updated at: " + text + "\n\n" +
                                        "SEK: " + text2 + "\n" +
                                        "EUR: " + text3 + "\n" +
                                        "JPY: " + text4 + "\n" +
                                        "GBP: " + text5 + "\n" +
                                        "AUD: " + text6
            }) { error -> error.printStackTrace() }

        mQueue!!.add(request)
    }

    private fun changeFragment() {
        val fragManager: FragmentManager = supportFragmentManager //Display
        val fragTransaction: FragmentTransaction = fragManager.beginTransaction()
        fragTransaction.replace(R.id.frameLayout, secondFragment) //What replacement
        fragTransaction.addToBackStack("Second fragment") //Back button
            fragTransaction.commit() //Go!
    }
}