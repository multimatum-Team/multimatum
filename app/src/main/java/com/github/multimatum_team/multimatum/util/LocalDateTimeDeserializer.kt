package com.github.multimatum_team.multimatum.util

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * custom serializer to deserialize LocalDateTime into Json with Gson
 * took from https://www.javaguides.net/2019/11/gson-localdatetime-localdate.html
 */
internal class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime?> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?):
            LocalDateTime {
        return LocalDateTime.parse(json.asString,
            DateTimeFormatter.ofPattern("d::MM::uuuu HH::mm::ss").withLocale(Locale.ENGLISH)
        )
    }
}
