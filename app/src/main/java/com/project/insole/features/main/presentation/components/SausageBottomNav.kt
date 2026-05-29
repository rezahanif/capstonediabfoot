package com.project.insole.features.main.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.R
import com.project.insole.core.theme.DashboardColors
import kotlin.math.abs

// ─── Design tokens ────────────────────────────────────────────────────────────
private val ColorNavBlue   = Color(0xFF114784)
private val ColorNavBg      = Color.White
private val ColorPageBg     = DashboardColors.Background

private val TotalHeight    = 86.dp    
private val BarHeight      = 58.dp    
private val PillHeight     = 56.dp    
private val PillWidth      = 150.dp   
private val CircleSize     = 50.dp    
private val CircleTop      = 12.dp    
private val PillTopOffset  = 9.dp     
private val ScallopRadius  = 28.dp    

data class NavItem(
    @DrawableRes val icon: Int,
    val label: String,
)

@Composable
fun SausageBottomNav(
    items: List<NavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedActiveIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "NavIndexAnimation"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(TotalHeight)
            .background(ColorPageBg),
    ) {
        val totalWidthDp  = maxWidth
        val itemWidthDp   = totalWidthDp / items.size   

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawScallopedNavBackground(
                        itemCount         = items.size,
                        animatedIndex     = animatedActiveIndex,
                        itemWidthPx       = itemWidthDp.toPx(),
                        totalHeight       = TotalHeight.toPx(),
                        barHeight         = BarHeight.toPx(),
                        pillHeight        = PillHeight.toPx(),
                        pillWidth         = PillWidth.toPx(),
                        pillTopPx         = PillTopOffset.toPx(),
                        circleSize        = CircleSize.toPx(),
                        circleTopPx       = CircleTop.toPx(),
                        scallop           = ScallopRadius.toPx(),
                        bgColor           = ColorNavBg,
                    )
                }
        ) {
            items.forEachIndexed { index, item ->
                val isActive = index == selectedIndex
                val slotCenterX = itemWidthDp * index + itemWidthDp / 2

                if (isActive) {
                    Row(
                        modifier = Modifier
                            .offset(
                                x = slotCenterX - PillWidth / 2,
                                y = PillTopOffset,
                            )
                            .size(width = PillWidth, height = PillHeight)
                            .clip(RoundedCornerShape(50.dp))
                            .background(ColorNavBlue)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { onItemSelected(index) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.label,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = item.label.uppercase(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp,
                            maxLines = 1,
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .offset(
                                x = slotCenterX - CircleSize / 2,
                                y = CircleTop,
                            )
                            .size(CircleSize)
                            .clip(CircleShape)
                            .background(ColorNavBlue)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { onItemSelected(index) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.label,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawScallopedNavBackground(
    itemCount:   Int,
    animatedIndex: Float,
    itemWidthPx: Float,
    totalHeight: Float,
    barHeight:   Float,
    pillHeight:  Float,
    pillWidth:   Float,
    pillTopPx:   Float,
    circleSize:  Float,
    circleTopPx: Float,
    scallop:     Float,
    bgColor:     Color,
) {
    val w           = size.width
    val barTopY     = totalHeight - barHeight
    val cornerR     = 28f   
    val pillR       = pillHeight / 2f
    val circleR     = circleSize / 2f
    val scallopR    = scallop + circleR   

    val path = Path()
    path.moveTo(0f, totalHeight)
    path.lineTo(w, totalHeight)
    path.lineTo(w, barTopY + cornerR)

    path.arcTo(
        rect = Rect(w - cornerR * 2, barTopY, w, barTopY + cornerR * 2),
        startAngleDegrees = 0f,
        sweepAngleDegrees = -90f,
        forceMoveTo = false,
    )

    val currentPillCenterX = itemWidthPx * animatedIndex + itemWidthPx / 2f
    var cursorX = w - cornerR

    for (i in (itemCount - 1) downTo 0) {
        val slotCenterX = itemWidthPx * i + itemWidthPx / 2f
        val distanceToPill = abs(slotCenterX - currentPillCenterX)

        if (distanceToPill < itemWidthPx / 2f) {
            val pillLeft  = currentPillCenterX - pillWidth / 2f
            val pillRight = currentPillCenterX + pillWidth / 2f
            val pillTop   = pillTopPx

            val transitionX = pillRight + pillR
            if (cursorX > transitionX) path.lineTo(transitionX, barTopY)

            path.arcTo(
                rect = Rect(pillRight - pillR, pillTop, pillRight + pillR, pillTop + pillHeight),
                startAngleDegrees = 90f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false,
            )
            path.lineTo(pillLeft, pillTop)
            path.arcTo(
                rect = Rect(pillLeft - pillR, pillTop, pillLeft + pillR, pillTop + pillHeight),
                startAngleDegrees = 0f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false,
            )
            cursorX = pillLeft - pillR
        } else {
            val scallopAlpha = (distanceToPill / itemWidthPx).coerceIn(0f, 1f)
            val currentScallopR = scallopR * scallopAlpha

            val rx = slotCenterX + currentScallopR
            if (cursorX > rx) path.lineTo(rx, barTopY)

            if (currentScallopR > 0f) {
                path.arcTo(
                    rect = Rect(slotCenterX - currentScallopR, barTopY - currentScallopR, slotCenterX + currentScallopR, barTopY + currentScallopR),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = -180f,
                    forceMoveTo = false,
                )
            }
            cursorX = slotCenterX - currentScallopR
        }
    }

    path.lineTo(cornerR, barTopY)
    path.arcTo(
        rect = Rect(0f, barTopY, cornerR * 2, barTopY + cornerR * 2),
        startAngleDegrees = 270f,
        sweepAngleDegrees = -90f,
        forceMoveTo = false,
    )

    path.lineTo(0f, totalHeight)
    path.close()

    drawPath(path = path, color = bgColor)
}
