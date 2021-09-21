package com.example.medlcx_android_code_test

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import com.example.medlcx_android_code_test.model.URLImageInfo
import com.example.medlcx_android_code_test.utils.Constant
import com.example.medlcx_android_code_test.utils.MethodUtils
import com.orhanobut.hawk.Hawk
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var bitmap: Bitmap
    private lateinit var webView: WebView
    private lateinit var tempImageView: ImageView
    private lateinit var urlText: EditText
    private lateinit var goBtn: Button
    private lateinit var captureBtn: Button
    private lateinit var historyBtn: Button
    private lateinit var urlImageInfoList: ArrayList<URLImageInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Hawk.init(this).build()
        val any: Any? = Hawk.get(Constant.HAWK_TOKEN_KEY)
        if(any == null) {
            urlImageInfoList = ArrayList()
            Hawk.put(Constant.HAWK_TOKEN_KEY, urlImageInfoList)
        } else {
            urlImageInfoList = Hawk.get(Constant.HAWK_TOKEN_KEY)
        }
        webView = findViewById(R.id.id_web_view)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                Log.e("RashidShafee", "onPageFinished")
                tempImageView.visibility = View.GONE
                webView.visibility = View.VISIBLE
            }
        }
        webView.settings.javaScriptEnabled = true


        urlText = findViewById(R.id.id_url_text)
        tempImageView = findViewById(R.id.id_temp_image_view)
        goBtn = findViewById(R.id.id_btn_go)
        captureBtn = findViewById(R.id.id_btn_capture)
        historyBtn = findViewById(R.id.id_btn_history)

        goBtn.setOnClickListener {
           loadWebView()
        }

        captureBtn.setOnClickListener {
            captureWebView()
        }

        historyBtn.setOnClickListener {
            val intent = Intent(this, URLHistoryActivity::class.java)
            resultLauncher.launch(intent)
        }
    }

    private fun loadWebView() {
        if(urlText.text.isEmpty()) {
            MethodUtils.showDialog(this, "Please enter the url to load")
        } else {
            webView.loadUrl(urlText.text.toString())
        }
    }

    private fun captureWebView() {
        if(urlText.text.isEmpty()) {
            MethodUtils.showDialog(this, "Please enter the url to load")
        } else {
            bitmap =  webView.drawToBitmap()
            setupPermissions()
        }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.e("RashidShafee","registerForActivityResult")
            val data: Intent? = result.data
            val resultURLText = data!!.getCharSequenceExtra(Constant.RETURN_URL)
            urlText.setText(resultURLText)
            val resultImageName = data.getCharSequenceExtra(Constant.RETURN_IMAGE_NAME)
            val imageUri = MethodUtils.getImageFromMediaStore(this, resultImageName.toString())
            tempImageView.setImageURI(imageUri)
            tempImageView.visibility = View.VISIBLE
            webView.visibility = View.GONE
            loadWebView()
        }
    }

    private fun saveBitmapAsImageToDevice(bitmap: Bitmap) {
        val resolver = this.contentResolver

        val imageStorageAddress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val date = Date()

        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "meldCX_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis())
        }

        try {
            val contentUri: Uri? = resolver.insert(imageStorageAddress, imageDetails)
            contentUri?.let { uri ->
                val outputStream: OutputStream? = resolver.openOutputStream(uri)
                outputStream?.let { outStream ->
                    val isBitmapCompressed =
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outStream)
                    if (isBitmapCompressed) {
                        outStream.flush()
                        outStream.close()
                    }
                    val urlImageInfo = URLImageInfo(urlText.text.toString(), date,
                        imageDetails[MediaStore.Images.Media.DISPLAY_NAME].toString()
                    )
                    urlImageInfoList = Hawk.get(Constant.HAWK_TOKEN_KEY)
                    urlImageInfoList.add(urlImageInfo)
                    Hawk.put(Constant.HAWK_TOKEN_KEY, urlImageInfoList)
                    MethodUtils.showDialog(this, "Capture done and saved")
                } ?: throw IOException("Failed to get output stream.")
            } ?: throw IOException("Failed to create new MediaStore record.")
        } catch (e: IOException) {
            throw e
        }
    }

    private fun setupPermissions() {
        val readPermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)

        val writePermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if ((readPermission != PackageManager.PERMISSION_GRANTED)
            && (writePermission != PackageManager.PERMISSION_GRANTED)) {
            makeRequest()
        } else{
            saveBitmapAsImageToDevice(bitmap)
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            101)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101 -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i("RequestInfo", "Permission has been denied by user")
                } else {
                    saveBitmapAsImageToDevice(bitmap)
                    Log.i("RequestInfo", "Permission has been granted by user")
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && this.webView.canGoBack()) {
            this.webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}