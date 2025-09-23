package it.unina.dietiestates.features.property.presentation._compontents

import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unina.dietiestates.ui.theme.Green80
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun NumberPicker(
    modifier: Modifier = Modifier,
    list: List<Int>,
    firstIndex: Int,
    onValueChange: (Int) -> Unit
) {

    val listSize = list.size
    val startIndex = (Int.MAX_VALUE / 2 / listSize) * listSize
    val listState = rememberLazyListState(startIndex + firstIndex - 1)

    val centralItemIndex by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex + 1
        }
    }

    LaunchedEffect(listState, list) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { isScrolling ->
                if(!isScrolling){
                    val currentIndexInList = centralItemIndex % listSize
                    onValueChange(list[currentIndexInList])
                }
            }
    }

    Box(
        modifier = modifier
            .height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .width(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState, snapPosition = SnapPosition.Start),
            state = listState
        ) {
            items(count = Int.MAX_VALUE, key = { it }){ i ->
                val currentItemIndex = i % listSize
                val itemValue = list[currentItemIndex]
                val isCentral = i == centralItemIndex

                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = itemValue.toString(),
                        fontSize = 22.sp,
                        color = if(isCentral) Green80 else Color.Black.copy(alpha = .3f),
                        fontWeight = if(isCentral) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}