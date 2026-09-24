package com.ambienttv.photosambient.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ambienttv.photosambient.ui.theme.GoogleBlue
import com.ambienttv.photosambient.ui.theme.SurfaceCard
import com.ambienttv.photosambient.ui.theme.TextPrimary
import com.ambienttv.photosambient.ui.theme.TextSecondary
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun QrCodeCard(
    userCode: String,
    verificationUrl: String,
    qrPayload: String = verificationUrl,
    modifier: Modifier = Modifier,
    qrSize: Dp = 220.dp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // High-contrast QR Code frame
            Box(
                modifier = Modifier
                    .size(qrSize)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                QrCodeImage(payload = qrPayload, modifier = Modifier.size(qrSize - 28.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Or enter code at:",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Text(
                text = verificationUrl,
                color = GoogleBlue,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Code highlight badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF13171F))
                    .border(1.dp, Color(0xFF4285F4).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = userCode,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp
                )
            }
        }
    }
}

/**
 * Renders a crisp procedural QR Code matrix representation with standard finder patterns
 */
@Composable
fun ProceduralQrMatrix(
    modifier: Modifier = Modifier,
    matrixColor: Color = Color(0xFF1A1A1A)
) {
    Canvas(modifier = modifier) {
        val size = size.width
        val moduleCount = 25
        val moduleSize = size / moduleCount

        // Helper to draw a module block
        fun drawBlock(col: Int, row: Int) {
            drawRect(
                color = matrixColor,
                topLeft = Offset(col * moduleSize, row * moduleSize),
                size = Size(moduleSize, moduleSize)
            )
        }

        // Helper to draw Finder Pattern (7x7 outer square, 3x3 inner square)
        fun drawFinder(startX: Int, startY: Int) {
            // Outer 7x7
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    if (r == 0 || r == 6 || c == 0 || c == 6) {
                        drawBlock(startX + c, startY + r)
                    }
                }
            }
            // Inner 3x3
            for (r in 2..4) {
                for (c in 2..4) {
                    drawBlock(startX + c, startY + r)
                }
            }
        }

        // Top-left finder
        drawFinder(0, 0)
        // Top-right finder
        drawFinder(18, 0)
        // Bottom-left finder
        drawFinder(0, 18)

        // Timing patterns
        for (i in 8..16) {
            if (i % 2 == 0) {
                drawBlock(6, i)
                drawBlock(i, 6)
            }
        }

        // Deterministic mock data cells representing encoded Ambient API setup payload
        val dataPoints = listOf(
            Pair(8, 2), Pair(9, 2), Pair(11, 2), Pair(13, 2), Pair(15, 2),
            Pair(8, 4), Pair(10, 4), Pair(12, 4), Pair(14, 4), Pair(16, 4),
            Pair(1, 8), Pair(3, 8), Pair(4, 8), Pair(9, 8), Pair(11, 8), Pair(14, 8), Pair(18, 8), Pair(20, 8), Pair(23, 8),
            Pair(0, 9), Pair(2, 9), Pair(5, 9), Pair(8, 9), Pair(12, 9), Pair(15, 9), Pair(19, 9), Pair(21, 9), Pair(24, 9),
            Pair(1, 10), Pair(3, 10), Pair(7, 10), Pair(10, 10), Pair(13, 10), Pair(17, 10), Pair(20, 10), Pair(22, 10),
            Pair(8, 11), Pair(11, 11), Pair(14, 11), Pair(16, 11), Pair(19, 11), Pair(22, 11),
            Pair(0, 12), Pair(4, 12), Pair(7, 12), Pair(9, 12), Pair(12, 12), Pair(15, 12), Pair(18, 12), Pair(21, 12),
            Pair(2, 13), Pair(5, 13), Pair(8, 13), Pair(10, 13), Pair(13, 13), Pair(17, 13), Pair(23, 13),
            Pair(1, 14), Pair(4, 14), Pair(9, 14), Pair(11, 14), Pair(14, 14), Pair(16, 14), Pair(20, 14),
            Pair(3, 15), Pair(7, 15), Pair(10, 15), Pair(12, 15), Pair(15, 15), Pair(18, 15), Pair(22, 15),
            Pair(8, 16), Pair(13, 16), Pair(17, 16), Pair(21, 16), Pair(24, 16),
            Pair(8, 18), Pair(10, 18), Pair(12, 18), Pair(14, 18), Pair(17, 18), Pair(19, 18), Pair(22, 18),
            Pair(9, 19), Pair(11, 19), Pair(13, 19), Pair(16, 19), Pair(18, 19), Pair(21, 19), Pair(23, 19),
            Pair(8, 20), Pair(10, 20), Pair(12, 20), Pair(15, 20), Pair(17, 20), Pair(20, 20), Pair(24, 20),
            Pair(9, 21), Pair(11, 21), Pair(14, 21), Pair(16, 21), Pair(19, 21), Pair(22, 21),
            Pair(8, 22), Pair(12, 22), Pair(15, 22), Pair(18, 22), Pair(20, 22), Pair(23, 22),
            Pair(10, 23), Pair(13, 23), Pair(16, 23), Pair(19, 23), Pair(21, 23), Pair(24, 23),
            Pair(9, 24), Pair(11, 24), Pair(14, 24), Pair(17, 24), Pair(20, 24), Pair(22, 24)
        )
        dataPoints.forEach { (col, row) ->
            drawBlock(col, row)
        }
    }
}

@Composable
fun QrCodeImage(payload: String, modifier: Modifier = Modifier) {
    val bitmap = remember(payload) {
        if (payload.isBlank()) null else {
            val matrix = QRCodeWriter().encode(
                payload,
                BarcodeFormat.QR_CODE,
                512,
                512,
                mapOf(EncodeHintType.MARGIN to 1)
            )
            val pixels = IntArray(512 * 512) { index ->
                if (matrix[index % 512, index / 512]) android.graphics.Color.BLACK
                else android.graphics.Color.WHITE
            }
            Bitmap.createBitmap(pixels, 512, 512, Bitmap.Config.ARGB_8888).asImageBitmap()
        }
    }
    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = "Scan setup link", modifier = modifier)
    }
}
