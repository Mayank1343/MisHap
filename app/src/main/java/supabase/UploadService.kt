package supabase

import android.graphics.BitmapFactory
import android.util.Log
import io.github.jan.supabase.storage.storage
import java.io.ByteArrayOutputStream
import java.io.File
import android.graphics.Bitmap


// ---------------- POST MEDIA UPLOAD ----------------

suspend fun uploadPostMedia(
    file: File,
    userId: String
): String {

    val bucket = SupabaseClient.client.storage.from("media")

    val fileExt = file.extension.ifEmpty { "jpg" }
    val fileName = "${userId}_${System.currentTimeMillis()}.$fileExt"
    val path = "posts/$fileName"

    Log.d("UPLOAD_POST", "Uploading to media/$path")

    bucket.upload(
        path = path,
        data = safeImageBytes(file),
        upsert = true
    )


    return bucket.publicUrl(path)
}

// ---------------- PROFILE IMAGE UPLOAD ----------------

suspend fun uploadProfileImage(
    file: File,
    userId: String
): String {

    val bucket = SupabaseClient.client.storage.from("media")

    val fileExt = file.extension.ifEmpty { "jpg" }
    val path = "profiles/$userId.$fileExt"

    Log.d("UPLOAD_PROFILE", "Uploading to media/$path")

    bucket.upload(
        path = path,
        data = safeImageBytes(file), // ✅ SAFE
        upsert = true
    )

    return bucket.publicUrl(path)
}

// ---------------- IMAGE COMPRESSION (CRASH FIX) ----------------

private fun safeImageBytes(file: File): ByteArray {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }

    // Read image dimensions ONLY
    BitmapFactory.decodeFile(file.path, options)

    // Calculate downscale factor (max 1024px)
    options.inSampleSize = calculateInSampleSize(options, 1024, 1024)
    options.inJustDecodeBounds = false

    val bitmap = BitmapFactory.decodeFile(file.path, options)
        ?: throw IllegalStateException("Bitmap decode failed")

    val output = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, output)
    bitmap.recycle()

    return output.toByteArray()
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val (height: Int, width: Int) = options.run {
        outHeight to outWidth
    }

    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while (
            halfHeight / inSampleSize >= reqHeight &&
            halfWidth / inSampleSize >= reqWidth
        ) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}


