package it.unina.dietiestates.core.presentation._compontents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import it.unina.dietiestates.R
import it.unina.dietiestates.core.presentation.util.parseImageUrl
import it.unina.dietiestates.core.domain.User

@Composable
fun TopBar(
    user: User,
    onEditProfileNavigation: () -> Unit
){

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 16.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .height(24.dp),
                painter = painterResource(R.drawable.dietiestates_logo_short),
                contentDescription = "",
                contentScale = ContentScale.FillHeight
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "DietiEstates",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )


        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 100))
                .clickable{
                    onEditProfileNavigation()
                }
        ) {
            if(user.profilePic != null){
                AsyncImage(
                    modifier = Modifier
                        .size(32.dp),
                    model = parseImageUrl(user.profilePic),
                    contentDescription = "User profile picture",
                    contentScale = ContentScale.Crop
                )
            }
            else{
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.firstName.first().uppercase(),
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        }

    }

}