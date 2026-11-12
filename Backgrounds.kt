package com.example.chargingscreen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.widget.ImageView
import java.io.File

object Backgrounds {

    val presets = listOf(
        intArrayOf(0xFF0F2027.toInt(), 0xFF203A43.toInt(), 0xFF00E5A0.toInt()),
        intArrayOf(0xFF1A0033.toInt(), 0xFF5B1E8C.toInt(), 0xFFFF4DA6.toInt()),
        intArrayOf(0xFF000000.toInt(), 0xFF0A1F44.toInt(), 0xFF2E86FF.toInt()),
        intArrayOf(0xFF200122.toInt(), 0xFF6F0000.toInt(), 0xFFFF8A00.toInt()),
        intArrayOf(0xFF000000.toInt(), 0xFF111111.toInt(), 0xFF444444.toInt())
    )

    private fun prefs(c: Context) = c.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun gradient(colors: IntArray) =
        GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, colors)

    fun selectPreset(c: Context, index: Int) {
        prefs(c).edit().putString("bg", "preset:$index").apply()
    }

    fun saveCustom(c: Context, uri: Uri): Boolean {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            c.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (bounds.outWidth / sample > 2000 || bounds.outHeight / sample > 2000) sample *= 2

            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val bmp = c.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            } ?: return false

            File(c.filesDir, "custom_bg.png").outputStream().use {
                bmp.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            prefs(c).edit().putString("bg", "custom").apply()
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun show(c: Context, view: ImageView) {
        val choice = prefs(c).getString("bg", "preset:0") ?: "preset:0"
        val file = File(c.filesDir, "custom_bg.png")

        if (choice == "custom" && file.exists()) {
            val bmp = BitmapFactory.decodeFile(file.path)
            if (bmp != null) {
                view.scaleType = ImageView.ScaleType.CENTER_CROP
                view.setBackgroundColor(Color.BLACK)
                view.setImageBitmap(bmp)
                return
            }
        }
        val index = choice.removePrefix("preset:").toIntOrNull()
            ?.coerceIn(0, presets.size - 1) ?: 0
        view.setImageDrawable(null)
        view.background = gradient(presets[index])
    }
}
