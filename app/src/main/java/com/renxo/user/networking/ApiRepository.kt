package com.renxo.user.networking


class ApiRepository(private val helper: ApiHelper) {
    suspend fun authenticateUser(body: AuthModel, url: String) =
        helper.postRequest<AuthResponse, AuthModel>(
            url + ApiEndpoints.AUTHENTICATE,
            body = body
        )

    suspend fun refreshToken(currentToken: String, url: String) =
        helper.postRequest<AuthResponse, RefreshTokenRequest>(
            url + ApiEndpoints.REFRESH_TOKEN,
            body = RefreshTokenRequest(currentToken)
        )
}
