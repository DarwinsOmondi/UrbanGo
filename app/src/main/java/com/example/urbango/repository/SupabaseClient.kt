package com.example.urbango.repository

import com.example.urbango.constants.Apikeys
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = Apikeys.suparbaseUrl,
        supabaseKey = Apikeys.suparbaseKey
    ) {
        install(Auth)
        //install(GoTrue)
        install(Postgrest)
        install(Storage)
        install(Realtime)
    }
}