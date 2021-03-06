package com.example.medlcx_android_code_test

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
        Hawk.init(this).build() //Hawk Key-value data storage library
        val any: Any? = Hawk.get(Constant.HAWK_TOKEN_KEY)
        if(any == null) {
            urlImageInfoList = ArrayList()
            Hawk.put(Constant.HAWK_TOKEN_KEY, urlImageInfoList)
        } else {
            urlImageInfoList = Hawk.get(Constant.HAWK_TOKEN_KEY)
        }
        webView = findViewById(R.id.id_web_view)
        webView.webViewClient = object : WebViewClient() {
            /**
             * onPageFinished() is called when url load is finished in WebView
             * Temporary image view visibility gone when loading finishes after coming back from URLHistoryActivity
             * WebView visibility Visible
             */
            override fun onPageFinished(view: WebView, url: String) {
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

    /**
     * Launching the URLHistoryActivity for returning result
     * After return setting the return image to Temporary ImageView
     */
    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val resultURLText = data!!.getCharSequenceExtra(Constant.RETURN_URL)
            urlText.setText(resultURLText)
            val resultImageName = data.getCharSequenceExtra(Constant.RETURN_IMAGE_NAME)
            val imageUri = MethodUtils.getImageFromMediaStore(this, resultImageName.toString())
            tempImageView.setImageURI(imageUri) //setting returned image Uri to temporary ImageView
            tempImageView.visibility = View.VISIBLE //Temporary ImageView visibility is VISIBLE
            webView.visibility = View.GONE //WebView visibility is GONE
            loadWebView() //Loading the WebView with returned URL
        }
    }

    /**
     * Method to load the entered URL in the WebView
     */
    private fun loadWebView() {
        if(urlText.text.isEmpty()) {
            MethodUtils.showInfoDialog(this, "Please enter the URL to load")
        } else {
            webView.loadUrl(urlText.text.toString())
        }
    }

    /**
     * Method to Capture the WebView
     */
    private fun captureWebView() {
        if(urlText.text.isEmpty()) {
            MethodUtils.showInfoDialog(this, "Please enter the URL to load")
        } else {
            bitmap =  webView.drawToBitmap() //capture the WebView content to a bitmap
            checkStoragePermission()
        }
    }

    /**
     * Method to save the bitmap of the WebView in Storage
     */
    private fun saveBitmapAsImageToDevice(bitmap: Bitmap) {
        val resolver = this.contentResolver

        val imageStorageAddress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val date = Date()

        //Setting Image Details
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
                    urlImageInfoList.add(urlImageInfo) //adding new image to existing list
                    Hawk.put(Constant.HAWK_TOKEN_KEY, urlImageInfoList) //updating the existing list
                    MethodUtils.showInfoDialog(this, "Capture done and saved")
                } ?: throw IOException("Failed to get output stream.")
            } ?: throw IOException("Failed to create new MediaStore record.")
        } catch (e: IOException) {
            throw e
        }
    }

    /**
     * Method to check storage permissions
     * Make request if not granted
     */
    private fun checkStoragePermission() {
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

    /**
     * Method to make storage request from user to grant
     */
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
                    //If request is granted then saving the bitmap to storage
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