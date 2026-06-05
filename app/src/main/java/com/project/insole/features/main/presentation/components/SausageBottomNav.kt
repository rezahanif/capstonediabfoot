package com.project.insole.features.main.presentation.components



import androidx.annotation.DrawableRes

import androidx.compose.animation.core.Spring

import androidx.compose.animation.core.animateDpAsState

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

import androidx.compose.ui.draw.shadow

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.res.painterResource

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import com.project.insole.core.theme.DashboardColors



// ─── Refined Token System ───────────────────────────────────────────────────

private val ColorNavBlue = Color(0xFF1F4784)

private val ColorNavBg = Color.White

private val ColorPageBg = DashboardColors.Background



private val NavContainerHeight = 100.dp // Extra space to prevent shadow clipping

private val ActivePillWidth = 160.dp

private val ActivePillHeight = 54.dp

private val InactiveCircleSize = 54.dp



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

    Box(

        modifier = modifier

            .fillMaxWidth()

            .height(NavContainerHeight)

            .background(ColorPageBg),

        contentAlignment = Alignment.BottomCenter

    ) {

// White cloud background panel with soft shadow boundary

        Row(

            modifier = Modifier

                .fillMaxWidth()

                .height(76.dp)

                .shadow(

                    elevation = 8.dp,

                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),

                    ambientColor = Color.Black.copy(alpha = 0.05f),

                    spotColor = Color.Black.copy(alpha = 0.1f)

                )

                .background(ColorNavBg),

            horizontalArrangement = Arrangement.SpaceAround,

            verticalAlignment = Alignment.CenterVertically

        ) {

// Empty space slots ensuring the items line up perfectly over the background

            items.forEachIndexed { index, _ ->

                val isActive = index == selectedIndex

                Box(

                    modifier = Modifier.width(if (isActive) ActivePillWidth else InactiveCircleSize),

                    contentAlignment = Alignment.Center

                ) {}

            }

        }



// Animated interactive element layer

        Row(

            modifier = Modifier

                .fillMaxWidth()

                .padding(bottom = 12.dp) // Aligns buttons cleanly within the cloud base

                .padding(horizontal = 16.dp),

            horizontalArrangement = Arrangement.SpaceAround,

            verticalAlignment = Alignment.CenterVertically

        ) {

            items.forEachIndexed { index, item ->

                val isActive = index == selectedIndex



// Calculate spring value normally

                val springPadding by animateDpAsState(

                    targetValue = if (isActive) 0.dp else 4.dp,

                    animationSpec = spring(

                        dampingRatio = Spring.DampingRatioLowBouncy,

                        stiffness = Spring.StiffnessMediumLow

                    ),

                    label = "NavPaddingAnimation"

                )



// CRITICAL FIX: Clamp the value so it can never drop below 0.dp during bounces

                val safeAnimatedVerticalPadding = remember(springPadding) {

                    if (springPadding < 0.dp) 0.dp else springPadding

                }



                Box(

                    modifier = Modifier

                        .padding(bottom = safeAnimatedVerticalPadding)

                        .height(ActivePillHeight),

                    contentAlignment = Alignment.Center

                ) {

                    if (isActive) {

// Selected Expandable Pill View Component

                        Row(

                            modifier = Modifier

                                .width(ActivePillWidth)

                                .fillMaxHeight()

                                .shadow(4.dp, RoundedCornerShape(50.dp))

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

                                fontSize = 11.sp,

                                fontWeight = FontWeight.Bold,

                                letterSpacing = 2.sp,

                                maxLines = 1,

                                )

                        }

                    } else {

// Unselected Uniform Circular Icon Buttons

                        Box(

                            modifier = Modifier

                                .size(InactiveCircleSize)

                                .shadow(2.dp, CircleShape)

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

                                modifier = Modifier.size(22.dp),

                                )

                        }

                    }

                }

            }

        }

    }

}