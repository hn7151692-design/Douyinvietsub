package com.example.ui.screens

import android.net.Uri
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.data.api.RetrofitClient
import com.example.data.model.OcrOverlayItem
import com.example.data.model.SubtitleItem
import com.example.data.model.TranslationHistory
import com.squareup.moshi.Types
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    history: TranslationHistory,
    onBack: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Decode subtitles & ocr overlays from room JSON
    val subtitles = remember(history.subtitlesJson) {
        try {
            val type = Types.newParameterizedType(List::class.java, SubtitleItem::class.java)
            val adapter = RetrofitClient.moshiInstance.adapter<List<SubtitleItem>>(type)
            adapter.fromJson(history.subtitlesJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    val ocrOverlays = remember(history.ocrOverlaysJson) {
        try {
            val type = Types.newParameterizedType(List::class.java, OcrOverlayItem::class.java)
            val adapter = RetrofitClient.moshiInstance.adapter<List<OcrOverlayItem>>(type)
            adapter.fromJson(history.ocrOverlaysJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Initialize ExoPlayer safely
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    // Dispose player when leaving
    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    // Load media uri
    LaunchedEffect(player, history.videoUri) {
        val mediaItem = MediaItem.fromUri(Uri.parse(history.videoUri))
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    // Polling current playback position to update overlays & subtitles
    var currentPosition by remember { mutableStateOf(0L) }
    LaunchedEffect(player) {
        while (true) {
            currentPosition = player.currentPosition
            delay(50) // Poll every 50ms for hyper-precise overlays matching
        }
    }

    // Active subtitle
    val activeSubtitle = remember(subtitles, currentPosition) {
        subtitles.find { currentPosition in it.startTime..it.endTime }
    }

    // Active OCR replacement overlays
    val activeOcrOverlays = remember(ocrOverlays, currentPosition) {
        ocrOverlays.filter { currentPosition in it.startTime..it.endTime }
    }

    var selectedSpeed by remember { mutableStateOf(1.0f) }

    LaunchedEffect(selectedSpeed) {
        player.playbackParameters = PlaybackParameters(selectedSpeed)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp, start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("player_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại"
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = history.videoTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Bản dịch & Phụ đề tự động hoạt động",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onExport,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("export_video_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Xuất",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Xuất Video", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Main Player Canvas containing Video + OCR Overlays
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // ExoPlayer View
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            useController = true
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Subtitle Overlay (Docked elegantly at bottom third)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 44.dp) // Lift up from native exo-controls
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = activeSubtitle != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        activeSubtitle?.let { sub ->
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.75f), shape = RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .widthIn(max = this@BoxWithConstraints.maxWidth * 0.8f)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (sub.originalText.isNotEmpty()) {
                                        Text(
                                            text = sub.originalText,
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 13.sp
                                        )
                                    }
                                    Text(
                                        text = sub.text,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.testTag("active_subtitle_text")
                                    )
                                }
                            }
                        }
                    }
                }

                // OCR Text Replacement Overlay layer
                activeOcrOverlays.forEachIndexed { i, ocr ->
                    val x = maxWidth * (ocr.xPct / 100f)
                    val y = maxHeight * (ocr.yPct / 100f)
                    val w = maxWidth * (ocr.widthPct / 100f)
                    val h = maxHeight * (ocr.heightPct / 100f)

                    Box(
                        modifier = Modifier
                            .offset(x = x, y = y)
                            .size(width = w, height = h)
                            .background(
                                color = Color(android.graphics.Color.parseColor(ocr.bgColor)),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ocr.translatedText,
                            color = Color(android.graphics.Color.parseColor(ocr.textColor)),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("ocr_replacement_$i")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Playback Speed Controller section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Tốc độ đọc",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Điều chỉnh tốc độ phát",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${selectedSpeed}x",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                    speeds.forEach { speed ->
                        val isActive = selectedSpeed == speed
                        OutlinedButton(
                            onClick = { selectedSpeed = speed },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .height(38.dp)
                                .testTag("speed_button_$speed")
                        ) {
                            Text(
                                text = "${speed}x",
                                fontSize = 11.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info details card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Thông tin dịch",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Chi tiết bản dịch video",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Tổng số phụ đề: ${subtitles.size}\n• Số khung chữ thay thế (OCR): ${ocrOverlays.size}\n• Độ dài video: ${history.duration / 1000} giây",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Mẹo: Bạn có thể giảm tốc độ xuống 0.75x để có nhiều thời gian đọc phụ đề Việt ngữ và khớp chữ viết dịch tự nhiên hơn.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
