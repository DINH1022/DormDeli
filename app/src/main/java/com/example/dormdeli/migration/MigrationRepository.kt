package com.example.dormdeli.migration

import com.example.dormdeli.firestore.CollectionName
import com.example.dormdeli.enums.DeliveryType
import com.example.dormdeli.enums.FoodCategory
import com.example.dormdeli.enums.NotificationTarget
import com.example.dormdeli.enums.OrderStatus
import com.example.dormdeli.enums.UserRole
import com.example.dormdeli.model.Favorite
import com.example.dormdeli.model.Food
import com.example.dormdeli.model.Notification
import com.example.dormdeli.model.Order
import com.example.dormdeli.model.OrderItem
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
    private val orderItemCol = db.collection(CollectionName.ORDER_ITEM.value)
    private val notiCol = db.collection(CollectionName.NOTIFICATION.value)

    private val imageUrl = "https://github.com/shadcn.png"

    suspend fun clearOldData() {
        val collections =
            listOf(
                userCol,
                storeCol,
                foodCol,
                shipperCol,
                favoriteCol,
                orderCol,
                orderItemCol,
                reviewCol,
                notiCol
            )
        for (col in collections) {
            val documents = col.get().await()
            if (documents.isEmpty) continue
            val batch = db.batch()
            for (doc in documents.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
        }
    }

    suspend fun mockData() {
        clearOldData()
        mockUsers()
        mockStores()
        mockFoods()
        mockShippers()
        mockFavorites()
        mockOrders()
        mockReviews()
        mockNotifications()
    }

    private suspend fun mockUsers() {
        val batch = db.batch()
        val users = listOf(
            // Students: Có đầy đủ thông tin phòng
            User(
                "u_student_1",
                "Nguyễn Văn An",
                "student1@test.com",
                "0901234567",
                "A1",
                "402",
                UserRole.STUDENT.value,
                imageUrl
            ),
            User(
                "u_student_2",
                "Trần Thị Bình",
                "student2@test.com",
                "0902345678",
                "B2",
                "305",
                UserRole.STUDENT.value,
                imageUrl
            ),
            User(
                "u_student_3",
                "Lê Minh Châu",
                "student3@test.com",
                "0903456789",
                "A1",
                "501",
                UserRole.STUDENT.value,
                imageUrl
            ),

            // Sellers: Không có số phòng (để trống dormBlock, roomNumber)
            User(
                "u_seller_1",
                "Trần Chủ Quán",
                "seller1@test.com",
                "0873232980",
                "",
                "",
                UserRole.SELLER.value,
                imageUrl
            ),
            User(
                "u_seller_2",
                "Nguyễn Thị Mai",
                "seller2@test.com",
                "0874567890",
                "",
                "",
                UserRole.SELLER.value,
                imageUrl
            ),

            User(
                "u_seller_3",
                "Nguyễn Thị Mai",
                "seller2@test.com",
                "0874567890",
                "",
                "",
                UserRole.SELLER.value,
                imageUrl
            ),

            // Shippers
            User(
                "u_shipper_1",
                "Lê Giao Hàng",
                "shipper2@test.com",
                "0881234567",
                "B2",
                "17",
                UserRole.SHIPPER.value,
                imageUrl
            ),
            User(
                "u_shipper_2",
                "Lê Giao Hàng 2",
                "shipper1@test.com",
                "0881234567",
                "B2",
                "17",
                UserRole.SHIPPER.value,
                imageUrl
            ),
            User(
                "u_shipper_3",
                "Lê Giao Hàng 3",
                "shipper3@test.com",
                "0881234567",
                "B2",
                "17",
                UserRole.SHIPPER.value,
                imageUrl
            ),

            // Admin
            User(
                "u_admin_1",
                "Admin System",
                "admin@test.com",
                "0890000000",
                "B3",
                "A5",
                UserRole.ADMIN.value,
                imageUrl
            )
        )

        users.forEach { user ->
            batch.set(userCol.document(user.uid), user)
        }
        batch.commit().await()
    }

    private suspend fun mockStores() {
        val batch = db.batch()
        val stores = listOf(
            Store(
                id = "s_com_tam",
                ownerId = "u_seller_1",
                name = "Cơm Tấm KTX",
                description = "Cơm tấm ngon, giá sinh viên",
                imageUrl = imageUrl,
                location = "Cổng B",
                openTime = "06:00",
                closeTime = "22:00",
                approved = true
            ),
            Store(
                id = "s_com_nieu",
                ownerId = "u_seller_2",
                name = "Cơm Niêu Sài Gòn",
                description = "Cơm niêu truyền thống",
                imageUrl = imageUrl,
                location = "Khu A",
                openTime = "10:00",
                closeTime = "21:00",
                approved = true
            ),
            Store(
                id = "s_tra_sua",
                ownerId = "u_seller_1",
                name = "Trà Sữa GenZ",
                description = "Trà sữa đậm vị, topping đa dạng",
                imageUrl = imageUrl,
                location = "Khu A",
                openTime = "08:00",
                closeTime = "23:00",
                approved = true
            ),
            Store(
                id = "s_cafe",
                ownerId = "u_seller_3",
                name = "Cà Phê Học Đường",
                description = "Cà phê nguyên chất, không gian yên tĩnh",
                imageUrl = imageUrl,
                location = "Cổng C",
                openTime = "06:30",
                closeTime = "22:30",
                approved = true
            ),
            Store(
                id = "s_banh_mi",
                ownerId = "u_seller_2",
                name = "Bánh Mì 555",
                description = "Bánh mì Sài Gòn đủ loại",
                imageUrl = imageUrl,
                location = "Khu B",
                openTime = "05:30",
                closeTime = "20:00",
                approved = true
            ),
            Store(
                id = "s_ga_ran",
                ownerId = "u_seller_4",
                name = "Gà Rán Golden",
                description = "Gà rán giòn tan, burger ngon",
                imageUrl = imageUrl,
                location = "Cổng A",
                openTime = "09:00",
                closeTime = "22:00",
                approved = true
            ),
            Store(
                id = "s_pho",
                ownerId = "u_seller_3",
                name = "Phở Hà Nội",
                description = "Phở bò Hà Nội chính gốc",
                imageUrl = imageUrl,
                location = "Khu C",
                openTime = "06:00",
                closeTime = "14:00",
                approved = true
            ),
            Store(
                id = "s_bun",
                ownerId = "u_seller_4",
                name = "Bún Bò Huế Authentique",
                description = "Bún bò Huế cay nồng",
                imageUrl = imageUrl,
                location = "Khu A",
                openTime = "06:30",
                closeTime = "21:00",
                approved = true
            ),
            Store(
                id = "s_pending",
                ownerId = "u_seller_3",
                name = "Quán Mới Test",
                description = "Đang chờ duyệt",
                imageUrl = imageUrl,
                location = "Khu D",
                approved = false
            )
        )

        stores.forEach { batch.set(storeCol.document(it.id), it) }
        batch.commit().await()
    }

    private suspend fun mockFoods() {
        val batch = db.batch()
        val foods = listOf(
            // Cơm Tấm KTX
            Food(
                id = "f_com_suon",
                storeId = "s_com_tam",
                name = "Cơm Sườn",
                description = "Sườn nướng đặc biệt",
                price = 30000,
                category = FoodCategory.RICE.value,
                imageUrl = imageUrl,
                ratingAvg = 4.5
            ),
            Food(
                id = "f_com_ga",
                storeId = "s_com_tam",
                name = "Cơm Gà",
                description = "Gà nướng mật ong",
                price = 35000,
                category = FoodCategory.RICE.value,
                imageUrl = imageUrl,
                ratingAvg = 4.2
            ),
            Food(
                id = "f_com_bi_cha",
                storeId = "s_com_tam",
                name = "Cơm Bì Chả",
                description = "Combo truyền thống",
                price = 32000,
                category = FoodCategory.RICE.value,
                imageUrl = imageUrl,
                ratingAvg = 4.7
            ),
            Food(
                id = "f_com_suon_bi_cha",
                storeId = "s_com_tam",
                name = "Cơm Sườn Bì Chả Trứng",
                description = "Đầy đủ topping",
                price = 45000,
                category = FoodCategory.RICE.value,
                imageUrl = imageUrl,
                ratingAvg = 4.8
            ),

            // Cơm Niêu
            Food(
                id = "f_com_nieu_ga",
                storeId = "s_com_nieu",
                name = "Cơm Niêu Gà",
                description = "",
                price = 40000,
                category = FoodCategory.RICE.value,
                imageUrl = imageUrl,
                ratingAvg = 4.6
            ),
            Food(
                id = "f_com_nieu_bo",
                storeId = "s_com_nieu",
                name = "Cơm Niêu Bò",
                description = "",
                price = 45000,
                category = FoodCategory.RICE.value,
                imageUrl = imageUrl,
                ratingAvg = 4.5
            ),

            // Trà Sữa GenZ
            Food(
                id = "f_tra_sua_o_long",
                storeId = "s_tra_sua",
                name = "Trà Sữa Ô Long",
                description = "Trà ô long nguyên chất",
                price = 25000,
                category = FoodCategory.DRINK.value,
                imageUrl = imageUrl,
                ratingAvg = 4.3
            ),
            Food(
                id = "f_tra_sua_truyen_thong",
                storeId = "s_tra_sua",
                name = "Trà Sữa Truyền Thống",
                description = "",
                price = 22000,
                category = FoodCategory.DRINK.value,
                imageUrl = imageUrl,
                ratingAvg = 4.4
            ),
            Food(
                id = "f_tra_sua_socola",
                storeId = "s_tra_sua",
                name = "Trà Sữa Socola",
                description = "",
                price = 28000,
                category = FoodCategory.DRINK.value,
                imageUrl = imageUrl,
                ratingAvg = 4.1
            ),
            Food(
                id = "f_tra_vai",
                storeId = "s_tra_sua",
                name = "Trà Vải",
                description = "Trà vải thanh mát",
                price = 20000,
                category = FoodCategory.DRINK.value,
                imageUrl = imageUrl,
                ratingAvg = 4.2
            ),

            // Cà Phê
            Food(
                id = "f_ca_phe_den",
                storeId = "s_cafe",
                name = "Cà Phê Đen",
                description = "",
                price = 15000,
                category = FoodCategory.DRINK.value,
                imageUrl = imageUrl,
                ratingAvg = 4.6
            ),
            Food(
                id = "f_ca_phe_sua",
                storeId = "s_cafe",
                name = "Cà Phê Sữa",
                description = "",
                price = 18000,
                category = FoodCategory.DRINK.value,
                imageUrl = imageUrl,
                ratingAvg = 4.5
            ),
            Food(
                id = "f_bac_xiu",
                storeId = "s_cafe",
                name = "Bạc Xỉu",
                description = "",
                price = 20000,
                category = FoodCategory.DRINK.value,
                imageUrl = imageUrl,
                ratingAvg = 4.4
            ),

            // Bánh Mì
            Food(
                id = "f_banh_mi_thit",
                storeId = "s_banh_mi",
                name = "Bánh Mì Thịt",
                description = "",
                price = 15000,
                category = FoodCategory.FAST_FOOD.value,
                imageUrl = imageUrl,
                ratingAvg = 4.5
            ),
            Food(
                id = "f_banh_mi_trung",
                storeId = "s_banh_mi",
                name = "Bánh Mì Trứng",
                description = "",
                price = 12000,
                category = FoodCategory.FAST_FOOD.value,
                imageUrl = imageUrl,
                ratingAvg = 4.3
            ),
            Food(
                id = "f_banh_mi_dac_biet",
                storeId = "s_banh_mi",
                name = "Bánh Mì Đặc Biệt",
                description = "",
                price = 20000,
                category = FoodCategory.FAST_FOOD.value,
                imageUrl = imageUrl,
                ratingAvg = 4.7
            ),

            // Gà Rán
            Food(
                id = "f_ga_ran_1_mieng",
                storeId = "s_ga_ran",
                name = "Gà Rán 1 Miếng",
                description = "",
                price = 25000,
                category = FoodCategory.FAST_FOOD.value,
                imageUrl = imageUrl,
                ratingAvg = 4.4
            ),
            Food(
                id = "f_ga_ran_3_mieng",
                storeId = "s_ga_ran",
                name = "Gà Rán 3 Miếng",
                description = "",
                price = 65000,
                category = FoodCategory.FAST_FOOD.value,
                imageUrl = imageUrl,
                ratingAvg = 4.6
            ),
            Food(
                id = "f_burger_ga",
                storeId = "s_ga_ran",
                name = "Burger Gà",
                description = "",
                price = 30000,
                category = FoodCategory.FAST_FOOD.value,
                imageUrl = imageUrl,
                ratingAvg = 4.2
            ),

            // Phở
            Food(
                id = "f_pho_bo_tai",
                storeId = "s_pho",
                name = "Phở Bò Tái",
                description = "",
                price = 35000,
                category = FoodCategory.NOODLE.value,
                imageUrl = imageUrl,
                ratingAvg = 4.7
            ),
            Food(
                id = "f_pho_bo_chin",
                storeId = "s_pho",
                name = "Phở Bò Chín",
                description = "",
                price = 35000,
                category = FoodCategory.NOODLE.value,
                imageUrl = imageUrl,
                ratingAvg = 4.6
            ),
            Food(
                id = "f_pho_dac_biet",
                storeId = "s_pho",
                name = "Phở Đặc Biệt",
                description = "",
                price = 45000,
                category = FoodCategory.NOODLE.value,
                imageUrl = imageUrl,
                ratingAvg = 4.8
            ),

            // Bún Bò Huế
            Food(
                id = "f_bun_bo_hue",
                storeId = "s_bun",
                name = "Bún Bò Huế",
                description = "",
                price = 35000,
                category = FoodCategory.NOODLE.value,
                imageUrl = imageUrl,
                ratingAvg = 4.5
            ),
            Food(
                id = "f_bun_bo_gio_heo",
                storeId = "s_bun",
                name = "Bún Bò Giò Heo",
                description = "",
                price = 40000,
                category = FoodCategory.NOODLE.value,
                imageUrl = imageUrl,
                ratingAvg = 4.6
            ),

            // Desserts
            Food(
                id = "f_che_ba_mau",
                storeId = "s_tra_sua",
                name = "Chè Ba Màu",
                description = "",
                price = 18000,
                category = FoodCategory.DESSERT.value,
                imageUrl = imageUrl,
                ratingAvg = 4.3
            ),
            Food(
                id = "f_pudding",
                storeId = "s_tra_sua",
                name = "Pudding",
                description = "",
                price = 15000,
                category = FoodCategory.DESSERT.value,
                imageUrl = imageUrl,
                ratingAvg = 4.1
            )
        )

        foods.forEach { batch.set(foodCol.document(it.id), it) }
        batch.commit().await()
    }

    private suspend fun mockShippers() {
        val batch = db.batch()
        val shippers = listOf(
            ShipperProfile("u_shipper_1", false, 0, 0),
            ShipperProfile("u_shipper_2", true, 200, 4000000),
            ShipperProfile("u_shipper_3", true, 100, 2000000)
        )
        shippers.forEach { batch.set(shipperCol.document(it.userId), it) }
        batch.commit().await()
    }

    private suspend fun mockFavorites() {
        val batch = db.batch()
        val favorites = listOf(
            Favorite(
                "u_student_1",
                listOf("f_com_suon", "f_tra_sua_o_long", "f_pho_bo_tai", "f_banh_mi_dac_biet"),
                listOf("s_com_tam", "s_tra_sua", "s_pho")
            ),
            Favorite(
                "u_student_2",
                listOf("f_tra_sua_truyen_thong", "f_ga_ran_3_mieng", "f_bun_bo_hue"),
                listOf("s_tra_sua", "s_ga_ran")
            ),
            Favorite(
                "u_student_3",
                listOf("f_com_nieu_ga", "f_ca_phe_sua"),
                listOf("s_com_nieu", "s_cafe")
            ),
            Favorite(
                "u_student_4",
                listOf("f_banh_mi_thit", "f_ca_phe_den"),
                listOf("s_banh_mi", "s_cafe")
            )
        )
        favorites.forEach { batch.set(favoriteCol.document(it.userId), it) }
        batch.commit().await()
    }

    private suspend fun mockOrders() {
        val orders = listOf(
            Order(
                id = "o_1",
                userId = "u_student_1",
                storeId = "s_com_tam",
                shipperId = "u_shipper_1",
                status = OrderStatus.COMPLETED.value,
                deliveryType = DeliveryType.ROOM.value,
                deliveryNote = "Gọi cửa 2 tiếng",
                totalPrice = 65000,
                paymentMethod = "Cash",
                createdAt = System.currentTimeMillis() - 86400000
            ),
            Order(
                id = "o_2",
                userId = "u_student_2",
                storeId = "s_tra_sua",
                shipperId = "u_shipper_2",
                status = OrderStatus.COMPLETED.value,
                deliveryType = DeliveryType.ROOM.value,
                deliveryNote = "",
                totalPrice = 75000,
                paymentMethod = "Momo",
                createdAt = System.currentTimeMillis() - 172800000
            ),
            Order(
                id = "o_3",
                userId = "u_student_3",
                storeId = "s_pho",
                shipperId = "u_shipper_1",
                status = OrderStatus.COMPLETED.value,
                deliveryType = DeliveryType.PICKUP.value,
                deliveryNote = "",
                totalPrice = 80000,
                paymentMethod = "ZaloPay",
                createdAt = System.currentTimeMillis() - 259200000
            ),
            Order(
                id = "o_4",
                userId = "u_student_4",
                storeId = "s_ga_ran",
                shipperId = "u_shipper_3",
                status = OrderStatus.DELIVERING.value,
                deliveryType = DeliveryType.ROOM.value,
                deliveryNote = "Để trước cửa",
                totalPrice = 95000,
                paymentMethod = "Cash",
                createdAt = System.currentTimeMillis() - 1800000
            ),
            Order(
                id = "o_5",
                userId = "u_student_5",
                storeId = "s_bun",
                shipperId = "u_shipper_2",
                status = OrderStatus.CONFIRMED.value,
                deliveryType = DeliveryType.ROOM.value,
                deliveryNote = "",
                totalPrice = 75000,
                paymentMethod = "Momo",
                createdAt = System.currentTimeMillis() - 900000
            )
        )

        // 2. Danh sách các OrderItems (Sử dụng orderId để liên kết)
        val orderItems = listOf(
            // Items cho Order 1
            OrderItem(
                orderId = "o_1",
                foodId = "f_com_suon",
                foodName = "Cơm Sườn",
                price = 30000,
                quantity = 1
            ),
            OrderItem(
                orderId = "o_1",
                foodId = "f_com_ga",
                foodName = "Cơm Gà",
                price = 35000,
                quantity = 1
            ),
            // Items cho Order 2
            OrderItem(
                orderId = "o_2",
                foodId = "f_tra_sua_o_long",
                foodName = "Trà Sữa Ô Long",
                price = 25000,
                quantity = 2
            ),
            OrderItem(
                orderId = "o_2",
                foodId = "f_che_ba_mau",
                foodName = "Chè Ba Màu",
                price = 18000,
                quantity = 1,
                note = "Ít đường"
            ),
            // Items cho Order 3
            OrderItem(
                orderId = "o_3",
                foodId = "f_pho_dac_biet",
                foodName = "Phở Đặc Biệt",
                price = 45000,
                quantity = 1
            ),
            // Items cho Order 4
            OrderItem(
                orderId = "o_4",
                foodId = "f_ga_ran_3_mieng",
                foodName = "Gà Rán 3 Miếng",
                price = 65000,
                quantity = 1
            ),
            // Items cho Order 5
            OrderItem(
                orderId = "o_5",
                foodId = "f_bun_bo_hue",
                foodName = "Bún Bò Huế",
                price = 35000,
                quantity = 1
            )
        )

        try {
            val batch = db.batch()

            orders.forEach { order ->
                val docRef = orderCol.document(order.id)
                batch.set(docRef, order)
            }

            orderItems.forEach { item ->
                val docRef = orderItemCol.document()
                batch.set(docRef, item)
            }

            batch.commit().await()

            println("Import thành công!")
        } catch (e: Exception) {
            println("Lỗi khi import: ${e.message}")
        }
    }

    private suspend fun mockReviews() {
        val batch = db.batch()
        val reviews = listOf(
            Review(
                "rev_1",
                "u_student_1",
                "Nguyễn Văn An",
                imageUrl,
                "s_com_tam",
                "f_com_suon",
                5,
                "Sườn rất mềm và ngon!",
                System.currentTimeMillis() - 86400000
            ),
            Review(
                "rev_2",
                "u_student_2",
                "Trần Thị Bình",
                imageUrl,
                "s_tra_sua",
                "f_tra_sua_o_long",
                4,
                "Trà ngon, hơi ngọt.",
                System.currentTimeMillis() - 172800000
            ),
            Review(
                "rev_3",
                "u_student_3",
                "Lê Minh Châu",
                imageUrl,
                "s_pho",
                "f_pho_dac_biet",
                5,
                "Phở đúng vị Hà Nội!",
                System.currentTimeMillis() - 259200000
            ),
            Review(
                "rev_4",
                "u_student_4",
                "Phạm Hoàng Dũng",
                imageUrl,
                "s_ga_ran",
                "f_ga_ran_3_mieng",
                4,
                "Gà giòn, giao nhanh.",
                System.currentTimeMillis() - 345600000
            ),
            Review(
                "rev_5",
                "u_student_5",
                "Võ Thị Lan",
                imageUrl,
                "s_bun",
                "f_bun_bo_hue",
                5,
                "Bún bò chuẩn vị Huế.",
                System.currentTimeMillis() - 432000000
            ),
            Review(
                "rev_6",
                "u_student_1",
                "Nguyễn Văn An",
                imageUrl,
                "s_banh_mi",
                "f_banh_mi_dac_biet",
                4,
                "Bánh mì giòn, nhân đầy.",
                System.currentTimeMillis() - 518400000
            ),
            Review(
                "rev_7",
                "u_student_3",
                "Lê Minh Châu",
                imageUrl,
                "s_cafe",
                "f_ca_phe_sua",
                5,
                "Cà phê đậm đà.",
                System.currentTimeMillis() - 604800000
            ),
            Review(
                "rev_8",
                "u_student_2",
                "Trần Thị Bình",
                imageUrl,
                "s_com_tam",
                "f_com_bi_cha",
                5,
                "Combo rất ngon!",
                System.currentTimeMillis() - 691200000
            ),
            Review(
                "rev_9",
                "u_student_4",
                "Phạm Hoàng Dũng",
                imageUrl,
                "s_com_nieu",
                "f_com_nieu_bo",
                4,
                "Cơm niêu thơm, bò mềm.",
                System.currentTimeMillis() - 777600000
            ),
            Review(
                "rev_10",
                "u_student_5",
                "Võ Thị Lan",
                imageUrl,
                "s_tra_sua",
                "f_che_ba_mau",
                4,
                "Chè ngọt vừa phải.",
                System.currentTimeMillis() - 864000000
            ),
            Review(
                "rev_11",
                "u_student_1",
                "Nguyễn Văn An",
                imageUrl,
                "s_pho",
                "f_pho_bo_tai",
                5,
                "Phở tái mềm, tuyệt vời!",
                System.currentTimeMillis() - 950400000
            ),
            Review(
                "rev_12",
                "u_student_3",
                "Lê Minh Châu",
                imageUrl,
                "s_ga_ran",
                "f_burger_ga",
                3,
                "Burger hơi khô.",
                System.currentTimeMillis() - 1036800000
            ),
            Review(
                "rev_13",
                "u_student_2",
                "Trần Thị Bình",
                imageUrl,
                "s_banh_mi",
                "f_banh_mi_thit",
                5,
                "Bánh mì giá rẻ mà ngon.",
                System.currentTimeMillis() - 1123200000
            ),
            Review(
                "rev_14",
                "u_student_4",
                "Phạm Hoàng Dũng",
                imageUrl,
                "s_cafe",
                "f_ca_phe_den",
                5,
                "Tỉnh táo cả ngày!",
                System.currentTimeMillis() - 1209600000
            ),
            Review(
                "rev_15",
                "u_student_5",
                "Võ Thị Lan",
                imageUrl,
                "s_com_tam",
                "f_com_ga",
                5,
                "Gà mật ong rất thơm.",
                System.currentTimeMillis() - 1296000000
            )
        )
        reviews.forEach { batch.set(reviewCol.document(it.id), it) }
        batch.commit().await()
    }

    private suspend fun mockNotifications() {
        val batch = db.batch()

        val notifications = listOf(
            // ===== ALL =====
            Notification(
                id = "noti_all_1",
                target = NotificationTarget.EVERYONE.value,
                subject = "Thông báo hệ thống",
                message = "Hệ thống sẽ bảo trì vào 23:00 tối nay."
            ),
            Notification(
                id = "noti_all_2",
                target = NotificationTarget.EVERYONE.value,
                subject = "Cập nhật phiên bản",
                message = "Ứng dụng đã được cập nhật phiên bản mới."
            ),

            // ===== USER =====
            Notification(
                id = "noti_user_1",
                target = NotificationTarget.USER.value,
                subject = "Đơn hàng đã xác nhận",
                message = "Đơn hàng của bạn đã được cửa hàng xác nhận."
            ),
            Notification(
                id = "noti_user_2",
                target = NotificationTarget.USER.value,
                subject = "Đơn hàng đang giao",
                message = "Shipper đang trên đường giao đơn hàng cho bạn."
            ),

            // ===== STORE =====
            Notification(
                id = "noti_store_1",
                target = NotificationTarget.STORE.value,
                subject = "Đơn hàng mới",
                message = "Bạn có một đơn hàng mới cần xác nhận."
            ),
            Notification(
                id = "noti_store_2",
                target = NotificationTarget.STORE.value,
                subject = "Doanh thu hôm nay",
                message = "Doanh thu cửa hàng hôm nay đã được cập nhật."
            ),

            // ===== SHIPPER =====
            Notification(
                id = "noti_shipper_1",
                target = NotificationTarget.SHIPPER.value,
                subject = "Đơn giao mới",
                message = "Có đơn hàng mới đang chờ bạn nhận."
            ),
            Notification(
                id = "noti_shipper_2",
                target = NotificationTarget.SHIPPER.value,
                subject = "Hoàn thành đơn giao",
                message = "Bạn đã hoàn thành một đơn giao thành công."
            )
        )

        notifications.forEach { noti ->
            batch.set(notiCol.document(noti.id), noti)
        }

        batch.commit().await()
    }
}