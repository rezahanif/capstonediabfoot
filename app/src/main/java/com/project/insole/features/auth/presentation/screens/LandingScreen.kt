package com.project.insole.features.auth.presentation.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.R
import com.project.insole.core.theme.AuthColors

/**
 * Landing screen — redesigned from the Figma CAPSTONE node 19-4569.
 *
 * Layout (top → bottom):
 *  • Full-screen gradient background
 *  • "SmartInsole" headline
 *  • "Smarter Steps / Better You" subtitle
 *  • Centred hero product image
 *  • Two pill buttons at the bottom: SIGN IN  |  SIGN UP
 */
@Composable
fun LandingScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to AuthColors.GradientTop,
                        1.0f to AuthColors.GradientBottom,
                    )
                )
            )
    ) {

        // ── Header text ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 24.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = "SmartInsole",
                color = AuthColors.TextLight,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Smarter Steps\nBetter You",
                color = AuthColors.TextLight,
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 1.sp,
                textAlign = TextAlign.End,
                lineHeight = 30.sp,
            )
        }

        // ── Hero product image (centred, fills most of the screen) ───────────
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(top = 60.dp, bottom = 120.dp)
        ) {
            val width = maxWidth
            val height = maxHeight

            // ── Outer Decorative Circle (Largest) ──────────────────────────
            Box(
                modifier = Modifier
                    .size(420.dp)
                    .offset(x = -100.dp, y = -10.dp)
                    .align(Alignment.CenterStart)
                    .border(width = 2.dp, color = Color.White.copy(alpha = 0.10f), shape = CircleShape)
            )

            // ── Inner Decorative Circle ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(x = -50.dp, y = -10.dp)
                    .align(Alignment.CenterStart)
                    .border(width = 2.dp, color = Color.White.copy(alpha = 0.15f), shape = CircleShape)
            )

            Image(
                painter = painterResource(id = R.drawable.insole_hero),
                contentDescription = "Smart Insole product",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .offset(x= -50.dp)
                    .align(Alignment.CenterStart)
            )

            //-- small decorative circles (positioned via precise percentage)
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .offset(x = width * 0.65f, y = height * 0.12f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF114797), Color(0xFF174F9F))
                        ),
                        shape = CircleShape
                    )
                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ){
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.connect), // 💡 Swap with your user or profile avatar asset
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(28.dp)
                    , // Clips the image to fit the circle perfectly
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }
            
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .offset(x = width * 0.67f, y = height * 0.35f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF114797), Color(0xFF174F9F))
                        ),
                        shape = CircleShape
                    )
                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ){
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.foot), // 💡 Swap with your user or profile avatar asset
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(28.dp)
                    , // Clips the image to fit the circle perfectly
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .offset(x = width * 0.41f, y = height * -0.03f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF114797), Color(0xFF174F9F))
                        ),
                        shape = CircleShape
                    )
                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ){
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.stat), // 💡 Swap with your user or profile avatar asset
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(24.dp)
                        , // Clips the image to fit the circle perfectly
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }
        }

        // ── Bottom CTA buttons ───────────────────────────────────────────────
        // ── Bottom CTA buttons ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 23.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(-10.dp), // Keeps your intentional overlapping layout design
        ) {
            // SIGN IN (Added your login/arrow icon resource here)
            PillButton(
                label = "SIGN IN",
                icon = R.drawable.sign_in, // 💡 Change this to match your actual drawable resource filename
                modifier = Modifier.weight(1f),
                onClick = onNavigateToLogin,
            )

            // SIGN UP (Added your add/person icon resource here)
            PillButton(
                label = "SIGN UP",
                icon = R.drawable.sign_up, // 💡 Change this to match your actual drawable resource filename
                modifier = Modifier.weight(1f),
                onClick = onNavigateToSignUp,
            )
        }
    }
}

/**
 * Reusable pill-shaped CTA button with Icon Support.
 */
@Composable
private fun PillButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null, // Restored optional icon token parameter
) {
    Box(
        modifier = modifier
            .height(79.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 13.dp)
                .height(56.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AuthColors.ButtonBg,
                contentColor = Color.White,
            ),
            contentPadding = PaddingValues(horizontal = 16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 2.dp
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                // If an icon asset is passed down, draw it cleanly alongside the text spacer boundary
                if (icon != null) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null, // Decorative icon
                        modifier = Modifier.size(25.dp)
                    )
                }


            }
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LandingScreenPreview() {
    LandingScreen(
        onNavigateToLogin = {},
        onNavigateToSignUp = {},
    )
}
