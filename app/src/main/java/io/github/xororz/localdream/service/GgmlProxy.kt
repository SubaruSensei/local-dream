package io.github.xororz.localdream.service

import android.util.Log
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.json.JSONObject
import android.util.Base64

class GgmlProxy {
    private var server: HttpServer? = null
    private val client = HttpClient.newHttpClient()

    fun start() {
        if (server != null) return
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 8081), 0)
        server?.createContext("/") { exchange ->
            Thread {
                try {
                    val requestBody = exchange.requestBody.readBytes().toString(Charsets.UTF_8)
                    val requestMethod = exchange.requestMethod
                    val requestPath = exchange.requestURI.path
                    Log.i("GgmlProxy", "Received: $requestMethod $requestPath")
                    
                    val newPath = if (requestPath == "/generate") "/sdapi/v1/txt2img" else requestPath
                    val targetUri = URI("http://127.0.0.1:8082$newPath")
                    
                    val builder = HttpRequest.newBuilder().uri(targetUri).header("Content-Type", "application/json")
                    val request = if (requestMethod == "POST") {
                        builder.POST(HttpRequest.BodyPublishers.ofString(requestBody)).build()
                    } else {
                        builder.GET().build()
                    }
                        
                    val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
                    val responseBytes = response.body()
                    
                    if (requestPath == "/generate" && response.statusCode() == 200) {
                        val json = JSONObject(String(responseBytes, Charsets.UTF_8))
                        val base64 = json.getJSONArray("images").getString(0)
                        val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                        exchange.sendResponseHeaders(200, imageBytes.size.toLong())
                        exchange.responseBody.write(imageBytes)
                    } else {
                        exchange.sendResponseHeaders(response.statusCode(), responseBytes.size.toLong())
                        exchange.responseBody.write(responseBytes)
                    }
                    exchange.close()
                } catch (e: Exception) {
                    Log.e("GgmlProxy", "Error", e)
                    exchange.sendResponseHeaders(500, 0)
                    exchange.close()
                }
            }.start()
        }
        server?.start()
        Log.i("GgmlProxy", "Proxy started on 8081")
    }

    fun stop() {
        server?.stop(0)
        server = null
    }
}
