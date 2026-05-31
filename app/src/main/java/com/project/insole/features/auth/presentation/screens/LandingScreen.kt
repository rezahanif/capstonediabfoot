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

// ─── Design Tokens (from Figma) ───────────────────────────────────────────────
private val ColorGradientTop   = Color(0xFF114797)   // #114797  (top / center)
private val ColorGradientBottom = Color(0xFF112B4E)  // #112B4E  (bottom)
private val ColorButtonBg      = Color(0xFF114784)   // #114784  (pill background)
private val ColorButtonPill    = Color(0xFFFFFFFF)   // white pill container
private val ColorTextLight     = Color(0xFFEBF1FF)   // #EBF1FF  (headline + subtitle)

/**
 * Landing screen — redesigned from the Figma CAPSTONE node 19-4569.
 *
 * Layout (top → bottom):
 *  • Full-screen gradient background  (#114797 → #112B4E)
 *  • "SmartInsole" headline  (top-right area)
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
                        0.0f to ColorGradientTop,
                        1.0f to ColorGradientBottom,
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
                color = ColorTextLight,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Smarter Steps\nBetter You",
                color = ColorTextLight,
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 1.sp,
                textAlign = TextAlign.End,
                lineHeight = 30.sp,
            )
        }

        // ── Hero product image (centred, fills most of the screen) ───────────
        // Replace R.drawable.insole_hero with the actual drawable resource name
        // in your project.  The image should be the "two white insoles" asset
        // exported from the Figma file.
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
                    .offset(x = width * 0.67f, y = height * 0.165f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF114797), Color(0xFF174F9F))
                        ),
                        shape = CircleShape
                    )
                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center // Automatically aligns the icon perfectly in the middle
            ) {
                Image(
                    painter = painterResource(id = R.drawable.connect),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp) // Keeps it smaller than the 50.dp container
                )
            }
            
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .offset(x = width * 0.65f, y = height * 0.42f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF114797), Color(0xFF174F9F))
                        ),
                        shape = CircleShape
                    )
                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ){
                Image(
                    painter = painterResource(id = R.drawable.foot),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp) // Keeps it smaller than the 50.dp container
                )
            }

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .offset(x = width * 0.38f, y = height * -0.014f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF114797), Color(0xFF174F9F))
                        ),
                        shape = CircleShape
                    )
                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ){
                Image(
                    painter = painterResource(id = R.drawable.stat),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp) // Keeps it smaller than the 50.dp container
                )
            }
        }

        // ── Bottom CTA buttons ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 23.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(-10.dp),
        ) {
            // SIGN IN
            PillButton(
                label = "SIGN IN",
                iconResId = R.drawable.sign_in,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToLogin,
            )

            // SIGN UP
            PillButton(
                label = "SIGN UP",
                iconResId = R.drawable.sign_up,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToSignUp,
            )
        }
    }
}

/**
 * Reusable pill-shaped CTA button that matches the Figma design:
 *  • White outer container (rounded-50px, height 79 dp)
 *  • Blue inner button   (rounded-50px, height 56 dp, color #114784)
 *  • Uppercase bold label in white, letter-spacing 4 sp
 */
@Composable
private fun PillButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes iconResId: Int? = null, // 1. Add an optional icon parameter
) {
    Box(
        modifier = modifier
            .height(79.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(ColorButtonPill),
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
                containerColor = ColorButtonBg,
                contentColor = Color.White,
            ),
            contentPadding = PaddingValues(horizontal = 16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 2.dp
            ),
        ) {
            // 2. Wrap the layout in a Row to place items side-by-side
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // 3. Render the icon if it is provided
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                )
                Spacer(modifier = Modifier.width(12.dp)) // Distance between icon and text
                if (iconResId != null) {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = null, // Set to a string if needed for accessibility
                        modifier = Modifier.size(30.dp), // Adjust the size as you like
                        tint = Color.White // Forces the icon to match your white text color
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