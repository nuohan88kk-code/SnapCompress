package com.example.snapcompress

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class PdfResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_result)
        val path = intent.getStringExtra("path") ?: ""
        val info: TextView = findViewById(R.id.pdfInfo)
        val f = File(path)
        if (f.exists()) {
            info.text = "PDF 已生成: $path\n大小: ${f.length()} bytes"
        } else {
            info.text = "PDF 文件不存在"
        }
    }

    companion object {
        fun intent(ctx: android.content.Context, path: String): android.content.Intent {
            val i = android.content.Intent(ctx, PdfResultActivity::class.java)
            i.putExtra("path", path)
            return i
        }
    }
}
