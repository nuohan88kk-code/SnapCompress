package com.snapcompress

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private var interstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        loadInterstitial()
        setContent {
            App()
        }
    }

    private fun loadInterstitial() {
        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            "ca-app-pub-6502794502629936/8525195841",
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {}
                }
            }
        )
    }

    @Composable
    fun App() {
        val context = LocalContext.current
        var bitmap by remember<MutableState<Bitmap?>> { mutableStateOf(null) }
        var quality by remember<MutableState<Float>> { mutableStateOf(80f) }
        var width by remember<MutableState<Float>> { mutableStateOf(1080f) }
        var savedUri by remember<MutableState<Uri?>> { mutableStateOf(null) }

        val pickImage = remember {
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                savedUri = null
                if (uri != null) {
                    context.contentResolver.openInputStream(uri).use { input ->
                        if (input != null) {
                            val bmp = BitmapFactory.decodeStream(input, null, BitmapFactory.Options())
                            bitmap = bmp
                            width = bmp?.width?.toFloat()?.coerceAtMost(2000f) ?: 1080f
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SnapCompress",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(onClick = { pickImage.launch("image/*") }) {
                Text("Select Photo")
            }
            bitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
                Text("Quality: ${quality.roundToInt()}%")
                Slider(
                    value = quality,
                    onValueChange = { quality = it },
                    valueRange = 10f..100f
                )
                Text("Max width: ${width.roundToInt()} px")
                Slider(
                    value = width,
                    onValueChange = { width = it },
                    valueRange = 320f..bmp.width.toFloat()
                )
                Button(onClick = {
                    val outUri = compressAndSave(context, bmp, quality.roundToInt(), width.roundToInt())
                    savedUri = outUri
                    interstitialAd?.show(this@MainActivity)
                }) {
                    Text("Compress & Save")
                }
            }
            savedUri?.let { uri ->
                Text(
                    text = "Saved",
                    textAlign = TextAlign.Center
                )
                Button(onClick = {
                    val share = Intent(Intent.ACTION_SEND)
                    share.type = "image/jpeg"
                    share.putExtra(Intent.EXTRA_STREAM, uri)
                    startActivity(Intent.createChooser(share, "Share"))
                }) {
                    Text("Share")
                }
            }
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                factory = { ctx ->
                    AdView(ctx).apply {
                        adSize = AdSize.BANNER
                        adUnitId = "ca-app-pub-6502794502629936/2594212312"
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }
    }
}

private fun compressAndSave(context: Context, src: Bitmap, quality: Int, maxWidth: Int): Uri? {
    val ratio = maxWidth.toFloat() / src.width.toFloat()
    val newWidth = maxWidth
    val newHeight = (src.height * ratio).roundToInt()
    val scaled = Bitmap.createScaledBitmap(src, newWidth, newHeight, true)
    val bos = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, quality, bos)
    val bytes = bos.toByteArray()
    val name = "IMG_${System.currentTimeMillis()}.jpg"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SnapCompress")
        if (Build.VERSION.SDK_INT >= 29) {
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    if (uri != null) {
        resolver.openOutputStream(uri).use { out ->
            out?.write(bytes)
        }
        if (Build.VERSION.SDK_INT >= 29) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
    }
    return uri
}
