package org.example.project.myPoolMyOrder.screen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.example.project.R
import org.example.project.SecureTokenStore
import org.example.project.UserStore
import org.example.project.location.domain.repository.LocationProvider
import org.example.project.location.screen.permissions.locationPermissionHandler
import org.example.project.map.domain.model.toMapMarker
import org.example.project.myPool.domian.model.OrderStatus
import org.example.project.myPool.ui.model.AgentsState
import org.example.project.myPool.ui.model.MyOrdersPoolUiState
import org.example.project.myPoolMyOrder.screen.model.OrdersBodyDeps
import org.example.project.myPool.ui.model.OrdersContentCallbacks
import org.example.project.myPoolMyOrder.screen.model.OrdersContentDeps
import org.example.project.myPoolMyOrder.screen.model.WireDeps
import org.example.project.myPool.ui.state.MyOrdersUiState
import org.example.project.myPool.ui.viewmodel.ActiveAgentsViewModel
import org.example.project.myPool.ui.viewmodel.MyOrdersViewModel
import org.example.project.myPool.ui.viewmodel.MyPoolViewModel
import org.example.project.myPool.ui.viewmodel.UpdateOrderStatusViewModel
import org.example.project.myPoolMyOrder.screen.component.bottomStickyButton
import org.example.project.myPoolMyOrder.screen.component.map.initialCameraPositionEffect
import org.example.project.myPoolMyOrder.screen.component.map.provideMapStates
import org.example.project.myPoolMyOrder.screen.component.ordersContent
import org.example.project.myPoolMyOrder.screen.component.ordersEffects
import org.example.project.myPoolMyOrder.screen.component.reassignBottomSheet
import org.example.project.util.AndroidUserStore
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun myOrdersScreen(
    navController: NavController,
) {
    val ordersVm: MyOrdersViewModel = koinViewModel()
    val updateVm: UpdateOrderStatusViewModel = koinViewModel()
    val agentsVm: ActiveAgentsViewModel = koinViewModel()
    val poolVm: MyPoolViewModel = koinViewModel()

    val snack = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val reassignOrderId = remember { mutableStateOf<String?>(null) }

    val tokenStore: SecureTokenStore = koinInject()

    LaunchedEffect(Unit) {
        val token = tokenStore.getAccessToken()
        if (token.isNullOrEmpty()) {
            println("⚠️ Token is empty — skipping initial orders fetch")
            return@LaunchedEffect
        }

        ordersVm.listVM.refreshOrders()
    }

    wireMyOrders(
        WireDeps(
            navController = navController,
            ordersVm = ordersVm,
            updateVm = updateVm,
            agentsVm = agentsVm,
            poolVm = poolVm,
            listState = listState,
            snack = snack,
            reassignOrderId = reassignOrderId,
        ),
    )

    ordersBody(
        OrdersBodyDeps(
            ordersVm = ordersVm,
            updateVm = updateVm,
            agentsVm = agentsVm,
            listState = listState,
            snack = snack,
            reassignOrderId = reassignOrderId,
            onOpenOrderDetails = {},

            ),
        navController = navController,

        )
}

@Composable
private fun ordersBody(deps: OrdersBodyDeps, navController: NavController) {
    val uiState by deps.ordersVm.uiState.collectAsState()
    val updatingIds by deps.updateVm.updatingIds.collectAsState()
    val agentsState by deps.agentsVm.state.collectAsState()

    ordersScaffold(
        deps = deps,
        updatingIds = updatingIds,
        navController = navController,
    )

    reassignSheet(
        deps = deps,
        uiOrders = uiState,
        agentsState = agentsState,
    )
}

@Composable
private fun ordersScaffold(
    deps: OrdersBodyDeps,
    updatingIds: Set<String>,
    navController: NavController,
) {
    val listState = deps.listState
    val snack = deps.snack

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = { bottomStickyButton(text = stringResource(R.string.order_pool)) {navController.navigate("my_pool_screen")} },
    ) { innerPadding ->
        ordersContent(
            ordersVm = deps.ordersVm,
            deps =
                OrdersContentDeps(
                    updateVm = deps.updateVm,
                    listState = listState,
                    updatingIds = updatingIds,
                ),
            cbs =
                OrdersContentCallbacks(
                    onOpenOrderDetails = deps.onOpenOrderDetails,
                    onReassignRequested = { id ->
                        deps.reassignOrderId.value = id
                        deps.agentsVm.load()
                    },
                ),
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        )
    }
}

@Composable
private fun reassignSheet(
    deps: OrdersBodyDeps,
    uiOrders: MyOrdersUiState,
    agentsState: AgentsState,
) {
    reassignBottomSheet(
        open = deps.reassignOrderId.value != null,
        state = agentsState,
        onDismiss = { deps.reassignOrderId.value = null },
        onRetry = { deps.agentsVm.load() },
        onSelect = { user ->
            val orderId = deps.reassignOrderId.value ?: return@reassignBottomSheet
            Log.d("ReassignFlow", "onSelect: orderId=$orderId → newAssignee=${user.id}")

            UpdateOrderStatusViewModel.OrderLogger.uiTap(
                orderId,
                uiOrders.orders.firstOrNull { it.id == orderId }?.orderNumber,
                "Menu:Reassign→${user.name}",
            )
            deps.ordersVm.statusVM.updateStatusLocally(
                id = orderId,
                newStatus = OrderStatus.ADDED,
                newAssigneeId = user.id,
            )
            deps.updateVm.update(
                orderId = orderId,
                targetStatus = OrderStatus.ADDED,
                assignedAgentId = user.id,
            )
            deps.reassignOrderId.value = null
        },
    )
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
private fun wireMyOrders(deps: WireDeps) {
    val ctx = LocalContext.current
    val poolUi by deps.poolVm.ui.collectAsState()

    myOrdersLocationSection(deps, poolUi)
    myOrdersUserSection(deps)
    myOrdersEffectsSection(deps, ctx)
    observeOrdersSearch(deps.navController, deps.ordersVm)
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun myOrdersLocationSection(
    deps: WireDeps,
    poolUi: MyOrdersPoolUiState,
) {
    val locationProvider: LocationProvider = get() // from Koin
    val context = LocalContext.current

    locationPermissionHandler(
        onPermissionGranted = { context ->
            val fused = LocationServices
                    .getFusedLocationProviderClient(context)
            fused.lastLocation.addOnSuccessListener { loc ->
                deps.poolVm.updateDeviceLocation(loc)
            }
        },
    )

    val markers = poolUi.orders.map { it.toMapMarker() }
    val mapStates = provideMapStates()

    forwardMyPoolLocationToMyOrders(deps.poolVm, deps.ordersVm)
}

@Composable
private fun myOrdersUserSection(deps: WireDeps) {
    val userStore: UserStore = koinInject()
    val currentUserId: String? = remember { userStore.getUserId() }
    LaunchedEffect(currentUserId) {
        deps.ordersVm.listVM.setCurrentUserId(currentUserId)
    }
}

@Composable
private fun myOrdersEffectsSection(
    deps: WireDeps,
    ctx: android.content.Context,
) {
    ordersEffects(
        vm = deps.ordersVm,
        updateVm = deps.updateVm,
        listState = deps.listState,
        snackbarHostState = deps.snack,
        context = ctx,
    )

    val uiState by deps.ordersVm.uiState.collectAsState()
    LaunchedEffect(uiState.query) { deps.listState.scrollToItem(0) }

    LaunchedEffect(Unit) {
        deps.updateVm.error.collect { (msg, retry) ->
            val res =
                deps.snack.showSnackbar(
                    message = msg,
                    actionLabel = ctx.getString(R.string.retry),
                    withDismissAction = true,
                )
            if (res == SnackbarResult.ActionPerformed) retry()
        }
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
private fun forwardMyPoolLocationToMyOrders(
    poolVm: MyPoolViewModel,
    ordersVm: MyOrdersViewModel,
) {
    val lastLoc by poolVm.lastLocation.collectAsState(initial = null)
    LaunchedEffect(lastLoc) { ordersVm.listVM.updateDeviceLocation(lastLoc) }
}

// Handle search for orders
@Composable
private fun observeOrdersSearch(
    navController: NavController,
    vm: MyOrdersViewModel,
) {
    val back = navController.currentBackStackEntry

    // Launched Effects for Search
    LaunchedEffect(back) {
        val h = back?.savedStateHandle ?: return@LaunchedEffect
        combine(
            h.getStateFlow("searching", false),
            h.getStateFlow("search_text", ""),
        ) { enabled, text -> if (enabled) text else null }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { q ->
                vm.searchVM.applySearchQuery(q)
            }
    }

    LaunchedEffect(back) {
        val h = back?.savedStateHandle ?: return@LaunchedEffect
        h.getStateFlow("search_submit", "").collect { submitted ->
            if (submitted.isNotEmpty()) {
                vm.searchVM.applySearchQuery(submitted)
                h["search_submit"] = ""
            }
        }
    }
}