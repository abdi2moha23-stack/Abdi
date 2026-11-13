package com.example.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.BlueSecondary
import com.example.ui.theme.BlueSurface
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenLight
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.RedDanger
import java.io.ByteArrayOutputStream
import java.util.Locale

object ImageUtils {
    fun bitmapToBase64(bitmap: Bitmap, maxDimension: Int = 240, quality: Int = 72): String {
        val ratio = minOf(
            maxDimension.toFloat() / bitmap.width.coerceAtLeast(1),
            maxDimension.toFloat() / bitmap.height.coerceAtLeast(1),
            1f
        )
        val w = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val h = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        val scaled = if (ratio < 1f) Bitmap.createScaledBitmap(bitmap, w, h, true) else bitmap
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bmp = BitmapFactory.decodeStream(stream) ?: return null
                bitmapToBase64(bmp)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun base64ToImageBitmap(base64: String): ImageBitmap? {
        if (base64.isBlank()) return null
        return try {
            val cleaned = base64.substringAfter("base64,", base64)
            val bytes = Base64.decode(cleaned, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
fun AvatarImage(
    photoBase64: String,
    name: String,
    size: Dp = 46.dp,
    accentColor: Color = NavyPrimary
) {
    val imageBitmap = remember(photoBase64) { ImageUtils.base64ToImageBitmap(photoBase64) }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(2.dp, accentColor, CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(accentColor, accentColor.copy(alpha = 0.72f))
                    )
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.65f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.trim().take(2).uppercase(Locale.ROOT).ifBlank { "?" },
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontSize = (size.value * 0.34f).sp
            )
        }
    }
}

@Composable
fun PhotoPickerSection(
    photoBase64: String,
    onPhotoChanged: (String) -> Unit,
    label: String,
    lang: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            ImageUtils.uriToBase64(context, uri)?.let { onPhotoChanged(it) }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            onPhotoChanged(ImageUtils.bitmapToBase64(bitmap))
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(
                context,
                if (lang == "so") "Fadlan oggolow kamaradda si sawir looga qaado." else "Camera permission is needed to take a photo.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = BlueSurface.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, BlueSecondary.copy(alpha = 0.28f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AvatarImage(
                    photoBase64 = photoBase64,
                    name = "Photo",
                    size = 56.dp,
                    accentColor = NavyLight
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NavyLight,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (lang == "so") "Sawir Dooro" else "Gallery", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val hasPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPerm) {
                                    cameraLauncher.launch(null)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenAccent,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (lang == "so") "Ka Qaad" else "Camera", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (photoBase64.isNotBlank()) {
                        OutlinedButton(
                            onClick = { onPhotoChanged("") },
                            border = BorderStroke(1.dp, RedDanger.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RedDanger),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (lang == "so") "Ka saar sawirka" else "Remove photo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
