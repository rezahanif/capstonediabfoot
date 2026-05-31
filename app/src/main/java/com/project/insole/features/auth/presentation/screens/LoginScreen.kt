package com.project.insole.features.auth.presentation.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.R
import com.project.insole.core.presentation.components.InsoleToast
import com.project.insole.core.presentation.components.ToastData
import com.project.insole.core.presentation.components.ToastType
import com.project.insole.features.auth.presentation.AuthViewModel
import kotlinx.coroutines.delay

// ─── Design Tokens (matching LandingScreen + Figma node 129-955) ──────────────
private val ColorGradientTop    = Color(0xFF114797)   // #114797
private val ColorGradientBottom = Color(0xFF112B4E)   // #112B4E
private val ColorButtonBg       = Color(0xFF114784)   // #114784 (primary blue button)
private val ColorTextLight      = Color(0xFFEBF1FF)   // #EBF1FF (headline + subtitle)
private val ColorCardBg         = Color(0xFFC9DBF2).copy(alpha = 0.05f)   // semi-transparent card surface
private val ColorInputBorder    = Color(0xFF2A5BAD)   // input field border
private val ColorInputBg        = Color(0xFF1E4585)   // input field background
private val ColorHint           = Color(0xFF8AAAD8)   // placeholder / hint text
private val ColorDivider        = Color(0xFF2A5BAD)   // "or continue with" divider

/**
 * Login screen — matching Figma CAPSTONE node 129-955.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onBack: () -> Unit
) {
    val state = viewModel.authState.collectAsState().value
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Toast State Management
    var toastData by remember { mutableStateOf<ToastData?>(null) }

    // Observe error messages from the ViewModel and show them as Toasts
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            toastData = ToastData(
                title = "Error",
                description = msg,
                type = ToastType.Error,
                isVisible = true
            )
            // Auto-hide after 5 seconds
            delay(5000)
            toastData = toastData?.copy(isVisible = false)
        }
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {

            // ── Back Arrow ───────────────────────────────────────────────────
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(top = 48.dp, start = 16.dp)
                    .size(40.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }

            // ── Header: Text (left) + Hero image (right) ─────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                // Decorative large circle (behind image, left side)
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .offset(x = 70.dp, y = (-20).dp)
                        .align(Alignment.Center)
                        .border(
                            width = 1.5.dp,
                            color = Color.White.copy(alpha = 0.12f),
                            shape = CircleShape
                        )
                )

                // Decorative inner circle
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .offset(x = 70.dp, y = (-20).dp)
                        .align(Alignment.Center)
                        .border(
                            width = 1.5.dp,
                            color = Color.White.copy(alpha = 0.18f),
                            shape = CircleShape
                        )
                )

                // SmartInsole title + subtitle (left)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 24.dp),
                ) {
                    Text(
                        text = "SmartInsole",
                        color = ColorTextLight,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Smarter Steps\nBetter You",
                        color = ColorTextLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.5.sp,
                        lineHeight = 22.sp,
                    )
                }

                // Hero product image (right side)
                Image(
                    painter = painterResource(id = R.drawable.insole_hero),
                    contentDescription = "Smart Insole product",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(200.dp)
                        .width(180.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-20).dp, y = 0.dp),
                )

                // ── Decorative Icon Bubbles (right of image) ─────────────────
                DecorativeIconBubble(
                    iconResId = R.drawable.stat,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-90).dp, y = (-40).dp),
                )
                DecorativeIconBubble(
                    iconResId = R.drawable.connect,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = (-25).dp, y = (-80).dp),
                )
                DecorativeIconBubble(
                    iconResId = R.drawable.foot,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-8).dp, y = (-90).dp),
                )
            }

            Spacer(Modifier.height(0.dp))

            // ── Sign In Card ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(ColorCardBg)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(24.dp),
                    )
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // Title
                    Text(
                        text = "Sign In",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(8.dp))

                    // Subtitle
                    Text(
                        text = "Welcome back! Please sign in to continue",
                        color = ColorHint,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                    )

                    Spacer(Modifier.height(24.dp))

                    // ── Email Field ──────────────────────────────────────────
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Email",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = {
                                Text(
                                    text = "Enter your email address",
                                    color = ColorHint,
                                    fontSize = 13.sp,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_email),
                                    contentDescription = null,
                                    tint = ColorHint,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = ColorInputBg,
                                unfocusedContainerColor = ColorInputBg,
                                focusedBorderColor = Color.White.copy(alpha = 0.4f),
                                unfocusedBorderColor = ColorInputBorder,
                                cursorColor = Color.White,
                            ),
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Password Field ───────────────────────────────────────
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Password",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = {
                                Text(
                                    text = "Enter your password",
                                    color = ColorHint,
                                    fontSize = 13.sp,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_lock),
                                    contentDescription = null,
                                    tint = ColorHint,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (passwordVisible) R.drawable.ic_visibility
                                            else R.drawable.ic_visibility_off
                                        ),
                                        contentDescription = if (passwordVisible) "Hide password"
                                        else "Show password",
                                        tint = ColorHint,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = ColorInputBg,
                                unfocusedContainerColor = ColorInputBg,
                                focusedBorderColor = Color.White.copy(alpha = 0.4f),
                                unfocusedBorderColor = ColorInputBorder,
                                cursorColor = Color.White,
                            ),
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Forget Password ──────────────────────────────────────
                    Text(
                        text = "Forget password?",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { /* TODO: Handle forgot password */ },
                    )

                    Spacer(Modifier.height(20.dp))

                    // ── Sign In Pill Button ──────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(50.dp))
                            ,
                        contentAlignment = Alignment.Center,
                    ) {
                        Button(
                            onClick = { viewModel.login(email, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 0.dp)
                                .height(60.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 2.dp,
                            ),
                            enabled = !state.isLoading
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Spacer(Modifier.size(30.dp))

                                Text(
                                    text = if (state.isLoading) "Signing In..." else "Sign In",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = ColorButtonBg,
                                )

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = ColorButtonBg,
                                            shape = CircleShape,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.sign_in),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                            .offset(x=2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }



            Spacer(Modifier.height(24.dp))

            // ── "Don't have an account? Sign Up" ─────────────────────────────
            val footerText = buildAnnotatedString {
                withStyle(SpanStyle(color = ColorTextLight, fontSize = 13.sp)) {
                    append("Don't have an account? ")
                }
                withStyle(
                    SpanStyle(
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                ) {
                    append("Sign Up")
                }
            }

            Text(
                text = footerText,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onNavigateToSignUp() }
                    .padding(bottom = 32.dp),
                textAlign = TextAlign.Center,
            )
        }
        
        if (state.isLoggedIn) {
            LaunchedEffect(Unit) {
                onNavigateToHome()
            }
        }

        // Overlay Toast at the top
        toastData?.let { data ->
            InsoleToast(
                data = data,
                onDismiss = { toastData = toastData?.copy(isVisible = false) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
            )
        }
    }
}

/**
 * Small circular decorative icon bubble — identical style to LandingScreen.
 */
@Composable
private fun DecorativeIconBubble(
    @DrawableRes iconResId: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF114797), Color(0xFF174F9F)),
                ),
                shape = CircleShape,
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
        )
    }
}
