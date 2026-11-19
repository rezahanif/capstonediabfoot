package com.project.insole.core.theme

import androidx.compose.ui.graphics.Color

object DashboardColors {
    val Background        = Color(0xFFEBF1FF)   // page bg
    val Navy              = Color(0xFF001633)   // dark text
    val Brand             = Color(0xFF114784)   // primary blue
    val StatusCard        = Color(0xFF114784)   // status card bg
    val Green             = Color(0xFF2EB88A)   // connected / good
    val GreenMint         = Color(0xFF10B981)   // circle progress / badge
    val GreenBar          = Color(0xFF34D399)   // bar chart bars
    val Yellow            = Color(0xFFEAB308)   // sync CTA / attention
    val TextMuted         = Color(0xFF44474E)
    val TextGray          = Color(0xFF6B7280)
    val TextLightGray     = Color(0xFF9CA3AF)
    val CardBg            = Color(0xFFFFFFFF)
    val InfoBadgeBg       = Color(0xFFECFDF5).copy(alpha = 0.5f)
    val InfoBadgeBorder   = Color(0xFFD1FAE5).copy(alpha = 0.5f)
    val ProgressTrack     = Color(0xFFE5E7EB)
    val StepBlue          = Color(0xFF0F3D7A)
    val TempGreen         = Color(0xFF10B981)
    val TempBlue          = Color(0xFF93C5FD)

    // Official tokens for one-off colors that screens used to hardcode.
    val CardTintBlue      = Color(0xFFF0F7FF)   // highlighted (recognized) card bg
    val SideRight         = Color(0xFFE91E63)   // RIGHT-insole accent
    val Alert             = Color(0xFFE53935)   // destructive / disabled states

    // TD-9: Official Heatmap Spectrum tokens (blue -> cyan -> green -> yellow -> red)
    val Heatmap00         = Color(0xFF0A1172)   // deep blue (zero pressure)
    val Heatmap15         = Color(0xFF0066CC)
    val Heatmap30         = Color(0xFF00AAFF)   // cyan
    val Heatmap45         = Color(0xFF00DDAA)   // teal
    val Heatmap60         = Color(0xFF44FF44)   // green
    val Heatmap75         = Color(0xFFFFFF00)   // yellow
    val Heatmap88         = Color(0xFFFF8800)   // orange
    val Heatmap100        = Color(0xFFFF2200)   // red (peak pressure)

    // Plantar Foot Silhouette and Neutral Indicators
    val FootSilhouette    = Color(0xFF0A2050)
    val FootOutlineMuted  = Color(0xFFAAAAAA)
    val InactiveTrack     = Color(0xFFCCCCCC)
}
