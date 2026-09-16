package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppCopyrightFooter(
    isNight: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = "© Studio Of Chase 2026. All Rights Reserved.",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Medium,
        color = if (isNight) Color(0xFF94A3B8).copy(alpha = 0.85f) else Color(0xFF64748B),
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("copyright_footer")
    )
}
