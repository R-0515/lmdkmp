package org.example.project

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import org.example.project.orderhistory.OrderHistoryViewModel
import org.example.project.orderhistory.domain.model.OrderHistoryStatus
import org.example.project.orderhistory.domain.model.OrderHistoryUi
import org.example.project.orderhistory.report.AndroidOrdersPdfExporter
import org.example.project.orderhistory.ui.ReportMeta
import org.example.project.orderhistory.ui.toSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersHistoryScreen() {
    val vm: OrderHistoryViewModel = koinViewModel()

    val orders by vm.orders.collectAsState()
    val isRefreshing by vm.isRefreshing.collectAsState()
    val isLoadingMore by vm.isLoadingMore.collectAsState()
    val endReached by vm.endReached.collectAsState()

    val filter by vm.filter.collectAsState()

    var showFilter by remember { mutableStateOf(false) }
    var showSort by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val exporter = remember { AndroidOrdersPdfExporter(context) }

    LaunchedEffect(Unit) { vm.loadOrders() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order history") },
                actions = {
                    IconButton(onClick = { showFilter = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { showSort = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                    }
                    IconButton(onClick = { vm.refreshOrders() }, enabled = !isRefreshing) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            try {
                                if (orders.isEmpty()) {
                                    snackbarHostState.showSnackbar("No orders to export")
                                    return@launch
                                }

                                val meta = ReportMeta(
                                    title = "Orders History",
                                    filterSummary = vm.filter.value.toSummary(),
                                    generatedAt = "Generated: " +
                                            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                                )

                                android.util.Log.d("PDF", "Export start: count=${orders.size}, meta=${meta.filterSummary}")
                                val res = exporter.export(orders = orders, meta = meta)
                                if (res.ok && res.uriString != null) {
                                    val uri = Uri.parse(res.uriString)
                                    android.util.Log.d("PDF", "Export OK: $uri")
                                    snackbarHostState.showSnackbar("PDF exported")
                                    // try to share; if no targets, open viewer
                                    runCatching { exporter.sharePdf(uri) }
                                        .onFailure {
                                            android.util.Log.w("PDF", "Share failed: ${it.message}; opening viewer")
                                            val view = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                setDataAndType(uri, "application/pdf")
                                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(view)
                                        }
                                } else {
                                    val err = res.error ?: "Unknown error"
                                    android.util.Log.e("PDF", "Export failed: $err")
                                    snackbarHostState.showSnackbar("Export failed: $err")
                                }
                            } catch (t: Throwable) {
                                android.util.Log.e("PDF", "Export crash: ${t.message}", t)
                                snackbarHostState.showSnackbar("Export crashed: ${t.message}")
                            }
                        }
                    }) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = "Export PDF")
                    }

                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (orders.isEmpty() && !isRefreshing) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No orders yet")
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = orders,
                        key = { _, item -> item.orderId }
                    ) { index, item ->
                        OrderHistoryRow(item)
                        LaunchedEffect(index, orders.size) {
                            vm.loadMoreIfNeeded(index)
                        }
                    }

                    item(key = "footer") {
                        when {
                            isLoadingMore -> LoadingFooter()
                            endReached -> EndFooter()
                        }
                    }
                }
            }

            if (isRefreshing) {
                Box(
                    Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
        }
    }

    // -------- Filter dialog --------
    if (showFilter) {
        FilterDialog(
            initialAllowed = filter.allowed,
            onApply = { allowed ->
                vm.setAllowedStatuses(allowed)
                showFilter = false
            },
            onReset = {
                vm.resetFilters()
                showFilter = false
                vm.loadOrders()
            },
            onDismiss = { showFilter = false }
        )
    }

    // -------- Sort dialog --------
    if (showSort) {
        SortDialog(
            initialAscending = filter.ageAscending,
            onApply = { asc ->
                vm.setAgeAscending(asc)
                showSort = false
            },
            onDismiss = { showSort = false }
        )
    }
}

@Composable
private fun OrderHistoryRow(ui: OrderHistoryUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = ui.number, fontWeight = FontWeight.SemiBold)
                StatusChip(name = ui.status.name, colorHex = ui.statusColor)
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = ui.customer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))
            Text(
                text = formatDateMillis(ui.createdAtMillis),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusChip(name: String, colorHex: String) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val bg = remember(colorHex, surfaceVariant) {
        runCatching { Color(android.graphics.Color.parseColor(colorHex)) }
            .getOrDefault(surfaceVariant)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun LoadingFooter() {
    Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EndFooter() {
    Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
        Text("• End of list •", style = MaterialTheme.typography.bodySmall)
    }
}

private fun formatDateMillis(millis: Long): String {
    if (millis <= 0L) return "-"
    val instant = Instant.fromEpochMilliseconds(millis)
    val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val month = dt.month.name.take(3).lowercase().replaceFirstChar { it.titlecase() }
    val hour12 = ((dt.hour + 11) % 12) + 1
    val ampm = if (dt.hour < 12) "AM" else "PM"
    return "${dt.dayOfMonth} $month ${dt.year}, $hour12:%02d $ampm".format(dt.minute)
}

/* ------------------- Dialogs ------------------- */

@Composable
private fun FilterDialog(
    initialAllowed: Set<OrderHistoryStatus>,
    onApply: (Set<OrderHistoryStatus>) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember(initialAllowed) { mutableStateOf(initialAllowed.toMutableSet()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by status") },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OrderHistoryStatus.values().forEach { st ->
                    val checked = st in selected
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                if (isChecked) selected.add(st) else selected.remove(st)
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = st.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(selected.toSet()) }) { Text("Apply") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onReset) { Text("Reset") }
                Spacer(Modifier.width(4.dp))
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

@Composable
private fun SortDialog(
    initialAscending: Boolean,
    onApply: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var ascending by remember(initialAscending) { mutableStateOf(initialAscending) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort by date") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = ascending, onClick = { ascending = true })
                    Spacer(Modifier.width(8.dp))
                    Text("Oldest first")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = !ascending, onClick = { ascending = false })
                    Spacer(Modifier.width(8.dp))
                    Text("Newest first")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(ascending) }) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
