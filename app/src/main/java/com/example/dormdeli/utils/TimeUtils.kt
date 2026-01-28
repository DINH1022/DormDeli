package com.example.dormdeli.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TimeUtils {

    fun isStoreOpen(openTime: String, closeTime: String): Boolean {
        if (openTime.isBlank() || closeTime.isBlank()) return true // Mặc định mở nếu không có giờ

        return try {
            val parser = SimpleDateFormat("HH:mm", Locale.getDefault())
            val open = parser.parse(openTime)
            val close = parser.parse(closeTime)
            
            if (open == null || close == null) return true

            // Lấy lịch hiện tại
            val now = Calendar.getInstance()
            val currentHour = now.get(Calendar.HOUR_OF_DAY)
            val currentMinute = now.get(Calendar.MINUTE)

            // Tạo lịch cho giờ mở
            val openCal = Calendar.getInstance().apply {
                time = open
                set(Calendar.YEAR, now.get(Calendar.YEAR))
                set(Calendar.MONTH, now.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
            }
            // Cập nhật lại giờ mở để lấy đúng giờ/phút từ parser (vì parse chỉ set năm 1970)
             // Cách trên có thể sai nếu openTime chỉ có giờ phút.
             // Cách an toàn hơn là so sánh phút trong ngày.
             
            val currentMinutes = currentHour * 60 + currentMinute
            
            val openCal2 = Calendar.getInstance().apply { time = open }
            val openMinutes = openCal2.get(Calendar.HOUR_OF_DAY) * 60 + openCal2.get(Calendar.MINUTE)

            val closeCal2 = Calendar.getInstance().apply { time = close }
            var closeMinutes = closeCal2.get(Calendar.HOUR_OF_DAY) * 60 + closeCal2.get(Calendar.MINUTE)

            // Xử lý trường hợp đóng cửa qua đêm (ví dụ mở 22:00 đóng 02:00)
            if (closeMinutes < openMinutes) {
                closeMinutes += 24 * 60
            }
            
            // Nếu hiện tại nhỏ hơn giờ đóng (tính cả qua đêm) và lớn hơn giờ mở
             val currentMinutesAdjusted = if (currentMinutes < openMinutes && closeMinutes > 24*60) {
                 currentMinutes + 24*60
             } else {
                 currentMinutes
             }

            return currentMinutesAdjusted in openMinutes..closeMinutes

        } catch (e: Exception) {
            e.printStackTrace()
            true // Mặc định mở nếu lỗi format
        }
    }
}
