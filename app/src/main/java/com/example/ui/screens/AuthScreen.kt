package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.AtharRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: (isNewUser: Boolean) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Tab state: 0 = "LOGIN", 1 = "REGISTER"
    var selectedAuthTab by remember { mutableIntStateOf(0) }

    // Form inputs
    var fullName by remember { mutableStateOf("") }
    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var privacyAccepted by remember { mutableStateOf(false) }

    // Validation errors
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var privacyError by remember { mutableStateOf<String?>(null) }

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 28.dp, bottom = 40.dp)
        ) {
            // 1. Branding Header
            item {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(AtharTealLight.copy(alpha = 0.35f), AtharTealPrimary.copy(alpha = 0.15f))
                            )
                        )
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon_1789845024443),
                        contentDescription = "Athar Brand Logo",
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "منصة أثر | Athar",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AtharTealPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "يحول الأفكار والتجارب إلى معرفة وأثر مستقبلي مستدام 🌱",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 2. Auth Container Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Segmented Tab Switcher: [تسجيل الدخول] | [حساب جديد]
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selectedAuthTab == 0) AtharTealPrimary else Color.Transparent)
                                        .clickable { selectedAuthTab = 0 }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Login,
                                            contentDescription = null,
                                            tint = if (selectedAuthTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "تسجيل الدخول",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (selectedAuthTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selectedAuthTab == 1) AtharTealPrimary else Color.Transparent)
                                        .clickable { selectedAuthTab = 1 }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            tint = if (selectedAuthTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "حساب جديد",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (selectedAuthTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ================= LOGIN FORM =================
                        if (selectedAuthTab == 0) {
                            OutlinedTextField(
                                value = emailOrUsername,
                                onValueChange = {
                                    emailOrUsername = it
                                    emailError = null
                                },
                                label = { Text("البريد الإلكتروني أو اسم المستخدم") },
                                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = AtharTealPrimary) },
                                isError = emailError != null,
                                supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    passwordError = null
                                },
                                label = { Text("كلمة المرور") },
                                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = AtharTealPrimary) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (passwordVisible) "إخفاء" else "إظهار"
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = passwordError != null,
                                supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Remember me & Forgot Password Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = rememberMe,
                                        onCheckedChange = { rememberMe = it },
                                        colors = CheckboxDefaults.colors(checkedColor = AtharTealPrimary)
                                    )
                                    Text("تذكرني", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                TextButton(onClick = { showForgotPasswordDialog = true }) {
                                    Text("نسيت كلمة المرور؟", fontSize = 12.sp, color = AtharTealPrimary, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Submit Login Button
                            Button(
                                onClick = {
                                    var hasError = false
                                    if (emailOrUsername.trim().isEmpty()) {
                                        emailError = "يرجى إدخال البريد الإلكتروني أو اسم المستخدم"
                                        hasError = true
                                    }
                                    if (password.length < 4) {
                                        passwordError = "يرجى إدخال كلمة المرور (4 خانات على الأقل)"
                                        hasError = true
                                    }
                                    if (!hasError) {
                                        AtharRepository.login(emailOrUsername.trim(), password)
                                        Toast.makeText(context, "أهلاً بك مجددًا في أثر ✨", Toast.LENGTH_SHORT).show()
                                        onAuthSuccess(false)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("تسجيل الدخول", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // ================= REGISTER FORM =================
                        if (selectedAuthTab == 1) {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = {
                                    fullName = it
                                    nameError = null
                                },
                                label = { Text("الاسم الكامل") },
                                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = AtharTealPrimary) },
                                isError = nameError != null,
                                supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = emailOrUsername,
                                onValueChange = {
                                    emailOrUsername = it
                                    emailError = null
                                },
                                label = { Text("البريد الإلكتروني") },
                                leadingIcon = { Icon(Icons.Outlined.Mail, contentDescription = null, tint = AtharTealPrimary) },
                                isError = emailError != null,
                                supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    passwordError = null
                                },
                                label = { Text("كلمة المرور (6 خانات فأكثر)") },
                                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = AtharTealPrimary) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (passwordVisible) "إخفاء" else "إظهار"
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = passwordError != null,
                                supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Privacy Checkbox
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = privacyAccepted,
                                    onCheckedChange = {
                                        privacyAccepted = it
                                        privacyError = null
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = AtharTealPrimary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "أوافق على ",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "سياسة الخصوصية وحماية البيانات",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AtharTealPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { showPrivacyDialog = true }
                                )
                            }
                            if (privacyError != null) {
                                Text(
                                    text = privacyError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Submit Register
                            Button(
                                onClick = {
                                    var hasError = false
                                    if (fullName.trim().length < 3) {
                                        nameError = "يرجى إدخال اسم مكون من 3 أحرف على الأقل"
                                        hasError = true
                                    }
                                    if (!emailOrUsername.contains("@") || !emailOrUsername.contains(".")) {
                                        emailError = "يرجى إدخال بريد إلكتروني صحيح"
                                        hasError = true
                                    }
                                    if (password.length < 6) {
                                        passwordError = "كلمة المرور يجب أن لا تقل عن 6 أحرف/أرقام"
                                        hasError = true
                                    }
                                    if (!privacyAccepted) {
                                        privacyError = "يجب الموافقة على سياسة الخصوصية للمتابعة"
                                        hasError = true
                                    }

                                    if (!hasError) {
                                        AtharRepository.register(fullName.trim(), emailOrUsername.trim(), password)
                                        Toast.makeText(context, "تم إنشاء الحساب بنجاح! ننتقل لمرحلة الاهتمامات", Toast.LENGTH_SHORT).show()
                                        onAuthSuccess(true)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("إنشاء الحساب والمتابعة", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick 1-Tap Demo Account Button
                        OutlinedButton(
                            onClick = {
                                AtharRepository.login("ahmed@athar.om", "123456")
                                Toast.makeText(context, "تم الدخول بحساب: أحمد البوسعيدي (تجريبي) ✨", Toast.LENGTH_SHORT).show()
                                onAuthSuccess(false)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = AtharTealPrimary.copy(alpha = 0.08f)
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(AtharTealPrimary, AtharAmberSecondary)),
                                width = 1.2.dp
                            )
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = AtharAmberSecondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تجربة فورية بحساب تجريبي (1-Tap Demo)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AtharTealPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Guest Mode Button
                        TextButton(
                            onClick = {
                                AtharRepository.continueAsGuest()
                                Toast.makeText(context, "مرحبًا بك كزائر في أثر", Toast.LENGTH_SHORT).show()
                                onAuthSuccess(false)
                            }
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "الدخول كزائر لاستكشاف المبادرات",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LockReset, contentDescription = null, tint = AtharTealPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("استعادة كلمة المرور", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("أدخل بريدك الإلكتروني المسجل لإرسال رابط إعادة تعيين كلمة المرور:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("البريد الإلكتروني") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showForgotPasswordDialog = false
                        Toast.makeText(context, "تم إرسال تعليمات الاستعادة إلى بريدك الإلكتروني", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                ) {
                    Text("إرسال الرابط")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = AtharTealPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("سياسة الخصوصية وحماية البيانات", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "في منصة أثر، نلتزم بحماية خصوصيتك ومعلوماتك الشخصية:\n\n" +
                                "1. الشفافية: نطلب فقط البيانات الضرورية لتخصيص التجربة واكتشاف النداءات والمبادرات المناسبة لك.\n" +
                                "2. أمان الموقع الجغرافي: موقعك يستخدم شخصيًا لعرض النداءات القريبة ولا يتم كشف موقعك الدقيق لأي مستخدم آخر على الخريطة.\n" +
                                "3. حماية الهوية: تتيح لك المنصة في لوحة الصدارة الظهور باسمك، أو باسم مستعار، أو كمستخدم مجهول بالكامل.\n" +
                                "4. التحكم بالبيانات: يمكنك تعديل بياناتك أو إيقاف مشاركة الموقع أو حذف حسابك بالكامل في أي وقت من الإعدادات.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        privacyAccepted = true
                        privacyError = null
                        showPrivacyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                ) {
                    Text("موافق ومتابعة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }
}
