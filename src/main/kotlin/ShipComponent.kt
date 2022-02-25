import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun Ship(shipData: ShipData) {
    val shipSize = shipData.size.dp
    Box(
        Modifier
            .offset(shipData.xOffset, shipData.yOffset)
            .size(shipSize)
            .rotate(shipData.visualAngle.toFloat())
            //.clip(CircleShape)
            .background(Color.Transparent)
    ) {
        Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
            drawPath(
                color = Color.Transparent,
                path = Path().apply {
                    val size = shipSize.toPx()
                    moveTo(0f, 0f) // Top-left corner...
                    lineTo(size, size / 2f) // ...to right-center...
                    lineTo(0f, size) // ... to bottom-left corner.
                }
            )
        })
        val imageModifier = Modifier
        Image(
            bitmap = imageFromResource("nave.png"),
            "image",
            imageModifier,
            contentScale = ContentScale.Fit
        )
    }
}