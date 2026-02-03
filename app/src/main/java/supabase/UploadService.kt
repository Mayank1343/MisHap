package supabase

import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import java.io.File
import android.util.Log

suspend fun uploadPostMedia(
    file: File,
    userId: String
): String {

    val fileExt = file.extension
    val fileName = "${userId}_${System.currentTimeMillis()}.$fileExt"
    val path = "posts/$fileName"

    Log.d("UPLOAD", "Uploading file: ${file.absolutePath}")
    Log.d("UPLOAD", "Bucket path: $path")
    Log.d("UPLOAD", "File size: ${file.length()}")

    SupabaseClient.client.storage
        .from("media")
        .upload(path, file)

    val publicUrl = SupabaseClient.client.storage
        .from("media")
        .publicUrl(path)

    Log.d("UPLOAD", "Uploaded successfully: $publicUrl")

    return publicUrl
}