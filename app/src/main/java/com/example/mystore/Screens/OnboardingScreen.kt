package com.example.mystore.Screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

// ─────────────────────────────────────────────────────────────
//  OnboardingScreen
// ─────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(navController: NavController) {

    // Image list: assets named onboarding_01.jpg … onboarding_26.jpg
    val ruralImages = remember {
        listOf(
            "onboarding_01", "onboarding_02", "onboarding_03", "onboarding_04", "onboarding_05",
            "onboarding_06", "onboarding_07", "onboarding_08", "onboarding_09", "onboarding_10",
            "onboarding_11", "onboarding_12", "onboarding_16", "onboarding_17", "onboarding_18",
            "onboarding_20", "onboarding_21", "onboarding_22", "onboarding_23", "onboarding_24",
            "onboarding_26"
        )
    }

    // One-shot animations via Animatable
    val fadeValue   = remember { Animatable(0f) }
    val slideValue  = remember { Animatable(50f) }
    val scaleValue  = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        // Fade  0 → 1  over first 60 % of 1 500 ms → ~900 ms
        fadeValue.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }
    LaunchedEffect(Unit) {
        // Slide 50 → 0  between 20 %–80 % → starts at 300 ms, ends at 1 200 ms
        kotlinx.coroutines.delay(300)
        slideValue.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }
    LaunchedEffect(Unit) {
        // Scale 0.8 → 1  with elastic feel over 1 000 ms
        scaleValue.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessLow
            )
        )
    }

    // ── Layout ──────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {

        // ── 1. Animated image grid background ───────────────────
        LazyVerticalGrid(
            columns             = GridCells.Fixed(3),
            modifier            = Modifier.fillMaxSize(),
            userScrollEnabled   = false,
            contentPadding      = PaddingValues(0.dp),
            verticalArrangement   = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            itemsIndexed(ruralImages) { index, imageName ->
                GridImageCell(imageName = imageName, index = index)
            }
        }

        // ── 2. Light gradient overlay ────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.White.copy(alpha = 0.10f),
                            0.3f to Color.White.copy(alpha = 0.20f),
                            0.6f to Color.White.copy(alpha = 0.40f),
                            1.0f to Color.White.copy(alpha = 0.60f),
                        )
                    )
                )
        )

        // ── 3. Bottom content panel ──────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .offset(y = slideValue.value.dp)
                .alpha(fadeValue.value)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.White.copy(alpha = 0.00f),
                                0.2f to Color.White.copy(alpha = 0.30f),
                                0.5f to Color.White.copy(alpha = 0.70f),
                                0.8f to Color.White.copy(alpha = 0.95f),
                                1.0f to Color.White,
                            )
                        )
                    )
            ) {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 32.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    // ── Logo ──────────────────────────────────────
                    Box(
                        modifier           = Modifier
                            .size(100.dp)
                            .scale(scaleValue.value)
                            .shadow(
                                elevation     = 16.dp,
                                shape         = CircleShape,
                                ambientColor  = Color.Black.copy(alpha = 0.2f),
                                spotColor     = Color.Black.copy(alpha = 0.2f),
                             )
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val logoResId = context.resources.getIdentifier(
                            "app_logo", "drawable", context.packageName
                        )
                        val hasLogo = logoResId != 0

                        if (hasLogo) {
                            Image(
                                painter            = painterResource(id = logoResId),
                                contentDescription = "Kutira-Kushala logo",
                                modifier           = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale       = ContentScale.Crop,
                            )
                        } else {
                            Text(
                                text  = "K",
                                color = Color(0xFF2563EB),
                                fontSize   = 48.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Title ─────────────────────────────────────
                    Text(
                        text       = "Welcome to Kutira Kushala",
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF1F2937),
                        textAlign  = TextAlign.Center,
                        lineHeight = 38.sp,
                        letterSpacing = 0.5.sp,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Subtitle ──────────────────────────────────
//                    Text(
//                        text       = "Community Help and Technology\nfor Uplifting Ruralities",
//                        fontSize   = 15.sp,
//                        color      = Color(0xFF4B5563),
//                        textAlign  = TextAlign.Center,
//                        fontStyle  = FontStyle.Italic,
//                        lineHeight = 22.sp,
//                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // ── Sign Up button ────────────────────────────
                    Button(
                        onClick  = { navController.navigate("register") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 400.dp)
                            .height(56.dp),
                        shape  = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626),
                            contentColor   = Color.White,
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    ) {
                        Text(
                            text       = "Sign up",
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Log In button ─────────────────────────────
                    TextButton(
                        onClick  = { navController.navigate("login") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 400.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                    ) {
                        Text(
                            text       = "Log in",
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color(0xFF1F2937),
                            letterSpacing = 0.5.sp,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Terms text ────────────────────────────────
                    Text(
                        text = "By continuing, you agree to Kutira-Kushala's Terms of Service " +
                                "and acknowledge you've read our Privacy Policy",
                        fontSize  = 11.sp,
                        color     = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier  = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Grid image cell with staggered fade-in + scale animation
// ─────────────────────────────────────────────────────────────

@Composable
private fun GridImageCell(imageName: String, index: Int) {
    val animValue = remember { Animatable(0f) }

    LaunchedEffect(index) {
        kotlinx.coroutines.delay((index * 50).toLong())
        animValue.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val cellScale = 0.8f + (animValue.value * 0.2f)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .alpha(animValue.value)
            .scale(cellScale)
    ) {
        // Resolve drawable ID outside runCatching — LocalContext.current is @Composable.
        val context = androidx.compose.ui.platform.LocalContext.current
        val drawableId = context.resources.getIdentifier(imageName, "drawable", context.packageName)

        if (drawableId != 0) {
            Image(
                painter            = painterResource(id = drawableId),
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
            )
        } else {
            // Fallback gradient when the image is missing
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFBFDBFE), // blue-100
                                Color(0xFFBBF7D0), // green-100
                                Color(0xFFFED7AA), // orange-100
                            )
                        )
                    )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Preview
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen(navController = rememberNavController())
}