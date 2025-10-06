package org.example.project.generalPool

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ntg.horizontallist.GeneralHorizontalList
import com.ntg.horizontallist.GeneralHorizontalListCallbacks
import org.example.project.generalPool.domain.model.GeneralPoolUiState
import org.example.project.generalPool.domain.model.OrderInfo
import org.example.project.generalPool.vm.GeneralPoolViewModel
import org.example.project.generalPool.components.orderCard
import org.lmd.project.R

@Composable
fun poolBottomContent(
    ui: GeneralPoolUiState,
    viewModel: GeneralPoolViewModel,
    focusOnOrder: (OrderInfo, Boolean) -> Unit,
//    onAddToMe: (OrderInfo) -> Unit,
) {
    when {
        ui.isLoading -> loadingText()
//        ui.mapOrders.isNotEmpty() -> ordersHorizontalList(ui, viewModel, focusOnOrder, onAddToMe)
        ui.mapOrders.isNotEmpty() -> ordersHorizontalList(ui, viewModel, focusOnOrder)
        else -> Text(
            text = "No orders",
            modifier = Modifier.padding(12.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun loadingText() {
    Box(Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.loading_text),
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
fun ordersHorizontalList(
    ui: GeneralPoolUiState,
    viewModel: GeneralPoolViewModel,
    focusOnOrder: (OrderInfo, Boolean) -> Unit,
//    onAddToMe: (OrderInfo) -> Unit,
) {

    val stripHeight = 180.dp

    Box(
        Modifier
            .fillMaxWidth()
            .height(stripHeight)
            .zIndex(10f)
            .padding(bottom = 8.dp)
    ) {
//        Box(Modifier.align(Alignment.BottomCenter)) {

        GeneralHorizontalList(
            items = ui.mapOrders,
            key = { it.id },
            callbacks = GeneralHorizontalListCallbacks(
                onCenteredItemChange = { order, _ ->
                    focusOnOrder(order, false)
                    viewModel.onOrderSelected(order)
                },
                onNearEnd = { idx ->
                    // TODO
                }
            )
        ) { order, _ ->
            orderCard(
                order = order,
//                    onAddClick = { onAddToMe(order) },
                onAddClick = { },
                onOrderClick = { clicked -> focusOnOrder(clicked, false) },
            )
        }
    }
}

