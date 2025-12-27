package com.example.dormdeli.migration

import com.example.dormdeli.enums.CollectionName
import com.example.dormdeli.enums.DeliveryType
import com.example.dormdeli.enums.FoodCategory
import com.example.dormdeli.enums.OrderStatus
import com.example.dormdeli.enums.UserRole
import com.example.dormdeli.model.Favorite
import com.example.dormdeli.model.Food
import com.example.dormdeli.model.Order
import com.example.dormdeli.model.Review
import com.example.dormdeli.model.ShipperProfile
import com.example.dormdeli.model.Store
import com.example.dormdeli.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MigrationRepository {
    private val db = FirebaseFirestore.getInstance()

    private val userCol = db.collection(CollectionName.USERS.value)
    private val storeCol = db.collection(CollectionName.STORES.value)
    private val foodCol = db.collection(CollectionName.FOODS.value)
    private val reviewCol = db.collection(CollectionName.REVIEWS.value)
    private val favoriteCol = db.collection(CollectionName.FAVORITES.value)
    private val shipperCol = db.collection(CollectionName.SHIPPER_PROFILE.value)
    private val orderCol = db.collection(CollectionName.ORDERS.value)

    private val imageUrl = "https://github.com/shadcn.png"

    /**
     * Gọi từ ViewModel / Activity bằng coroutine
     */
    private suspend fun clearCollection(collectionRef: com.google.firebase.firestore.CollectionReference) {
        val documents = collectionRef.get().await()
        for (doc in documents.documents) {
            doc.reference.delete().await()
        }
    }

    suspend fun clearOldData() {
        clearCollection(userCol)
        clearCollection(storeCol)
        clearCollection(foodCol)
        clearCollection(shipperCol)
        clearCollection(favoriteCol)
        clearCollection(orderCol)
        clearCollection(reviewCol)
    }

    suspend fun mockData() {
        // 1. Clean dữ liệu cũ
        clearOldData()

        // 2. Mock dữ liệu mới
        mockUsers()
        mockStores()
        mockFoods()
        mockShippers()
        mockFavorites()
        mockOrders()
        mockReviews()
    }

    /* ---------------- USERS ---------------- */
    private suspend fun mockUsers() {
        val users = listOf(
            User(
                uid = "student_1",
                fullName = "Nguyễn Văn Sinh Viên",
                email = "student@gmail.com",
                phone = "0912345678",
                dormBlock = "A1",
                roomNumber = "402",
                role = UserRole.STUDENT.value,
                avatarUrl = imageUrl,
                active = true,
                createdAt = System.currentTimeMillis()
            ),
            User(
                uid = "owner_1",
                fullName = "Trần Chủ Quán",
                email = "owner@gmail.com",
                phone = "0988888888",
                role = UserRole.SELLER.value,
                avatarUrl = imageUrl,
                active = true,
                createdAt = System.currentTimeMillis()
            ),
            User(
                uid = "shipper_5",
                fullName = "Lê Giao Hàng",
                email = "shipper@gmail.com",
                phone = "0977777777",
                role = UserRole.SHIPPER.value,
                avatarUrl = imageUrl,
                active = true,
                createdAt = System.currentTimeMillis()
            )
        )

        users.forEach { userCol.document(it.uid).set(it).await() }
    }

    /* ---------------- STORES ---------------- */
    private suspend fun mockStores() {
        val stores = listOf(
            Store(
                ownerId = "owner_1",
                name = "Cơm Trưa DormDeli",
                description = "Chuyên các món cơm gia đình sạch sẽ",
                imageUrl = imageUrl,
                location = "Khu dịch vụ nhà B3",
                openTime = "08:00",
                closeTime = "20:00",
                isApproved = true,
                isActive = true,
                createdAt = System.currentTimeMillis()
            )
        )
        // Dùng document ID cố định để dễ mock Food/Order ở dưới
        stores.forEach { storeCol.document("store_1").set(it).await() }
    }

    /* ---------------- FOODS ---------------- */
    private suspend fun mockFoods() {
        val foods = listOf(
            Food(
                id = "food_1",
                storeId = "store_1",
                name = "Cơm Sườn Nướng",
                description = "Sườn nướng mật ong kèm kim chi",
                price = 35000,
                category = FoodCategory.RICE.value,
                imageUrl = imageUrl,
                available = true,
                ratingAvg = 4.8,
                createdAt = System.currentTimeMillis()
            ),
            Food(
                id = "food_2",
                storeId = "store_1",
                name = "Trà Chanh Giã Tay",
                description = "Giải nhiệt mùa hè",
                price = 15000,
                category = FoodCategory.DRINK.value,
                imageUrl = imageUrl,
                available = true,
                ratingAvg = 4.5,
                createdAt = System.currentTimeMillis()
            )
        )
        foods.forEach { foodCol.document(it.id).set(it).await() }
    }

    /* ---------------- SHIPPERS ---------------- */
    private suspend fun mockShippers() {
        val shipper = ShipperProfile(
            userId = "shipper_1",
            isApproved = true,
            totalOrders = 50,
            totalIncome = 1500000
        )
        shipperCol.document(shipper.userId).set(shipper).await()
    }

    /* ---------------- FAVORITES ---------------- */
    private suspend fun mockFavorites() {
        val favorite = Favorite(
            userId = "student_1",
            foodIds = listOf("food_1", "food_2"),
            storeIds = listOf("store_1")
        )
        favoriteCol.document(favorite.userId).set(favorite).await()
    }

    /* ---------------- ORDERS ---------------- */
    private suspend fun mockOrders() {
        val order = Order(
            userId = "student_1",
            storeId = "store_1",
            shipperId = "shipper_1",
            status = OrderStatus.COMPLETED.value,
            deliveryType = DeliveryType.ROOM.value,
            deliveryNote = "Gửi ở bảo vệ nếu không gọi được",
            totalPrice = 50000, // 35k + 15k
            paymentMethod = "Cash",
            createdAt = System.currentTimeMillis()
        )
        // Bạn có thể tạo thêm sub-collection "items" cho order này nếu cần
        orderCol.add(order).await()
    }

    /* ---------------- REVIEWS ---------------- */
    private suspend fun mockReviews() {
        val reviews = listOf(
            Review(
                id = "review_1",
                userId = "student_1",
                userName = "Nguyễn Văn Sinh Viên",
                userAvatarUrl = imageUrl,
                storeId = "store_1",
                foodId = "food_1",
                rating = 5,
                comment = "Cơm rất ngon, sườn mềm!",
                createdAt = System.currentTimeMillis()
            )
        )
        reviews.forEach { reviewCol.document(it.id).set(it).await() }
    }


}