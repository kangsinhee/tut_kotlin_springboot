package sinhee.kang.tutorial.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import sinhee.kang.tutorial.TutorialApplication
import sinhee.kang.tutorial.domain.auth.domain.verification.EmailVerification
import sinhee.kang.tutorial.domain.auth.domain.verification.enums.EmailVerificationStatus
import sinhee.kang.tutorial.domain.auth.domain.verification.repository.EmailVerificationRepository
import sinhee.kang.tutorial.domain.auth.dto.request.*
import sinhee.kang.tutorial.domain.auth.dto.response.TokenResponse
import sinhee.kang.tutorial.domain.user.domain.user.repository.UserRepository
import sinhee.kang.tutorial.infra.redis.EmbeddedRedisConfig

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TutorialApplication::class, EmbeddedRedisConfig::class],
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserApiTest {
    @Autowired
    private lateinit var mvc: MockMvc
    @Autowired
    private lateinit var emailVerificationRepository: EmailVerificationRepository
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    val testMail = "rkdtlsgml50@naver.com"
    val passwd = "1234"
    val username = "user"


    @Test
    @Throws
    fun signUpTest() {
        signUp()
        val user = userRepository.findByNickname("user")
                ?:{ throw Exception() }()
        userRepository.delete(user)
    }


    @Test
    @Throws
    fun nicknameVerifyTest() {
        mvc.perform(get("/users/nickname")
                .param("nickname", "user"))
                .andExpect(status().isOk)
                .andDo(print())
    }


    @Test
    @Throws
    fun changePasswordTest() {
        signUp()
        emailVerify("rkdtlsgml50@naver.com")

        val request = ChangePasswordRequest("rkdtlsgml50@naver.com", "1234")
        requestMvc(put("/users/password"), request)

        userRepository.findByNickname("user")
                ?.let { userRepository.delete(it) }
                ?:{ throw Exception() }()
    }


    private fun emailVerify(email: String) {
        emailVerificationRepository.save(EmailVerification(
                email = email,
                authCode = "CODE",
                status = EmailVerificationStatus.UNVERIFID
        ))

        val request = VerifyCodeRequest(email, "CODE")
        requestMvc(put("/users/email/verify"), request)
    }


    private fun signUp() {
        emailVerify("rkdtlsgml50@naver.com")
        val request = SignUpRequest("rkdtlsgml50@naver.com", passwordEncoder.encode("1234"), "user")
        requestMvc(post("/users"), request)
    }


    private fun requestMvc(method: MockHttpServletRequestBuilder, obj: Any? = null): String {
        return mvc.perform(
                method
                        .content(ObjectMapper()
                                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                                .writeValueAsString(obj))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk)
                .andReturn().response.contentAsString
    }


    private fun accessToken(): String {
        val content = requestMvc(post("/auth"), SignInRequest(testMail, passwd))
        val response = mappingResponse(content, TokenResponse::class.java) as TokenResponse
        return response.accessToken
    }


    private fun mappingResponse(obj: String, cls: Class<*>): Any {
        return objectMapper
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                .readValue(obj, cls)
    }
}