package com.veilsync.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.content.Intent
import android.net.Uri
import android.util.Log
import java.io.File

/**
 * Silent background activity that handles file shares without showing UI
 * Saves shared files directly to the hidden vault and finishes immediately
 */
class ShareReceiverActivity : ComponentActivity() {
    private lateinit var vaultManager: HiddenVaultManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize vault manager
        vaultManager = HiddenVaultManager(this)
        
        // Handle the share intent
        handleIncomingShare(intent)
        
        // Finish immediately without showing any UI
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingShare(intent)
        finish()
    }

    private fun handleIncomingShare(intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_SEND -> {
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                if (uri != null) {
                    // Save single file/image/video to vault
                    saveSharedFileToVault(uri)
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                if (uris != null) {
                    // Save multiple files/images/videos to vault
                    for (fileUri in uris) {
                        saveSharedFileToVault(fileUri)
                    }
                }
            }
        }
    }

    private fun saveSharedFileToVault(uri: Uri) {
        try {
            // Get the original filename from the URI
            var fileName = when {
                uri.scheme == "content" -> {
                    val cursor = contentResolver.query(uri, null, null, null, null)
                    val nameIndex = cursor?.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME) ?: -1
                    cursor?.moveToFirst()
                    val name = if (nameIndex >= 0) cursor?.getString(nameIndex) else null
                    cursor?.close()
                    name ?: "file_${System.currentTimeMillis()}"
                }
                else -> uri.lastPathSegment ?: "file_${System.currentTimeMillis()}"
            }

            // If filename already exists, add timestamp to make it unique
            var destFile = File(vaultManager.getFileUri(fileName))
            if (destFile.exists()) {
                val nameWithoutExt = fileName.substringBeforeLast(".")
                val extension = fileName.substringAfterLast(".", "")
                fileName = if (extension.isNotEmpty()) {
                    "${nameWithoutExt}_${System.currentTimeMillis()}.${extension}"
                } else {
                    "${fileName}_${System.currentTimeMillis()}"
                }
            }

            // Save file to vault
            val saved = vaultManager.saveFileFromUri(this, uri, fileName)
            if (saved) {
                Log.d("ShareReceiver", "Shared file saved in background: $fileName")
            } else {
                Log.e("ShareReceiver", "Failed to save shared file: $fileName")
            }
        } catch (e: Exception) {
            Log.e("ShareReceiver", "Error saving shared file", e)
        }
    }
}

