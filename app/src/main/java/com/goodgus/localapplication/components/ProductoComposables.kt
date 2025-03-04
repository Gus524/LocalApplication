package com.goodgus.localapplication.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.goodgus.localapplication.models.data.Producto
import com.goodgus.localapplication.models.data.Venta
import com.goodgus.localapplication.utilidades.formatPrecio
import com.goodgus.localapplication.utilidades.parsePrecio

/**
 * Composables utilizados en toda la aplicacion
 * TODO separarlos de tal manera que esten por clase cada componente o por clase dependiendo de los componentes que se usen juntos (por ejemplo Info Card y los botones del Card)
 */

/**
 * Composable para mostrar una carta reutilizable
 *
 * @param title Titulo que tendra la tarjeta
 * @param dataList Lista de tipo String, String los cuales se mostraran en el cuerpo de la tarjeta
 * @param onEditClick Evento que se ejecutara al dar click en el boton de editar
 * @param onDeleteClick Evento que se ejecutara al dar click en el boton de borrar
 * @param modifier Se pasa el modifier para hacerlo mas composable
 */

@Composable
fun InfoCard(
    title: String,
    dataList: List<Pair<String, String>>,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier
) {
    Card(modifier = modifier) {
        Row (
            Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(8.dp).weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                dataList.forEach { data ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${data.first}: ",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = data.second,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.End
            ) {
                ProductButton(
                    onClick = onEditClick,
                    icon = Icons.Filled.Edit,
                    contentDescription = "Editar",
                    iconColor = Color(0xFF1C45BD),
                    containerColor = Color.Transparent
                )
                ProductButton(
                    onClick = onDeleteClick,
                    icon = Icons.Filled.Delete,
                    contentDescription = "Eliminar",
                    iconColor = Color(0xFF8F0303),
                    containerColor = Color.Transparent
                )
            }
        }
    }
}

/**
 * Composable de una barra de busqueda
 *
 * @param onTextChange Evento para mostrar el cambio en el texto
 * @param resultados Lista de productos que coincidan con la busqueda
 * @param isLoading Animacion a mostrar cuando se esten cargando los resultados (tal vez no este funcionando)
 * @param onProductSelect Evento que se ejecuta al dar click a uno de los resultados
 * @param isLazyVisible determina si la lista se muestra o no
 *
 * TODO modificar el composable para hacerlo mas generico por ejemplo para Ventas o para pedidos
 */

@Composable
fun SearchProduct(
    search: String,
    onTextChange: (String) -> Unit,
    resultados: List<Producto>,
    isLoading: Boolean,
    onProductSelect: (Producto) -> Unit,
    isLazyVisible: Boolean
) {
    Column {
        OutlinedTextField(
            value = search,
            onValueChange = onTextChange,
            label = { Text("Buscar producto") },
            modifier = Modifier.fillMaxWidth()
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        if(isLazyVisible) {
            LazyColumn {
                items(resultados) { producto ->
                    ProductItem(
                        producto = producto,
                        onClick = { onProductSelect(producto) }
                    )
                }
            }
        }
    }
}

/**
 * Composable para mostrar los productos del Lazy del buscador
 *
 * @param producto un objeto de tipo Producto
 * @param Evento que ocurre al dar click en el producto
 */

@Composable
fun ProductItem(
    producto: Producto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ){
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = producto.nombre, style = MaterialTheme.typography.labelSmall)
            Text(text = "Precio venta: $${producto.precioVenta}", style = MaterialTheme.typography.labelSmall)
        }
    }
}

/**
 * Composable para mostrar un titulo reutilizable
 *
 * @param nombre Texto a mostrar
 */

@Composable
fun CampoTitle(nombre: String) {
    OutlinedTextField(
        value = nombre,
        onValueChange = {},
        label = { "Nombre" },
        readOnly = true,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Composable para los inputs que deben mostrar cantidades de producto
 *
 * @param cantidad la cantidad a mostrar
 * @param onCantidadChange Evento que ocurre al cambiar la cantidad del producto
 */

@Composable
fun CampoCantidad(
    cantidad: Int,
    onCantidadChange: (Int) -> Unit
) {
    OutlinedTextField(
        value = cantidad.toString(),
        onValueChange = {
            onCantidadChange(it.toIntOrNull() ?: 0)
        },
        label = { Text("Cantidad") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Composable para los input que muestren precios
 *
 * @param precio Precio a mostrar
 * @param onPrecioChange Cuando se puede modificar es el evento que muestra el cambio
 * @param readOnly determina si el campo es modificable o no
 * @param label muestra el label a mostrar por defualt es "Precio"
 */

@Composable
fun CampoPrecio(
    precio: Double,
    onPrecioChange: (Double) -> Unit,
    readOnly: Boolean = false,
    label: String = "Precio"
) {
    var textValue by remember { mutableStateOf(formatPrecio(precio)) }

    // Efecto para sincronizar el valor externo (precio) con el estado interno (textValue)
    LaunchedEffect(precio) {
        textValue = formatPrecio(precio)
    }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            // Validar que el nuevo valor sea un número válido o un separador decimal
            if (newValue.matches(Regex("^\\d*([.,]\\d{0,2})?$"))) {
                textValue = newValue
                // Convertir a Double y notificar el cambio
                onPrecioChange(parsePrecio(newValue))
            }
        },
        label = { Text(label) },
        readOnly = readOnly,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Composable para mostrar botones en cualqueir composable, como un alert
 *
 * @param onGuardar Evento a ejecutar cuando se da click en guardar
 * @param onCancelar Evento a ejecutar cuando se da click en cancelar
 *
 */


@Composable
fun BotonesAcciones(
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = onCancelar,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        ) {
            Text("Cancelar")
        }
        Button(
            onClick = onGuardar,
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        ) {
            Text("Guardar")
        }
    }
}

/**
 * Composable para mostrar un boton personalizable
 *
 * @param onClick Evento cuando se de click al boton
 * @param icon Icono para mostrar en el boton
 * @param contentDescription Descripcion del IconButton
 * @param iconColor Color que tendra el icono
 * @param containerColor Color que tendra el contenido del icono
 */

@Composable
fun ProductButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    iconColor: Color,
    containerColor: Color
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = iconColor
        ),
    ) {
        Icon(icon, contentDescription = contentDescription)
    }
}

/**
 * Composable para campo de texto generico
 */

@Composable
fun CampoText(
    text: String,
    onTextChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = text,
        onValueChange = { onTextChange(it) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Composable para mostrar un Alert generico personalizable
 */

@Composable
fun ShowAlert(
    onDismissRequest: () -> Unit,
    text: String,
    title: String,
    confirmButtonText: String = "Aceptar",
    onConfirmButtonClick: () -> Unit
){
    AlertDialog (
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = onConfirmButtonClick
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismissRequest
            ) {
                Text("Cancelar")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}