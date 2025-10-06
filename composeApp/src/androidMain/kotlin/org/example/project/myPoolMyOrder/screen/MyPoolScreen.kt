package org.example.project.myPoolMyOrder.screen

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.example.project.R
import org.example.project.location.domain.model.Coordinates
import org.example.project.location.domain.model.IMapStates
import org.example.project.location.domain.repository.LocationProvider
import org.example.project.location.screen.permissions.locationPermissionHandler
import org.example.project.map.domain.model.MapMarker
import org.example.project.myPool.ui.model.MapOverlayCallbacks
import org.example.project.myPoolMyOrder.screen.model.MapOverlayState
import org.example.project.myPool.ui.model.MyOrdersPoolUiState
import org.example.project.myPool.ui.viewmodel.MyPoolViewModel
import org.example.project.myPoolMyOrder.screen.component.list.HorizontalListCallbacks
import org.example.project.myPoolMyOrder.screen.component.list.generalHorizontalList
import org.example.project.myPoolMyOrder.screen.component.map.initialCameraPositionEffect
import org.example.project.myPoolMyOrder.screen.component.map.mapScreen
import org.example.project.myPoolMyOrder.screen.component.map.provideMapStates
import org.example.project.myPoolMyOrder.screen.component.map.rememberFocusOnMarker
import org.example.project.myPoolMyOrder.screen.component.myPoolOrderCardItem
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel


// Zero fallback coordinates
private val ZERO_COORDS = Coordinates(0.0, 0.0)
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun myPoolScreen(
/*
    onOpenOrderDetails: (String) -> Unit,
*/
) {
    val poolVm: MyPoolViewModel = koinViewModel()

    val ui by poolVm.ui.collectAsState()
    var bottomBarHeight by remember { mutableStateOf(0.dp) }

    // decoupled location permission handling
    val context = LocalContext.current
    val locationProvider: LocationProvider = get()
    val coroutineScope = rememberCoroutineScope()

    locationPermissionHandler(
        onPermissionGranted =
            @androidx.annotation.RequiresPermission(
                allOf = [
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                ],
            ) { ctx ->
                val fused = LocationServices.getFusedLocationProviderClient(ctx)
                fused.lastLocation.addOnSuccessListener { loc ->
                    poolVm.updateDeviceLocation(loc)
                }
            },
    )
    val mapStates = provideMapStates()
    //initialCameraPositionEffect(ui.markers, ui.selectedMarkerId, mapStates)

    val state = overlayState(ui, bottomBarHeight, mapStates)
    val callbacks =
        rememberOverlayCallbacks(
            viewModel = poolVm,
            mapStates = mapStates,
            onOpenOrderDetails = {/*onOpenOrderDetails*/ },
            onNearEnd = { idx -> poolVm.loadNextIfNeeded(idx) },
            setBottomBarHeight = { bottomBarHeight = it },
        )

    mapWithBottomOverlay(state = state, callbacks = callbacks)
}

@Composable
private fun overlayState(
    ui: MyOrdersPoolUiState,
    bottomBarHeight: Dp,
    mapStates: IMapStates,
): MapOverlayState {
    val extra = dimensionResource(R.dimen.largeSpace)
    return MapOverlayState(
        isLoading = ui.isLoading,
        isLoadingMore = ui.isLoadingMore,
        orders = ui.orders,
        bottomPadding = bottomBarHeight + extra,
        mapUi = ui,
        mapStates = mapStates,
    )
}

@Composable
private fun rememberOverlayCallbacks(
    viewModel: MyPoolViewModel,
    mapStates: IMapStates,
    onOpenOrderDetails: (String) -> Unit,
    onNearEnd: (Int) -> Unit,
    setBottomBarHeight: (Dp) -> Unit,
): MapOverlayCallbacks {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val focus =
        rememberFocusOnMarker(
            onCenterChange = { marker ->
                // find matching order for marker
                val order =
                    viewModel.ui.value.orders
                        .firstOrNull { it.id == marker.id }
                order?.let { viewModel.onCenteredOrderChange(it) }
            },
            mapStates = mapStates,
            scope = scope,
        )

    return MapOverlayCallbacks(
        onBottomHeightMeasured = { px -> setBottomBarHeight(with(density) { px.toDp() }) },
        onCenteredOrderChange = { order, index ->
            val marker =
                MapMarker(
                    id = order.id,
                    title = order.name,
                    coordinates = Coordinates(order.lat, order.lng),
                    distanceKm = order.distanceKm,
                    snippet = order.orderNumber,
                )
            focus(marker)
            viewModel.onCenteredOrderChange(order, index)
        },
        onOpenOrderDetails = onOpenOrderDetails,
        onNearEnd = onNearEnd,
    )
}

@Composable
private fun mapWithBottomOverlay(
    state: MapOverlayState,
    callbacks: MapOverlayCallbacks,
) {
    Box(Modifier.fillMaxSize()) {
        mapScreen(
            ui = state.mapUi,
            mapStates = state.mapStates,
            deviceLatLng = ZERO_COORDS,
            bottomOverlayPadding = state.bottomPadding,
        )
        if (state.orders.isNotEmpty()) {
            bottomOverlay(state, callbacks)
        }
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun BoxScope.bottomOverlay(
    state: MapOverlayState,
    callbacks: MapOverlayCallbacks,
) {
    Column(
        Modifier
            .align(Alignment.BottomCenter)
            .onGloballyPositioned { callbacks.onBottomHeightMeasured(it.size.height) },
    ) {
        loadingMoreIndicator(state)
        ordersHorizontalList(state, callbacks)
    }
}

@Composable
private fun loadingMoreIndicator(state: MapOverlayState) {
    AnimatedVisibility(visible = state.isLoadingMore) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = dimensionResource(R.dimen.smallSpace)),
            )
        }
    }
}


@Composable
private fun ordersHorizontalList(
    state: MapOverlayState,
    callbacks: MapOverlayCallbacks,
) {
    generalHorizontalList(
        orders = state.orders,
        callbacks =
            HorizontalListCallbacks(
                onCenteredOrderChange = { order, index ->
                    callbacks.onCenteredOrderChange(order, index)
                },
                onNearEnd = { idx -> callbacks.onNearEnd(idx) },
            ),
        cardContent = { order, _ ->
            myPoolOrderCardItem(
                order = order,
                onOpenOrderDetails = callbacks.onOpenOrderDetails,
                onCall = { },
            )
        },
    )
}
