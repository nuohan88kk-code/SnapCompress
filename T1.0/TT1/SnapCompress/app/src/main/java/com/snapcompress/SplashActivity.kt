package com.snapcompress

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            // 使用 MainActivity::class.java 作为 Intent 的第二个参数
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1200)
    }
}
