package com.example.dormdeli.firestore

object ModelFields {

    // --- USER ---
    object User {
        const val UID = "uid"
        const val FULL_NAME = "fullName"
        const val EMAIL = "email"
        const val PHONE = "phone"
        const val DORM_BLOCK = "dormBlock"
        const val ROOM_NUMBER = "roomNumber"
        const val ROLE = "role"
        const val AVATAR_URL = "avatarUrl"
        const val ACTIVE = "active"
        const val CREATED_AT = "createdAt"
    }

    // --- USER ADDRESS ---
    object UserAddress {
        const val ID = "id"
        const val LABEL = "label"
        const val ADDRESS = "address"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val IS_DEFAULT = "isDefault"
    }

    // --- FOOD ---
    object Food {
        const val ID = "id"
        const val STORE_ID = "storeId"
        const val NAME = "name"
        const val DESCRIPTION = "description"
        const val PRICE = "price"
        const val CATEGORY = "category"
        const val IMAGE_URL = "imageUrl"
        const val AVAILABLE = "available"
        const val RATING_AVG = "ratingAvg"
        const val CREATED_AT = "createdAt"
    }

    // --- STORE ---
    object Store {
        const val ID = "id"
        const val OWNER_ID = "ownerId"
        const val NAME = "name"
        const val DESCRIPTION = "description"
        const val IMAGE_URL = "imageUrl"
        const val LOCATION = "location"
        const val OPEN_TIME = "openTime"
        const val CLOSE_TIME = "closeTime"
        const val APPROVED = "approved"
        const val ACTIVE = "active"
        const val REJECTED = "rejected"
        const val CREATED_AT = "createdAt"
    }

    // --- ORDER ---
    object Order {
        const val USER_ID = "userId"
        const val STORE_ID = "storeId"
        const val SHIPPER_ID = "shipperId"
        const val STATUS = "status"
        const val DELIVERY_TYPE = "deliveryType"
        const val DELIVERY_NOTE = "deliveryNote"
        const val TOTAL_PRICE = "totalPrice"
        const val PAYMENT_METHOD = "paymentMethod"
        const val CREATED_AT = "createdAt"
    }

    // --- ORDER ITEM ---
    object OrderItem {
        const val FOOD_ID = "foodId"
        const val FOOD_NAME = "foodName"
        const val PRICE = "price"
        const val QUANTITY = "quantity"
        const val NOTE = "note"
    }

    // --- CART ITEM ---
    object CartItem {
        const val FOOD = "food" // Lưu ý: đây là object Food
        const val QUANTITY = "quantity"
    }

    // --- REVIEW ---
    object Review {
        const val ID = "id"
        const val USER_ID = "userId"
        const val USER_NAME = "userName"
        const val USER_AVATAR_URL = "userAvatarUrl"
        const val STORE_ID = "storeId"
        const val FOOD_ID = "foodId"
        const val RATING = "rating"
        const val COMMENT = "comment"
        const val CREATED_AT = "createdAt"
    }

    // --- FAVORITE ---
    object Favorite {
        const val USER_ID = "userId"
        const val FOOD_IDS = "foodIds"
        const val STORE_IDS = "storeIds"
    }

    // --- SHIPPER PROFILE ---
    object ShipperProfile {
        const val USER_ID = "userId"
        const val IS_APPROVED = "approved"
        const val TOTAL_ORDERS = "totalOrders"
        const val TOTAL_INCOME = "totalIncome"
    }
}