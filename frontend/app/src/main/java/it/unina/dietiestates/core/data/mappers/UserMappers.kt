package it.unina.dietiestates.core.data.mappers

import it.unina.dietiestates.core.data.dto.UserDto
import it.unina.dietiestates.core.domain.User

fun UserDto.toUser() =
    User(
        userId = userId,
        email = email,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        profilePic = profilePic,
        role = role,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun User.toUserDto() =
    UserDto(
        userId = userId,
        email = email,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        profilePic = profilePic,
        role = role,
        createdAt = createdAt,
        updatedAt = updatedAt
    )