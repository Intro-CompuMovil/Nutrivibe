package com.example.proyecto

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.firebase.database.*

class TutorialWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var logoImageView: ImageView
    private lateinit var pageTitle: TextView
    private lateinit var rootLayout: ConstraintLayout
    private val TAG = "TutorialWebViewActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial_web_view)

        webView = findViewById(R.id.tutorialWebView)
        logoImageView = findViewById(R.id.logoImageView)
        pageTitle = findViewById(R.id.pageTitle)
        rootLayout = findViewById(R.id.root_layout)
        configureWebView()

        // Check initial orientation
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            enterFullScreenMode()
        }

        val tutorialUrl = intent.getStringExtra("tutorial_url")
        val tutorialKey = intent.getStringExtra("tutorialKey")

        if (!tutorialUrl.isNullOrEmpty()) {
            Log.d(TAG, "Loading tutorial URL directly: $tutorialUrl")
            loadUrlInWebView(tutorialUrl)
        } else if (!tutorialKey.isNullOrEmpty()) {
            Log.d(TAG, "Loading tutorial using Firebase key: $tutorialKey")
            loadTutorialFromFirebase(tutorialKey)
        } else {
            Log.e(TAG, "No tutorial data provided.")
            finish()
        }
    }

    private fun configureWebView() {
        webView.webViewClient = WebViewClient()
        val webSettings: WebSettings = webView.settings
        webSettings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            mediaPlaybackRequiresUserGesture = false
            builtInZoomControls = false
            displayZoomControls = false
        }
    }

    private fun loadUrlInWebView(url: String) {
        try {
            val embedUrl = if (url.contains("youtube.com")) {
                url.replace("watch?v=", "embed/") + "?playsinline=0&rel=0&showinfo=0&controls=1"
            } else {
                url
            }
            webView.loadUrl(embedUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading URL in WebView: ${e.message}")
            showErrorAndFinish("Error al cargar el tutorial.")
        }
    }

    private fun loadTutorialFromFirebase(tutorialKey: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("tutorials")
        databaseReference.child(tutorialKey).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tutorialUrl = snapshot.getValue(String::class.java)
                if (!tutorialUrl.isNullOrEmpty()) {
                    loadUrlInWebView(tutorialUrl)
                } else {
                    showErrorAndFinish("No se encontró el tutorial.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showErrorAndFinish("Error al cargar el tutorial.")
            }
        })
    }

    private fun showErrorAndFinish(message: String) {
        Log.e(TAG, message)
        finish()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            enterFullScreenMode()
        } else {
            exitFullScreenMode()
        }
    }

    private fun enterFullScreenMode() {
        // Ocultar elementos del header
        logoImageView.visibility = View.GONE
        pageTitle.visibility = View.GONE

        // Actualizar constraints del WebView para ocupar toda la pantalla
        val constraintSet = ConstraintSet()
        constraintSet.clone(rootLayout)

        constraintSet.clear(R.id.tutorialWebView, ConstraintSet.TOP)
        constraintSet.connect(R.id.tutorialWebView, ConstraintSet.TOP,
            ConstraintSet.PARENT_ID, ConstraintSet.TOP)

        constraintSet.applyTo(rootLayout)

        // Configurar la ventana para fullscreen
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        // Ocultar ActionBar
        supportActionBar?.hide()
    }

    private fun exitFullScreenMode() {
        logoImageView.visibility = View.VISIBLE
        pageTitle.visibility = View.VISIBLE

        val constraintSet = ConstraintSet()
        constraintSet.clone(rootLayout)

        constraintSet.clear(R.id.tutorialWebView, ConstraintSet.TOP)
        constraintSet.connect(R.id.tutorialWebView, ConstraintSet.TOP,
            R.id.pageTitle, ConstraintSet.BOTTOM)

        constraintSet.applyTo(rootLayout)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        supportActionBar?.show()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}