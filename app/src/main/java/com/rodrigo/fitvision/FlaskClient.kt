package com.rodrigo.fitvision

import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FlaskApi {
    @Multipart
    @POST("/process_frame")
    suspend fun sendImage(@Part image: MultipartBody.Part): Response<ResponseBody>
}

object FlaskClient {
    private const val BASE_URL = "http://192.168.1.85:5000/"

    private val okHttpClient = OkHttpClient.Builder()
        .build()

    val instance: FlaskApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(FlaskApi::class.java)
    }


    fun enviarImagen(bitmap: Bitmap, onSuccess: (ByteArray) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val stream = java.io.ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val byteArray = stream.toByteArray()

                val requestFile = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", "frame.jpg", requestFile)

                val response = instance.sendImage(body)

                if (response.isSuccessful) {
                    val bytes = response.body()?.bytes()
                    if (bytes != null) {
                        onSuccess(bytes)
                    } else {
                        onError("Respuesta vacía del servidor")
                    }
                } else {
                    onError("Error HTTP: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("FlaskClient", "Error al enviar imagen", e)
                onError("Excepción: ${e.localizedMessage}")
            }
        }
    }
}
