package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubtitleItem(
    val startTime: Long, // in ms
    val endTime: Long,   // in ms
    val text: String,    // translated Vietnamese text
    val originalText: String = "" // original Chinese text
)

@JsonClass(generateAdapter = true)
data class OcrOverlayItem(
    val startTime: Long, // in ms
    val endTime: Long,   // in ms
    val originalText: String,
    val translatedText: String,
    val xPct: Float,      // X percentage of container width (0 to 100)
    val yPct: Float,      // Y percentage of container height (0 to 100)
    val widthPct: Float,  // width percentage (0 to 100)
    val heightPct: Float, // height percentage (0 to 100)
    val bgColor: String = "#E61A1A1A", // Background color to cover Chinese text (hex format)
    val textColor: String = "#FFFFFF"   // Text color for translated text
)

@Entity(tableName = "translation_history")
data class TranslationHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val videoTitle: String,
    val videoUri: String,
    val duration: Long, // in ms
    val createdAt: Long = System.currentTimeMillis(),
    val subtitlesJson: String,  // JSON string of List<SubtitleItem>
    val ocrOverlaysJson: String // JSON string of List<OcrOverlayItem>
)
