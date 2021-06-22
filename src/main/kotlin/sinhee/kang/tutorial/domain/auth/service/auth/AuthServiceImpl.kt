package sinhee.kang.tutorial.domain.auth.service.auth

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

import sinhee.kang.tutorial.domain.auth.dto.request.SignInRequest
import sinhee.kang.tutorial.domain.auth.dto.response.TokenResponse
import sinhee.kang.tutorial.domain.auth.service.validate.ValidateService
import sinhee.kang.tutorial.global.exception.exceptions.unAuthorized.UnAuthorizedException
import sinhee.kang.tutorial.domain.user.domain.user.User
import sinhee.kang.tutorial.domain.user.domain.user.repository.UserRepository
import sinhee.kang.tutorial.global.exception.exceptions.badRequest.BadRequestException
import sinhee.kang.tutorial.global.security.authentication.AuthenticationService
import sinhee.kang.tutorial.global.security.jwt.JwtTokenProvider
import sinhee.kang.tutorial.global.exception.exceptions.notFound.UserNotFoundException

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Service
class AuthServiceImpl(
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: JwtTokenProvider,
    private val authenticationService: AuthenticationService,

    private val validateService: ValidateService,

    private val userRepository: UserRepository,
): AuthService {

    override fun signIn(signInRequest: SignInRequest, httpServletResponse: HttpServletResponse): TokenResponse {
        val email = signInRequest.email
        val password = signInRequest.password

        validateService.apply {
            validateEmail(email)
            validatePassword(password)
        }

        val user = userRepository.findByEmail(email)
            ?.apply { isMatchedPassword(passwordEncoder, password) }
            ?: throw UserNotFoundException()

        return tokenProvider.setToken(httpServletResponse, user.nickname)
    }

    override fun extendAuthTokens(
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): TokenResponse {
        tokenProvider.apply {
            return getRefreshToken(httpServletRequest)
                ?.takeIf { isRefreshToken(it) && isValidateToken(it) }
                ?.let { setToken(httpServletResponse, getUsername(it)) }
                ?: throw BadRequestException()
        }
    }

    override fun verifyCurrentUser(): User {
        val currentUsername = authenticationService.getUserName()

        return userRepository.findByNickname(currentUsername)
            ?: throw UnAuthorizedException()
    }
}
