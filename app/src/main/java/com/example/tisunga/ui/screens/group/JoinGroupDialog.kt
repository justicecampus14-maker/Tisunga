package com.example.tisunga.ui.screens.group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel

@Composable
fun JoinGroupDialog(onDismiss: () -> Unit, viewModel: GroupViewModel) {
    var groupCode by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.enter_group_code_title),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = Bold,
                        fontSize = 20.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = groupCode,
                    onValueChange = { groupCode = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 32.sp,
                        fontWeight = Bold,
                        color = TextSecondary
                    ),
                    placeholder = { Text(stringResource(R.string.group_code_hint), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFFFF0F0),
                        focusedContainerColor = Color(0xFFFFF0F0),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.or_text), fontWeight = Bold, fontSize = 14.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clickable { /* QR Scanner logic */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.QrCode2, null, modifier = Modifier.size(60.dp), tint = NavyBlue)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        stringResource(R.string.ok_button),
                        modifier = Modifier.clickable { 
                            if (groupCode.isNotEmpty()) {
                                viewModel.joinGroup(groupCode)
                            }
                        },
                        fontWeight = Bold,
                        fontSize = 24.sp,
                        color = TextPrimary
                    )
                }
                
                if (uiState.errorMessage.isNotEmpty()) {
                    Text(uiState.errorMessage, color = RedAccent, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}
