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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makusha.incomatic.util.groupThousands
import com.makusha.incomatic.util.sanitizeCurrency

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
            accessibleLabel = label,
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
                onChange = { raw -> onChange(sanitizeCurrency(raw)) },
                placeholder = placeholder,
                keyboardType = KeyboardType.Decimal,
                focused = focused,
                onFocusChanged = { focused = it },
                textStyle = IncType.cardMoney,
                accessibleLabel = label,
                modifier = Modifier.weight(1f),
                visualTransformation = ThousandsSeparatorVisualTransformation,
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
    accessibleLabel: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val colors = incColors()
    BasicTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier
            .semantics { contentDescription = accessibleLabel }
            .onFocusChanged { onFocusChanged(it.isFocused) },
        textStyle = textStyle.copy(color = colors.text),
        singleLine = true,
        cursorBrush = SolidColor(colors.sage),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        decorationBox = { inner ->
            if (value.isEmpty() && placeholder != null) {
                Text(placeholder, style = textStyle, color = colors.textMute)
            }
            inner()
        },
    )
}

/**
 * Live "1,234.56"-style grouping for money fields — [value] stays raw
 * (un-grouped) in state; this only affects what's drawn. Mirrors iOS's
 * CalculatorFields.currencyBinding's get-side formatting. Cursor offsets are
 * mapped via a forward pass counting commas inserted before each raw index
 * (an iterative approach, not a closed-form formula, since it's the more
 * reliable way to get this right).
 */
private object ThousandsSeparatorVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val transformed = groupThousands(raw)

        val dotIndex = raw.indexOf('.')
        val intLen = if (dotIndex >= 0) dotIndex else raw.length
        // commasBefore[i] = number of commas inserted before raw index i, for i in 0..intLen.
        val commasBefore = IntArray(intLen + 1)
        for (i in 1..intLen) {
            val digitsAfter = intLen - i
            val extra = if (digitsAfter > 0 && digitsAfter % 3 == 0) 1 else 0
            commasBefore[i] = commasBefore[i - 1] + extra
        }
        val totalCommas = if (intLen == 0) 0 else commasBefore[intLen]
        val transformedIntLen = intLen + totalCommas

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, raw.length)
                return if (o <= intLen) o + commasBefore[o] else o + totalCommas
            }

            override fun transformedToOriginal(offset: Int): Int {
                val o = offset.coerceIn(0, transformed.length)
                return if (o <= transformedIntLen) {
                    var commas = 0
                    for (i in 0 until o) if (transformed[i] == ',') commas++
                    o - commas
                } else {
                    o - totalCommas
                }
            }
        }
        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}
