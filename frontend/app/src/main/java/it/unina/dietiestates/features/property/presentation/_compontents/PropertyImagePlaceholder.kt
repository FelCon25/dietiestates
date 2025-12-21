package it.unina.dietiestates.features.property.presentation._compontents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.unina.dietiestates.ui.theme.Green80

/**
 * A beautiful placeholder for property images when no image is available.
 * Uses a gradient background with a centered home icon.
 *
 * @param modifier Modifier to be applied to the placeholder
 * @param iconSize Size of the home icon (default 64.dp for cards, use 80.dp for detail screens)
 */
@Composable
fun PropertyImagePlaceholder(
    modifier: Modifier = Modifier,
    iconSize: Dp = 64.dp
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Green80.copy(alpha = 0.3f),
                        Green80.copy(alpha = 0.1f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Home,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = Green80.copy(alpha = 0.5f)
        )
    }
}

