package com.zapposandroid.rafaep.androidvillekotlin

import android.util.Log
import retrofit2.Response
import java.io.IOException
import java.lang.Exception

open class SafeApiCall {

    suspend fun <T : Any> safeCall(call: suspend () -> Response<T>?, errorMessage: String, errorHandler: () -> Any): T? {

        val result : Result<T> = safeResult(call, errorMessage)
        var data : T? = null

        when(result) {
            is Result.Success ->
                data = result.data
            is Result.Error -> {
                Log.d("AV-SafeApiCall", "$errorMessage & Exception - ${result.exception}")
                errorHandler()
            }
        }


        return data

    }

    private suspend fun <T: Any> safeResult(call: suspend ()-> Response<T>?, errorMessage: String) : Result<T>{
        var response : Response<T>? = null
        try {
            response = call.invoke() ?: null
        } catch(e : Exception) {
            Log.d("AV-SafeApiCall", "call error.")
            Log.d("AV-SafeApiCall", e.message)
        }
        if(response?.isSuccessful == true) return Result.Success(response.body()!!)

        return Result.Error(IOException("Error Occurred during getting safe Api result, Custom ERROR - $errorMessage"))
    }
}