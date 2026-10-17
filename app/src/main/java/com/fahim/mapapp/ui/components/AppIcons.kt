package com.fahim.mapapp.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Material symbols that are not in material-icons-core. Defined inline so the app
 * doesn't pull in the multi-megabyte material-icons-extended artifact.
 */
private fun materialSymbol(name: String, vararg paths: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        paths.forEach { addPath(pathData = addPathNodes(it), fill = SolidColor(Color.Black)) }
    }.build()

val MinusIcon: ImageVector by lazy {
    materialSymbol("Remove", "M19,13H5v-2h14v2z")
}

val MyLocationIcon: ImageVector by lazy {
    materialSymbol(
        "MyLocation",
        "M12,8c-2.21,0 -4,1.79 -4,4s1.79,4 4,4 4,-1.79 4,-4 -1.79,-4 -4,-4zM20.94,11c-0.46,-4.17 -3.77,-7.48 -7.94,-7.94L13,1h-2v2.06C6.83,3.52 3.52,6.83 3.06,11L1,11v2h2.06c0.46,4.17 3.77,7.48 7.94,7.94L11,23h2v-2.06c4.17,-0.46 7.48,-3.77 7.94,-7.94L23,13v-2h-2.06zM12,19c-3.87,0 -7,-3.13 -7,-7s3.13,-7 7,-7 7,3.13 7,7 -3.13,7 -7,7z"
    )
}

val DevicesIcon: ImageVector by lazy {
    materialSymbol(
        "Devices",
        "M4,6h18L22,4L4,4c-1.1,0 -2,0.9 -2,2v11L0,17v3h14v-3L4,17L4,6zM23,8h-6c-0.55,0 -1,0.45 -1,1v10c0,0.55 0.45,1 1,1h6c0.55,0 1,-0.45 1,-1L24,9c0,-0.55 -0.45,-1 -1,-1zM22,17h-4v-7h4v7z"
    )
}

val SmartphoneIcon: ImageVector by lazy {
    materialSymbol(
        "Smartphone",
        "M17,1.01L7,1c-1.1,0 -1.99,0.9 -1.99,2v18c0,1.1 0.89,2 1.99,2h10c1.1,0 2,-0.9 2,-2L19,3c0,-1.1 -0.9,-1.99 -2,-1.99zM17,19L7,19L7,5h10v14z"
    )
}

val StopCircleIcon: ImageVector by lazy {
    materialSymbol(
        "StopCircle",
        "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM16,16H8V8h8v8z"
    )
}

val MapIcon: ImageVector by lazy {
    materialSymbol(
        "Map",
        "M20.5,3l-0.16,0.03L15,5.1 9,3 3.36,4.9c-0.21,0.07 -0.36,0.25 -0.36,0.48V20.5c0,0.28 0.22,0.5 0.5,0.5l0.16,-0.03L9,18.9l6,2.1 5.64,-1.9c0.21,-0.07 0.36,-0.25 0.36,-0.48V3.5c0,-0.28 -0.22,-0.5 -0.5,-0.5zM15,19l-6,-2.11V5l6,2.11V19z"
    )
}

val OpenInNewIcon: ImageVector by lazy {
    materialSymbol(
        "OpenInNew",
        "M19,19H5V5h7V3H5c-1.11,0 -2,0.9 -2,2v14c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2v-7h-2v7zM14,3v2h3.59l-9.83,9.83 1.41,1.41L19,6.41V10h2V3h-7z"
    )
}
