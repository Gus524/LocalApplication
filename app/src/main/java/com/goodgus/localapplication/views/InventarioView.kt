package com.goodgus.localapplication.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.goodgus.localapplication.components.InfoCard
import com.goodgus.localapplication.components.ShowAlert
import com.goodgus.localapplication.models.data.Producto
import com.goodgus.localapplication.viewModels.InventarioViewModel

@Composable
fun InventarioScreen(
    navigateToProducto: (String) -> Unit,
    viewModel: InventarioViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    ShowStorage(
        productos = uiState.productos,
        navigateToProducto = navigateToProducto,
        uiState = uiState,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShowStorage(
    productos: List<Producto>,
    navigateToProducto: (String) -> Unit,
    uiState: InventarioViewModel.InventarioUIState,
    viewModel: InventarioViewModel
) {
    val listState = rememberLazyListState()
    var isFabVisible by remember { mutableStateOf(true) }
    var previousIndex by remember { mutableIntStateOf(0) }

    isFabVisible = remember {
        derivedStateOf {
            val currentFirstVisibleIndex = listState.firstVisibleItemIndex
            val isScrollingUp = currentFirstVisibleIndex < previousIndex
            previousIndex = currentFirstVisibleIndex // Actualiza previousIndex aquí
            isScrollingUp || listState.firstVisibleItemIndex == 0 // Muestra el FAB si sube o está en el primer elemento
        }
    }.value

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            stickyHeader {
                Surface(tonalElevation = 4.dp) {
                    OutlinedTextField(
                        value = uiState.searchText,
                        onValueChange = {
                            viewModel.updateSearch(it)
                            viewModel.searchProducts()
                        },
                        label = { Text("Buscar producto") },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        trailingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        }
                    )
                }
            }
            items(
                items = productos,
                key = { producto -> producto.idProducto }
            ) { producto ->
                key(producto.idProducto) {
                    InventarioCard(
                        producto = producto,
                        onEdit = { navigateToProducto(producto.idProducto.toString()) },
                        onDelete = { viewModel.showAlert(producto.idProducto) },
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = isFabVisible,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            FloatingActionButton(
                onClick = { navigateToProducto("") },
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add producto"
                )
            }
        }
    }
    if(uiState.showDelete){
        ShowDownAlert(
            nombre = "¿Seguro que deseas dar de baja este producto?",
            onDismissDialog = { viewModel.closeAlert() },
            onConfirm = { viewModel.downProduct() }
        )
    }
}

@Composable
fun ShowDownAlert(
    nombre: String,
    onDismissDialog: () -> Unit,
    onConfirm: () -> Unit
){
    ShowAlert(
        onDismissRequest = onDismissDialog,
        text = nombre,
        title = "Dar de baja producto",
        onConfirmButtonClick = onConfirm
    )
}

@Composable
fun InventarioCard(
    producto: Producto,
    onEdit: (Producto) -> Unit,
    onDelete: (Producto) -> Unit
) {
    val productoData = remember(producto) {
        listOf(
            "ID" to producto.idProducto.toString(),
            "Nombre" to producto.nombre,
            "Precio venta" to "$${producto.precioVenta}",
            "Disponibles" to producto.disponibles.toString(),
            "Marca" to producto.marca,
            "Tipo" to producto.tipo,
            "Estado" to when(producto.estado) {
                1 -> "Activo"
                2 -> "Inactivo"
                else -> "Desconocido"
            }
        )
    }
    InfoCard(
        title = producto.nombre,
        dataList = productoData,
        onEditClick = { onEdit(producto) },
        onDeleteClick = { onDelete(producto) },
        modifier = Modifier.padding(4.dp)
    )
}
