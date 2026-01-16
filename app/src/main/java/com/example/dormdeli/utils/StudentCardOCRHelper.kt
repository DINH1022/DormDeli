package com.example.dormdeli.utils

import android.content.Context
import android.graphics.Bitmap
import com.example.dormdeli.model.StudentCardInfo
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class StudentCardOCRHelper(private val context: Context) {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val barcodeScanner = BarcodeScanning.getClient()

    suspend fun processStudentCard(bitmap: Bitmap): StudentCardInfo {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        // 1. Quét mã vạch (Ưu tiên vì độ chính xác tuyệt đối cho MSSV)
        val barcodes = try {
            barcodeScanner.process(image).await()
        } catch (e: Exception) {
            null
        }
        val barcodeData = barcodes?.firstOrNull()?.rawValue

        // 2. Nhận diện văn bản (OCR)
        val visionText = try {
            textRecognizer.process(image).await()
        } catch (e: Exception) {
            null
        }

        var extractedId: String? = barcodeData
        var extractedName: String? = null
        var extractedUniversity: String? = null

        visionText?.textBlocks?.forEach { block ->
            val text = block.text.trim()
            val textLower = text.lowercase()

            // Tìm Trường (Nếu chứa từ khóa Khoa học tự nhiên)
            if (textLower.contains("khoa học tự nhiên")) {
                extractedUniversity = "Trường ĐH Khoa học Tự nhiên - ĐHQG HCM"
            }

            // Tìm MSSV bằng OCR (Nếu mã vạch lỗi)
            if (extractedId.isNullOrEmpty()) {
                if (textLower.contains("mssv:")) {
                    extractedId = text.substringAfter(":").trim().filter { it.isDigit() }
                } else {
                    val idRegex = Regex("\\b\\d{7,9}\\b") // MSSV thường 7-9 chữ số
                    idRegex.find(text)?.let { extractedId = it.value }
                }
            }

            // Tìm Họ tên: Dòng viết hoa toàn bộ và nằm sau tiêu đề "THẺ SINH VIÊN" hoặc trước "Ngày sinh"
            // Logic: Nếu block toàn chữ in hoa, không có số, và không phải là tiêu đề
            if (text == text.uppercase() && 
                text.length > 5 && 
                !text.contains("THẺ SINH VIÊN") && 
                !text.contains("ĐẠI HỌC") &&
                text.all { it.isLetter() || it.isWhitespace() }
            ) {
                extractedName = text
            }
        }

        return StudentCardInfo(
            studentId = extractedId,
            fullName = extractedName,
            university = extractedUniversity,
            rawBarcodeData = barcodeData
        )
    }
}
