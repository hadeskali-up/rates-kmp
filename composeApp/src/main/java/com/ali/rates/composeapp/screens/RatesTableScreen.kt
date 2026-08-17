package com.ali.rates.composeapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ali.rates.shared.FxService
import kotlinx.coroutines.launch

@Composable
fun RatesTableScreen(base: String) {
    val fx = remember { FxService() }
    val scope = rememberCoroutineScope()

    var rates by remember { mutableStateOf<Map<String, Double>?>(null) }
    var symbols by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                rates = fx.latestRates(base)
            } catch (e: Exception) {
                error = "Could not fetch rates"
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        try { symbols = fx.symbols() } catch (_: Exception) {}
    }
    LaunchedEffect(base) { load() }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search currency...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        when {
            loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            error != null -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { load() }) { Text("Retry") }
                }
            }
            rates != null -> {
                val rows = rates!!.entries
                    .filter { it.key.contains(query, true) || (symbols[it.key] ?: "").contains(query, true) }
                    .sortedBy { it.key }
                LazyColumn(Modifier.weight(1f)) {
                    items(rows) { e ->
                        ListItem(
                            headlineContent = {
                                Text(e.key, fontWeight = FontWeight.SemiBold)
                            },
                            supportingContent = {
                                Text(symbols[e.key] ?: "", fontSize = 12.sp)
                            },
                            trailingContent = {
                                Text(
                                    if (e.value < 1) "%.4f".format(e.value) else "%.2f".format(e.value),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            },
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}
