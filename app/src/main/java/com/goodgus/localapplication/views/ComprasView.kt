package com.goodgus.localapplication.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.goodgus.localapplication.components.InfoCard
import com.goodgus.localapplication.models.data.Compra
import com.goodgus.localapplication.viewModels.CompraViewModel

@Composable
fun CompraScreen(
    navigateToDetalle: (String) -> Unit,
    viewModel: CompraViewModel
){
    val uiState by viewModel.uiState.collectAsState()

    if(uiState.compras.isNotEmpty()){
        ShowCompras(
            compras = uiState.compras,
            navigateToDetalle = navigateToDetalle,
            uiState = uiState,
            viewModel = viewModel
        )
    } else {
        Button(
            onClick = { navigateToDetalle("") },
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .padding(16.dp)
                .size(1.dp)
        ) {
            Text("Agregar una compra")
        }
    }
}

@Composable
fun ShowCompras(
    compras: List<Compra>,
    navigateToDetalle: (String) -> Unit,
    uiState: CompraViewModel.CompraUIState,
    viewModel: CompraViewModel
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
        ){
            items(compras) { compra ->
                CompraCard(
                    compra = compra,
                    onDetails = { navigateToDetalle(compra.idCompra.toString()) },
                )
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
                onClick = { navigateToDetalle("") },
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add compra"
                )
            }
        }
    }
}

@Composable
fun CompraCard(
    compra: Compra,
    onDetails: (Compra) -> Unit
) {
    val compraData = listOf(
        "ID" to compra.idCompra.toString(),
        "Total" to compra.totalCompra.toString(),
        "Fecha" to compra.fechaCompra.toString()
    )
    InfoCard(
        title = "Compra",
        dataList = compraData,
        onEditClick = { onDetails(compra) },
        onDeleteClick = {},
        modifier = Modifier.padding(4.dp)
    )
}