package com.github.multimatum_team.multimatum.util

import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Custom serializer to deserialize Deadline into Json with Gson.
 */
internal class DeadlineDeserializer : JsonDeserializer<Deadline> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Deadline {
        val obj = json.asJsonObject
        val title = obj.get("title").asString
        val state = DeadlineState.valueOf(obj.get("state").asString)
        // took from https://www.javaguides.net/2019/11/gson-localdatetime-localdate.html
        val dateTime = LocalDateTime.parse(
            obj.get("dateTime").asString,
            DateTimeFormatter.ofPattern("d::MM::uuuu HH::mm::ss").withLocale(Locale.ENGLISH)
        )
        val description = obj.get("description").asString
        return Deadline(title, state, dateTime, description)
    }
}
