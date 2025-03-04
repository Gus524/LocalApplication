package com.goodgus.localapplication.views

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.goodgus.localapplication.components.InfoCard
import com.goodgus.localapplication.components.ShowAlert
import com.goodgus.localapplication.models.dataView.GetCuenta
import com.goodgus.localapplication.viewModels.HomeViewModel


@Composable
fun HomeScreen(
    navigateToVenta: (String) -> Unit,
    viewModel: HomeViewModel
) {
    val cuentaActivaState: State<List<GetCuenta>> = viewModel.cuenta.collectAsState()
    val cuentaActiva = cuentaActivaState.value
    val uiState by viewModel.uiState.collectAsState()

    if(cuentaActiva.isNotEmpty()) {
        ShowCatalogo(cuentaActiva, navigateToVenta, uiState, viewModel)
    } else {
        viewModel.tryCuenta()
        if(uiState.cuentaSinVentas.isNotEmpty()) {
            ShowCatalogo(cuentaActiva, navigateToVenta, uiState, viewModel)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No hay cuenta activa",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp, top = 32.dp)
                )
                Button(onClick = { viewModel.openCuenta() }) {
                    Text("Abrir Cuenta")
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShowCatalogo(
    ventas: List<GetCuenta>,
    navigateToVenta: (String) -> Unit,
    uiState: HomeViewModel.CuentaUIState,
    viewModel: HomeViewModel
){
    val listState = rememberLazyListState()
    var isFabVisible by remember { mutableStateOf(true) }
    var previousIndex by remember { mutableIntStateOf(0) }

    isFabVisible = remember {
        derivedStateOf {
            val currentFirstVisibleIndex = listState.firstVisibleItemIndex
            val isScrollingUp = currentFirstVisibleIndex < previousIndex
            previousIndex = currentFirstVisibleIndex // Actualiza previousIndex aquí
            println("Índice actual: $currentFirstVisibleIndex, Índice anterior: $previousIndex, Subiendo: $isScrollingUp")
            isScrollingUp || listState.firstVisibleItemIndex == 0 // Muestra el FAB si sube o está en el primer elemento
        }
    }.value



    Box(
        modifier = Modifier.fillMaxSize(),
    ) {

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ){
            stickyHeader {
                Surface(tonalElevation = 4.dp) {
                    Row (
                        modifier = Modifier
                            .padding(start = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ){
                        Text("Total: $${String.format("%.2f", ventas.firstOrNull()?.totalCuenta)}",
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
            items(ventas) { venta ->
                VentaCard(
                    venta = venta,
                    onEdit = { navigateToVenta(venta.idVenta.toString()) },
                    onDelete = { viewModel.showAlert(venta.idVenta) },
                )
            }
        }
        AnimatedVisibility(
            visible = isFabVisible,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(32.dp),
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            FloatingActionButton(
                onClick = { viewModel.showClose() }
            ) {
                Icon(
                    Icons.Filled.Clear,
                    contentDescription = "Cerrar cuenta"
                )
            }
        }
        AnimatedVisibility(
            visible = isFabVisible,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp),
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            FloatingActionButton(
                onClick = { navigateToVenta("") },
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add venta"
                )
            }
        }
    }

    if(uiState.showDelete){
        ShowDeleteAlert(
            nombre = "¿Seguro que deseas eliminar esta venta?",
            title = "Eliminar Venta",
            onDismissDialog = { viewModel.closeAlert() },
            onConfirm = { viewModel.deleteVenta() }
        )
    }

    if(uiState.showClose){
        ShowDeleteAlert(
            nombre = "Seguro que desas cerrar la cuenta actual?",
            title = "Cerrar Cuenta",
            onDismissDialog = { viewModel.closeAlert() },
            onConfirm = { viewModel.closeCuenta() }
        )
    }
}

@Composable
fun ShowDeleteAlert(
    nombre: String,
    title: String,
    onDismissDialog: () -> Unit,
    onConfirm: () -> Unit
){
    ShowAlert(
        onDismissRequest = onDismissDialog,
        text = nombre,
        title = title,
        onConfirmButtonClick = onConfirm
    )
}

@Composable
fun VentaCard(
    venta: GetCuenta,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    val ventaData = listOf(
        "Hora" to venta.horaVenta,
        "Marca" to venta.marca,
        "Cantidad" to venta.cantidadProducto.toString(),
        "Venta" to "$${venta.parcialVenta}"
    )

    InfoCard(
        title = venta.nombre,
        dataList = ventaData,
        onEditClick = onEdit,
        onDeleteClick = onDelete,
        modifier = Modifier.padding(4.dp)
    )
}
