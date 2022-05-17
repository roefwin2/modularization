package com.ellcie.ellcieauthenticationlibrary.authmanager

import android.content.Intent
import com.ellcie.ellcieauthenticationlibrary.network.api.AuthApi
import com.ellcie.ellcieauthenticationlibrary.utils.Constants.CLIENT_ID
import com.ellcie.toolkitlibrary.resource.Resource
import com.ellcie.toolkitlibrary.userauth.EllcieAuthManager
import com.ellcie.toolkitlibrary.userauth.UserAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.channelFlow
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import javax.inject.Inject
import javax.inject.Singleton

//Need to have @Inject + custom component to force lateinit var injection
/**
 * Implementation Manager for authentication
 */
@Singleton
class EllcieAuthManagerImpl @Inject constructor() : EllcieAuthManager {
    private val _currentAuth: MutableStateFlow<Resource<UserAuth>> =
        MutableStateFlow(Resource.Loading)
    override val currentAuth: StateFlow<Resource<UserAuth>> get() = _currentAuth


    @Inject
    /**
     * current authState token
     */
    internal lateinit var authState: AuthState

    /**
     * intent for call the url
     */
    @Inject
    lateinit var authIntent: Intent

    @Inject
    /**
     * auth service to get all the action give by the AppAuth
     */
    internal lateinit var authService: AuthorizationService

    @Inject
    /**
     * Trigger the http call for the logout
     */
    internal lateinit var retrofitAuthApi: AuthApi

    //TODO remove collect not safe collect always
    /**
     * login function to use intent for the login process
     */

    fun getAuthStateInJson() :String {
        return authState.jsonSerializeString()
    }

    fun restoreAuthState(authState: AuthState?){
        if (authState != null) {
            this.authState = authState
        }

    }

    override suspend fun login(data: Intent?) = channelFlow {
        send(Resource.Loading)
        val resp = data?.let { AuthorizationResponse.fromIntent(it) }
        val ex = AuthorizationException.fromIntent(data)
        try {
            if (resp != null) {
                authState.update(resp, ex)
                performTokenRequest(resp).collect {
                    send(it)
                }
            } else {
                _currentAuth.value = Resource.Error(ex.toString())
                send(Resource.Error(ex.toString()))
            }
        } catch (e: Exception) {
            _currentAuth.value = Resource.Error(e.toString())
            send(Resource.Error(e.toString()))
        }
    }

    /**
     * internal function to retrieve the token response
     */
    private fun performTokenRequest(authorizationResponse: AuthorizationResponse) = callbackFlow{
        try {
            authService.performTokenRequest(
                authorizationResponse.createTokenExchangeRequest()
            ) { resp, ex ->
                if(ex != null){
                    _currentAuth.value = Resource.Error(ex.toString())
                    trySend(Resource.Error(ex.toString()))
                }else {
                    authState.update(resp, ex)
                    _currentAuth.value = Resource.Success(
                        UserAuth.Authorize(
                            authState.accessToken ?: "error login token form Keycloack"
                        )
                    )
                    trySend(
                        Resource.Success(
                            UserAuth.Authorize(
                                authState.accessToken ?: "error login token form Keycloack"
                            )
                        )
                    )
                }
            }
        } catch (e: Exception) {
            _currentAuth.value = Resource.Error(e.toString())
            trySend(Resource.Error(e.toString()))
        }
        awaitClose {  }
    }

    /**
     * logout function call the rertofit httpclient to request the logout
     */
    override suspend fun logout() = flow {
        try {
            if(authState.isAuthorized) {
                retrofitAuthApi.postLogout(CLIENT_ID, authState.refreshToken!!)
                _currentAuth.value =
                    Resource.Success(UserAuth.UnAuthorize("logout Successful"))
                emit(Resource.Success(UserAuth.UnAuthorize("logout Successful")))
            }else{
                _currentAuth.value = Resource.Error("failed logout authstate not authorize")
                emit(Resource.Error("failed logout authstate not authorize"))
            }
        } catch (e: Exception) {
            _currentAuth.value = Resource.Error(e.toString())
            emit(Resource.Error(e.toString()))
        }
    }

    /**
     * refresh token function to trigger the expired token refresh action
     */
    override suspend fun refreshToken() = callbackFlow {
      authState.performActionWithFreshTokens(authService) { accessToken, idToken, exception ->
            if (exception != null) {
                // negotiation for fresh tokens failed, check ex for more details
                _currentAuth.value = Resource.Error(exception.toJsonString())
                trySend(Resource.Error(exception.toJsonString()))
            }
            if (accessToken != null) {
                _currentAuth.value =
                    Resource.Success(
                        UserAuth.Authorize(
                            accessToken
                        )
                    )
                trySend(Resource.Success(accessToken))
            }
        }
        awaitClose { }
    }
}