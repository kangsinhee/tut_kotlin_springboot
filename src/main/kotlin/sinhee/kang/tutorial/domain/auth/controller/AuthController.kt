package sinhee.kang.tutorial.domain.auth.controller

import org.springframework.web.bind.annotation.*
import sinhee.kang.tutorial.domain.auth.dto.request.SignInRequest
import sinhee.kang.tutorial.domain.auth.service.auth.AuthService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping
    fun signIn(@RequestBody signInRequest: SignInRequest, response: HttpServletResponse) =
        authService.signIn(signInRequest, response)

    @PutMapping
    fun extendAuthTokens(request: HttpServletRequest, response: HttpServletResponse) =
        authService.extendAuthTokens(request, response)
}
