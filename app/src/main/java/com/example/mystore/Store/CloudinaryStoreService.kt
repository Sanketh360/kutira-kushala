package com.example.mystore

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object CloudinaryStoreService {
    private const val CLOUD_NAME = "dihvyw4n4"
    private const val UPLOAD_PRESET = "navarasa"
    private val client = OkHttpClient()

    /**
     * Helper function to create a temporary file from a Content URI.
     */
    private suspend fun getFileFromUri(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun uploadStoreLogo(context: Context, imageUri: Uri): String? = withContext(Dispatchers.IO) {
        val file = getFileFromUri(context, imageUri) ?: return@withContext null
        uploadFileToCloudinary(file, "Store/Logos")
    }

    suspend fun uploadProductImages(context: Context, imageUris: List<Uri>): List<String> = withContext(Dispatchers.IO) {
        val uploadedUrls = mutableListOf<String>()
        for (uri in imageUris) {
            val file = getFileFromUri(context, uri)
            if (file != null) {
                val url = uploadFileToCloudinary(file, "Store/Products")
                if (url != null) {
                    uploadedUrls.add(url)
                }
            }
        }
        uploadedUrls
    }

    private suspend fun uploadFileToCloudinary(file: File, folder: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("folder", folder)
                .addFormDataPart(
                    "file", file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        val jsonObject = JSONObject(responseData)
                        return@withContext jsonObject.getString("secure_url")
                    }
                } else {
                    println("Cloudinary upload failed: ${response.code}")
                }
            }
            null
        } catch (e: Exception) {
            println("Cloudinary error: ${e.message}")
            null
        }
    }
}
