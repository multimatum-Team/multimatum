package com.github.multimatum_team.multimatum

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * custom serializer to serialize LocalDateTime into Json with Gson
 * took from https://www.javaguides.net/2019/11/gson-localdatetime-localdate.html
 */
internal class LocalDateTimeSerializer : JsonSerializer<LocalDateTime?> {
    override fun serialize(localDateTime: LocalDateTime?, srcType: Type?, context: JsonSerializationContext?):
            JsonElement {
        return JsonPrimitive(formatter.format(localDateTime))
    }

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("d::MMM::uuuu HH::mm::ss")
    }
}