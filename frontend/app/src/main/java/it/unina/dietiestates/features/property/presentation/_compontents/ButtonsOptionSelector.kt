package it.unina.dietiestates.features.property.presentation._compontents

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unina.dietiestates.ui.theme.Green80

@Composable
fun ButtonsOptionSelector(
    modifier: Modifier = Modifier,
    options: List<String>,
    selectedOptionIndex: Int = 0,
    onOptionSelected: (Int, String) -> Unit
) {

    Row(
        modifier = modifier
            .height(42.dp)
            .clip(ShapeDefaults.Small)
            .border(1.dp, Color.Gray, ShapeDefaults.Small)
    ) {

        repeat(options.size){ i ->
            val isSelected = i == selectedOptionIndex

            val backColor by animateColorAsState(
                targetValue = if(isSelected) Green80 else Color.White,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )

            val textColor by animateColorAsState(
                targetValue = if(isSelected) Color.White else Color.Black,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(backColor)
                    .clickable{
                        onOptionSelected(i, options[i])
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = options[i],
                    color = textColor,
                    fontSize = 10.sp
                )

                if( i < options.size - 1){
                    VerticalDivider(
                        thickness = 1.dp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }
    }

}