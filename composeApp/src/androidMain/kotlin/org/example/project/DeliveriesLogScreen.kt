package org.example.project

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel

// ---- Your domain + VM ----
import org.example.project.delivery.domain.model.DeliveryLog
import org.example.project.delivery.domain.model.DeliveryState
import org.example.project.delivery.ui.vm.DeliveriesLogViewModel

// ---- Your vertical list library (adjust package if different) ----
import com.example.verticallist.PagingState
import com.example.verticallist.VerticalListConfig
import com.example.verticallist.defaultVerticalListConfig
import com.example.verticallist.verticalListComponent

// ------------------------------------------------------------------
//  Screen
// ------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveriesLogScreen() {
    val vm: DeliveriesLogViewModel = koinViewModel()

    LaunchedEffect(Unit) { vm.load() }

    // VM state
    val logs by vm.logs.collectAsState()
    val isLoadingMore by vm.isLoadingMore.collectAsState()
    val endReached by vm.endReached.collectAsState()
    val isRefreshing by vm.isRefreshing.collectAsState()

    // List + paging plumbing (from your vertical list lib)
    val listState = rememberLazyListState()
    val paging = remember(isRefreshing, isLoadingMore, endReached) {
        PagingState(
            isRefreshing = isRefreshing,
            onRefresh = { vm.refresh() },
            isLoadingMore = isLoadingMore,
            endReached = endReached,
            onLoadMore = { vm.loadMore() },
        )
    }
    val listConfig: VerticalListConfig =
        defaultVerticalListConfig(
            listState = listState,
            paging = paging,
        ).copy(
            emptyContent = {
                if (!isRefreshing && !isLoadingMore) EmptyState(Modifier.fillMaxSize())
            },
            loadingFooter = { LoadingFooter() },
            endFooter = { EndFooter() },
        )

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HeaderRow()
        HorizontalDivider()
        Box(Modifier.weight(1f)) {
            verticalListComponent(
                items = logs,                 // List<DeliveryLog>
                key = { it.number },         // stable key from your domain
                itemContent = { DeliveryLogItem(it) },
                config = listConfig,
            )
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
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
private val ICON_CELL = 48.dp
private val TIME_CELL = 100.dp
private val GAP = 8.dp

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
                androidx.compose.material3.Icon(
                    Icons.Filled.CheckCircle, null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
            DeliveryState.CANCELLED, DeliveryState.FAILED ->
                androidx.compose.material3.Icon(
                    Icons.Filled.Cancel, null,
                    tint = MaterialTheme.colorScheme.error
                )
            else ->
                androidx.compose.material3.Icon(
                    Icons.Filled.CheckCircle, null,
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
    // Your domain already provides orderDate as a string; render as-is
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
    // If backend already sends a human-readable string, just return it.
    // Otherwise, try to parse a common ISO-ish format, fallback to raw.
    return try {
        val input = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.getDefault())
        val date = input.parse(raw.removeSuffix("Z"))
        val output = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
        date?.let { output.format(it) } ?: raw
    } catch (e: Throwable) {
        Log.w("DateFormat", "Failed to parse date: $raw", e)
        raw
    }
}
