# Hướng Dẫn Sử Dụng OTP trong DormDeli

## Tổng Quan
Ứng dụng sử dụng Firebase Phone Authentication để xác thực số điện thoại thông qua mã OTP (One-Time Password).

## Cách Hoạt Động

### 1. Gửi Mã OTP
Khi người dùng nhập số điện thoại và nhấn "Sign in", hệ thống sẽ:
- Format số điện thoại với prefix +44 (UK)
- Gửi yêu cầu OTP đến Firebase
- Firebase sẽ gửi SMS chứa mã OTP đến số điện thoại
- Màn hình sẽ tự động chuyển sang màn hình nhập OTP

### 2. Xác Minh OTP
Người dùng nhập mã OTP 6 chữ số nhận được qua SMS:
- Nhập mã vào trường OTP
- Nhấn "Xác minh"
- Hệ thống xác minh mã với Firebase
- Nếu thành công, người dùng được đăng nhập

### 3. Gửi Lại Mã OTP
Nếu không nhận được mã, người dùng có thể:
- Nhấn "Gửi lại mã OTP"
- Hệ thống sẽ gửi lại mã mới đến số điện thoại

## Cấu Hình Firebase

### Bước 1: Bật Phone Authentication trong Firebase Console
1. Vào Firebase Console: https://console.firebase.google.com
2. Chọn project "dormdeli"
3. Vào **Authentication** > **Sign-in method**
4. Bật **Phone** provider
5. Lưu lại

### Bước 2: Thêm SHA-1 Fingerprint (Cho Android)
1. Lấy SHA-1 fingerprint của app:
   ```bash
   # Trên Windows
   cd android
   gradlew signingReport
   
   # Hoặc với keytool
   keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
   ```

2. Copy SHA-1 fingerprint
3. Vào Firebase Console > Project Settings > Your apps
4. Thêm SHA-1 fingerprint vào Android app

### Bước 3: Cấu Hình OTP cho Testing (Tùy chọn)
Trong Firebase Console > Authentication > Sign-in method > Phone:
- Có thể thêm số điện thoại test để nhận mã OTP trong môi trường development

## Code Implementation

### AuthRepository
File: `app/src/main/java/com/example/dormdeli/repository/AuthRepository.kt`

Các phương thức chính:
- `sendPhoneVerificationCode()`: Gửi mã OTP
- `verifyPhoneNumberWithCode()`: Xác minh mã OTP
- `setVerificationId()`: Lưu verification ID từ callback

### AuthViewModel
File: `app/src/main/java/com/example/dormdeli/ui/auth/AuthViewModel.kt`

Quản lý state và logic:
- `signInWithPhone()`: Xử lý đăng nhập bằng số điện thoại
- `verifyOTP()`: Xử lý xác minh OTP
- `resendOTP()`: Gửi lại mã OTP

### OTPScreen
File: `app/src/main/java/com/example/dormdeli/ui/auth/OTPScreen.kt`

UI để nhập mã OTP:
- Input field cho mã 6 chữ số
- Button "Xác minh"
- Button "Gửi lại mã OTP"

## Quy Trình Đăng Nhập

```
Login Screen
    ↓
Nhập số điện thoại
    ↓
Nhấn "Sign in"
    ↓
Firebase gửi OTP qua SMS
    ↓
OTP Screen
    ↓
Nhập mã OTP
    ↓
Nhấn "Xác minh"
    ↓
Firebase xác minh
    ↓
Tạo/Cập nhật user trong Firestore
    ↓
Đăng nhập thành công
```

## Xử Lý Lỗi

Các lỗi phổ biến:
1. **"Invalid phone number"**: Số điện thoại không hợp lệ
   - Kiểm tra format số điện thoại
   - Đảm bảo có prefix quốc gia (+44)

2. **"Verification code expired"**: Mã OTP hết hạn
   - Mã OTP có thời hạn 60 giây
   - Yêu cầu gửi lại mã mới

3. **"Invalid verification code"**: Mã OTP sai
   - Kiểm tra lại mã nhập
   - Yêu cầu gửi lại mã nếu cần

4. **"Quota exceeded"**: Vượt quá giới hạn
   - Firebase có giới hạn số lượng SMS/ngày
   - Nâng cấp plan hoặc chờ đến ngày hôm sau

## Lưu Ý Quan Trọng

1. **Development vs Production**:
   - Development: Có thể dùng số điện thoại test
   - Production: Cần verify và có thể tốn phí SMS

2. **Security**:
   - Không bao giờ log verification ID hoặc mã OTP
   - Luôn validate input từ user

3. **User Experience**:
   - Hiển thị loading state khi đang xử lý
   - Thông báo lỗi rõ ràng cho user
   - Cho phép gửi lại mã OTP

## Tùy Chỉnh

### Thay Đổi Thời Gian Hết Hạn OTP
Trong `AuthRepository.kt`, method `sendPhoneVerificationCode()`:
```kotlin
.setTimeout(60L, TimeUnit.SECONDS) // Thay đổi số giây ở đây
```

### Thay Đổi Format Số Điện Thoại
Trong `AuthViewModel.kt`, method `signInWithPhone()`:
```kotlin
val formattedPhone = if (phone.startsWith("+")) phone else "+44$phone"
// Thay đổi prefix +44 thành prefix khác nếu cần
```

