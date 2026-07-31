package com.makusha.incomatic.design

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Underline text field — plain string input (ticker symbols, free text). */
@Composable
fun IncTextField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val colors = incColors()
    var focused by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
        IncLabel(label)
        UnderlineInput(
            value = value,
            onChange = onChange,
            placeholder = placeholder,
            keyboardType = keyboardType,
            focused = focused,
            onFocusChanged = { focused = it },
            textStyle = IncType.title,
            modifier = Modifier.fillMaxWidth().underline(if (focused) colors.sage else colors.hairline),
        )
    }
}

/**
 * Money field — "$" prefix + decimal keyboard, sharing the same underline
 * treatment as every other field in the design.
 */
@Composable
fun IncMoneyField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    suffix: String? = null,
    placeholder: String = "0.00",
) {
    val colors = incColors()
    var focused by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
        IncLabel(label, suffix = suffix)
        Row(
            modifier = Modifier.fillMaxWidth().underline(if (focused) colors.sage else colors.hairline),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                "$",
                style = IncType.cardMoney.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                color = if (focused) colors.sage else colors.textMute,
            )
            Spacer(Modifier.width(8.dp))
            UnderlineInput(
                value = value,
                onChange = onChange,
                placeholder = placeholder,
                keyboardType = KeyboardType.Decimal,
                focused = focused,
                onFocusChanged = { focused = it },
                textStyle = IncType.cardMoney,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun Modifier.underline(color: Color): Modifier = this
    .padding(bottom = 8.dp)
    .drawBehind {
        drawLine(color, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx())
    }

@Composable
private fun UnderlineInput(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String?,
    keyboardType: KeyboardType,
    focused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    val colors = incColors()
    BasicTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier.onFocusChanged { onFocusChanged(it.isFocused) },
        textStyle = textStyle.copy(color = colors.text),
        singleLine = true,
        cursorBrush = SolidColor(colors.sage),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        decorationBox = { inner ->
            if (value.isEmpty() && placeholder != null) {
                Text(placeholder, style = textStyle, color = colors.textMute)
            }
            inner()
        },
    )
}
