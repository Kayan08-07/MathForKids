package com.example.mathforkids

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.appwrite.Client
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

object AppwriteClient {

    private lateinit var client: Client
    private val mainHandler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    @JvmStatic
    fun init(context: Context) {
        client = Client(context.applicationContext)
            .setProject(AppwriteConfig.APPWRITE_PROJECT_ID)
            .setEndpoint(AppwriteConfig.APPWRITE_PUBLIC_ENDPOINT)
    }

    @JvmStatic
    fun getClient(): Client = client

    @JvmStatic
    fun getAccount(): Account = Account(client)

    @JvmStatic
    fun getDatabases(): Databases = Databases(client)

    @JvmStatic
    fun getStorage(): Storage = Storage(client)

    @JvmStatic
    fun testConnection(callback: ConnectionCallback) {
        executor.execute {
            try {
                val response = runBlocking { client.ping() }
                mainHandler.post {
                    if (response.contains("Pong", ignoreCase = true)) {
                        callback.onSuccess()
                    } else {
                        callback.onError("Unexpected response: $response")
                    }
                }
            } catch (e: AppwriteException) {
                mainHandler.post {
                    callback.onError("${e.code}: ${e.message}")
                }
            } catch (e: Exception) {
                mainHandler.post {
                    callback.onError(e.message ?: e.javaClass.simpleName)
                }
            }
        }
    }

    interface ConnectionCallback {
        fun onSuccess()
        fun onError(message: String)
    }
}
