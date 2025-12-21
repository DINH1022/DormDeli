# Tài Liệu Triển Khai Đăng Nhập/Đăng Ký DormDeli

## Tổng Quan
Đã triển khai hệ thống đăng nhập và đăng ký với UI giống design, sử dụng Firebase Authentication. Logic Firebase được giữ nguyên như yêu cầu.

## Các File Đã Tạo

### 1. Repository Layer
- **`AuthRepository.kt`**: Quản lý Firebase Authentication
  - Đăng nhập/đăng ký bằng email/password
  - Gửi và xác minh OTP qua số điện thoại
  - Quản lý session

### 2. UI Components
- **`LoginScreen.kt`**: Màn hình đăng nhập
  - Input số điện thoại với UK flag
  - Checkbox "Remember me"
  - Nút "Sign in" (chỉ active khi có số điện thoại hợp lệ)
  - Social login buttons (Google, Facebook, Apple)
  - Link chuyển sang đăng ký

- **`SignUpScreen.kt`**: Màn hình đăng ký
  - Input số điện thoại
  - Input email
  - Input họ tên
  - Checkbox "Remember me"
  - Nút "Register" (chỉ active khi điền đủ thông tin)
  - Social sign-up buttons
  - Link chuyển sang đăng nhập

- **`OTPScreen.kt`**: Màn hình nhập mã OTP
  - Input mã OTP 6 chữ số
  - Nút "Xác minh"
  - Nút "Gửi lại mã OTP"

- **`AuthNavigation.kt`**: Điều hướng giữa các màn hình auth

- **`AuthViewModel.kt`**: Quản lý state và logic xử lý

### 3. Theme
- **`Color.kt`**: Thêm màu cam theo design
  - `OrangePrimary`: #FF6B35 (màu cam chính)
  - `OrangeLight`: #FFB399 (màu cam nhạt cho disabled states)
  - `OrangeDark`: #CC5529 (màu cam đậm)

## Tính Năng

### Đăng Nhập Bằng Số Điện Thoại (với OTP)
1. Người dùng nhập số điện thoại
2. Nhấn "Sign in"
3. Firebase gửi mã OTP qua SMS
4. Màn hình chuyển sang nhập OTP
5. Nhập mã 6 chữ số và xác minh
6. Đăng nhập thành công, tạo/cập nhật user trong Firestore

### Đăng Ký Bằng Email/Password
1. Người dùng điền số điện thoại, email, họ tên
2. Nhấn "Register"
3. Tạo tài khoản Firebase với email/password
4. Tạo user trong Firestore với thông tin đã nhập
5. Đăng nhập tự động

### Social Login (Chưa implement đầy đủ)
- Có UI buttons nhưng logic chưa được implement
- Có thể mở rộng sau với Firebase Social Auth

## UI/UX Features

### States Management
- **Empty State**: Nút bị disable (màu nhạt), social buttons bị grey out
- **Typing State**: Nút được enable khi có đủ thông tin
- **Filled State**: Tất cả elements active, màu cam đậm

### Validation
- Phone: Tối thiểu 10 ký tự
- Email: Phải chứa "@"
- OTP: Chính xác 6 chữ số
- Full Name: Không được trống

## Flow Đăng Nhập

```
MainActivity
    ↓
AuthNavigation
    ↓
┌─────────────────────────────────────┐
│  isSignedIn?                        │
└─────────────────────────────────────┘
    ↓ No                    ↓ Yes
LoginScreen          MainScreen
    ↓
┌─────────────────────────────────────┐
│  Chọn: Login hoặc Sign Up          │
└─────────────────────────────────────┘
    ↓ Login              ↓ Sign Up
Nhập phone          SignUpScreen
    ↓                    ↓
Nhấn Sign in        Nhập thông tin
    ↓                    ↓
OTPScreen           Nhấn Register
    ↓                    ↓
Nhập OTP            Tạo account
    ↓                    ↓
Verify              Đăng nhập
    ↓                    ↓
isSignedIn = true ←────────┘
    ↓
MainScreen
```

## Cấu Hình Cần Thiết

### Firebase Console
1. Bật Phone Authentication provider
2. Thêm SHA-1 fingerprint của app
3. (Tùy chọn) Thêm số điện thoại test cho development

### Dependencies (Đã có sẵn)
- Firebase Auth
- Firebase Firestore
- Jetpack Compose
- Material3

## Lưu Ý Quan Trọng

### Logic Firebase
- **UserRepository** giữ nguyên logic hiện tại
- Chỉ tạo mới AuthRepository để xử lý authentication
- User được lưu vào Firestore với cấu trúc hiện tại

### OTP Implementation
- Xem file `OTP_GUIDE.md` để biết chi tiết về OTP
- Cần cấu hình SHA-1 fingerprint
- Có thể tốn phí SMS trong production

### Password cho Sign Up
Hiện tại khi đăng ký bằng email, password được tự động tạo từ số điện thoại. Có thể cải thiện bằng cách:
- Thêm field nhập password vào SignUpScreen
- Hoặc yêu cầu user đặt password sau khi đăng ký

## Cải Thiện Có Thể Thêm

1. **Social Login đầy đủ**: Implement Google, Facebook, Apple Sign In
2. **Password field**: Thêm input password cho đăng ký
3. **Phone formatting**: Tự động format số điện thoại khi nhập (ví dụ: 20 1234 5678)
4. **Remember me**: Lưu session và tự động đăng nhập lại
5. **Forgot password**: Chức năng quên mật khẩu
6. **Input validation messages**: Hiển thị thông báo lỗi cụ thể cho từng field

## Testing

### Test Cases
1. Đăng nhập bằng số điện thoại và OTP
2. Đăng ký bằng email/password
3. Chuyển đổi giữa Login và Sign Up screens
4. Validation các input fields
5. Error handling khi OTP sai hoặc hết hạn

### Test Phone Number
Trong Firebase Console, có thể thêm số điện thoại test để nhận mã OTP ngay lập tức trong development.

## Troubleshooting

### OTP không nhận được
- Kiểm tra SHA-1 fingerprint đã được thêm vào Firebase
- Kiểm tra số điện thoại có đúng format không (+44...)
- Xem logs trong Logcat để debug

### Lỗi compilation
- Đảm bảo đã sync Gradle
- Kiểm tra tất cả imports đều đúng
- Clean và rebuild project

