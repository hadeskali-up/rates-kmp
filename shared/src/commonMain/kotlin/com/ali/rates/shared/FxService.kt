package com.ali.rates.shared

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

/**
 * Free, key-less FX APIs:
 *  - open.er-api.com        : 170+ currencies, latest rates
 *  - api.frankfurter.dev/v1 : ECB rates, historical timeseries (~30 currencies)
 *  - jsDelivr CDN (fawazahmed0/currency-api) : currency display names
 */
class FxService(private val client: HttpClient = defaultHttpClient()) {

    companion object {
        internal val JSON = Json { ignoreUnknownKeys = true }
        internal const val ER_API = "https://open.er-api.com/v6"
        internal const val FRANKFURTER = "https://api.frankfurter.dev/v1"
        internal const val NAMES_CDN =
            "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies.json"
    }

    /** Latest rates: {code -> rate} for one unit of [base]. */
    suspend fun latestRates(base: String): Map<String, Double> =
        withContext(Dispatchers.Default) {
            val body = client.get("$ER_API/latest/$base").bodyAsText()
            val rates = JSON.parseToJsonElement(body).jsonObject["rates"]!!.jsonObject
            rates.mapValues { it.value.jsonPrimitive.content.toDouble() }
        }

    /** Currency display names (uppercase code -> name); built-in fallback on failure. */
    suspend fun symbols(): Map<String, String> = withContext(Dispatchers.Default) {
        try {
            val body = client.get(NAMES_CDN).bodyAsText()
            val raw = JSON.parseToJsonElement(body).jsonObject
            val out = raw.entries
                .filter { it.key.length == 3 }
                .associate { it.key.uppercase() to it.value.jsonPrimitive.content }
            if (out.isNotEmpty()) out else FALLBACK_SYMBOLS
        } catch (_: Exception) {
            FALLBACK_SYMBOLS
        }
    }

    /** Today's date in Kuala Lumpur (MYT) — rates context for the user. */
    internal fun todayMyt(): LocalDate = Clock.System.todayIn(TimeZone.of("Asia/Kuala_Lumpur"))

    /** Daily timeseries over [days] back from today for one pair (ECB coverage only). */
    suspend fun timeseries(base: String, quote: String, days: Int): Map<String, Double> =
        withContext(Dispatchers.Default) {
            val end = todayMyt()
            val start = end.minus(DatePeriod(days = days))
            val url = "$FRANKFURTER/$start..$end?base=$base&symbols=$quote"
            val body = client.get(url).bodyAsText()
            val rates = JSON.parseToJsonElement(body).jsonObject["rates"]?.jsonObject
                ?: return@withContext emptyMap()
            rates.entries.associate { (d, v) ->
                d to v.jsonObject[quote]!!.jsonPrimitive.content.toDouble()
            }
        }

    internal val FALLBACK_SYMBOLS = mapOf(
        "USD" to "US Dollar", "EUR" to "Euro", "GBP" to "British Pound",
        "JPY" to "Japanese Yen", "AUD" to "Australian Dollar", "CAD" to "Canadian Dollar",
        "CHF" to "Swiss Franc", "CNY" to "Chinese Yuan", "SGD" to "Singapore Dollar",
        "HKD" to "Hong Kong Dollar", "NZD" to "New Zealand Dollar", "INR" to "Indian Rupee",
        "IDR" to "Indonesian Rupiah", "THB" to "Thai Baht", "PHP" to "Philippine Peso",
        "VND" to "Vietnamese Dong", "KRW" to "South Korean Won", "TWD" to "New Taiwan Dollar",
        "AED" to "UAE Dirham", "SAR" to "Saudi Riyal", "QAR" to "Qatari Riyal",
        "BND" to "Brunei Dollar", "MYR" to "Malaysian Ringgit", "TRY" to "Turkish Lira",
        "RUB" to "Russian Ruble", "ZAR" to "South African Rand", "BRL" to "Brazilian Real",
        "MXN" to "Mexican Peso", "SEK" to "Swedish Krona", "NOK" to "Norwegian Krone",
        "DKK" to "Danish Krone", "PLN" to "Polish Zloty", "CZK" to "Czech Koruna",
        "HUF" to "Hungarian Forint", "ILS" to "Israeli Shekel", "PKR" to "Pakistani Rupee",
        "BDT" to "Bangladeshi Taka", "LKR" to "Sri Lankan Rupee", "NPR" to "Nepalese Rupee",
        "KES" to "Kenyan Shilling", "NGN" to "Nigerian Naira", "GHS" to "Ghanaian Cedi",
        "EGP" to "Egyptian Pound", "MAD" to "Moroccan Dirham", "TND" to "Tunisian Dinar",
    )
}
