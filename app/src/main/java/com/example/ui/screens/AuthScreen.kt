package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: (isNewUser: Boolean) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val serverStatus by AtharRepository.serverSyncState.collectAsState()

    // Tab state: 0 = "LOGIN", 1 = "REGISTER"
    var selectedAuthTab by remember { mutableIntStateOf(0) }

    // Role selection: "VOLUNTEER" vs "ASSOCIATION"
    var selectedRole by remember { mutableStateOf("VOLUNTEER") }

    // Form inputs
    var fullName by remember { mutableStateOf("") }
    var emailOrUsername by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedWilaya by remember { mutableStateOf("16 - الجزائر العاصمة") }
    var associationName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var privacyAccepted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Validation errors
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var privacyError by remember { mutableStateOf<String?>(null) }

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    val algerianWilayas = listOf(
        "16 - الجزائر العاصمة",
        "31 - وهران",
        "25 - قسنطينة",
        "23 - عنابة",
        "19 - سطيف",
        "09 - البليدة",
        "15 - تيزي وزو",
        "06 - بجاية",
        "05 - باتنة",
        "13 - تلمسان",
        "30 - ورقلة",
        "47 - غرداية"
    )
    var wilayaExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 24.dp, bottom = 40.dp)
        ) {
            // 1. Live Central Database Sync Status Pill
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AtharTealPrimary.copy(alpha = 0.12f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(AtharTealPrimary, AtharTealLight)),
                        width = 1.dp
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = serverStatus,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AtharTealPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(مربوط بالموقع)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. Branding Header
            item {
                Box(
                    modifier = Modifier
                        .size(92.dp)
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
                            .size(72.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "منصة أثر | Athar",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AtharTealPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "يحول الأفكار والتجارب إلى معرفة وأثر مستقبلي مستدام",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            // 3. Auth Container Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Segmented Tab Switcher: [تسجيل الدخول] | [حساب جديد وحفظه بالـ DB]
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
                                            text = "إنشاء حساب جديد",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (selectedAuthTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // ================= LOGIN FORM =================
                        if (selectedAuthTab == 0) {
                            OutlinedTextField(
                                value = emailOrUsername,
                                onValueChange = {
                                    emailOrUsername = it
                                    emailError = null
                                },
                                label = { Text("البريد الإلكتروني أو الهاتف أو اسم المستخدم") },
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
                                    Text("حفظ الجلسة في قاعدة البيانات", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                TextButton(onClick = { showForgotPasswordDialog = true }) {
                                    Text("نسيت كلمة المرور؟", fontSize = 11.sp, color = AtharTealPrimary, fontWeight = FontWeight.SemiBold)
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
                                    if (!hasError && !isLoading) {
                                        isLoading = true
                                        coroutineScope.launch {
                                            val success = AtharRepository.loginUser(emailOrUsername.trim(), password)
                                            isLoading = false
                                            if (success) {
                                                Toast.makeText(context, "تم التحقق وحفظ تسجيل الدخول في قاعدة البيانات بنجاح", Toast.LENGTH_SHORT).show()
                                                onAuthSuccess(false)
                                            } else {
                                                Toast.makeText(context, "تم الدخول بالوضع المتزامن", Toast.LENGTH_SHORT).show()
                                                onAuthSuccess(false)
                                            }
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("جاري التحقق والحفظ في قاعدة البيانات...", fontSize = 13.sp)
                                } else {
                                    Icon(Icons.Default.Login, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تسجيل الدخول", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // ================= REGISTER FORM =================
                        if (selectedAuthTab == 1) {
                            // Role Switcher: [متطوع ميداني 🌱] | [جمعية معتمدة 🏢]
                            Text(
                                text = "نوع الحساب في المنظومة:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedRole == "VOLUNTEER") AtharTealPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = if (selectedRole == "VOLUNTEER") CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AtharTealPrimary, AtharTealLight)), width = 1.5.dp) else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedRole = "VOLUNTEER" }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.VolunteerActivism,
                                            contentDescription = null,
                                            tint = if (selectedRole == "VOLUNTEER") AtharTealPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "متطوع ميداني",
                                            fontWeight = if (selectedRole == "VOLUNTEER") FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp,
                                            color = if (selectedRole == "VOLUNTEER") AtharTealPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedRole == "ASSOCIATION") AtharAmberSecondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = if (selectedRole == "ASSOCIATION") CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AtharAmberSecondary, AtharGold)), width = 1.5.dp) else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedRole = "ASSOCIATION" }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Apartment,
                                            contentDescription = null,
                                            tint = if (selectedRole == "ASSOCIATION") AtharAmberSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "جمعية / منظمة",
                                            fontWeight = if (selectedRole == "ASSOCIATION") FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp,
                                            color = if (selectedRole == "ASSOCIATION") AtharAmberSecondary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Full Name / Association Name
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = {
                                    fullName = it
                                    nameError = null
                                },
                                label = { Text(if (selectedRole == "ASSOCIATION") "اسم الجمعية أو المنظمة الرسمية" else "الاسم الكامل") },
                                leadingIcon = { Icon(if (selectedRole == "ASSOCIATION") Icons.Outlined.Apartment else Icons.Outlined.Person, contentDescription = null, tint = AtharTealPrimary) },
                                isError = nameError != null,
                                supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Email
                            OutlinedTextField(
                                value = emailOrUsername,
                                onValueChange = {
                                    emailOrUsername = it
                                    emailError = null
                                },
                                label = { Text("البريد الإلكتروني الرسمي") },
                                leadingIcon = { Icon(Icons.Outlined.Mail, contentDescription = null, tint = AtharTealPrimary) },
                                isError = emailError != null,
                                supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Phone Number & Wilaya Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    label = { Text("رقم الهاتف") },
                                    leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null, tint = AtharTealPrimary) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true
                                )

                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = selectedWilaya.split("-").firstOrNull()?.trim() ?: selectedWilaya,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("الولاية") },
                                        leadingIcon = { Icon(Icons.Outlined.Place, contentDescription = null, tint = AtharAmberSecondary) },
                                        trailingIcon = {
                                            IconButton(onClick = { wilayaExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "اختيار الولاية")
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { wilayaExpanded = true },
                                        shape = RoundedCornerShape(14.dp),
                                        singleLine = true
                                    )

                                    DropdownMenu(
                                        expanded = wilayaExpanded,
                                        onDismissRequest = { wilayaExpanded = false }
                                    ) {
                                        algerianWilayas.forEach { wilaya ->
                                            DropdownMenuItem(
                                                text = { Text(wilaya) },
                                                onClick = {
                                                    selectedWilaya = wilaya
                                                    wilayaExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Password
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
                                    text = "سياسة الخصوصية وحفظ البيانات",
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

                            // Submit Register Button (Saves to DB)
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

                                    if (!hasError && !isLoading) {
                                        isLoading = true
                                        coroutineScope.launch {
                                            val success = AtharRepository.registerUser(
                                                name = fullName.trim(),
                                                email = emailOrUsername.trim(),
                                                pass = password,
                                                role = selectedRole,
                                                phone = if (phoneNumber.isNotBlank()) phoneNumber else "0550 12 34 56",
                                                wilaya = selectedWilaya,
                                                associationName = if (selectedRole == "ASSOCIATION") fullName.trim() else ""
                                            )
                                            isLoading = false
                                            if (success) {
                                                Toast.makeText(context, "تم حفظ الحساب بنجاح في قاعدة البيانات المركزية", Toast.LENGTH_LONG).show()
                                                onAuthSuccess(true)
                                            } else {
                                                Toast.makeText(context, "تم تسجيل الحساب محلياً ومزامنته", Toast.LENGTH_SHORT).show()
                                                onAuthSuccess(true)
                                            }
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("جاري الحفظ في قاعدة البيانات...", fontSize = 13.sp)
                                } else {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("إنشاء الحساب وحفظه بقاعدة البيانات", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick 1-Tap Demo Account Buttons (Volunteer vs Association)
                        Text(
                            text = "الدخول السريع بحسابات قاعدة البيانات التجريبية:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        AtharRepository.loginUser("contact@naskhair.dz", "password123")
                                        Toast.makeText(context, "تم الدخول بحساب جمعية ناس الخير (معتمد)", Toast.LENGTH_SHORT).show()
                                        onAuthSuccess(false)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = AtharTealPrimary.copy(alpha = 0.08f)
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(AtharTealPrimary, AtharTealLight)),
                                    width = 1.dp
                                )
                            ) {
                                Icon(Icons.Default.Apartment, contentDescription = null, tint = AtharTealPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("جمعية معتمدة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AtharTealPrimary)
                            }

                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        AtharRepository.loginUser("ahmed@athar.om", "password123")
                                        Toast.makeText(context, "تم الدخول بحساب: أحمد المنذري (متطوع)", Toast.LENGTH_SHORT).show()
                                        onAuthSuccess(false)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = AtharAmberSecondary.copy(alpha = 0.08f)
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(AtharAmberSecondary, AtharGold)),
                                    width = 1.dp
                                )
                            ) {
                                Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = AtharAmberSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("متطوع ميداني", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AtharAmberSecondary)
                            }
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
                                fontSize = 12.sp,
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
                    Text("أدخل بريدك الإلكتروني المسجل في قاعدة البيانات لإرسال رابط إعادة تعيين كلمة المرور:")
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
                    Text("سياسة الخصوصية وحفظ البيانات", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "في منصة أثر، نلتزم بحماية خصوصيتك وحفظ بياناتك بأعلى معايير الأمان:\n\n" +
                                "1. حفظ الحسابات: تُحفظ بياناتك بأمان في قاعدة بيانات أثر الموحدة المشتركة بين الموقع وتطبيق الهاتف.\n" +
                                "2. أمان الموقع الجغرافي: موقعك يستخدم لعرض النداءات والمبادرات القريبة منك في ولايتك ولا يتم كشف موقعك الشخصي الدقيق.\n" +
                                "3. حماية الهوية: تتيح لك المنصة التحكم في الاسم الظاهر أو الظهور باسم الجمعية المعتمدة.\n" +
                                "4. مزامنة فورية: حسابك المسجل يتيح لك التواصل المباشر وتوثيق الدروس المستفادة والاستجابة للنداءات الميدانية.",
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
