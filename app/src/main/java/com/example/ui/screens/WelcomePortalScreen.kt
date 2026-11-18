package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.example.localization.AppStrings
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun WelcomePortalScreen(
    viewModel: MainViewModel,
    onNavigateToAdmin: () -> Unit,
    onNavigateToTeacher: () -> Unit,
    onNavigateToParent: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val lang by viewModel.language.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val schoolConfig by viewModel.schoolConfig.collectAsState()

    var showAdminPasscodeDialog by remember { mutableStateOf(false) }
    var adminPasscode by remember { mutableStateOf("") }
    var passcodeError by remember { mutableStateOf(false) }

    var showParentRegisterDialog by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser == null) {
            val clientId = try {
                context.getString(R.string.default_web_client_id)
            } catch (_: Exception) {
                null
            }
            if (!clientId.isNullOrBlank()) {
                try {
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(true)
                        .setServerClientId(clientId)
                        .setAutoSelectEnabled(true)
                        .build()
                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()
                    val activityContext = context.findActivity() ?: context
                    val cred = credentialManager.getCredential(activityContext, request).credential
                    if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                        Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                        viewModel.refreshAuthUser()
                    }
                } catch (_: Exception) {
                    // Silent auto sign-in fallback handled by explicit Google button
                }
            }
        }
    }

    fun signInWithGoogle(onSuccessRole: ((String) -> Unit)? = null) {
        if (isSigningIn) return
        authError = null

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            authError = if (lang == "so") {
                "Cilad dhanka habaynta Google Sign-In ah: default_web_client_id lama helin."
            } else {
                "Google Web Client ID missing: ${e.message}"
            }
            return
        }

        val activityContext = context.findActivity() ?: context
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        isSigningIn = true
        scope.launch {
            try {
                val result = credentialManager.getCredential(activityContext, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    Firebase.auth.signInWithCredential(authCredential).await()
                    viewModel.refreshAuthUser()
                    isSigningIn = false
                    authError = null
                    onSuccessRole?.invoke("AUTH_OK")
                } else {
                    authError = if (lang == "so") {
                        "Nooca aqoonsiga Google ee la helay lama aqoonsan."
                    } else {
                        "Unexpected credential format"
                    }
                    isSigningIn = false
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}", e)
                isSigningIn = false
                val msg = e.errorMessage?.toString().orEmpty()
                if (msg.contains("developer", ignoreCase = true) ||
                    msg.contains("sha", ignoreCase = true) ||
                    msg.contains("10", ignoreCase = true) ||
                    msg.contains("28444", ignoreCase = true)
                ) {
                    authError = if (lang == "so") {
                        "Xaqiijinta Google way diiday ($msg). Fadlan isticmaal APK-ga cusub."
                    } else {
                        "Google Sign-In configuration error: $msg"
                    }
                }
            } catch (e: NoCredentialException) {
                Log.w("Auth", "No Google credentials found: ${e.message}", e)
                authError = if (lang == "so") {
                    "Akoon Google oo firfircoon lagama helin taleefankaaga. Fadlan ku dar Gmail."
                } else {
                    "No Google account available on this device. Please add a Google account."
                }
                isSigningIn = false
            } catch (e: GetCredentialException) {
                Log.e("Auth", "Credential Manager error: ${e.type} - ${e.errorMessage}", e)
                authError = e.errorMessage?.toString()
                    ?: e.localizedMessage
                    ?: if (lang == "so") "Gelitaanka Google wuu fashilmay." else "Google Sign-In failed"
                isSigningIn = false
            } catch (e: Exception) {
                Log.e("Auth", "Google Sign-In failed", e)
                authError = e.localizedMessage
                    ?: if (lang == "so") "Gelitaanka Google wuu fashilmay." else "Sign-in failed"
                isSigningIn = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A1636),
                        Color(0xFF152C6B),
                        Color(0xFF0B1E45),
                        Color(0xFF07122B)
                    )
                )
            )
            .drawBehind {
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = 0.14f),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.88f, size.height * 0.08f)
                )
                drawCircle(
                    color = Color(0xFF10B981).copy(alpha = 0.11f),
                    radius = size.width * 0.5f,
                    center = Offset(size.width * 0.08f, size.height * 0.45f)
                )
                drawCircle(
                    color = Color(0xFFF59E0B).copy(alpha = 0.08f),
                    radius = size.width * 0.45f,
                    center = Offset(size.width * 0.85f, size.height * 0.85f)
                )
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Language Switcher Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GreenLight)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = schoolConfig.academicYear,
                        color = Color.White,
                        fontFamily = MonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier.testTag("welcome_language_toggle")
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Surface(
                        onClick = { viewModel.setLanguage("so") },
                        shape = RoundedCornerShape(50),
                        color = if (lang == "so") AmberWarning else Color.Transparent,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Text(
                                "Somali",
                                color = if (lang == "so") Color(0xFF0F172A) else Color.White,
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Surface(
                        onClick = { viewModel.setLanguage("en") },
                        shape = RoundedCornerShape(50),
                        color = if (lang == "en") AmberWarning else Color.Transparent,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Text(
                                "English",
                                color = if (lang == "en") Color(0xFF0F172A) else Color.White,
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Luminous Halo School Emblem
        Box(
            modifier = Modifier
                .size(126.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            AmberWarning.copy(alpha = 0.35f),
                            BlueLight.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .shadow(14.dp, CircleShape, ambientColor = AmberWarning, spotColor = BlueLight)
                    .clip(CircleShape)
                    .border(
                        3.dp,
                        Brush.linearGradient(listOf(AmberWarning, BlueLight, GreenLight)),
                        CircleShape
                    )
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.school_logo),
                    contentDescription = "School Logo",
                    modifier = Modifier
                        .size(94.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // School Name & Motto
        Text(
            text = schoolConfig.name.uppercase(java.util.Locale.ROOT),
            fontFamily = OutfitFontFamily,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = schoolConfig.motto,
            color = Color(0xFF7DD3FC),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )

        // Single School Restriction Pill Badge
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFF1E40AF).copy(alpha = 0.45f),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(listOf(AmberWarning, BlueLight, GreenLight))
            ),
            modifier = Modifier.padding(top = 8.dp, bottom = 22.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Verified,
                    contentDescription = null,
                    tint = AmberWarning,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Single School System • ID: ${schoolConfig.schoolId}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Section Title: Select Portal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.verticalGradient(listOf(AmberWarning, GreenLight)))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = AppStrings.get("select_role", lang),
                fontFamily = OutfitFontFamily,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 3 Vibrant Jewel-Toned Role Cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Role 1: ADMIN (Royal Blue + Golden Amber)
            RolePortalCard(
                title = AppStrings.get("admin", lang),
                portalName = AppStrings.get("admin_portal", lang),
                desc = if (lang == "so") "Maamul ardayda, macallimiinta, fasalada, imtixaannada & lacagaha" else "Manage teachers, students, classes, examinations & finances",
                icon = Icons.Default.AdminPanelSettings,
                accentColor = AmberWarning,
                bgGradient = listOf(Color(0xFF1D4ED8), Color(0xFF1E3A8A), Color(0xFF0F172A)),
                tag = "portal_admin_button",
                actionLabel = if (lang == "so") "Gal" else "Open",
                onClick = {
                    if (currentUser == null) {
                        signInWithGoogle {
                            showAdminPasscodeDialog = true
                        }
                    } else if (userProfile?.role == "ADMIN") {
                        onNavigateToAdmin()
                    } else {
                        showAdminPasscodeDialog = true
                    }
                }
            )

            // Role 2: TEACHER (Electric Sky Blue + Cyan)
            RolePortalCard(
                title = AppStrings.get("teacher", lang),
                portalName = AppStrings.get("teacher_portal", lang),
                desc = if (lang == "so") "Fur fasalkaaga, xaadiriska, buundooyinka ardayda, fiiga & fariimaha" else "Class password login, attendance, marks entry, fees & remarks",
                icon = Icons.Default.School,
                accentColor = BlueLight,
                bgGradient = listOf(Color(0xFF0284C7), Color(0xFF1D4ED8), Color(0xFF172554)),
                tag = "portal_teacher_button",
                actionLabel = if (lang == "so") "Gal" else "Open",
                onClick = {
                    if (currentUser == null) {
                        signInWithGoogle {
                            onNavigateToTeacher()
                        }
                    } else {
                        onNavigateToTeacher()
                    }
                }
            )

            // Role 3: PARENT (Emerald Green + Mint)
            RolePortalCard(
                title = AppStrings.get("parent", lang),
                portalName = AppStrings.get("parent_portal", lang),
                desc = if (lang == "so") "Natiijooyinka carruurtaada, xaadiriska, fiiga & fariimaha macallinka" else "Track child report cards, daily attendance, fee receipts & remarks",
                icon = Icons.Default.FamilyRestroom,
                accentColor = Color(0xFF34D399),
                bgGradient = listOf(Color(0xFF059669), Color(0xFF047857), Color(0xFF064E3B)),
                tag = "portal_parent_button",
                actionLabel = if (lang == "so") "Gal" else "Open",
                onClick = {
                    if (currentUser == null) {
                        signInWithGoogle {
                            if (viewModel.hasCreatedParentRoom()) {
                                onNavigateToParent()
                            } else {
                                showParentRegisterDialog = true
                            }
                        }
                    } else if (viewModel.hasCreatedParentRoom()) {
                        onNavigateToParent()
                    } else {
                        showParentRegisterDialog = true
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Active Sign-in status / Glossy Google Button
        if (currentUser == null) {
            Surface(
                onClick = { signInWithGoogle() },
                enabled = !isSigningIn,
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                border = BorderStroke(1.5.dp, Brush.horizontalGradient(GradientRoyalBlue)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .height(54.dp)
                    .testTag("google_sign_in_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = NavyLight)
                    } else {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(GradientRoyalBlue)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            AppStrings.get("sign_in_google", lang),
                            fontFamily = OutfitFontFamily,
                            color = NavyPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(GradientEmerald)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentUser?.displayName ?: "User",
                                fontFamily = OutfitFontFamily,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = currentUser?.email ?: "",
                                color = Color(0xFFBAE6FD),
                                fontSize = 12.sp
                            )
                        }
                    }

                    VibrantGradientButton(
                        text = AppStrings.get("sign_out", lang),
                        onClick = { viewModel.signOut {} },
                        gradient = GradientCrimson,
                        height = 38.dp,
                        fontSize = 12,
                        modifier = Modifier.testTag("welcome_sign_out_button")
                    )
                }
            }
        }

        if (authError != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = RedDanger.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, RedLight.copy(alpha = 0.6f)),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = authError ?: "",
                    color = Color(0xFFFECACA),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    // Admin Passcode Dialog
    if (showAdminPasscodeDialog) {
        AlertDialog(
            onDismissRequest = { showAdminPasscodeDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(GradientRoyalBlue)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        AppStrings.get("admin_portal", lang),
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                }
            },
            text = {
                Column {
                    Text(
                        if (lang == "so")
                            "Geli furaha sirta ah ee maamulka (Master Passcode: IFTIN#2026):"
                        else
                            "Enter the administrator security passcode (Master Passcode: IFTIN#2026):",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = adminPasscode,
                        onValueChange = {
                            adminPasscode = it
                            passcodeError = false
                        },
                        label = { Text(AppStrings.get("admin_access_code", lang)) },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = passcodeError,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_passcode_input")
                    )
                    if (passcodeError) {
                        Text(
                            AppStrings.get("invalid_passcode", lang),
                            color = RedDanger,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                VibrantGradientButton(
                    text = AppStrings.get("verify", lang),
                    icon = Icons.Default.VerifiedUser,
                    gradient = GradientRoyalBlue,
                    height = 42.dp,
                    onClick = {
                        viewModel.verifyAdminPasscode(
                            enteredPasscode = adminPasscode,
                            onSuccess = {
                                showAdminPasscodeDialog = false
                                onNavigateToAdmin()
                            },
                            onError = { passcodeError = true }
                        )
                    },
                    modifier = Modifier.testTag("admin_passcode_submit")
                )
            },
            dismissButton = {
                TextButton(onClick = { showAdminPasscodeDialog = false }) {
                    Text(AppStrings.get("cancel", lang), color = RedDanger, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // Parent Room Creation Dialog
    if (showParentRegisterDialog) {
        AlertDialog(
            onDismissRequest = { showParentRegisterDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(GradientEmerald)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        AppStrings.get("create_parent_room", lang),
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = GreenAccent
                    )
                }
            },
            text = {
                Text(
                    if (lang == "so")
                        "Ma doonaysaa in aad samaysato qolka waalidka ee Iftin Academy si aad ula socoto carruurtaada?"
                    else
                        "Do you want to create your Parent Room for Iftin Academy to monitor your children's progress?",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                VibrantGradientButton(
                    text = AppStrings.get("create_parent_room", lang),
                    icon = Icons.Default.CheckCircle,
                    gradient = GradientEmerald,
                    height = 42.dp,
                    onClick = {
                        viewModel.createParentRoom {
                            showParentRegisterDialog = false
                            onNavigateToParent()
                        }
                    },
                    modifier = Modifier.testTag("create_parent_room_confirm")
                )
            },
            dismissButton = {
                TextButton(onClick = { showParentRegisterDialog = false }) {
                    Text(AppStrings.get("cancel", lang), color = RedDanger, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@Composable
fun RolePortalCard(
    title: String,
    portalName: String,
    desc: String,
    icon: ImageVector,
    accentColor: Color,
    bgGradient: List<Color>,
    tag: String,
    actionLabel: String = "Open",
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "role_card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(10.dp, RoundedCornerShape(20.dp), ambientColor = bgGradient.first(), spotColor = accentColor)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = accentColor),
                onClick = onClick
            )
            .testTag(tag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.horizontalGradient(bgGradient))
                .border(1.5.dp, accentColor.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                .drawBehind {
                    drawCircle(
                        color = accentColor.copy(alpha = 0.14f),
                        radius = size.height * 0.9f,
                        center = Offset(size.width * 0.95f, size.height * 0.2f)
                    )
                }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(accentColor.copy(alpha = 0.22f))
                        .border(1.2.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            fontFamily = OutfitFontFamily,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = accentColor.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = portalName,
                                color = accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = desc,
                        color = Color.White.copy(alpha = 0.86f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = accentColor,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = actionLabel,
                            color = Color(0xFF0F172A),
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
