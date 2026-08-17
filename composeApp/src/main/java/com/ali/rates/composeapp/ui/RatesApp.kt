package com.ali.rates.composeapp.ui

@file:OptIn(ExperimentalMaterial3Api::class)

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.ali.rates.composeapp.screens.ChartScreen
import com.ali.rates.composeapp.screens.ConverterScreen
import com.ali.rates.composeapp.screens.RatesTableScreen

@Composable
fun RatesApp() {
    RatesTheme {
        var base by remember { mutableStateOf("USD") }
        var quote by remember { mutableStateOf("MYR") }
        var tab by remember { mutableIntStateOf(0) }

        val titles = listOf("Convert", "Chart", "Rates")

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "${titles[tab]} · $base/$quote",
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                    actions = {
                        if (tab != 2) {
                            IconButton(onClick = { val t = base; base = quote; quote = t }) {
                                Icon(Icons.Default.CurrencyExchange, "Swap pair")
                            }
                        }
                    },
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                ) {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        icon = { Icon(Icons.Default.CurrencyExchange, null) },
                        label = { Text("Convert") },
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        icon = { Icon(Icons.Default.ShowChart, null) },
                        label = { Text("Chart") },
                    )
                    NavigationBarItem(
                        selected = tab == 2,
                        onClick = { tab = 2 },
                        icon = { Icon(Icons.Default.TableRows, null) },
                        label = { Text("Rates") },
                    )
                }
            },
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when (tab) {
                    0 -> ConverterScreen(
                        base = base, quote = quote,
                        onBaseChange = { base = it }, onQuoteChange = { quote = it },
                    )
                    1 -> ChartScreen(
                        base = base, quote = quote,
                        onBaseChange = { base = it }, onQuoteChange = { quote = it },
                    )
                    2 -> RatesTableScreen(base = base)
                }
            }
        }
    }
}
