package it.unina.dietiestates.core.data.googleAuth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import it.unina.dietiestates.BuildConfig

class GoogleAuthUtil(
    private val context: Context
) {

    private val credentialManager = CredentialManager.Companion.create(context)

    suspend fun sendSignInRequest(onSuccess: (token: String) -> Unit, onFailure : () -> Unit){
        try{
            val result = buildRequest()
            val token = handleSignInResult(result)

            if(token != null){
                onSuccess(token)
            }
            else{
                onFailure()
            }
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

    private suspend fun buildRequest(): GetCredentialResponse {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .setServerClientId(
                        BuildConfig.GOOGLE_CLIENT_ID
                    )
                    .build()
            ).build()

       return credentialManager.getCredential(
           request = request,
           context = context
       )
    }

    private fun handleSignInResult(result: GetCredentialResponse): String?{
        val credential = result.credential

        return if(credential is CustomCredential && credential.type == GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.Companion.createFrom(credential.data)
                googleIdTokenCredential.idToken
            }
            catch (e: GoogleIdTokenParsingException){
                e.printStackTrace()
                null
            }
        } else null
    }
}