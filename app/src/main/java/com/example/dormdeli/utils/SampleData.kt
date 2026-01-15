package com.example.dormdeli.utils

import android.content.Context
import android.widget.Toast
import com.example.dormdeli.model.Order
import com.example.dormdeli.model.OrderItem
import com.google.firebase.firestore.FirebaseFirestore

object SampleData {
    private val db = FirebaseFirestore.getInstance()

    fun seedSampleOrders(context: Context) {
        val orders = listOf(
            Order(
                userId = "sample_user_1",
                status = "pending",
                shipperId = "",
                deliveryType = "Room 402, Block B",
                deliveryNote = "Please knock gently",
                totalPrice = 125000,
                paymentMethod = "Cash",
                items = listOf(
                    OrderItem(foodName = "Phở Bò Đặc Biệt", price = 65000, quantity = 1),
                    OrderItem(foodName = "Trà Sữa Thái Xanh", price = 30000, quantity = 2)
                )
            ),
            Order(
                userId = "sample_user_2",
                status = "pending",
                shipperId = "",
                deliveryType = "Room 105, Block A",
                deliveryNote = "Call me when you arrive",
                totalPrice = 85000,
                paymentMethod = "E-Wallet",
                items = listOf(
                    OrderItem(foodName = "Cơm Tấm Sườn Bì Chả", price = 45000, quantity = 1),
                    OrderItem(foodName = "Nước Mía", price = 10000, quantity = 4)
                )
            )
        )

        val batch = db.batch()
        orders.forEach { order ->
            val docRef = db.collection("orders").document()
            batch.set(docRef, order)
        }

        batch.commit()
            .addOnSuccessListener { 
                Toast.makeText(context, "Dữ liệu mẫu đã được tạo!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e -> 
                Toast.makeText(context, "Lỗi tạo dữ liệu: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
