package com.chatur.frontend.authentication

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.example.mystore.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/**
 * AuthService handles Google Sign-In flow and Firebase authentication.
 *
 * Usage (in an Activity):
 *   1. Call getGoogleSignInIntent() to get the Intent for Google account picker.
 *   2. Launch it via startActivityForResult or ActivityResultLauncher.
 *   3. In onActivityResult, call handleGoogleSignInResult(data) and observe callback.
 */
class AuthService(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    /**
     * Returns the Google Sign-In Intent to be launched by the caller Activity.
     */
    fun getGoogleSignInIntent(): Intent = googleSignInClient.signInIntent

    /**
     * Handles the result Intent returned after Google account selection.
     * Calls [onSuccess] with AuthResult on success, or [onFailure] with an Exception.
     */
    fun handleGoogleSignInResult(
        data: Intent?,
        onSuccess: (AuthResult) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
                ?: return onFailure(Exception("Google ID token is null"))

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { result -> onSuccess(result) }
                .addOnFailureListener { e -> onFailure(e) }

        } catch (e: ApiException) {
            onFailure(e)
        }
    }

    /**
     * Signs out from both Firebase and Google.
     */
    fun signOut(onComplete: () -> Unit = {}) {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener { onComplete() }
    }

    /**
     * Returns the currently signed-in Firebase user, or null.
     */
    fun currentUser() = auth.currentUser
}
