package com.makn.footballquiz.ui.common

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.makn.footballquiz.R
import com.makn.footballquiz.domain.model.KitColour
import com.makn.footballquiz.domain.text.QuizStrings
import com.makn.footballquiz.domain.text.QuizText
import java.text.NumberFormat

/** [QuizStrings] from the app's string resources, in the language of [context]. */
class ResourceQuizStrings(private val context: Context) : QuizStrings {

    override val languageCode: String = context.resources.configuration.locales[0].language

    override val shortsUsePluralColour: Boolean = context.resources.getBoolean(R.bool.kit_shorts_plural)

    override fun text(key: QuizText, vararg args: Any): String = context.getString(key.resId(), *args)

    override fun colour(colour: KitColour, plural: Boolean): String =
        context.getString(if (plural) colour.pluralResId() else colour.resId())

    override fun number(value: Int): String =
        NumberFormat.getIntegerInstance(context.resources.configuration.locales[0]).format(value)
}

/** Resource-backed strings for the current screen's language; rebuilt when the language changes. */
@Composable
fun rememberQuizStrings(): QuizStrings {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(context, configuration) { ResourceQuizStrings(context) }
}

private fun QuizText.resId(): Int = when (this) {
    QuizText.STADIUM_PROMPT -> R.string.q_stadium_prompt
    QuizText.STADIUM_EXPLANATION -> R.string.q_stadium_explanation
    QuizText.STADIUM_CLUB_PROMPT -> R.string.q_stadium_club_prompt
    QuizText.STADIUM_CLUB_EXPLANATION -> R.string.q_stadium_club_explanation
    QuizText.CAPACITY_PROMPT -> R.string.q_capacity_prompt
    QuizText.CAPACITY_EXPLANATION -> R.string.q_capacity_explanation
    QuizText.NICKNAME_PROMPT -> R.string.q_nickname_prompt
    QuizText.NICKNAME_EXPLANATION -> R.string.q_nickname_explanation
    QuizText.NICKNAME_CLUB_PROMPT -> R.string.q_nickname_club_prompt
    QuizText.NICKNAME_CLUB_EXPLANATION -> R.string.q_nickname_club_explanation
    QuizText.FOUNDED_PROMPT -> R.string.q_founded_prompt
    QuizText.FOUNDED_EXPLANATION -> R.string.q_founded_explanation
    QuizText.OLDEST_PROMPT -> R.string.q_oldest_prompt
    QuizText.LEAGUE_PROMPT -> R.string.q_league_prompt
    QuizText.LEAGUE_EXPLANATION -> R.string.q_league_explanation
    QuizText.CITY_PROMPT -> R.string.q_city_prompt
    QuizText.CITY_EXPLANATION -> R.string.q_city_explanation
    QuizText.MANAGER_PROMPT -> R.string.q_manager_prompt
    QuizText.MANAGER_EXPLANATION -> R.string.q_manager_explanation
    QuizText.KIT_PROMPT -> R.string.q_kit_prompt
    QuizText.KIT_EXPLANATION -> R.string.q_kit_explanation
    QuizText.KIT_PLAIN -> R.string.kit_plain
    QuizText.KIT_SLEEVES -> R.string.kit_sleeves
    QuizText.KIT_STRIPES -> R.string.kit_stripes
    QuizText.KIT_HOOPS -> R.string.kit_hoops
    QuizText.KIT_HALVES -> R.string.kit_halves
    QuizText.KIT_QUARTERS -> R.string.kit_quarters
}

private fun KitColour.resId(): Int = when (this) {
    KitColour.RED -> R.string.colour_red
    KitColour.WHITE -> R.string.colour_white
    KitColour.BLACK -> R.string.colour_black
    KitColour.NAVY -> R.string.colour_navy
    KitColour.BLUE -> R.string.colour_blue
    KitColour.SKY -> R.string.colour_sky
    KitColour.CLARET -> R.string.colour_claret
    KitColour.AMBER -> R.string.colour_amber
    KitColour.GOLD -> R.string.colour_gold
    KitColour.YELLOW -> R.string.colour_yellow
    KitColour.ORANGE -> R.string.colour_orange
    KitColour.GREEN -> R.string.colour_green
    KitColour.LIME -> R.string.colour_lime
}

private fun KitColour.pluralResId(): Int = when (this) {
    KitColour.RED -> R.string.colour_red_pl
    KitColour.WHITE -> R.string.colour_white_pl
    KitColour.BLACK -> R.string.colour_black_pl
    KitColour.NAVY -> R.string.colour_navy_pl
    KitColour.BLUE -> R.string.colour_blue_pl
    KitColour.SKY -> R.string.colour_sky_pl
    KitColour.CLARET -> R.string.colour_claret_pl
    KitColour.AMBER -> R.string.colour_amber_pl
    KitColour.GOLD -> R.string.colour_gold_pl
    KitColour.YELLOW -> R.string.colour_yellow_pl
    KitColour.ORANGE -> R.string.colour_orange_pl
    KitColour.GREEN -> R.string.colour_green_pl
    KitColour.LIME -> R.string.colour_lime_pl
}
