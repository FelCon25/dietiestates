package it.unina.dietiestates.features.auth.presentation._compontents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: @Composable (() -> Unit),
    icon: @Composable (() -> Unit),
    isPasswordTextField: Boolean = false
) {

    var isTextFieldFocused by remember{ mutableStateOf(false)}
    var showPassword by remember { mutableStateOf(false) }

    BasicTextField(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0), ShapeDefaults.Medium)
            .border(if(isTextFieldFocused) 1.dp else 0.dp, if(isTextFieldFocused) MaterialTheme.colorScheme.primary else Color.Transparent, ShapeDefaults.Medium)
            .height(40.dp)
            .onFocusChanged{ isTextFieldFocused = it.isFocused},
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        visualTransformation = if(showPassword || !isPasswordTextField) VisualTransformation.None else PasswordVisualTransformation(),
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
        decorationBox = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                icon()

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if(value.isEmpty()){
                        placeholder()
                    }
                    it()
                }

                if(isPasswordTextField){
                    Icon(
                        modifier = Modifier.clickable{
                            showPassword = !showPassword
                        },
                        imageVector = if(showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = "Show/Hide password"
                    )
                }
            }
        }
    )
}