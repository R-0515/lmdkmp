package org.example.project.myPool.domian.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActiveUsersEnvelope(
    val success: Boolean,
    val data: List<ActiveUserDto>?,

    @SerialName("total_count")
    val totalCount: Int?,

    @SerialName("current_user_id")
    val currentUserId: String?,
)

@Serializable
data class ActiveUserDto(
    val id: String,
    val name: String,
)
