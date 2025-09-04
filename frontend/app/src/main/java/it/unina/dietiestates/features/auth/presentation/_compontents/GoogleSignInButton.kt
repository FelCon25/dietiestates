package it.unina.dietiestates.features.auth.presentation._compontents

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.unina.dietiestates.R

@Composable
fun GoogleSignInButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        modifier = modifier
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.DarkGray
        ),
        shape = ShapeDefaults.Medium,
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.google),
                contentDescription = "Google logo"
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                "Continue with google",
                color = Color.White
            )
        }
    }
}


@Preview
@Composable
fun Preview(){
    GoogleSignInButton(){

    }
}