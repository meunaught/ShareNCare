package com.example.sharencare.async

import android.os.AsyncTask
import android.util.Log
import com.example.sharencare.constants.AppConfig
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class deleteFriendApiCall: AsyncTask<String, Void, JSONObject>() {
    override fun onPreExecute() {

    }

    override fun doInBackground(vararg param: String?): JSONObject? {
        val uid = param[0]
        val fuid = param[1]
        val url = "https://" + AppConfig.AppDetails.APP_ID + ".api-us.cometchat.io/v3/users/" + uid + "/friends"
        val client = OkHttpClient()
        val mediaType = MediaType.parse("application/json")
        val body = RequestBody.create(mediaType, "{\"friends\":[\"$fuid\"]}")
        val request = Request.Builder()
            .url(url)
            .delete(body)
            .addHeader("apiKey", AppConfig.AppDetails.API_KEY)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build()
        val response = client.newCall(request).execute()
        Log.d("deleteFriend", response.toString())
        return null
    }

    override fun onProgressUpdate(vararg values: Void?) {

    }

    override fun onPostExecute(result: JSONObject?) {

    }

}