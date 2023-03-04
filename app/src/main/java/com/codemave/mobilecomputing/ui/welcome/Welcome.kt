package com.codemave.mobilecomputing.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.systemBarsPadding
import com.codemave.mobilecomputing.R
import com.codemave.mobilecomputing.ui.theme.Gray

@Composable
fun Welcome(navController: NavController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "WELCOME\nTO\nPHONEBOSS!",
                color = MaterialTheme.colors.primary,
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(30.dp))
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_app),
                contentDescription = "app_icon",
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(50.dp))
            Button(
                onClick = { navController.navigate("login")},
                enabled = true,
                modifier = Modifier.fillMaxWidth().size(55.dp),
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = "Log In",
                    color = Color.White,
                    fontSize = 30.sp
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = { navController.navigate("signup") },
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(55.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(Gray)
            ) {
                Text(
                    text = "Sign Up",
                    color = MaterialTheme.colors.primary,
                    fontSize = 30.sp
                )
            }
        }
    }
}