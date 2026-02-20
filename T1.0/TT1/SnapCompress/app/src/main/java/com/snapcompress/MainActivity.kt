package com.example.snapcompress

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.image)
        val selectButton: Button = findViewById(R.id.selectButton)
        val compressButton: Button = findViewById(R.id.compressButton)

        selectButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
            } else {
                openGallery()
            }
        }

        compressButton.setOnClickListener {
            selectedImageUri?.let { uri ->
                compressImage(uri)
            } ?: run {
                Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            imageView.setImageURI(selectedImageUri)
        }
    }

    private fun compressImage(imageUri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val compressedBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                originalBitmap.width / 2,
                originalBitmap.height / 2,
                true
            )

            val file = File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "compressed_${System.currentTimeMillis()}.jpg"
            )
            val outputStream = FileOutputStream(file)
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.close()

            Toast.makeText(this, "图片已压缩并保存到: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "压缩失败", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show()
        }
    }
}
