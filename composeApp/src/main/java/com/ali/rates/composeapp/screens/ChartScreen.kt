package com.ali.rates.composeapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.ali.rates.shared.FxService
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch

private val RANGES = listOf("1M" to 30, "3M" to 90, "6M" to 182, "1Y" to 365)

@Composable
fun ChartScreen(
    base: String,
    quote: String,
    onBaseChange: (String) -> Unit,
    onQuoteChange: (String) -> Unit,
) {
    val fx = remember { FxService() }
    val scope = rememberCoroutineScope()

    var rangeDays by remember { mutableIntStateOf(365) }
    var series by remember { mutableStateOf<Map<String, Double>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                val s = fx.timeseries(base, quote, rangeDays)
                series = s
                if (s.isEmpty()) error = "No ECB data for $base/$quote"
            } catch (e: Exception) {
                error = "Could not load chart data"
            }
            loading = false
        }
    }

    LaunchedEffect(base, quote, rangeDays) { load() }

    Column(Modifier.fillMaxSize()) {
        // Range chips
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RANGES.forEach { (label, days) ->
                FilterChip(
                    selected = rangeDays == days,
                    onClick = { rangeDays = days },
                    label = { Text(label) },
                )
            }
        }

        when {
            loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            error != null -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error!!, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { load() }) { Text("Retry") }
                }
            }
            series != null && series!!.isNotEmpty() -> {
                val s = series!!
                val min = s.values.min()
                val max = s.values.max()
                Text(
                    "1 $base in $quote · min ${"%.4f".format(min)} – max ${"%.4f".format(max)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                AndroidView(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    factory = { ctx ->
                        LineChart(ctx).apply {
                            axisLeft.textColor = 0x61FFFFFF
                            axisRight.isEnabled = false
                            xAxis.isEnabled = false
                            description.isEnabled = false
                            legend.isEnabled = false
                            setDrawGridBackground(false)
                            setGridColor(0x0FFFFFFF)
                        }
                    },
                    update = { chart ->
                        val entries = s.entries
                            .sortedBy { it.key }
                            .mapIndexed { i, e -> Entry(i.toFloat(), e.value.toFloat()) }
                        val ds = LineDataSet(entries, "$base/$quote").apply {
                            color = 0xFF00E5A0.toInt()
                            lineWidth = 2f
                            setDrawCircles(false)
                            setDrawValues(false)
                            setDrawFilled(true)
                            fillColor = 0x1400E5A0
                        }
                        chart.data = LineData(ds)
                        chart.invalidate()
                    },
                )
            }
        }
    }
}
