package com.example.tisunga.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.AuthViewModel

@Composable
fun CreateAccountScreen(navController: NavController, viewModel: AuthViewModel) {
    var phone by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val onContinue = {
        if (phone.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty()) {
            viewModel.register(firstName, middleName, lastName, phone)
            navController.navigate(Routes.VERIFICATION)
        } else {
            showError = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_desc),
                    tint = NavyBlue
                )
            }
            Text(
                text = stringResource(R.string.create_account_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue
            )
        }

        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { input -> 
                        if (input.all { it.isDigit() }) {
                            phone = input
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    label = { Text(stringResource(R.string.phone_number_label)) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = NavyBlue,
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    label = { Text(stringResource(R.string.first_name_label)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = NavyBlue,
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = middleName,
                    onValueChange = { middleName = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    label = { Text(stringResource(R.string.middle_name_label)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = NavyBlue,
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    label = { Text(stringResource(R.string.last_name_label)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = NavyBlue,
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { onContinue() }
                    )
                )
                
                if (showError) {
                    Text(
                        text = stringResource(R.string.error_fill_all_fields),
                        color = RedAccent,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text(stringResource(R.string.continue_button), color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text(stringResource(R.string.already_have_account_text), fontSize = 14.sp, color = TextSecondary)
                    TextButton(onClick = { navController.navigate(Routes.SIGN_IN) }) {
                        Text(stringResource(R.string.signin_link), color = NavyBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = stringResource(R.string.terms_and_privacy),
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}
