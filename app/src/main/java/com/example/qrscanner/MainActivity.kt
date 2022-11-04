package com.example.qrscanner

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*


private const val CAMERA_REQUEST_CODE = 101

class MainActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    private lateinit var tvResults: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPermission()
        codeScanner()

    }

    private fun codeScanner() {
        val codeScannerID = findViewById<CodeScannerView>(R.id.scanner_view)
        tvResults = findViewById<TextView>(R.id.tvResult)

        codeScanner = CodeScanner(this, codeScannerID)
        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS

            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                runOnUiThread {
                    // Write code here
                    var result: String = it.text
                    tvResults.text = result
                    if(result.contains("http://") || result.contains("www.") || result.contains("https://")) {
                        Log.i("Website", result)
                        codeScanner.stopPreview()
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result))
                        startActivity(Intent.createChooser(intent, "Browse with"))
                    }
                }
            }

            errorCallback = ErrorCallback {
                runOnUiThread {
                    Log.e("Main", "Camera initialization error: ${it.message}")
                }
            }
        }
        codeScannerID.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun setupPermission() {
        val permission: Int = ContextCompat.checkSelfPermission(this,
        android.Manifest.permission.CAMERA)
        if(permission != PackageManager.PERMISSION_GRANTED)
            makeRequest()
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),
        CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You need the camera permission", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}