package com.example.snapcompress

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var statusView: TextView
    private lateinit var selectButton: Button
    private lateinit var compressButton: Button
    private lateinit var multiToPdfButton: Button
    private lateinit var shareImageButton: Button
    private lateinit var sharePdfButton: Button
    private lateinit var saveImageLocalButton: Button
    private lateinit var savePdfLocalButton: Button
    private lateinit var scanButton: Button
    private lateinit var scanToPdfButton: Button

    private var selectedUri: Uri? = null
    private var lastImageFile: File? = null
    private var lastPdfFile: File? = null

    private var pendingCaptureUri: Uri? = null
    private val scannedUris = mutableListOf<Uri>()

    private val pickSingle = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedUri = uri
            imageView.setImageURI(uri)
            statusView.text = "已选择图片 / Image selected"
            compressButton.isEnabled = true
        }
    }

    private val pickMultiple = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (!uris.isNullOrEmpty()) {
            val list = if (uris.size > 30) uris.subList burgeoning@ (
