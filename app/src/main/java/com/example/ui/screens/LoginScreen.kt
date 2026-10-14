package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Supervisor
import com.example.data.model.SupervisorDirectory
import com.example.data.remote.LoginResult
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.ErrorRose50
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (Supervisor) -> Unit,
    onLoginWithApi: (suspend (String, String) -> LoginResult)? = null
) {
    var usernameInput by remember { mutableStateOf("Agung") }
    var passwordInput by remember { mutableStateOf("123") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    fun attemptLogin() {
        errorMessage = null
        val userTrimmed = usernameInput.trim()
        val passTrimmed = passwordInput.trim()

        if (userTrimmed.isBlank()) {
            errorMessage = "Silakan masukkan Username / NIK"
            return
        }
        if (passTrimmed.isBlank()) {
            errorMessage = "Silakan masukkan Password / PIN"
            return
        }

        isLoading = true

        if (onLoginWithApi != null) {
            coroutineScope.launch {
                val result = onLoginWithApi(userTrimmed, passTrimmed)
                isLoading = false
                when (result) {
                    is LoginResult.Success -> {
                        // Navigasi & state ditangani secara otomatis oleh ViewModel
                    }
                    is LoginResult.Error -> {
                        errorMessage = result.message
                    }
                }
            }
            return
        }

        // Validasi kredensial via direktori lokal jika lambda API tidak tersedia
        val supervisor = SupervisorDirectory.authenticate(userTrimmed, passTrimmed)
        isLoading = false
        if (supervisor != null && supervisor.isAktif) {
            onLoginSuccess(supervisor)
        } else {
            errorMessage = "Gagal Masuk: Akun tidak terdaftar atau telah dinonaktifkan di Website Admin."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Brand Header & Logo
            Surface(
                modifier = Modifier.size(76.dp),
                shape = RoundedCornerShape(22.dp),
                color = Emerald600, // Emerald Forest #059669
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PrecisionManufacturing,
                        contentDescription = "Logo RKCE",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "RKCE",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate900,
                    letterSpacing = (-0.5).sp
                )
            )

            Text(
                text = "Sistem Manajemen Rencana & Realisasi Alat Berat",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Slate500,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    val isTargetManager = usernameInput.equals("Lutfi", ignoreCase = true) ||
                            usernameInput.contains("manager", ignoreCase = true) ||
                            usernameInput.startsWith("mgr", ignoreCase = true)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTargetManager) "Masuk Sebagai Manager" else "Masuk Sebagai Kasie Lapangan",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isTargetManager) Color(0xFFFEF3C7) else Emerald50
                        ) {
                            Text(
                                text = if (isTargetManager) "MANAGER" else "KASIE AKTIF",
                                color = if (isTargetManager) Color(0xFF92400E) else Emerald900,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isTargetManager) 
                            "Mode Manager: Memantau riwayat SPK dan analitik seluruh armada tanpa form input." 
                        else 
                            "Mode Kasie Lapangan: Formulir rencana & realisasi tersinkron dengan Web Admin.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    // Quick Select Card (Hanya Akun Kasie Utama: Agung)
                    Text(
                        text = "Pilih Akun Sinkron Website Admin:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Akun Kasie Lapangan Aktif: Agung (Full Width Card)
                    val isAgungActive = usernameInput.equals("Agung", ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isAgungActive) Emerald50 else Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isAgungActive) 1.5.dp else 1.dp,
                            if (isAgungActive) Emerald600 else Slate200
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                usernameInput = "Agung"
                                passwordInput = "123"
                                errorMessage = null
                            }
                            .testTag("chip_login_agung")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isAgungActive) Emerald600 else Slate200,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isAgungActive) Color.White else Slate500,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Agung",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAgungActive) Emerald900 else Slate900
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Emerald100
                                        ) {
                                            Text(
                                                text = "KASIE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Emerald900,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Kasie • Geodesi PG1",
                                        fontSize = 12.sp,
                                        color = if (isAgungActive) Emerald700 else Slate500,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isAgungActive) Emerald600 else Slate100
                            ) {
                                Text(
                                    text = if (isAgungActive) "TERPILIH" else "PILIH",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isAgungActive) Color.White else Slate600,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error Notification (Pesat Peringatan Jelas: Akun tidak aktif atau kredensial salah)
                    AnimatedVisibility(visible = errorMessage != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = ErrorRose50,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRose.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = ErrorRose,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = ErrorRose,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Field 1: Username / NIK Akun
                    Text(
                        text = "Username / NIK",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = {
                            usernameInput = it
                            errorMessage = null
                        },
                        placeholder = { Text("Masukkan username (misal: Agung)", color = Slate400) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "User Icon", tint = Emerald600)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald600,
                            unfocusedBorderColor = Slate200,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_login_username")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 2: Password
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            errorMessage = null
                        },
                        placeholder = { Text("Masukkan password Anda", color = Slate400) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Lock Icon", tint = Emerald600)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPasswordVisible) "Sembunyikan sandi" else "Tampilkan sandi",
                                    tint = Slate400
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            attemptLogin()
                        }),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald600,
                            unfocusedBorderColor = Slate200,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_login_password")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Tombol Submit Login
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            attemptLogin()
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_submit_login"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTargetManager) Color(0xFF0F766E) else Emerald600
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Memverifikasi Kredensial...")
                        } else {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTargetManager) "Masuk Sebagai Manager" else "Masuk Sebagai Kasie",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "RKCE • Terintegrasi Otomatis dengan Web Admin API",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 11.sp)
            )
        }
    }
}
