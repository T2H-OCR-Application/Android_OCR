package com.t2h.ocr.ui.screens.auth

import android.R.attr.fontWeight
import android.health.connect.datatypes.ExerciseCompletionGoal
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.Unspecified
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.t2h.ocr.R
import com.t2h.ocr.ui.components.GoogleButton
import com.t2h.ocr.ui.screens.validator.AuthValidator
import androidx.navigation.NavController

@Composable
fun RegisterScreen(paddingValues: PaddingValues,
                   navController: NavController){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmVisible by remember { mutableStateOf(false) }
    var confirmError by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } //loading state khi bấm đăng kí(bấm nút đki sẽ quay quay ở nút đó)
    Box(modifier = Modifier.fillMaxSize()
    ){
        Image(painter = painterResource(R.drawable.auth_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop)

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(modifier = Modifier.height(30.dp))

            //app logo
            Image(
                painter = painterResource(R.drawable.t2h_logo),
                contentDescription = "",
                modifier = Modifier
                    .size(100.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(text = "ĐĂNG KÝ",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFFFFFF)
            )

            Spacer(modifier = Modifier.height(120.dp))

            //ô điền email
            TextField(
                value = email,
                onValueChange = {email = it
                    emailError = AuthValidator.validateEmail(it)} ,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                label = {Text(emailError.ifEmpty { "Email" }, color =if (emailError.isNotEmpty()) Red else Unspecified)},
                placeholder = {Text("Nhập email của bạn")},
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.email_icon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint =  colorResource(R.color.Icon_cl)
                    )
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            )
            Spacer(modifier = Modifier.height(45.dp))

            //điền mật khẩu
            TextField(
                value = password,
                onValueChange = {password = it
                    passwordError = AuthValidator.validatePassword(it)} ,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = {Text(passwordError.ifEmpty { "Mật khẩu" }, color =if (passwordError.isNotEmpty()) Red else Unspecified)},
                placeholder = {Text("Nhập mật khẩu của bạn")},
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.lock_icon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint =  colorResource(R.color.Icon_cl)
                    )
                },
                visualTransformation = if(passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val visible= if(passwordVisible){
                        painterResource(R.drawable.visibility_24dp)
                    }else {
                        painterResource(R.drawable.visibility_off_24dp)
                    }
                    Icon(
                        painter = visible,
                        contentDescription = "",
                        modifier = Modifier.clickable{passwordVisible =!passwordVisible}
                            .size(20.dp)
                    )
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(45.dp))

            TextField(
                value = confirmPassword,
                onValueChange = {confirmPassword = it
                    confirmError = AuthValidator.validateConfirmPassword(it,password)} ,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = {Text(confirmError.ifEmpty { "Xác nhận mật khẩu" },
                    color =if (confirmError.isNotEmpty()) Red else Unspecified)},
                placeholder = {Text("Nhập lại mật khẩu của bạn")},
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.lock_icon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint =  colorResource(R.color.Icon_cl)
                    )
                },
                visualTransformation = if(confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val visible= if(confirmVisible){
                        painterResource(R.drawable.visibility_24dp)
                    }else {
                        painterResource(R.drawable.visibility_off_24dp)
                    }
                    Icon(
                        painter = visible,
                        contentDescription = "",
                        modifier = Modifier.clickable{confirmVisible =!confirmVisible}
                            .size(20.dp),
                    )
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            )

            Spacer(Modifier.height(45.dp))

            Button(onClick = {
                isLoading= true
                emailError = AuthValidator.validateEmail(email)
                passwordError = AuthValidator.validatePassword(password)
                confirmError = AuthValidator.validateConfirmPassword(confirmPassword,password)

                //luồng logic đăng kí
                if(emailError.isEmpty() && passwordError.isEmpty() && confirmError.isEmpty()){
                    //sign up logic
                    navController.navigate("login")
                }
                isLoading=false
            },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.Icon_cl)
                )
            ) {

                //khi bấm đăn kí thì hiện loading
                if(isLoading){
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else{
                    Text(text = "ĐĂNG KÝ",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFFFFF)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(text = "-----OR-----",
                fontSize = 20.sp,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = colorResource(R.color.Unspecified))

            Spacer(Modifier.height(20.dp))

            Text(text = "Continue with",
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = colorResource(R.color.Unspecified)
            )

            Spacer(Modifier.height(10.dp))

            GoogleButton(onClick = { /*TODO*/ })

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Already have an account? Login",

                color = Color.White,

                modifier = Modifier.clickable {

                    navController.navigate("login")

                }
            )
        }
    }


}
