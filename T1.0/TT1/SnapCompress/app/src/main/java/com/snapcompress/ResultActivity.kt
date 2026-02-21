package com.snapcompress

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        val path = intent.getStringExtra("path") ?: ""
        val preview: ImageView = findViewById(R.id.resultImage)
        val info: TextView = findViewById(R.id.resultInfo)
        val f = File(path)
        if (f.exists()) {
            preview.setImageBitmap(BitmapFactory.decodeFile(path))
            info.text = "已保存: $path\n大小: ${f.length()} bytes"
        } else {
            info.text = "文件不存在"
        }
    }

    companion object {
        fun intent(ctx: android.content.Context, path: String): android.content.Intent {
            val i = android.content.Intent(ctx, ResultActivity::class.java)
            i.putExtra("path", path)
            return i
        }
    }
}
