package org.example.project

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.unit.Dp
import org.koin.androidx.compose.koinViewModel
import org.lmd.project.delivery.domain.model.DeliveryLog
import org.lmd.project.delivery.domain.model.DeliveryState

// ---- domain + vm ----
import org.lmd.project.delivery.ui.vm.DeliveriesLogViewModel
import org.lmd.project.navigation.NavigationHandler

@Composable
fun DeliveriesLogScreen(navigationHandler: NavigationHandler,) {
    val vm: DeliveriesLogViewModel = koinViewModel()

    LaunchedEffect(Unit) { vm.load() }

    val logs by vm.logs.collectAsState()
    val isLoadingMore by vm.isLoadingMore.collectAsState()
    val endReached by vm.endReached.collectAsState()
    val isRefreshing by vm.isRefreshing.collectAsState()

    val listState = rememberLazyListState()

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HeaderRow()
        HorizontalDivider()

        Box(Modifier.weight(1f)) {
            when {
                isRefreshing && logs.isEmpty() -> CenterLoader()
                logs.isEmpty() -> EmptyState(Modifier.fillMaxSize())
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(logs, key = { _, it -> it.number }) { index, item ->
                            DeliveryLogItem(item)
                            // ask VM to prefetch next page near the tail
                            LaunchedEffect(index, logs.size) {
                                // prefetchThreshold = 3
                                if (index >= logs.size - 3 && !isLoadingMore && !endReached) {
                                    vm.loadMore()
                                }
                            }
                        }

                        item("footer") {
                            when {
                                isLoadingMore -> LoadingFooter()
                                endReached     -> EndFooter()
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------
//  Header
// ------------------------------------------------------------------
@Composable
private fun HeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderText("SLA")
        HeaderText("Order details")
        HeaderText("Delivery time")
    }
}

@Composable
private fun HeaderText(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
    )
}

// ------------------------------------------------------------------
//  List slot content
// ------------------------------------------------------------------
@Composable private fun CenterLoader() =
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }

@Composable
private fun LoadingFooter() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) { CircularProgressIndicator() }
}

@Composable
private fun EndFooter() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) { Text("• End of list •", style = MaterialTheme.typography.bodySmall) }
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(
            text = "No deliveries yet",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}

// ------------------------------------------------------------------
//  Item row
// ------------------------------------------------------------------
private val ICON_CELL: Dp = 48.dp
private val TIME_CELL: Dp = 100.dp
private val GAP: Dp = 8.dp

@Composable
private fun DeliveryLogItem(log: DeliveryLog) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            val detailsWidth = maxWidth - ICON_CELL - TIME_CELL - (GAP * 2)
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusCell(log.state)
                Spacer(Modifier.width(GAP))
                DetailsCell(log, Modifier.width(detailsWidth))
                Spacer(Modifier.width(GAP))
                TimeCell(log)
            }
        }
    }
}

@Composable
private fun StatusCell(state: DeliveryState) {
    Box(Modifier.width(ICON_CELL), contentAlignment = Alignment.Center) {
        when (state) {
            DeliveryState.DELIVERED ->
                Icon(
                    Icons.Filled.CheckCircle, contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
            DeliveryState.CANCELLED, DeliveryState.FAILED ->
                Icon(
                    Icons.Filled.Cancel, contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            else ->
                Icon(
                    Icons.Filled.CheckCircle, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
        }
    }
}

@Composable
private fun DetailsCell(
    log: DeliveryLog,
    modifier: Modifier = Modifier,
) {
    val formattedDate = safeFormatOrderDate(log.createdAt)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            formattedDate,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            log.number,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun TimeCell(log: DeliveryLog) {
    val color =
        when (log.state) {
            DeliveryState.DELIVERED -> MaterialTheme.colorScheme.tertiary
            DeliveryState.CANCELLED, DeliveryState.FAILED -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    Box(Modifier.width(TIME_CELL), contentAlignment = Alignment.Center) {
        Text(log.createdAt, color = color, textAlign = TextAlign.Center, maxLines = 1)
    }
}

// ------------------------------------------------------------------
//  Helpers
// ------------------------------------------------------------------
private fun safeFormatOrderDate(raw: String): String {
    return try {
        // Try with timezone first
        val tzFmt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", java.util.Locale.getDefault())
        val out = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
        val date = runCatching { tzFmt.parse(raw) }.getOrNull()
            ?: run {
                // Fallback without millis/timezone
                val altIn = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                altIn.parse(raw.removeSuffix("Z"))
            }
        date?.let { out.format(it) } ?: raw
    } catch (e: Throwable) {
        Log.w("DateFormat", "Failed to parse date: $raw", e)
        raw
    }
}
