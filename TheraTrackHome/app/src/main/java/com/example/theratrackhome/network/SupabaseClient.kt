package com.example.theratrackhome.network

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClient {

    val client = createSupabaseClient(
        supabaseUrl = "https://ntgstwgrmxmnfpttiuby.supabase.co",
        supabaseKey = "sb_publishable_wHeBBpFs8FVKM64SQI0b1A_6u_fHzt0"
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
    }
}
