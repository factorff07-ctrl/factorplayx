package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Immersive UI Core Palette
val ImmersiveLavender = Color(0xFFD0BCFF)
val ImmersiveDeepPurple = Color(0xFF381E72)
val ImmersiveMediumPurple = Color(0xFF4F378B)
val ImmersiveLightPurple = Color(0xFFEADDFF)
val ImmersiveDarkPurple = Color(0xFF21005D)
val ImmersiveNeonViolet = Color(0xFFA855F7)
val ImmersiveElectricIndigo = Color(0xFF6366F1)
val ImmersiveCoral = Color(0xFFF43F5E)
val ImmersiveEmerald = Color(0xFF10B981)
val ImmersiveAmber = Color(0xFFF59E0B)

// Immersive Deep Dark Backgrounds & Glass Surfaces
val ImmersiveBackground = Color(0xFF050505)
val ImmersiveSurface = Color(0xFF1C1B1F)
val ImmersiveSurfaceVariant = Color(0xFF2B2930)
val ImmersiveSurfaceElevated = Color(0xFF36343B)
val ImmersiveGlassCard = Color(0x14FFFFFF) // white/8
val ImmersiveGlassBorder = Color(0x1FFFFFFF) // white/12
val ImmersiveGlassBorderSubtle = Color(0x0FFFFFFF) // white/6

// Gradients
val ImmersiveHeroGradient = Brush.linearGradient(
    listOf(ImmersiveLavender, ImmersiveDeepPurple)
)

val ImmersivePlayGradient = Brush.linearGradient(
    listOf(Color(0xFFD0BCFF), Color(0xFF4F378B))
)

val ImmersiveCardGlassGradient = Brush.verticalGradient(
    listOf(Color(0x1AFFFFFF), Color(0x05FFFFFF))
)

val ImmersiveVideoThumbnailGradient = Brush.linearGradient(
    listOf(Color(0x336366F1), Color(0x0D000000))
)

val PlayerScrimGradient = Brush.verticalGradient(
    listOf(
        Color(0xE6050505),
        Color(0x00000000),
        Color(0x00000000),
        Color(0xF2050505)
    )
)

// Aliases for compatibility
val NeonCyan = ImmersiveLavender
val NeonCyanDark = ImmersiveDeepPurple
val CyberViolet = ImmersiveLavender
val CyberPurple = ImmersiveDeepPurple
val ElectricBlue = ImmersiveElectricIndigo
val EmeraldGreen = ImmersiveEmerald
val CoralRed = ImmersiveCoral
val AmberGold = ImmersiveAmber

val DarkBackground = ImmersiveBackground
val DarkSurface = ImmersiveSurface
val DarkSurfaceVariant = ImmersiveSurfaceVariant
val DarkSurfaceElevated = ImmersiveSurfaceElevated
val DarkCardGlass = ImmersiveGlassCard
val DarkBorderGlass = ImmersiveGlassBorder
val DarkBorderCyan = ImmersiveGlassBorder

val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF0D0D0F)
val AmoledCard = Color(0xFF16151A)
val AmoledBorder = Color(0x1FFFFFFF)

val LightBackground = Color(0xFFF7F5FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEDE7F6)
val LightCardGlass = Color(0xEEFFFFFF)
val LightBorderGlass = Color(0x1A4F378B)

val CyanVioletGradient = ImmersiveHeroGradient
val CardGlassGradient = ImmersiveCardGlassGradient

