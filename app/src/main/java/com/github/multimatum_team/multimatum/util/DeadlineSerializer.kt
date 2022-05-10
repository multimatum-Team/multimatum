package com.github.multimatum_team.multimatum.util

import com.github.multimatum_team.multimatum.model.Deadline
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.format.DateTimeFormatter

/**
 * custom serializer to serialize LocalDateTime into Json with Gson
 * took from https://www.javaguides.net/2019/11/gson-localdatetime-localdate.html
 */
internal class DeadlineSerializer : JsonSerializer<Deadline> {
    override fun serialize(
        deadline: Deadline,
        srcType: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val obj = JsonObject()
        obj.addProperty("title", deadline.title)
        obj.addProperty("state", deadline.state.name)
        obj.addProperty("dateTime", formatter.format(deadline.dateTime))
        obj.addProperty("description", deadline.description)
        return obj
    }

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("d::MM::uuuu HH::mm::ss")
    }
}