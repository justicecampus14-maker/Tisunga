package com.example.tisunga.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

@Composable
fun MyGroupCard(group: Group, onClick: () -> Unit, onSaveClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFD4E6B5)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(group.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = GreenAccent)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(group.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Group savings : MK ${group.totalSavings}", fontSize = 12.sp, color = TextSecondary)
                    Text("My Savings: MK ${group.mySavings}", fontSize = 12.sp, color = TextSecondary)
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
                        group.description,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Text(
                    "Save Now",
                    color = BlueLink,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSaveClick() }
                )
            }
        }
    }
}

@Composable
fun DiscoverGroupItemCard(group: Group, onJoinClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    modifier = Modifier.size(70.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFD4E6B5)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(group.name.take(1).uppercase(), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = GreenAccent)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(group.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = group.location, fontSize = 12.sp, color = TextSecondary)
                    Text(text = "Period: ${group.savingPeriod} months", fontSize = 12.sp, color = TextSecondary)
                    Text(text = "Min: MK ${group.minContribution}", fontSize = 12.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(GreenAccent, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open", fontSize = 12.sp, color = GreenAccent)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${group.maxMembers} members max", fontSize = 10.sp, color = TextSecondary)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(color = BackgroundGray, thickness = 1.dp)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Meetings: ${group.meetingDay} @ ${group.meetingTime}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Button(
                    onClick = onJoinClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text("View and Join", color = White, fontSize = 12.sp)
                }
            }
        }
    }
}
