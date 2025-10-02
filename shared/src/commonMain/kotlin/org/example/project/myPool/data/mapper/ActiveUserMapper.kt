package org.example.project.myPool.data.mapper

import org.example.project.myPool.domian.model.ActiveUser
import org.example.project.myPool.domian.model.ActiveUserDto

fun ActiveUserDto.toDomain() = ActiveUser(
    id = id,
    name = name
)