package it.unina.dietiestates.features.property.presentation._compontents

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ButtonImagesSelector(
    modifier: Modifier = Modifier,
    imageNumber: Int = 1,
    onImagesSelected: (List<Uri>) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val launcherMultiple = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
           onImagesSelected(uris)
        }
    )

    val launcherSingle = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                onImagesSelected(listOf(uri))
            }
        }
    )

    OutlinedButton(
        modifier = modifier,
        onClick = {
            if(imageNumber == 1){
                launcherSingle.launch(
                    PickVisualMediaRequest(
                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
            else if(imageNumber > 1){
                launcherMultiple.launch(
                    PickVisualMediaRequest(
                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                        maxItems = imageNumber
                    )
                )
            }
            else{
                coroutineScope.launch {
                    Toast.makeText(context, "Max number of images reached", Toast.LENGTH_SHORT).show()
                }
            }
        },
        shape = ShapeDefaults.Small
    ) {
        Icon(imageVector = Icons.Outlined.PhotoLibrary, contentDescription = "Photo library icon")

        Spacer(modifier = Modifier.width(8.dp))

        Text("Select property image")
    }

}