package supabase

import android.net.http.HttpResponseCache.install
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://vcybeqkmtasvvuwlfmnn.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZjeWJlcWttdGFzdnZ1d2xmbW5uIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njk2NjY5NDAsImV4cCI6MjA4NTI0Mjk0MH0.vx6amnZzW1SmDSPQLaXqHD0xIfcS9RG78w-trNOpdqA"
    ) {
        install(Storage)
    }
}