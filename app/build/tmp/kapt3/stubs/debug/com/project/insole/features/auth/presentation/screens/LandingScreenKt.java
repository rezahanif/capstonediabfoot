package com.project.insole.features.auth.presentation.screens;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000&\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u001a$\u0010\u0007\u001a\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\b0\n2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\b0\nH\u0007\u001a\b\u0010\f\u001a\u00020\bH\u0003\u001a(\u0010\r\u001a\u00020\b2\u0006\u0010\u000e\u001a\u00020\u000f2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\b0\n2\b\b\u0002\u0010\u0011\u001a\u00020\u0012H\u0003\"\u0010\u0010\u0000\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0003\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0004\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0005\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0006\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\u00a8\u0006\u0013"}, d2 = {"ColorButtonBg", "Landroidx/compose/ui/graphics/Color;", "J", "ColorButtonPill", "ColorGradientBottom", "ColorGradientTop", "ColorTextLight", "LandingScreen", "", "onNavigateToLogin", "Lkotlin/Function0;", "onNavigateToSignUp", "LandingScreenPreview", "PillButton", "label", "", "onClick", "modifier", "Landroidx/compose/ui/Modifier;", "app_debug"})
public final class LandingScreenKt {
    private static final long ColorGradientTop = 0L;
    private static final long ColorGradientBottom = 0L;
    private static final long ColorButtonBg = 0L;
    private static final long ColorButtonPill = 0L;
    private static final long ColorTextLight = 0L;
    
    /**
     * Landing screen — redesigned from the Figma CAPSTONE node 19-4569.
     *
     * Layout (top → bottom):
     * • Full-screen gradient background  (#114797 → #112B4E)
     * • "SmartInsole" headline  (top-right area)
     * • "Smarter Steps / Better You" subtitle
     * • Centred hero product image
     * • Two pill buttons at the bottom: SIGN IN  |  SIGN UP
     */
    @androidx.compose.runtime.Composable()
    public static final void LandingScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToLogin, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToSignUp) {
    }
    
    /**
     * Reusable pill-shaped CTA button that matches the Figma design:
     * • White outer container (rounded-50px, height 79 dp)
     * • Blue inner button   (rounded-50px, height 56 dp, color #114784)
     * • Uppercase bold label in white, letter-spacing 4 sp
     */
    @androidx.compose.runtime.Composable()
    private static final void PillButton(java.lang.String label, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 390, heightDp = 844)
    @androidx.compose.runtime.Composable()
    private static final void LandingScreenPreview() {
    }
}