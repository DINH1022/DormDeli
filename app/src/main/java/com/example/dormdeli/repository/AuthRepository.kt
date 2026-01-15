package com.example.dormdeli.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    // Lấy user hiện tại
    fun getCurrentUser() = auth.currentUser

    // Kiểm tra xem User đã liên kết với Google chưa
    fun isGoogleLinked(): Boolean {
        val user = auth.currentUser
        return user?.providerData?.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } ?: false
    }

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

    // Liên kết tài khoản Google với tài khoản hiện tại
    fun linkGoogleAccount(
        account: GoogleSignInAccount,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser
        if (user != null) {
            // Kiểm tra xem chính tài khoản này đã link Google chưa
            if (isGoogleLinked()) {
                onFailure(Exception("Tài khoản này đã được liên kết với Google."))
                return
            }

            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            user.linkWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess()
                    } else {
                        val exception = task.exception
                        if (exception is FirebaseAuthUserCollisionException) {
                            onFailure(Exception("Tài khoản Google này đã được liên kết với một người dùng khác."))
                        } else {
                            onFailure(exception ?: Exception("Liên kết tài khoản Google thất bại."))
                        }
                    }
                }
        } else {
            onFailure(Exception("Người dùng chưa đăng nhập."))
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
