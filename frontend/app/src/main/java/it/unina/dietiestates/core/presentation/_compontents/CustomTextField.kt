package it.unina.dietiestates.core.presentation._compontents

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.saveable.rememberSaveable
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
    isPasswordTextField: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onFocusChanged: ((Boolean) -> Unit)? = null
) {

    var isTextFieldFocused by rememberSaveable { mutableStateOf(false)}
    var showPassword by rememberSaveable { mutableStateOf(false) }

    BasicTextField(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F8F8), ShapeDefaults.Medium)
            .border(
                width = if(isTextFieldFocused) 1.5.dp else 1.dp, 
                color = if(isTextFieldFocused) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.2f), 
                shape = ShapeDefaults.Medium
            )
            .height(40.dp)
            .onFocusChanged{ focusState ->
                isTextFieldFocused = focusState.isFocused
                onFocusChanged?.invoke(focusState.isFocused)
            },
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = keyboardOptions,
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