package com.github.multimatum_team.multimatum.util

import com.google.gson.GsonBuilder
import com.github.multimatum_team.multimatum.model.Deadline

class JsonDeadlineConverter {
    companion object {
        private val gsonBuilder = GsonBuilder().registerTypeAdapter(
            Deadline::class.java,
            DeadlineSerializer()
        ).registerTypeAdapter(
            Deadline::class.java,
            DeadlineDeserializer()
        )
    }

    private val gson = gsonBuilder.create()!!

    fun toJson(deadline: Deadline): String {
        return gson.toJson(deadline)
    }

    fun fromJson(json: String): Deadline {
        return gson.fromJson(json, Deadline::class.java)
    }
}