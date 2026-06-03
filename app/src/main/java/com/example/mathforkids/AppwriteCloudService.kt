package com.example.mathforkids

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList
import java.util.UUID
import java.util.concurrent.Executors

object AppwriteCloudService {

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    @JvmStatic
    fun saveChildProfile(name: String, callback: CloudCallback) {
        val data = JSONObject()
            .put("name", name)
            .put("level", "مبتدئ")
        createDocument(AppwriteConfig.CHILDREN_TABLE_ID, data, callback)
    }

    @JvmStatic
    fun saveResult(question: String, answer: Int, correctAnswer: Int, isCorrect: Boolean, callback: CloudCallback) {
        val data = JSONObject()
            .put("question", question)
            .put("answer", answer)
            .put("correctAnswer", correctAnswer)
            .put("isCorrect", isCorrect)
        createDocument(AppwriteConfig.RESULTS_TABLE_ID, data, callback)
    }

    @JvmStatic
    fun saveImageRecord(question: String, fileUrl: String, callback: CloudCallback) {
        val data = JSONObject()
            .put("question", question)
            .put("fileUrl", fileUrl)
            .put("isCorrect", true)
        createDocument(AppwriteConfig.IMAGES_TABLE_ID, data, callback)
    }

    @JvmStatic
    fun uploadSolutionImage(question: String, bitmap: Bitmap, callback: CloudCallback) {
        executor.execute {
            try {
                val fileId = UUID.randomUUID().toString()
                val boundary = "MathForKidsBoundary${System.currentTimeMillis()}"
                val url = URL("${AppwriteConfig.APPWRITE_PUBLIC_ENDPOINT}/storage/buckets/${AppwriteConfig.STORAGE_BUCKET_ID}/files")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 10000
                    readTimeout = 10000
                    doOutput = true
                    addAppwriteHeaders(this)
                    setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                }

                val output = DataOutputStream(connection.outputStream)
                writeFormField(output, boundary, "fileId", fileId)
                writeFormField(output, boundary, "permissions[]", "read(\"any\")")
                output.writeBytes("--$boundary\r\n")
                output.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"solution.jpg\"\r\n")
                output.writeBytes("Content-Type: image/jpeg\r\n\r\n")
                output.write(bitmapToBytes(bitmap))
                output.writeBytes("\r\n--$boundary--\r\n")
                output.flush()
                output.close()

                val responseCode = connection.responseCode
                val response = readResponse(connection, responseCode)
                connection.disconnect()

                if (responseCode in 200..299) {
                    val fileUrl = "${AppwriteConfig.APPWRITE_PUBLIC_ENDPOINT}/storage/buckets/${AppwriteConfig.STORAGE_BUCKET_ID}/files/$fileId/view?project=${AppwriteConfig.APPWRITE_PROJECT_ID}"
                    saveImageRecord(question, fileUrl, object : CloudCallback {
                        override fun onSuccess(message: String) {
                            postSuccess(callback, fileUrl)
                        }

                        override fun onError(message: String) {
                            postError(callback, message)
                        }
                    })
                } else {
                    postError(callback, "رفع الصورة فشل: $response")
                }
            } catch (e: Exception) {
                postError(callback, e.message ?: "خطأ غير معروف في رفع الصورة")
            }
        }
    }

    @JvmStatic
    fun loadCorrectAnswerImages(callback: ImagesCallback) {
        executor.execute {
            try {
                val url = URL("${AppwriteConfig.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConfig.DATABASE_ID}/collections/${AppwriteConfig.IMAGES_TABLE_ID}/documents")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                    addAppwriteHeaders(this)
                }

                val responseCode = connection.responseCode
                val response = readResponse(connection, responseCode)
                connection.disconnect()

                if (responseCode in 200..299) {
                    val images = ArrayList<CloudImage>()
                    val documents = JSONObject(response).getJSONArray("documents")
                    for (index in documents.length() - 1 downTo 0) {
                        val document = documents.getJSONObject(index)
                        val data = document.optJSONObject("data") ?: document
                        val fileUrl = data.optString("fileUrl", "")
                        val isCorrect = data.optBoolean("isCorrect", true)
                        if (fileUrl.isNotBlank() && isCorrect) {
                            images.add(
                                CloudImage(
                                    document.optString("\$id", ""),
                                    data.optString("question", "إجابة صحيحة"),
                                    fileUrl
                                )
                            )
                        }
                    }
                    mainHandler.post { callback.onSuccess(images) }
                } else {
                    mainHandler.post { callback.onError(response) }
                }
            } catch (e: Exception) {
                mainHandler.post { callback.onError(e.message ?: "خطأ في تحميل اللقطات") }
            }
        }
    }

    @JvmStatic
    fun loadResults(callback: ResultsCallback) {
        executor.execute {
            try {
                val url = URL("${AppwriteConfig.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConfig.DATABASE_ID}/collections/${AppwriteConfig.RESULTS_TABLE_ID}/documents")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                    addAppwriteHeaders(this)
                }

                val responseCode = connection.responseCode
                val response = readResponse(connection, responseCode)
                connection.disconnect()

                if (responseCode in 200..299) {
                    val results = ArrayList<ProgressItem>()
                    val documents = JSONObject(response).getJSONArray("documents")
                    for (index in documents.length() - 1 downTo 0) {
                        val document = documents.getJSONObject(index)
                        val data = document.optJSONObject("data") ?: document
                        results.add(
                            ProgressItem(
                                document.optString("\$id", ""),
                                data.optString("question", "سؤال حساب"),
                                data.optInt("answer", 0),
                                data.optInt("correctAnswer", 0),
                                data.optBoolean("isCorrect", false)
                            )
                        )
                    }
                    mainHandler.post { callback.onSuccess(results) }
                } else {
                    mainHandler.post { callback.onError(response) }
                }
            } catch (e: Exception) {
                mainHandler.post { callback.onError(e.message ?: "خطأ في تحميل سجل التقدم") }
            }
        }
    }

    @JvmStatic
    fun deleteResult(documentId: String, callback: CloudCallback) {
        executor.execute {
            try {
                if (documentId.isBlank()) {
                    postError(callback, "لا يوجد معرف لهذا السجل")
                    return@execute
                }
                if (deleteDocument(AppwriteConfig.RESULTS_TABLE_ID, documentId)) {
                    postSuccess(callback, "تم حذف السجل")
                } else {
                    postError(callback, "فشل حذف السجل من Appwrite")
                }
            } catch (e: Exception) {
                postError(callback, e.message ?: "فشل حذف السجل")
            }
        }
    }

    @JvmStatic
    fun deleteImage(documentId: String, fileUrl: String, callback: CloudCallback) {
        executor.execute {
            try {
                if (documentId.isBlank()) {
                    postError(callback, "لا يوجد معرف لهذه الصورة")
                    return@execute
                }

                val fileId = extractFileId(fileUrl)
                if (fileId.isNotBlank() && !deleteStorageFile(fileId)) {
                    postError(callback, "فشل حذف ملف الصورة من Storage")
                    return@execute
                }

                if (deleteDocument(AppwriteConfig.IMAGES_TABLE_ID, documentId)) {
                    postSuccess(callback, "تم حذف الصورة")
                } else {
                    postError(callback, "فشل حذف سجل الصورة من Appwrite")
                }
            } catch (e: Exception) {
                postError(callback, e.message ?: "فشل حذف الصورة")
            }
        }
    }

    private fun createDocument(tableId: String, data: JSONObject, callback: CloudCallback) {
        executor.execute {
            try {
                val body = JSONObject()
                    .put("documentId", "unique()")
                    .put("permissions", JSONArray().put("read(\"any\")"))
                    .put("data", data)
                val url = URL("${AppwriteConfig.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConfig.DATABASE_ID}/collections/$tableId/documents")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 8000
                    readTimeout = 8000
                    doOutput = true
                    addAppwriteHeaders(this)
                    setRequestProperty("Content-Type", "application/json")
                }
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                val responseCode = connection.responseCode
                val response = readResponse(connection, responseCode)
                connection.disconnect()

                if (responseCode in 200..299) {
                    postSuccess(callback, response)
                } else {
                    postError(callback, response)
                }
            } catch (e: Exception) {
                postError(callback, e.message ?: "خطأ غير معروف")
            }
        }
    }

    private fun deleteDocument(tableId: String, documentId: String): Boolean {
        val url = URL("${AppwriteConfig.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConfig.DATABASE_ID}/collections/$tableId/documents/$documentId")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "DELETE"
            connectTimeout = 8000
            readTimeout = 8000
            addAppwriteHeaders(this)
        }
        val responseCode = connection.responseCode
        connection.disconnect()
        return responseCode in 200..299
    }

    private fun deleteStorageFile(fileId: String): Boolean {
        val url = URL("${AppwriteConfig.APPWRITE_PUBLIC_ENDPOINT}/storage/buckets/${AppwriteConfig.STORAGE_BUCKET_ID}/files/$fileId")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "DELETE"
            connectTimeout = 8000
            readTimeout = 8000
            addAppwriteHeaders(this)
        }
        val responseCode = connection.responseCode
        connection.disconnect()
        return responseCode in 200..299 || responseCode == 404
    }

    private fun extractFileId(fileUrl: String): String {
        val marker = "/files/"
        val start = fileUrl.indexOf(marker)
        if (start == -1) return ""
        val fileIdStart = start + marker.length
        val fileIdEnd = fileUrl.indexOf("/", fileIdStart)
        return if (fileIdEnd == -1) fileUrl.substring(fileIdStart) else fileUrl.substring(fileIdStart, fileIdEnd)
    }

    private fun writeFormField(output: DataOutputStream, boundary: String, name: String, value: String) {
        output.writeBytes("--$boundary\r\n")
        output.writeBytes("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
        output.writeBytes("$value\r\n")
    }

    private fun addAppwriteHeaders(connection: HttpURLConnection) {
        connection.setRequestProperty("X-Appwrite-Project", AppwriteConfig.APPWRITE_PROJECT_ID)
        connection.setRequestProperty("X-Appwrite-Key", AppwriteConfig.APPWRITE_API_KEY)
    }

    private fun bitmapToBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        return stream.toByteArray()
    }

    private fun readResponse(connection: HttpURLConnection, responseCode: Int): String {
        val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
        return stream?.bufferedReader()?.use { it.readText() } ?: ""
    }

    private fun postSuccess(callback: CloudCallback, message: String) {
        mainHandler.post { callback.onSuccess(message) }
    }

    private fun postError(callback: CloudCallback, message: String) {
        mainHandler.post { callback.onError(message) }
    }

    interface CloudCallback {
        fun onSuccess(message: String)
        fun onError(message: String)
    }

    interface ImagesCallback {
        fun onSuccess(images: ArrayList<CloudImage>)
        fun onError(message: String)
    }

    interface ResultsCallback {
        fun onSuccess(results: ArrayList<ProgressItem>)
        fun onError(message: String)
    }
}
