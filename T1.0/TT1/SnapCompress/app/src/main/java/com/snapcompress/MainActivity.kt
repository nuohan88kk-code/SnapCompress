package com.snapcompress

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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

    private var selectedUri: Uri? = null
    private var lastImageFile: File? = null
    private var lastPdfFile: File? = null

    private val pickSingle = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedUri = uri
            imageView.setImageURI(uri)
            statusView.text = "已选择图片"
            compressButton.isEnabled = true
        }
    }

    private val pickMultiple = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (!uris.isNullOrEmpty()) {
            val list = if (uris.size > 30) uris.subList(0, 30) else uris
            lifecycleScope.launch {
                statusView.text = "正在生成PDF"
                val pdfFile = withContext(Dispatchers.IO) { createPdfFromImages(list) }
                lastPdfFile = pdfFile
                statusView.text = "PDF 已生成: ${pdfFile.absolutePath}"
                startActivity(PdfResultActivity.intent(this@MainActivity, pdfFile.absolutePath))
                sharePdfButton.isEnabled = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finishAffinity() }

        imageView = findViewById(R.id.image)
        statusView = findViewById(R.id.statusText)
        selectButton = findViewById(R.id.selectButton)
        compressButton = findViewById(R.id.compressButton)
        multiToPdfButton = findViewById(R.id.multiToPdfButton)
        shareImageButton = findViewById(R.id.shareImageButton)
        sharePdfButton = findViewById(R.id.sharePdfButton)

        compressButton.isEnabled = false
        shareImageButton.isEnabled = false
        sharePdfButton.isEnabled = false

        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        val adView: com.google.android.gms.ads.AdView = findViewById(R.id.adView)
        adView.loadAd(adRequest)

        selectButton.setOnClickListener { pickSingle.launch("image/*") }

        compressButton.setOnClickListener {
            val uri = selectedUri ?: return@setOnClickListener
            lifecycleScope.launch {
                statusView.text = "正在压缩"
                val file = withContext(Dispatchers.IO) { compressAndSave(uri) }
                lastImageFile = file
                statusView.text = "已保存: ${file.absolutePath}"
                imageView.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
                startActivity(ResultActivity.intent(this@MainActivity, file.absolutePath))
                shareImageButton.isEnabled = true
            }
        }

        multiToPdfButton.setOnClickListener { pickMultiple.launch("image/*") }

        shareImageButton.setOnClickListener {
            val f = lastImageFile ?: return@setOnClickListener
            shareFile(f, "image/jpeg")
        }

        sharePdfButton.setOnClickListener {
            val f = lastPdfFile ?: return@setOnClickListener
            shareFile(f, "application/pdf")
        }
    }

    private fun compressAndSave(uri: Uri): File {
        val bitmap = decodeScaled(uri, 2048)
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: filesDir
        val name = "IMG_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".jpg"
        val outFile = File(dir, name)
        FileOutputStream(outFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            out.flush()
        }
        return outFile
    }

    private fun createPdfFromImages(uris: List<Uri>): File {
        val pdf = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        uris.forEachIndexed { index, uri ->
            val bmp = decodeScaled(uri, 2000)
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            val page = pdf.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            val scale = minOf(pageWidth.toFloat() / bmp.width, pageHeight.toFloat() / bmp.height)
            val drawW = (bmp.width * scale).toInt()
            val drawH = (bmp.height * scale).toInt()
            val left = (pageWidth - drawW) / 2f
            val top = (pageHeight - drawH) / 2f
            val scaled = Bitmap.createScaledBitmap(bmp, drawW, drawH, true)
            canvas.drawBitmap(scaled, left, top, null)
            pdf.finishPage(page)
        }
        val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: filesDir
        val name = "PDF_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".pdf"
        val outFile = File(dir, name)
        FileOutputStream(outFile).use { out ->
            pdf.writeTo(out)
            out.flush()
        }
        pdf.close()
        return outFile
    }

    private fun decodeScaled(uri: Uri, maxSize: Int): Bitmap {
        val opts = BitmapFactory.Options()
        opts.inJustDecodeBounds = true
        contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        }
        var inSampleSize = 1
        val w = opts.outWidth
        val h = opts.outHeight
        if (w > maxSize || h > maxSize) {
            val halfW = w / 2
            val halfH = h / 2
            while ((halfW / inSampleSize) >= maxSize || (halfH / inSampleSize) >= maxSize) {
                inSampleSize *= 2
            }
        }
        val opts2 = BitmapFactory.Options()
        opts2.inSampleSize = inSampleSize
        return contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts2)
        }!!
    }

    private fun shareFile(file: File, mime: String) {
        val uri = FileProvider.getUriForFile(
            this,
            BuildConfig.APPLICATION_ID + ".fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = mime
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "分享"))
    }
}
