package sinhee.kang.tutorial.global.businessException.exception.auth

import sinhee.kang.tutorial.global.businessException.BusinessException
import sinhee.kang.tutorial.global.businessException.ErrorCode

class UserAlreadyExistsException : BusinessException(ErrorCode.USER_DUPLICATION)