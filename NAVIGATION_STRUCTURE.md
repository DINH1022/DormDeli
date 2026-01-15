# DormDeli - Cấu trúc dự án

## Tổng quan
Ứng dụng được cấu trúc lại với kiến trúc **1 cấp, đơn giản, rõ ràng**.

## Cấu trúc thư mục

```
com.example.dormdeli/
├── MainActivity.kt                 # Entry point
├── model/                          # Data models
│   ├── Favorite.kt
│   ├── Food.kt
│   ├── Order.kt
│   ├── OrderItem.kt
│   ├── ProfileView.kt
│   ├── Review.kt
│   ├── ShipperProfile.kt
│   ├── Store.kt
│   └── User.kt
├── enums/                          # Enums
│   ├── DeliveryType.kt
│   ├── FoodCategory.kt
│   ├── OrderStatus.kt
│   └── UserRole.kt
├── repository/                     # Data layer
│   ├── AuthRepository.kt
│   ├── UserRepository.kt
│   └── store/
│       ├── StoreFoodRepository.kt
│       └── StoreRepository.kt
└── ui/
    ├── screens/                    # ✨ TẤT CẢ SCREENS (1 cấp)
    │   ├── AuthScreen.kt          # Auth state enum
    │   ├── LoginScreen.kt
    │   ├── SignUpScreen.kt
    │   ├── OTPScreen.kt
    │   ├── HomeScreen.kt
    │   ├── StoreScreen.kt
    │   ├── FoodDetail.kt
    │   └── ReviewScreen.kt
    ├── components/                 # ✨ TẤT CẢ COMPONENTS (1 cấp)
    │   ├── CategoryChip.kt
    │   ├── CategoryChips.kt
    │   ├── FoodItem.kt
    │   ├── HomeHeader.kt
    │   ├── HomeSearchBar.kt
    │   ├── RestaurantCard.kt
    │   ├── SectionTitle.kt
    │   └── StoreNavBar.kt
    ├── viewmodels/                 # ✨ TẤT CẢ VIEWMODELS (1 cấp)
    │   ├── AuthViewModel.kt
    │   └── StoreViewModel.kt
    ├── navigation/                 # Navigation
    │   ├── Screen.kt              # Routes definition
    │   └── MainNavigation.kt      # Navigation graph
    └── theme/                      # Theme
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

## Nguyên tắc cấu trúc

### ✅ 1 cấp duy nhất
- Mỗi package chỉ chứa files, **KHÔNG có sub-packages**
- Dễ tìm, dễ quản lý, không phải đào sâu nhiều cấp

### 📦 Phân loại rõ ràng
1. **screens/** - Toàn bộ màn hình
2. **components/** - Toàn bộ UI components tái sử dụng
3. **viewmodels/** - Toàn bộ business logic
4. **navigation/** - Routing và navigation
5. **theme/** - Colors, typography, theme

### 🎯 Đặt tên nhất quán
- Screens: `*Screen.kt` (LoginScreen, HomeScreen, etc.)
- ViewModels: `*ViewModel.kt`
- Components: Tên mô tả rõ ràng

## Navigation Flow

### Screen Routes (Screen.kt)
```kotlin
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object StoreDetail : Screen("store/{storeId}") {
        fun createRoute(storeId: String) = "store/$storeId"
    }
}
```

### Main Navigation Flow
```
Login → Home → StoreDetail → FoodDetail → Reviews
                ↓
             Profile
```

## Import Convention

### Screens import
```kotlin
import com.example.dormdeli.ui.screens.*
```

### Components import
```kotlin
import com.example.dormdeli.ui.components.*
```

### ViewModels import
```kotlin
import com.example.dormdeli.ui.viewmodels.AuthViewModel
import com.example.dormdeli.ui.viewmodels.StoreViewModel
```

## Ưu điểm cấu trúc mới

✅ **Đơn giản**: Chỉ 1 cấp, dễ navigate  
✅ **Rõ ràng**: Biết ngay file nào ở đâu  
✅ **Dễ scale**: Thêm screen/component mới rất đơn giản  
✅ **Performance**: IDE load nhanh hơn  
✅ **Team-friendly**: Dễ onboard người mới  

## Migration completed

✅ Đã xóa: `ui/auth/`, `ui/home/`, `ui/store/`, `ui/food/`, `ui/profile/`, `ui/review/`, `ui/nav/`  
✅ Đã tạo: `ui/screens/`, `ui/components/`, `ui/viewmodels/`  
✅ Cập nhật: Tất cả package declarations và imports  
✅ Kiểm tra: Không còn compile errors
