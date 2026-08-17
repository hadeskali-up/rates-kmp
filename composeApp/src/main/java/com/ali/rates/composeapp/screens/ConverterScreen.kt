package com.ali.rates.composeapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ali.rates.shared.FxService
import kotlinx.coroutines.launch

@Composable
fun ConverterScreen(
    base: String,
    quote: String,
    onBaseChange: (String) -> Unit,
    onQuoteChange: (String) -> Unit,
) {
    val fx = remember { FxService() }
    val scope = rememberCoroutineScope()

    var amountText by remember { mutableStateOf("1") }
    var rates by remember { mutableStateOf<Map<String, Double>?>(null) }
    var symbols by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

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

    val amount = amountText.toDoubleOrNull()
    val rate = rates?.get(quote)
    val result = if (amount != null && rate != null) amount * rate else null

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Amount card
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Amount", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { t -> if (t.matches(Regex("^\\d*\\.?\\d*"))) amountText = t },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 32.sp, fontWeight = FontWeight.SemiBold,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        // From / To card
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                CurrencyRow("From", base, symbols[base] ?: "", onBaseChange, symbols, quote)
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                CurrencyRow("To", quote, symbols[quote] ?: "", onQuoteChange, symbols, base)
            }
        }
        Spacer(Modifier.height(24.dp))

        when {
            loading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.padding(24.dp))
            }
            error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
            result != null -> Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "${"%.${if (result < 1) 4 else 2}f".format(result)} $quote",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "1 $base = ${"%.4f".format(rate!!)} $quote",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CurrencyRow(
    label: String,
    code: String,
    name: String,
    onPick: (String) -> Unit,
    symbols: Map<String, String>,
    exclude: String,
) {
    var showPicker by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
        Spacer(Modifier.width(16.dp))
        Text(code, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(12.dp))
        Text(
            name,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        IconButton(onClick = { showPicker = true }) {
            Icon(Icons.Default.KeyboardArrowDown, "Pick $label")
        }
    }
    if (showPicker) {
        CurrencyPickerSheet(
            symbols = symbols,
            exclude = exclude,
            onDismiss = { showPicker = false },
            onPick = {
                onPick(it)
                showPicker = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPickerSheet(
    symbols: Map<String, String>,
    exclude: String,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val entries = symbols.entries
        .filter { it.key != exclude && (it.key.contains(query, true) || it.value.contains(query, true)) }
        .sortedBy { it.key }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search currency...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f, fill = false)) {
            items(entries) { e ->
                ListItem(
                    headlineContent = { Text(e.key, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text(e.value) },
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
