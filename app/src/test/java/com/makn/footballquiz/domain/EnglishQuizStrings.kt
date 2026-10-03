package com.makn.footballquiz.domain

import com.makn.footballquiz.domain.model.KitColour
import com.makn.footballquiz.domain.text.QuizStrings
import com.makn.footballquiz.domain.text.QuizText
import java.io.File

/** [QuizStrings] backed by the app's real `res/values/strings.xml`, for JVM unit tests. */
object EnglishQuizStrings : QuizStrings {

    private val strings: Map<String, String> by lazy {
        val xml = listOf("src/main/res/values/strings.xml", "app/src/main/res/values/strings.xml")
            .map(::File).first { it.exists() }.readText()
        Regex("""<string name="(\w+)"[^>]*>(.*?)</string>""").findAll(xml)
            .associate { it.groupValues[1] to it.groupValues[2].replace("\\'", "'").replace("\\\"", "\"").replace("&amp;", "&") }
    }

    override val languageCode = "en"
    override val shortsUsePluralColour = false

    override fun text(key: QuizText, vararg args: Any): String {
        val name = when {
            key.name.startsWith("KIT_") && key != QuizText.KIT_PROMPT && key != QuizText.KIT_EXPLANATION -> key.name.lowercase()
            else -> "q_" + key.name.lowercase()
        }
        return String.format(strings.getValue(name), *args)
    }

    override fun colour(colour: KitColour, plural: Boolean): String =
        strings.getValue("colour_" + colour.name.lowercase() + if (plural) "_pl" else "")

    override fun number(value: Int): String = "%,d".format(value)
}
