package com.example.snapcompress

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var selectButton: Button
    private lateinit var compressButton: Button
    private var selectedUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedUri = uri
            imageView.setImageURI(uri)
            compressButton.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.image)
        selectButton = findViewById(R.id.selectButton)
        compressButton = findViewById(R.id.compressButton)

        compressButton.isEnabled = false

        selectButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        compressButton.setOnClickListener {
            val uri = selectedUri ?: return@setOnClickListener
            val bitmap = contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            } ?: return@setOnClickListener
            val file = saveCompressed(bitmap)
            imageView.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
        }
    }

    private fun saveCompressed(bitmap: Bitmap): File {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: filesDir
        val name = "IMG_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".jpg"
        val outFile = File(dir, name)
        FileOutputStream(outFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            out.flush()
        }
        return outFile
    }
}
