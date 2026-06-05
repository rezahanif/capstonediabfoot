package com.project.insole.features.auth.presentation.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.R
import com.project.insole.core.presentation.components.InsoleToast
import com.project.insole.core.presentation.components.ToastData
import com.project.insole.core.presentation.components.ToastType
import com.project.insole.features.auth.presentation.AuthViewModel
import kotlinx.coroutines.delay

// ─── Design Tokens ────────────────────────────────────────────────────────────
private val ColorGradientTop    = Color(0xFF114797)
private val ColorGradientBottom = Color(0xFF112B4E)
private val ColorButtonBg       = Color(0xFF114784)
private val ColorTextLight      = Color(0xFFEBF1FF)
private val ColorCardBg         = Color(0xFFC9DBF2).copy(alpha = 0.05f)
private val ColorInputBorder    = Color(0xFF2A5BAD)
private val ColorInputBg        = Color(0xFF1E4585)
private val ColorHint           = Color(0xFF8AAAD8)

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onBack: () -> Unit,
) {
    val state = viewModel.authState.collectAsState().value
    var currentStep by remember { mutableIntStateOf(0) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rePassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rePasswordVisible by remember { mutableStateOf(false) }
    var leftPaired  by remember { mutableStateOf(false) }
    var rightPaired by remember { mutableStateOf(false) }

    // Toast State Management
    var toastData by remember { mutableStateOf<ToastData?>(null) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            toastData = ToastData(
                title = "Error",
                description = msg,
                type = ToastType.Error,
                isVisible = true
            )
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
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
                    Text(
                        text = "Sign Up",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(6.dp))
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith
                                    fadeOut(animationSpec = tween(300))
                        },
                        label = "StepDescriptionTransition"
                    ) { step ->
                        Text(
                            text = if (step == 0)
                                "Create your account to get started"
                            else
                                "Pair your device to get started",
                            color = ColorHint,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut())
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut())
                            }.using(
                                SizeTransform(clip = false)
                            )
                        },
                        label = "SignUpStepTransition"
                    ) { step ->
                        if (step == 0) {
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
                                        Text("Enter your email address", color = ColorHint, fontSize = 13.sp)
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
                                    colors = signUpTextFieldColors(),
                                )

                                Spacer(Modifier.height(14.dp))

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
                                        Text("Enter your password", color = ColorHint, fontSize = 13.sp)
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
                                                contentDescription = null,
                                                tint = ColorHint,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible)
                                        VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = signUpTextFieldColors(),
                                )

                                Spacer(Modifier.height(14.dp))

                                Text(
                                    text = "Re-enter Password",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = rePassword,
                                    onValueChange = { rePassword = it },
                                    placeholder = {
                                        Text("Enter your password", color = ColorHint, fontSize = 13.sp)
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
                                        IconButton(onClick = { rePasswordVisible = !rePasswordVisible }) {
                                            Icon(
                                                painter = painterResource(
                                                    id = if (rePasswordVisible) R.drawable.ic_visibility
                                                    else R.drawable.ic_visibility_off
                                                ),
                                                contentDescription = null,
                                                tint = ColorHint,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    },
                                    visualTransformation = if (rePasswordVisible)
                                        VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = signUpTextFieldColors(),
                                )

                                Spacer(Modifier.height(20.dp))
                                StepIndicator(currentStep = 0, totalSteps = 2, modifier = Modifier.align(Alignment.CenterHorizontally))
                                Spacer(Modifier.height(16.dp))

                                Button(
                                    onClick = { currentStep = 1 },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = ColorButtonBg,
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 6.dp,
                                        pressedElevation = 2.dp,
                                    ),
                                    enabled = email.isNotBlank() && password.isNotBlank() && rePassword.isNotBlank() && password == rePassword,
                                ) {
                                    Text(
                                        text = "Next",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = ColorButtonBg,
                                    )
                                }
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    InsoleCard(
                                        label = "Left\nInsole",
                                        isPaired = leftPaired,
                                        mirrored = false,
                                        modifier = Modifier.weight(1f),
                                        onClick = { leftPaired = !leftPaired },
                                    )
                                    InsoleCard(
                                        label = "Right\nInsole",
                                        isPaired = rightPaired,
                                        mirrored = true,
                                        modifier = Modifier.weight(1f),
                                        onClick = { rightPaired = !rightPaired },
                                    )
                                }

                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "Make sure both insoles are powered on",
                                    color = ColorHint,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(Modifier.height(20.dp))
                                StepIndicator(currentStep = 1, totalSteps = 2)
                                Spacer(Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Button(
                                        onClick = { currentStep = 0 },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(60.dp),
                                        shape = RoundedCornerShape(50.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = ColorButtonBg,
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 6.dp,
                                            pressedElevation = 2.dp,
                                        ),
                                    ) {
                                        Text(
                                            text = "Back",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = ColorButtonBg,
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(60.dp)
                                            .clip(RoundedCornerShape(50.dp)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Button(
                                            onClick = { viewModel.signUp(email, password) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(60.dp),
                                            shape = RoundedCornerShape(50.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.White,
                                                contentColor = ColorButtonBg,
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp),
                                            elevation = ButtonDefaults.buttonElevation(
                                                defaultElevation = 6.dp,
                                                pressedElevation = 2.dp,
                                            ),
                                            enabled = !state.isLoading && leftPaired && rightPaired,
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Spacer(Modifier.size(30.dp))
                                                Text(
                                                    text = if (state.isLoading) "..." else "Finish",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.sp,
                                                    color = ColorButtonBg,
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .background(color = ColorButtonBg, shape = CircleShape),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.sign_in),
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(22.dp).offset(x=2.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            val footerText = buildAnnotatedString {
                withStyle(SpanStyle(color = ColorTextLight, fontSize = 13.sp)) {
                    append("Already have an account? ")
                }
                withStyle(SpanStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)) {
                    append("Sign In")
                }
            }

            Text(
                text = footerText,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onNavigateToSignIn() }
                    .padding(bottom = 32.dp),
                textAlign = TextAlign.Center,
            )
        }

        if (state.isLoggedIn) {
            LaunchedEffect(Unit) { onNavigateToHome() }
        }

        toastData?.let { data ->
            InsoleToast(
                data = data,
                onDismiss = { toastData = toastData?.copy(isVisible = false) },
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp)
            )
        }
    }
}

@Composable
private fun signUpTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = ColorInputBg,
    unfocusedContainerColor = ColorInputBg,
    focusedBorderColor = Color.White.copy(alpha = 0.4f),
    unfocusedBorderColor = ColorInputBorder,
    cursorColor = Color.White,
)

@Composable
private fun InsoleCard(
    label: String,
    isPaired: Boolean,
    mirrored: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // ── Adjustable Offsets ───────────────────────────────────────────────────
    val imageOffsetX = if (mirrored) -24.dp else (24).dp
    val buttonOffsetX = if (mirrored) (15).dp else (-15).dp

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A4090).copy(alpha = 0.60f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 16.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )
            
            Spacer(Modifier.height(10.dp))
            
            Box(
                modifier = Modifier
                    .height(90.dp) // Increased height for larger image
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // ── Insole Image ─────────────────────────────────────────────
                // Swapped: Positioned END for Mirrored (Right), START for Normal (Left)
                Image(
                    painter = painterResource(id = R.drawable.insole_pair),
                    contentDescription = label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(120.dp) // Increased from 72dp
                        .align(if (mirrored) Alignment.CenterEnd else Alignment.CenterStart)
                        .offset(x = imageOffsetX)
                        .then(
                            if (mirrored) Modifier.scale(scaleX = -1f, scaleY = 1f)
                            else Modifier
                        )
                )

                // ── Bluetooth / Pairing Button ────────────────────────────────
                // Swapped: Positioned START for Mirrored, END for Normal
                Box(
                    modifier = Modifier
                        .size(38.dp) // Slightly larger button
                        .align(if (mirrored) Alignment.CenterStart else Alignment.CenterEnd)
                        .offset(x = buttonOffsetX)
                        .background(
                            color = if (isPaired) Color(0xFF2E7D32) else ColorButtonBg,
                            shape = CircleShape
                        )
                        .clickable { onClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isPaired) R.drawable.ic_check else R.drawable.ic_close
                        ),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(10.dp))
            
            Text(
                text = if (isPaired) "Paired ✓" else "Click to Pair",
                color = if (isPaired) Color(0xFF81C784) else ColorHint,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep
            Box(modifier = Modifier.size(if (isActive) 10.dp else 8.dp).clip(CircleShape).background(color = if (isActive) Color.White else Color.Transparent).then(if (!isActive) Modifier.border(width = 1.5.dp, color = Color.White.copy(alpha = 0.50f), shape = CircleShape) else Modifier))
        }
    }
}

@Composable
private fun DecorativeIconBubble(@DrawableRes iconResId: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(44.dp).background(brush = Brush.linearGradient(colors = listOf(Color(0xFF114797), Color(0xFF174F9F))), shape = CircleShape).border(width = 1.dp, color = Color.White.copy(alpha = 0.15f), shape = CircleShape), contentAlignment = Alignment.Center) {
        Image(painter = painterResource(id = iconResId), contentDescription = null, modifier = Modifier.size(22.dp))
    }
}
