package com.github.multimatum_team.multimatum.util

import com.google.gson.GsonBuilder
import java.time.LocalDateTime
import com.github.multimatum_team.multimatum.model.Deadline

class JsonDeadlineConverter {
    val gsonBuilder = GsonBuilder().registerTypeAdapter(
        LocalDateTime::class.java,
        LocalDateTimeSerializer()
    ).registerTypeAdapter(
        LocalDateTime::class.java,
        LocalDateTimeDeserializer()
    )
    val gson = gsonBuilder.create()

    fun toJson(deadline: Deadline): String {
        return gson.toJson(deadline)
    }

    fun fromJson(json: String): Deadline {
        return gson.fromJson(json, Deadline::class.java)
    }
}