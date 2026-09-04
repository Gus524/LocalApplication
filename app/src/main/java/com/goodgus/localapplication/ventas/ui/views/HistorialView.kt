package com.goodgus.localapplication.ventas.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goodgus.localapplication.core.theme.LocalApplicationTheme
import com.goodgus.localapplication.core.ui.components.chips.BadgeEstado
import com.goodgus.localapplication.core.ui.components.chips.TipoEstadoSemantico
import com.goodgus.localapplication.core.ui.components.dialogs.DialogoAlerta
import com.goodgus.localapplication.core.ui.components.feedback.EstadoVacio
import com.goodgus.localapplication.shared.utilidades.FechaUtils
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.model.EstadoCuenta
import com.goodgus.localapplication.ventas.domain.model.InformacionCuenta
import com.goodgus.localapplication.ventas.ui.viewModels.HistorialAction
import com.goodgus.localapplication.ventas.ui.viewModels.HistorialEffect
import com.goodgus.localapplication.ventas.ui.viewModels.HistorialUiState
import com.goodgus.localapplication.ventas.ui.viewModels.HistorialViewModel
import java.util.Locale

@Composable
fun HistorialScreen(
    navigateToDetalle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistorialViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HistorialEffect.NavegarADetalleCuenta -> navigateToDetalle(effect.idCuenta)
            }
        }
    }

    HistorialContent(
        state = uiState,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@Composable
fun HistorialContent(
    state: HistorialUiState,
    onAction: (HistorialAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Historial de Cuentas",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (state.cuentas.isEmpty() && !state.cargando) {
            EstadoVacio(
                icono = Icons.AutoMirrored.Filled.ReceiptLong,
                titulo = "No hay cuentas cerradas",
                mensaje = "Cuando realices el corte y cierre de una cuenta activa, aquí aparecerá archivado su desglose completo."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.cuentas, key = { it.id.valor }) { cuenta ->
                    CuentaHistoricoItemCard(
                        cuenta = cuenta,
                        onClick = { onAction(HistorialAction.OnSeleccionarCuenta(cuenta.id.valor)) }
                    )
                }
            }
        }
    }

    state.mensajeAlerta?.let { mensaje ->
        DialogoAlerta(
            mensaje = mensaje,
            onAceptar = { onAction(HistorialAction.OnDismissAlerta) }
        )
    }
}

@Composable
fun CuentaHistoricoItemCard(
    cuenta: Cuenta,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Corte de Cuenta #${cuenta.id.valor}",
                    style = MaterialTheme.typography.titleMedium
                )
                BadgeEstado(
                    texto = "Cerrada",
                    tipo = TipoEstadoSemantico.NEUTRO
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fecha: ${FechaUtils.formatearAUi(cuenta.informacion.fecha)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$ ${String.format(Locale.US, "%.2f", cuenta.totalCalculado.monto)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${cuenta.ventas.count { it.estaActiva }} ventas registradas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun HistorialContentPreview() {
    LocalApplicationTheme {
        HistorialContent(
            state = HistorialUiState(
                cuentas = listOf(
                    Cuenta(
                        id = CuentaId(1),
                        informacion = InformacionCuenta("2026-09-02", EstadoCuenta.CERRADA)
                    )
                )
            ),
            onAction = {}
        )
    }
}
