package com.example.sharencare.async

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.os.AsyncTask
import android.util.Log
import com.example.sharencare.constants.AppConfig
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class deleteApiCall: AsyncTask<String, Void, JSONObject>() {

    override fun onPreExecute() {

    }

    override fun doInBackground(vararg param: String?): JSONObject? {
        val uid = param[0]
        val url = "https://" + AppConfig.AppDetails.APP_ID + ".api-us.cometchat.io/v3/users/" + uid
        val client = OkHttpClient()
        val mediaType = MediaType.parse("application/json")
        val body = RequestBody.create(mediaType, "{\"permanent\":true}")
        val request = Request.Builder()
            .url(url)
            .delete(body)
            .addHeader("apiKey", AppConfig.AppDetails.API_KEY)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build()

        val response = client.newCall(request).execute()
        Log.d("deleteAPI", response.toString())
        return null
    }

    override fun onProgressUpdate(vararg values: Void?) {

    }

    override fun onPostExecute(result: JSONObject?) {

    }

}