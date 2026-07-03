package com.example.data.repository

import com.example.data.model.OcrOverlayItem
import com.example.data.model.SubtitleItem
import com.example.data.model.TranslationHistory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

object PresetSamples {
    private val moshi = Moshi.Builder().build()
    private val subtitleListType = Types.newParameterizedType(List::class.java, SubtitleItem::class.java)
    private val ocrListType = Types.newParameterizedType(List::class.java, OcrOverlayItem::class.java)

    private val subtitleAdapter = moshi.adapter<List<SubtitleItem>>(subtitleListType)
    private val ocrAdapter = moshi.adapter<List<OcrOverlayItem>>(ocrListType)

    val SAMPLES = listOf(
        SampleVideo(
            title = "[Mẫu 1] Hướng dẫn nấu Đậu Hũ Ma Bà Tứ Xuyên (四川麻婆豆腐)",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            duration = 15000, // 15 seconds
            subtitles = listOf(
                SubtitleItem(0, 3000, "Chào mừng mọi người đến với kênh nấu ăn Tứ Xuyên!", "欢迎大家来到四川美食频道！"),
                SubtitleItem(3000, 6000, "Hôm nay tôi sẽ hướng dẫn các bạn làm món Đậu Hũ Ma Bà truyền thống.", "今天我来教大家做一道正宗的麻婆豆腐。"),
                SubtitleItem(6000, 10000, "Đầu tiên chúng ta cần chuẩn bị đậu hũ non và thịt bò băm.", "首先，我们需要准备嫩豆腐和碎牛肉。"),
                SubtitleItem(10000, 13000, "Xào thịt bò cho thơm trước khi cho gia vị vào.", "在放调料之前，先要把牛肉炒香。"),
                SubtitleItem(13000, 15000, "Thế là món đậu hũ cay nồng đã sẵn sàng thưởng thức rồi!", "香辣浓郁的麻婆豆腐就可以出锅享用了！")
            ),
            ocrOverlays = listOf(
                OcrOverlayItem(
                    startTime = 500,
                    endTime = 4000,
                    originalText = "四川麻婆豆腐教學",
                    translatedText = "HƯỚNG DẪN NẤU ĐẬU HŨ MA BÀ",
                    xPct = 15f,
                    yPct = 12f,
                    widthPct = 70f,
                    heightPct = 8f,
                    bgColor = "#E6D32F2F", // Red theme
                    textColor = "#FFFFFF"
                ),
                OcrOverlayItem(
                    startTime = 4500,
                    endTime = 8500,
                    originalText = "食材准备: 嫩豆腐, 牛肉碎",
                    translatedText = "Nguyên liệu: Đậu hũ non, Thịt bò băm",
                    xPct = 10f,
                    yPct = 75f,
                    widthPct = 80f,
                    heightPct = 10f,
                    bgColor = "#CC151515",
                    textColor = "#FFEB3B" // Yellow text
                ),
                OcrOverlayItem(
                    startTime = 9000,
                    endTime = 14500,
                    originalText = "秘诀: 大火爆炒, 慢火焖入味",
                    translatedText = "Bí quyết: Xào lửa lớn, om nhỏ lửa cho ngấm",
                    xPct = 20f,
                    yPct = 25f,
                    widthPct = 60f,
                    heightPct = 12f,
                    bgColor = "#E64E342E", // Brown theme
                    textColor = "#FFFFFF"
                )
            )
        ),
        SampleVideo(
            title = "[Mẫu 2] Khám phá Nghệ thuật Trà Đạo Trung Hoa (中国茶文化)",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            duration = 14000, // 14 seconds
            subtitles = listOf(
                SubtitleItem(0, 3500, "Văn hóa trà Trung Hoa đã có lịch sử hàng ngàn năm.", "中国茶文化已经有几千年的历史了。"),
                SubtitleItem(3500, 7000, "Trà không chỉ là một thức uống, mà còn là một lối sống thanh tao.", "茶不仅是一种饮料，更 section 是一种高雅的生活方式。"),
                SubtitleItem(7000, 11000, "Hôm nay chúng ta sẽ thưởng thức trà Long Tỉnh Tây Hồ.", "今天我们来品尝一下西湖龙井茶。"),
                SubtitleItem(11000, 14000, "Cùng tịnh tâm và cảm nhận hương thơm thuần khiết này.", "让我们静下心来，感受这份纯净的的茶香。")
            ),
            ocrOverlays = listOf(
                OcrOverlayItem(
                    startTime = 500,
                    endTime = 5000,
                    originalText = "中国传统茶艺演示",
                    translatedText = "Nghệ Thuật Trà Đạo Truyền Thống Trung Hoa",
                    xPct = 10f,
                    yPct = 15f,
                    widthPct = 80f,
                    heightPct = 10f,
                    bgColor = "#E62E7D32", // Green theme
                    textColor = "#FFFFFF"
                ),
                OcrOverlayItem(
                    startTime = 6000,
                    endTime = 10500,
                    originalText = "西湖龙井: 色翠、香郁、味甘",
                    translatedText = "Trà Long Tỉnh: Sắc xanh, hương nồng, vị ngọt",
                    xPct = 15f,
                    yPct = 80f,
                    widthPct = 70f,
                    heightPct = 8f,
                    bgColor = "#CC1A1A1A",
                    textColor = "#81C784"
                ),
                OcrOverlayItem(
                    startTime = 11000,
                    endTime = 13800,
                    originalText = "静心品茗",
                    translatedText = "TĨNH TÂM THƯỞNG TRÀ",
                    xPct = 35f,
                    yPct = 45f,
                    widthPct = 30f,
                    heightPct = 10f,
                    bgColor = "#E63E2723",
                    textColor = "#FFCC80"
                )
            )
        ),
        SampleVideo(
            title = "[Mẫu 3] Cuộc Hội thoại Giao tiếp Tiếng Trung Hàng ngày (日常中文)",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            duration = 15000, // 15 seconds
            subtitles = listOf(
                SubtitleItem(0, 3000, "A: Chào bạn! Đã lâu không gặp, dạo này khỏe không?", "A: 你好！好久不见，最近怎么样？"),
                SubtitleItem(3000, 6000, "B: Chào cậu! Tớ vẫn khỏe, dạo này công việc hơi bận rộn.", "B: 你好！我挺好的，最近工作有点忙。"),
                SubtitleItem(6000, 9500, "A: Cuối tuần này cậu có rảnh không? Cùng đi uống cà phê nhé.", "A: 这个周末你有空吗？我们一起去喝咖啡吧。"),
                SubtitleItem(9500, 12500, "B: Ý kiến hay đó! Tớ rảnh cả ngày thứ Bảy.", "B: 好主意！我周六整天都有空。"),
                SubtitleItem(12500, 15000, "A: Vậy hẹn gặp cậu lúc 9 giờ sáng thứ Bảy tại quán cũ nhé!", "A: 那我们周六早上九点老地方见！")
            ),
            ocrOverlays = listOf(
                OcrOverlayItem(
                    startTime = 200,
                    endTime = 4500,
                    originalText = "第一课: 朋友相遇与问候",
                    translatedText = "BÀI 1: BẠN BÈ GẶP GỠ & CHÀO HỎI",
                    xPct = 15f,
                    yPct = 10f,
                    widthPct = 70f,
                    heightPct = 8f,
                    bgColor = "#E61565C0", // Blue theme
                    textColor = "#FFFFFF"
                ),
                OcrOverlayItem(
                    startTime = 5000,
                    endTime = 9000,
                    originalText = "常用句型: ...你有空吗？",
                    translatedText = "Mẫu câu: Bạn có rảnh ... không?",
                    xPct = 20f,
                    yPct = 25f,
                    widthPct = 60f,
                    heightPct = 8f,
                    bgColor = "#CC1F1F1F",
                    textColor = "#FF4081"
                ),
                OcrOverlayItem(
                    startTime = 10000,
                    endTime = 14500,
                    originalText = "约定口语: 老地方见",
                    translatedText = "Thành ngữ: Hẹn gặp ở quán cũ (chỗ cũ)",
                    xPct = 15f,
                    yPct = 75f,
                    widthPct = 70f,
                    heightPct = 9f,
                    bgColor = "#CC3F51B5",
                    textColor = "#FFFFFF"
                )
            )
        )
    )

    fun getSampleByUrl(url: String): SampleVideo? {
        return SAMPLES.find { it.url == url }
    }

    fun toTranslationHistory(sample: SampleVideo): TranslationHistory {
        val subsJson = subtitleAdapter.toJson(sample.subtitles)
        val ocrsJson = ocrAdapter.toJson(sample.ocrOverlays)
        return TranslationHistory(
            videoTitle = sample.title,
            videoUri = sample.url,
            duration = sample.duration,
            subtitlesJson = subsJson,
            ocrOverlaysJson = ocrsJson
        )
    }
}

data class SampleVideo(
    val title: String,
    val url: String,
    val duration: Long,
    val subtitles: List<SubtitleItem>,
    val ocrOverlays: List<OcrOverlayItem>
)
