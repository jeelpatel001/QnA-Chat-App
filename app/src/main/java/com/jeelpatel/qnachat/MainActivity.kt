package com.jeelpatel.qnachat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.jeelpatel.qnachat.helper.BannerAdHelper
import com.jeelpatel.qnachat.helper.InterstitialAdHelper
import com.jeelpatel.qnachat.helper.MessageAdapter
import com.jeelpatel.qnachat.helper.MessageModal
import org.json.JSONObject
import pl.droidsonroids.gif.GifImageView

class MainActivity : AppCompatActivity() {

    private lateinit var queryEdt: TextInputEditText
    lateinit var gifImageView: GifImageView
    lateinit var message: RecyclerView
    lateinit var progressBar: ProgressBar
    lateinit var messageAdapter: MessageAdapter
    lateinit var bannerAd: MaterialCardView
    lateinit var iconButton: Button
    lateinit var messageList: ArrayList<MessageModal>
    var url = "https://api.openai.com/v1/completions"
    private var mAuth: FirebaseAuth? = null
    private var gsc: GoogleSignInClient? = null
    private var interstitialAdHelper: InterstitialAdHelper? = null
    private var bannerAdHelper: BannerAdHelper? = null

    private val AD_UNIT_ID_interstitial = "xxxxxxxxxxxx" // Add your interstitial add id
    private val AD_UNIT_ID_banner = "xxxxxxxxxxxx" // Add your banner add id


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeAuthAndGoogleSignIn()
        iconButton = findViewById(R.id.iconButton)
        progressBar = findViewById(R.id.progressBar)
        queryEdt = findViewById(R.id.inputQuestion)
        message = findViewById(R.id.recyclerview)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(messageList)
        val layoutManager = LinearLayoutManager(applicationContext)
        message.layoutManager = layoutManager
        message.adapter = messageAdapter
        gifImageView = findViewById(R.id.gifBackground)
        bannerAd = findViewById(R.id.bannerAd)

        interstitialAdHelper = InterstitialAdHelper(this, AD_UNIT_ID_interstitial)
        interstitialAdHelper!!.loadInterstitialAd()

        bannerAdHelper = BannerAdHelper(this,AD_UNIT_ID_banner,bannerAd,windowManager)
        bannerAdHelper!!.loadBanner()


        val topAppBar: MaterialToolbar = findViewById(R.id.topAppBar)
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.logout -> {
                    interstitialAdHelper!!.showAd()
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Are you sure ???")
                        .setMessage("Log out from QnA Chat App")
                        .setNeutralButton("CANCEL", null)
                        .setPositiveButton("YES") { _, _ ->
                            FirebaseAuth.getInstance().signOut()
                            gsc?.signOut()
                            startActivity(
                                Intent(
                                    applicationContext,
                                    AuthenticationActivity::class.java
                                )
                            )
                            finish()
                        }
                        .show()
                    true
                }

                R.id.deleteAccount -> {
                    interstitialAdHelper!!.showAd()
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Are you sure ???")
                        .setMessage("Delete account from QnA Chat App ")
                        .setNeutralButton("CANCEL", null)
                        .setPositiveButton("YES") { _, _ ->
                            val user = FirebaseAuth.getInstance().currentUser!!
                            user.delete().addOnSuccessListener {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Account Deleted success..",
                                    Toast.LENGTH_SHORT
                                ).show()
                                FirebaseAuth.getInstance().signOut()
                                gsc!!.signOut()
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        AuthenticationActivity::class.java
                                    )
                                )
                                finish()
                            }.addOnFailureListener {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Please Logout and Login again After You can Delete your Account..",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        .show()
                    // Handle favorite icon press
                    true
                }

                R.id.version -> {
                    interstitialAdHelper!!.showAd()
                    true
                }

                else -> false
            }
        }

        queryEdt.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                if (queryEdt.text.toString().isNotEmpty()) {
                    messageList.add(MessageModal(queryEdt.text.toString().trim(), "user"))
                    messageAdapter.notifyDataSetChanged()
                    getResponse(queryEdt.text.toString().trim())
                    message.smoothScrollToPosition(messageAdapter.itemCount - 1)
                    gifImageView.visibility = View.GONE
                } else {
                    Toast.makeText(this, "Question Box Empty", Toast.LENGTH_SHORT).show()
                }
                return@OnEditorActionListener true
            }
            false
        })

        iconButton.setOnClickListener(View.OnClickListener { v->
            if (queryEdt.text.toString().isNotEmpty()) {
                messageList.add(MessageModal(queryEdt.text.toString().trim(), "user"))
                messageAdapter.notifyDataSetChanged()
                getResponse(queryEdt.text.toString().trim())
                message.smoothScrollToPosition(messageAdapter.itemCount - 1)
                gifImageView.visibility = View.GONE
            } else {
                Toast.makeText(this, "Question Box Empty", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun getResponse(query: String) {
        queryEdt.setText("")
        progressBar.visibility = View.VISIBLE

        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        val jsonObject = JSONObject()
        jsonObject.put("model", "text-davinci-003")
        jsonObject.put("prompt", query)
        jsonObject.put("temperature", 0)
        jsonObject.put("max_tokens", 512)
        jsonObject.put("top_p", 1)
        jsonObject.put("frequency_penalty", 0.0)
        jsonObject.put("presence_penalty", 0.0)

        val postRequest: JsonObjectRequest = @SuppressLint("NotifyDataSetChanged")
        object : JsonObjectRequest(
            Method.POST, url, jsonObject,
            Response.Listener { response ->
                val responseMsg: String =
                    response.getJSONArray("choices").getJSONObject(0).getString("text")
                messageList.add(MessageModal(responseMsg.trim(), "bot"))
                messageAdapter.notifyDataSetChanged()

                progressBar.visibility = View.GONE

                message.smoothScrollToPosition(messageAdapter.itemCount - 1)
            },
            Response.ErrorListener {
                Toast.makeText(applicationContext, "RETRY", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/json"
                params["Authorization"] =
                    "xxxxxxxxxxxxxxxxx"  // add your api key from OpenAi
                return params
            }
        }
        postRequest.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            override fun retry(error: VolleyError?) {

            }
        }
        queue.add(postRequest)
    }

    private fun initializeAuthAndGoogleSignIn() {
        mAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail().build()
        gsc = GoogleSignIn.getClient(this, gso)
    }

}