package com.example.myapplication

import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

//https://blog.csdn.net/xingyu19911016/article/details/123954358
class HttpExample {

    private val client = OkHttpClient()

    // GET请求（同步）
    fun getMethod(url: String): String {
        val request = Request.Builder().url(url).build() // 新建Request对象
        val response = client.newCall(request).execute() // Response为OkHttp中的响应
        return response.body?.string() ?: ""
    }

    // POST请求（同步）
    val JSON = "application/json; charset=utf-8".toMediaType()
    fun postMethod(url: String, json: String): String {
        val body = RequestBody.create(JSON, json)
        val request = Request.Builder().url(url).post(body).build()
        val response = client.newCall(request).execute()
        return response.body?.string() ?: ""
    }

    // GET请求（异步）
    fun runGet() {
        val request = Request.Builder()
            //.url("http://publicobject.com/helloworld.txt")
            .url("https://www.baidu.com")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    for ((name, value) in response.headers) {
                        println("zcc $name: $value")
                    }

                    println(response.body!!.string())
                }
            }
        })
    }

    // POST请求（异步）
    fun runPost() {
        val formBody = FormBody.Builder()
            .add("search", "Jurassic Park")
            .build()
        val request = Request.Builder()
            .url("https://en.wikipedia.org/w/index.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    for ((name, value) in response.headers) {
                        println("zcc $name: $value")
                    }

                    println(response.body!!.string())
                }
            }
        })
    }

    fun testFunction() {
        runGet()
    }

}