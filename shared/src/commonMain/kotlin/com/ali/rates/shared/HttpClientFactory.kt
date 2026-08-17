package com.ali.rates.shared

import io.ktor.client.*
import io.ktor.client.plugins.*

fun defaultHttpClient(): HttpClient = HttpClient {
    install(UserAgent) {
        agent = "rates-kmp/1.0"
    }
}
