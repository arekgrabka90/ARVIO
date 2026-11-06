package com.arflix.tv.ui.screens.player.mobile

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp

/**
 * Visual design tokens for ARVIO Mobile Player matching the HTML prototype.
 */
object MobilePlayerTokens {
    val Accent = Color(0xFFB99A68)
    val InkPrimary = Color(0xFFE8EBF0)
    val InkSecondary = Color(0xC2E8EBF0)
    val InkTertiary = Color(0x78E8EBF0)

    val TrackBg = Color(0x35E8EBF0)
    val TrackBuffered = Color(0x70E8EBF0)
    val TrackFill = Color(0xFFB99A68)

    val PageBg = Color(0xFF090B0F)
    val PanelBg = Color(0xFF171B22)
    val PanelBg2 = Color(0xFF1B2029)
    val PanelBorder = Color(0x35B99A68)
    val RowHover = Color(0x14B99A68)

    val ScrimColor = Color(0x40000000)     // 25%

    val ShapeScreen = RoundedCornerShape(16.dp)
    val ShapeDrawer = RoundedCornerShape(0.dp)
    val ShapeSheet = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
    val ShapePrompt = RoundedCornerShape(12.dp)
    val ShapeCard = RoundedCornerShape(12.dp)
    val ShapeThumb = RoundedCornerShape(8.dp)
    val ShapeBtn = RoundedCornerShape(8.dp)
    val ShapePill = RoundedCornerShape(16.dp)

    val TextShadow = Shadow(
        color = Color(0x99000000),
        offset = Offset(0f, 2f),
        blurRadius = 8f
    )
}
