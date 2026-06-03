package com.example.mathforkids

object AppwriteConfig {
    const val APPWRITE_PROJECT_ID = "69315ba90022c7df934c"
    const val APPWRITE_PROJECT_NAME = "Kayanproject"
    const val APPWRITE_PUBLIC_ENDPOINT = "https://fra.cloud.appwrite.io/v1"
    const val APPWRITE_API_KEY = BuildConfig.APPWRITE_API_KEY

    // أنشئ قاعدة بيانات بهذه المعرفات في Appwrite Console أو غيّر القيم حسب مشروعك.
    const val DATABASE_ID = "mathforkids"
    const val CHILDREN_TABLE_ID = "children"
    const val RESULTS_TABLE_ID = "results"
    const val IMAGES_TABLE_ID = "solution_images"
    const val STORAGE_BUCKET_ID = "kids_solutions"
}
