package com.project.insole.features.auth.presentation.screens;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000&\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\u001a$\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0007\u001a\b\u0010\u0005\u001a\u00020\u0001H\u0003\u001a9\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\b\b\u0002\u0010\n\u001a\u00020\u000b2\n\b\u0003\u0010\f\u001a\u0004\u0018\u00010\rH\u0003\u00a2\u0006\u0002\u0010\u000e\u00a8\u0006\u000f"}, d2 = {"LandingScreen", "", "onNavigateToLogin", "Lkotlin/Function0;", "onNavigateToSignUp", "LandingScreenPreview", "PillButton", "label", "", "onClick", "modifier", "Landroidx/compose/ui/Modifier;", "icon", "", "(Ljava/lang/String;Lkotlin/jvm/functions/Function0;Landroidx/compose/ui/Modifier;Ljava/lang/Integer;)V", "app_debug"})
public final class LandingScreenKt {
    
    /**
     * Landing screen — redesigned from the Figma CAPSTONE node 19-4569.
     *
     * Layout (top → bottom):
     * • Full-screen gradient background
     * • "SmartInsole" headline
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
     * Reusable pill-shaped CTA button with Icon Support.
     */
    @androidx.compose.runtime.Composable()
    private static final void PillButton(java.lang.String label, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, androidx.compose.ui.Modifier modifier, @androidx.annotation.DrawableRes()
    java.lang.Integer icon) {
    }
    
    @androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 390, heightDp = 844)
    @androidx.compose.runtime.Composable()
    private static final void LandingScreenPreview() {
    }
}