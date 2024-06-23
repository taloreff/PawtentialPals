package com.example.pawtentialpals.services

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File

object UploadService {
    private const val CLOUD_NAME = "dd7nwvjli"
    private const val UPLOAD_PRESET = "wxfrsvmd"
    private const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

    fun uploadImg(filePath: String): String? {
        val client = OkHttpClient()
        val file = File(filePath)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .addFormDataPart("file", file.name, RequestBody.create("image/*".toMediaTypeOrNull(), file))
            .build()

        val request = Request.Builder()
            .url(UPLOAD_URL)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            return if (response.isSuccessful) {
                val jsonResponse = JSONObject(response.body?.string() ?: "")
                jsonResponse.getString("secure_url")
            } else {
                Log.e("UploadService", "Failed to upload image: ${response.message}")
                null
            }
        }
    }
}
