package it.unina.dietiestates.features.profile.presentation._components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unina.dietiestates.features.profile.domain.Session

@Composable
fun SessionItem(
    session: Session,
    onSessionDelete: () -> Unit,
    isCurrentSession: Boolean = false
) {

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if(isCurrentSession){
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, ShapeDefaults.Small)
                        .padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = "Current session",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = session.userAgent,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Created at: ${session.createdAt}",
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Expires at: ${session.expiresAt}",
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = onSessionDelete,
                shape = ShapeDefaults.Medium,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Text("Delete session")
            }
        }
    }

}