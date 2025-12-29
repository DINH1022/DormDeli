package com.example.dormdeli.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.GoogleAuthProvider
class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    // Lấy user hiện tại
    fun getCurrentUser() = auth.currentUser

    // Đăng nhập bằng email/password
    fun signInWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception ?: Exception("Đăng nhập thất bại"))
                }
            }
    }

    // Đăng ký bằng email/password
    fun signUpWithEmail(
        email: String,
        password: String,
        fullName: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Cập nhật display name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()

                    task.result?.user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener {
                            if (it.isSuccessful) {
                                onSuccess()
                            } else {
                                onFailure(it.exception ?: Exception("Cập nhật thông tin thất bại"))
                            }
                        }
                } else {
                    onFailure(task.exception ?: Exception("Đăng ký thất bại"))
                }
            }
    }

    fun signInWithGoogle(
        account: GoogleSignInAccount,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception ?: Exception("Google sign in failed"))
                }
            }
    }

    // Gửi OTP đến số điện thoại
    fun sendPhoneVerificationCode(
        phoneNumber: String,
        activity: android.app.Activity,
        callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callback)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Xác minh OTP
    fun verifyPhoneNumberWithCode(
        code: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val credential = verificationId?.let {
            PhoneAuthProvider.getCredential(it, code)
        }

        if (credential != null) {
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure(task.exception ?: Exception("Xác minh mã OTP thất bại"))
                    }
                }
        } else {
            onFailure(Exception("Mã xác minh không hợp lệ"))
        }
    }

    // Lưu verification ID (được gọi từ callback)
    fun setVerificationId(id: String) {
        verificationId = id
    }

    // Lưu resend token (được gọi từ callback)
    fun setResendToken(token: PhoneAuthProvider.ForceResendingToken) {
        resendToken = token
    }

    // Đăng xuất
    fun signOut() {
        auth.signOut()
    }

    // Kiểm tra đã đăng nhập chưa
    fun isSignedIn(): Boolean = auth.currentUser != null
}

