package com.koner.typst.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class GithubReleasesApi(private val owner: String, private val repo: String) {

    private val client = OkHttpClient()

    fun fetchLatestVersion(): String? {
        return runCatching {
            val request = Request.Builder()
                .url("https://api.github.com/repos/$owner/$repo/releases/latest")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null

                val body = response.body.string()

                val json = JSONObject(body)
                json.getString("tag_name").removePrefix("v")
            }
        }.getOrNull()
    }
}