package com.example.chargingscreen

import android.Manifest
import android.content.Intent
import android.graphics.Outline
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var preview: ImageView

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                val ok = Backgrounds.saveCustom(this, uri)
                Toast.makeText(
                    this,
                    if (ok) "Sfondo impostato" else "Immagine non valida",
                    Toast.LENGTH_SHORT
                ).show()
                Backgrounds.show(this, preview)
            }
        }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preview = findViewById(R.id.preview)
        preview.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, dp(20).toFloat())
            }
        }
        preview.clipToOutline = true
        Backgrounds.show(this, preview)

        val row = findViewById<LinearLayout>(R.id.presets)
        Backgrounds.presets.forEachIndexed { index, colors ->
            val item = View(this)
            item.layoutParams = LinearLayout.LayoutParams(dp(64), dp(64)).apply {
                setMargins(dp(6), dp(6), dp(6), dp(6))
            }
            item.background = Backgrounds.gradient(colors).apply {
                cornerRadius = dp(16).toFloat()
            }
            item.setOnClickListener {
                Backgrounds.selectPreset(this, index)
                Backgrounds.show(this, preview)
            }
            row.addView(item)
        }

        findViewById<Button>(R.id.btnPick).setOnClickListener {
            pickImage.launch("image/*")
        }

        findViewById<Button>(R.id.btnTest).setOnClickListener {
            startActivity(Intent(this, ChargingActivity::class.java))
        }

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
                return@setOnClickListener
            }
            if (Build.VERSION.SDK_INT >= 33) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
            startForegroundService(Intent(this, ChargingService::class.java))
        }
    }
}
