package com.reznor.emulation.libretro

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

class RomPickerContract : ActivityResultContract<String?, Uri?>() {

    private var mimeType: String? = null

    override fun createIntent(context: Context, input: String?): Intent {
        mimeType = input
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = input ?: "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/octet-stream", // Generic binary
                "application/x-nes-rom",    // NES
                "application/x-snes-rom",   // SNES
                "application/x-genesis-rom", // Genesis
                "application/x-psx-rom"     // PS1
            ))
        }
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return intent?.data
    }
}