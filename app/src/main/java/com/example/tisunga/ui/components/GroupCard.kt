package com.example.tisunga.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tisunga.data.model.Group
import com.example.tisunga.ui.theme.*
import com.example.tisunga.utils.FormatUtils

@Composable
fun GroupCard(group: Group, onClick: () -> Unit, onSaveNowClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFD4E6B5)
                ) {
                    // Placeholder for group logo
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(group.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Total Savings", fontSize = 10.sp, color = TextSecondary)
                            Box(
                                modifier = Modifier
                                    .background(BackgroundGray, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    FormatUtils.formatMoney(group.totalSavings),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyBlue
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("My Savings", fontSize = 10.sp, color = TextSecondary)
                            Box(
                                modifier = Modifier
                                    .background(BackgroundGray, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    FormatUtils.formatMoney(group.mySavings),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenAccent
                                )
                            }
                        }
                    }
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, NavyBlue),
                    color = Color.Transparent
                ) {
                    Text(
                        group.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = NavyBlue,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = BackgroundGray, shape = RoundedCornerShape(20.dp)) {
                    Text(
                        "description",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Text(
                    "Save Now",
                    color = BlueLink,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSaveNowClick() }
                )
            }
        }
    }
}
