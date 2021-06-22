package sinhee.kang.tutorial.domain.auth.service.validate

import org.springframework.stereotype.Service

import sinhee.kang.tutorial.domain.auth.service.email.enums.SendType
import sinhee.kang.tutorial.domain.user.domain.user.repository.UserRepository
import sinhee.kang.tutorial.global.exception.exceptions.conflict.UserAlreadyExistsException
import sinhee.kang.tutorial.global.exception.exceptions.badRequest.BadRequestException
import sinhee.kang.tutorial.global.exception.exceptions.notFound.UserNotFoundException

import java.util.regex.Pattern

@Service
class ValidateServiceImpl(
    private val userRepository: UserRepository
): ValidateService {

    override fun validateEmail(email: String) {
        val emailRegex = "^[0-9a-zA-Z._-]+@[0-9a-zA-Z.-]+\\.[a-zA-Z]{2,3}$"

        if (!validateRegexPattern(emailRegex, email))
            throw BadRequestException()
    }

    override fun validatePassword(password: String) {
        val passwordRegex = "(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}"

        if (!validateRegexPattern(passwordRegex, password))
            throw BadRequestException()
    }

    override fun validateNickname(nickname: String) {
        userRepository.findByNickname(nickname)
            ?.let { throw UserAlreadyExistsException() }

        if (nickname.isEmpty() || nickname.isBlank())
            throw BadRequestException()
    }

    override fun validateExistEmail(email: String, sendType: SendType?) {
        val user = userRepository.findByEmail(email)

        when(sendType) {
            SendType.REGISTER -> user
                ?.let { throw UserAlreadyExistsException() }
            SendType.USER -> user
                ?: throw UserNotFoundException()
        }
    }

    private fun validateRegexPattern(regex: String, input: String) =
        Pattern
            .compile(regex)
            .matcher(input)
            .matches()
}
