package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.TranslationProgressScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ExportUiState
import com.example.ui.viewmodel.TranslationUiState
import com.example.ui.viewmodel.TranslationViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TranslationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val historyList by viewModel.historyList.collectAsState()
                val translationState by viewModel.translationState.collectAsState()
                val selectedHistoryItem by viewModel.selectedHistoryItem.collectAsState()
                val exportState by viewModel.exportState.collectAsState()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold")
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = Pair(selectedHistoryItem, translationState),
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_transition"
                        ) { (historyItem, transState) ->
                            when {
                                transState is TranslationUiState.Processing -> {
                                    TranslationProgressScreen(
                                        message = transState.message,
                                        progress = transState.progress,
                                        onCancel = { viewModel.resetTranslationState() }
                                    )
                                }
                                historyItem != null -> {
                                    PlayerScreen(
                                        history = historyItem,
                                        onBack = { viewModel.selectHistoryItem(null) },
                                        onExport = { viewModel.exportVideo(historyItem) }
                                    )
                                }
                                else -> {
                                    DashboardScreen(
                                        historyList = historyList,
                                        onStartTranslation = { uri, title ->
                                            viewModel.startTranslation(uri, title)
                                        },
                                        onSelectHistory = { viewModel.selectHistoryItem(it) },
                                        onDeleteHistory = { viewModel.deleteHistoryItem(it) },
                                        onClearAllHistory = { viewModel.clearAllHistory() }
                                    )
                                }
                            }
                        }

                        // Export Progress / Dialog Layer
                        ExportOverlay(
                            exportState = exportState,
                            onDismiss = { viewModel.resetExportState() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExportOverlay(
    exportState: ExportUiState,
    onDismiss: () -> Unit
) {
    when (exportState) {
        is ExportUiState.Idle -> { /* Do nothing */ }
        is ExportUiState.Exporting -> {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("exporting_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "Đang xuất video...",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        LinearProgressIndicator(
                            progress = { exportState.progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = exportState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        is ExportUiState.Success -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Thành công",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "Xuất Video Thành Công!",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Bản dịch video đã được kết xuất và lưu thành công về máy của bạn.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Đường dẫn lưu tệp:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "• Video: ${exportState.videoPath}\n• Phụ đề SRT: ${exportState.srtPath ?: "Không có"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mẹo: Bạn có thể sao chép hai tệp này sang máy tính hoặc dùng trình phát video VLC trên điện thoại để tự động phát phụ đề rời một cách đồng bộ.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = 16.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("export_success_ok_button")
                    ) {
                        Text("Đóng")
                    }
                }
            )
        }
        is ExportUiState.Error -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Lỗi",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "Xuất Video Thất Bại",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Đã xảy ra lỗi trong quá trình kết xuất video: ${exportState.message}. Vui lòng thử lại.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}
