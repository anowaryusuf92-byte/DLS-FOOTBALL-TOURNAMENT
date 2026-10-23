package com.example.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.SUPER_ADMIN_UID
import com.example.data.model.UserProfile
import com.example.data.repository.TournamentRepository
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object Unauthenticated : AuthUiState
    data class Authenticated(val user: FirebaseUser, val profile: UserProfile) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val repository: TournamentRepository
) : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val TAG = "AuthViewModel"

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    init {
        checkCurrentAuth()
    }

    private fun checkCurrentAuth() {
        val user = auth.currentUser
        if (user != null) {
            observeProfile(user)
        } else {
            _uiState.value = AuthUiState.Unauthenticated
        }
    }

    private fun observeProfile(user: FirebaseUser) {
        viewModelScope.launch {
            repository.observeUserProfile(user.uid).collectLatest { profile ->
                val finalProfile = profile ?: UserProfile(
                    uid = user.uid,
                    name = user.displayName ?: "খেলোয়াড়",
                    email = user.email ?: "",
                    photoUrl = user.photoUrl?.toString() ?: "",
                    role = if (user.uid == SUPER_ADMIN_UID) "super_admin" else "player"
                )
                _uiState.value = AuthUiState.Authenticated(user, finalProfile)
            }
        }
    }

    fun toggleAdminMode() {
        _isAdminMode.value = !_isAdminMode.value
    }

    fun setAdminMode(active: Boolean) {
        _isAdminMode.value = active
    }

    fun loginWithEmail(
        email: String,
        pass: String,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || pass.isBlank()) {
            onError("দয়া করে ইমেইল এবং পাসওয়ার্ড পূরণ করুন")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val res = auth.signInWithEmailAndPassword(email.trim(), pass.trim()).await()
                val user = res.user
                if (user != null) {
                    observeProfile(user)
                } else {
                    _uiState.value = AuthUiState.Unauthenticated
                    onError("লগইন ব্যর্থ হয়েছে")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Email login error", e)
                _uiState.value = AuthUiState.Unauthenticated
                val msg = e.localizedMessage ?: "লগইন করতে সমস্যা হয়েছে"
                onError(msg)
            }
        }
    }

    fun registerWithEmail(
        name: String,
        phone: String,
        email: String,
        pass: String,
        onError: (String) -> Unit
    ) {
        if (name.isBlank() || phone.isBlank() || email.isBlank() || pass.isBlank()) {
            onError("সব তথ্য সঠিক ভাবে পূরণ করুন")
            return
        }
        if (pass.length < 6) {
            onError("পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val res = auth.createUserWithEmailAndPassword(email.trim(), pass.trim()).await()
                val user = res.user
                if (user != null) {
                    val role = if (user.uid == SUPER_ADMIN_UID) "super_admin" else "player"
                    val newProfile = UserProfile(
                        uid = user.uid,
                        name = name.trim(),
                        email = email.trim(),
                        phone = phone.trim(),
                        role = role
                    )
                    repository.saveUserProfile(newProfile)
                    observeProfile(user)
                } else {
                    _uiState.value = AuthUiState.Unauthenticated
                    onError("রেজিস্ট্রেশন ব্যর্থ হয়েছে")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Registration error", e)
                _uiState.value = AuthUiState.Unauthenticated
                onError(e.localizedMessage ?: "রেজিস্ট্রেশন সম্পন্ন করা যায়নি")
            }
        }
    }

    fun signInWithGoogle(
        context: Context,
        onError: (String) -> Unit
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.w(TAG, "Web Client ID not resolved: ${e.message}")
            onError("Google সাইন-ইন কনফিগারেশন পাওয়া যায়নি")
            return
        }

        val credentialManager = CredentialManager.create(context)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = credentialManager.getCredential(context as Activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authRes = auth.signInWithCredential(authCredential).await()
                    val user = authRes.user
                    if (user != null) {
                        // Create initial profile if first time
                        val role = if (user.uid == SUPER_ADMIN_UID) "super_admin" else "player"
                        val initProfile = UserProfile(
                            uid = user.uid,
                            name = user.displayName ?: "খেলোয়াড়",
                            email = user.email ?: "",
                            photoUrl = user.photoUrl?.toString() ?: "",
                            role = role
                        )
                        repository.saveUserProfile(initProfile)
                        observeProfile(user)
                    }
                } else {
                    _uiState.value = AuthUiState.Unauthenticated
                    onError("Google শংসাপত্র যাচাই ব্যর্থ")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In cancelled: ${e.message}")
                _uiState.value = AuthUiState.Unauthenticated
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                _uiState.value = AuthUiState.Unauthenticated
                onError(e.localizedMessage ?: "Google সাইন-ইন ব্যর্থ হয়েছে")
            }
        }
    }

    fun updateProfile(
        name: String,
        phone: String,
        whatsapp: String,
        inGameId: String,
        teamName: String,
        photoUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentState = _uiState.value
        if (currentState !is AuthUiState.Authenticated) return

        viewModelScope.launch {
            val updated = currentState.profile.copy(
                name = name,
                phone = phone,
                whatsapp = whatsapp,
                inGameId = inGameId,
                teamName = teamName,
                photoUrl = photoUrl
            )
            val res = repository.saveUserProfile(updated)
            if (res.isSuccess) {
                _uiState.value = currentState.copy(profile = updated)
                onSuccess()
            } else {
                onError(res.exceptionOrNull()?.localizedMessage ?: "প্রোফাইল আপডেট করতে সমস্যা হয়েছে")
            }
        }
    }

    fun signOut(context: Context) {
        auth.signOut()
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Error clearing credential manager state: ${e.message}")
            }
            _isAdminMode.value = false
            _uiState.value = AuthUiState.Unauthenticated
        }
    }
}
