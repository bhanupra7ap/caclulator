package com.example.calculator

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException

/**
 * Manages the hidden vault folder and file operations
 * Files are stored in the app's private data directory which is not accessible by users or other apps
 */
class HiddenVaultManager(context: Context) {
    private val vaultDirectory: File = File(context.filesDir, ".hidden_vault")

    init {
        // Create the hidden vault directory if it doesn't exist
        if (!vaultDirectory.exists()) {
            vaultDirectory.mkdirs()
        }
    }

    /**
     * Get list of all files in the vault, sorted by last modified (newest first)
     */
    fun getVaultFiles(): List<VaultFile> {
        return vaultDirectory.listFiles()?.map { file ->
            VaultFile(
                name = file.name,
                size = file.length(),
                lastModified = file.lastModified(),
                type = getFileType(file.name)
            )
        }?.sortedByDescending { it.lastModified } ?: emptyList()
    }

    /**
     * Get files filtered by type
     */
    fun getFilesByType(fileType: FileType): List<VaultFile> {
        return getVaultFiles().filter { it.type == fileType }
    }

    /**
     * Determine file type based on extension
     */
    private fun getFileType(fileName: String): FileType {
        val extension = fileName.substringAfterLast(".").lowercase()
        return when (extension) {
            in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp") -> FileType.IMAGE
            in listOf("mp4", "avi", "mkv", "mov", "flv", "wmv", "3gp", "webm") -> FileType.VIDEO
            else -> FileType.FILE
        }
    }

    /**
     * Save a file to the vault
     */
    fun saveFile(fileName: String, content: String): Boolean {
        return try {
            val file = File(vaultDirectory, fileName)
            file.writeText(content)
            true
        } catch (e: IOException) {
            false
        }
    }

    /**
     * Save a binary file (image, video, etc.) from URI to the vault
     */
    fun saveFileFromUri(context: Context, uri: Uri, fileName: String): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val file = File(vaultDirectory, fileName)
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Read a file from the vault
     */
    fun readFile(fileName: String): String? {
        return try {
            val file = File(vaultDirectory, fileName)
            if (file.exists()) file.readText() else null
        } catch (e: IOException) {
            null
        }
    }

    /**
     * Get the file path for a vault file
     */
    fun getFileUri(fileName: String): String {
        return File(vaultDirectory, fileName).absolutePath
    }

    /**
     * Delete a file from the vault
     */
    fun deleteFile(fileName: String): Boolean {
        return try {
            val file = File(vaultDirectory, fileName)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if the vault has any files
     */
    fun isVaultEmpty(): Boolean {
        return vaultDirectory.listFiles()?.isEmpty() ?: true
    }

    /**
     * Clear the entire vault
     */
    fun clearVault(): Boolean {
        return try {
            vaultDirectory.listFiles()?.forEach { file ->
                file.delete()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get vault directory size in bytes
     */
    fun getVaultSize(): Long {
        return vaultDirectory.listFiles()?.sumOf { it.length() } ?: 0L
    }
}

data class VaultFile(
    val name: String,
    val size: Long,
    val lastModified: Long,
    val type: FileType = FileType.FILE
)

enum class FileType {
    IMAGE,
    VIDEO,
    FILE
}
