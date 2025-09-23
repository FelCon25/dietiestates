package it.unina.dietiestates.features.property.presentation._compontents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unina.dietiestates.ui.theme.Green80

@Composable
fun ProgressStepIndicator(
    modifier: Modifier = Modifier,
    steps: List<String>,
    currentStep: Int,
){
    val progressAnimation by animateFloatAsState(
        targetValue = (currentStep / steps.lastIndex.toFloat()),
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
    )

    Column(
        modifier = modifier
    ) {

        BoxWithConstraints(
            contentAlignment = Alignment.Center
        ) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(horizontal = maxWidth / (steps.size * 2)),
                progress = {progressAnimation},
                trackColor = Color.LightGray.copy(alpha = .35f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                repeat(steps.size) { i ->
                    StepIcon(
                        isCompleted = i < currentStep,
                        isCurrent = i == currentStep
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            repeat(steps.size){ i ->
                StepLabel(
                    text = steps[i],
                    isCurrent = i == currentStep,
                    isCompleted = i < currentStep
                )
            }
        }
    }
}

@Composable
private fun RowScope.StepIcon(
    isCompleted: Boolean,
    isCurrent: Boolean
){
    Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(Green80, RoundedCornerShape(100))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ){

            this@StepIcon.AnimatedVisibility(
                visible = isCompleted
            ) {
                Icon(
                    imageVector = Icons.Outlined.Done,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            this@StepIcon.AnimatedVisibility(
                visible = isCurrent
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color.White, RoundedCornerShape(100))
                )
            }
        }
    }
}

@Composable
private fun RowScope.StepLabel(
    text: String,
    isCurrent: Boolean,
    isCompleted: Boolean,
){
    val color by animateColorAsState(
        targetValue = if(isCompleted || isCurrent) Green80 else Color.Black,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
    )

    Text(
        modifier = Modifier.weight(1f),
        text = text,
        fontSize = 12.sp,
        color = color,
        textAlign = TextAlign.Center,
        fontWeight = if(isCurrent) FontWeight.SemiBold else FontWeight.Normal
    )
}