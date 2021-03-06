package sinhee.kang.tutorial

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.util.MultiValueMap

import sinhee.kang.tutorial.domain.auth.domain.verification.SignUpVerification
import sinhee.kang.tutorial.domain.auth.domain.verification.enums.EmailVerificationStatus
import sinhee.kang.tutorial.domain.auth.dto.request.SignInRequest
import sinhee.kang.tutorial.domain.auth.dto.response.TokenResponse
import sinhee.kang.tutorial.domain.user.domain.friend.enums.FriendStatus
import sinhee.kang.tutorial.domain.user.domain.user.User
import sinhee.kang.tutorial.infra.redis.EmbeddedRedisConfig

import javax.servlet.http.Cookie
import kotlin.reflect.KClass

@SpringBootTest(classes = [TutorialApplication::class, EmbeddedRedisConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test", "local")
class TestLib: CombineVariables() {

    protected fun requestBody(
        method: MockHttpServletRequestBuilder,
        obj: Any? = null,
        token: String = ""
    ): ResultActions =
        mvc.perform(method
            .header("Authorization", token)
            .content(objectMapper
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                .writeValueAsString(obj))
            .contentType(MediaType.APPLICATION_JSON_VALUE))

    protected fun requestBody(
        method: MockHttpServletRequestBuilder,
        obj: Any? = null,
        cookie: Cookie?
    ): ResultActions =
        mvc.perform(method
            .cookie(cookie)
            .content(objectMapper
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                .writeValueAsString(obj))
            .contentType(MediaType.APPLICATION_JSON_VALUE))

    protected fun requestParams(
        method: MockHttpServletRequestBuilder,
        params: MultiValueMap<String, String>,
        token: String = ""
    ): ResultActions =
        mvc.perform(method
            .header("Authorization", token)
            .params(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE))

    protected fun getAccessToken(signInRequest: SignInRequest): String {
        val content = requestBody(post("/auth"), signInRequest)
            .andReturn().response.contentAsString
        val tokenResponse = mappingResponse(content, TokenResponse::class.java) as TokenResponse

        return "Bearer ${tokenResponse.accessToken}"
    }

    protected fun getRefreshToken(signInRequest: SignInRequest): Cookie? {
        return requestBody(post("/auth"), signInRequest)
            .andReturn().response.cookies.first { it.name == "_Refresh" }
    }

    protected fun emailVerify(user: User) {
        signUpVerificationRepository.save(
            SignUpVerification(
                email = user.email,
                authCode = "AUTH-CODE",
                emailStatus = EmailVerificationStatus.VERIFIED,
                nickname = user.nickname
            )
        )
    }

    protected fun isCheckUserAndTargetUserExist(user: User, targetUser: User) =
        friendRepository.findByUserAndTargetUser(user, user2)
            ?.let { true }
            ?: false

    protected fun isConnection(user1: User, user2: User, friendStatus: FriendStatus): Boolean {
        val connection1 = user1.isExistUserAndTargetUser(user2, friendStatus)
        val connection2 = user2.isExistUserAndTargetUser(user1, friendStatus)

        return connection1 || connection2
    }

    private fun User.isExistUserAndTargetUser(targetUser: User, friendStatus: FriendStatus) =
        friendRepository.findByUserAndTargetUserAndStatus(this, targetUser, friendStatus)
            ?.let { true }
            ?: false

    protected fun mappingResponse(obj: String, cls: Class<*>): Any =
        objectMapper
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
            .readValue(obj, cls)
}
