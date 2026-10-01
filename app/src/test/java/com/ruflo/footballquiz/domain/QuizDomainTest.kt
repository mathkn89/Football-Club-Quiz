package com.ruflo.footballquiz.domain

import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.domain.generator.BadgeQuestionGenerator
import com.ruflo.footballquiz.domain.generator.CapacityQuestionGenerator
import com.ruflo.footballquiz.domain.generator.CityQuestionGenerator
import com.ruflo.footballquiz.domain.generator.FoundedYearQuestionGenerator
import com.ruflo.footballquiz.domain.generator.ManagerQuestionGenerator
import com.ruflo.footballquiz.domain.generator.NicknameClubQuestionGenerator
import com.ruflo.footballquiz.domain.generator.NicknameQuestionGenerator
import com.ruflo.footballquiz.domain.generator.OldestClubQuestionGenerator
import com.ruflo.footballquiz.domain.generator.StadiumClubQuestionGenerator
import com.ruflo.footballquiz.domain.generator.StadiumQuestionGenerator
import com.ruflo.footballquiz.domain.model.Leagues
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class QuizDomainTest {

    private fun club(
        id: String,
        shortName: String = "Club ${id.uppercase()}",
        nickname: String = "Nick $id",
        stadium: String = "Ground $id",
        capacity: Int = 1000,
        city: String = "Town $id",
        founded: Int = 1900,
        manager: String = "Manager $id",
        badgeQuizUrl: String? = "https://example.com/$id.png",
    ) = ClubEntity(
        id = id, name = "$shortName F.C.", shortName = shortName, nickname = nickname, stadiumName = stadium,
        stadiumCapacity = capacity, foundedYear = founded, city = city, badgeDrawableName = null,
        badgeRemoteUrl = null, version = 1, manager = manager, league = "League One", badgeQuizUrl = badgeQuizUrl,
    )

    private val pool = listOf(club("a"), club("b"), club("c"), club("d"))

    @Test
    fun `nickname question is skipped when target nickname is blank`() {
        assertNull(NicknameQuestionGenerator().generate(club("x", nickname = ""), pool))
    }

    @Test
    fun `blank distractors are never offered as options`() {
        val withBlanks = pool + club("e", nickname = "") + club("f", nickname = " ")
        repeat(20) {
            val question = NicknameQuestionGenerator().generate(club("x"), withBlanks)
            assertNotNull(question)
            assertFalse(question!!.options.any { it.isBlank() })
            assertEquals(4, question.options.size)
        }
    }

    @Test
    fun `founded year options are four distinct years close to the real one`() {
        assertNull(FoundedYearQuestionGenerator().generate(club("x", founded = 0), pool))
        repeat(20) {
            val question = FoundedYearQuestionGenerator().generate(club("x", founded = 1886), pool)!!
            assertEquals(4, question.options.toSet().size)
            assertTrue(question.options.all { abs(it.toInt() - 1886) <= 12 })
            assertEquals("1886", question.options[question.correctOptionIndex])
        }
    }

    @Test
    fun `questions whose answer is in the club name are skipped`() {
        assertNull(StadiumQuestionGenerator().generate(club("x", shortName = "Brentford", stadium = "Brentford Community Stadium"), pool))
        assertNull(CityQuestionGenerator().generate(club("x", shortName = "Wigan Athletic", city = "Wigan"), pool))
        assertNotNull(CityQuestionGenerator().generate(club("x", shortName = "Tranmere Rovers", city = "Birkenhead"), pool))
        // Generic words like "City" / "Stadium" alone don't count as a giveaway.
        assertNotNull(StadiumQuestionGenerator().generate(club("x", shortName = "Cardiff City", stadium = "City Stadium"), pool))
    }

    @Test
    fun `reverse nickname question is skipped when another club shares the nickname`() {
        val shared = pool + club("y", nickname = "The Robins")
        assertNull(NicknameClubQuestionGenerator().generate(club("x", nickname = "Robins"), shared))
        assertNotNull(NicknameClubQuestionGenerator().generate(club("x", nickname = "The Tykes"), shared))
    }

    @Test
    fun `comparison questions put the oldest or biggest club as the answer`() {
        val younger = listOf(club("a", founded = 1900), club("b", founded = 1910), club("c", founded = 1920))
        val oldest = OldestClubQuestionGenerator().generate(club("x", founded = 1880), younger)!!
        assertEquals("Club X", oldest.options[oldest.correctOptionIndex])
        assertNull(OldestClubQuestionGenerator().generate(club("x", founded = 1899), younger))

        val smaller = listOf(club("a", capacity = 5000), club("b", capacity = 8000), club("c", capacity = 9000))
        val biggest = CapacityQuestionGenerator().generate(club("x", capacity = 30000), smaller)!!
        assertEquals("Ground x", biggest.options[biggest.correctOptionIndex])
        assertNull(CapacityQuestionGenerator().generate(club("x", capacity = 10000), smaller))
    }

    @Test
    fun `grounds differing only by punctuation count as the same name`() {
        val withTwin = pool + club("y", stadium = "St James Park")
        assertNull(StadiumClubQuestionGenerator().generate(club("x", stadium = "St James' Park"), withTwin))
        repeat(20) {
            val question = StadiumQuestionGenerator().generate(club("x", stadium = "St James' Park"), withTwin)!!
            assertFalse("St James Park" in question.options)
        }
    }

    @Test
    fun `manager question skips caretakers and vacancies`() {
        assertNull(ManagerQuestionGenerator().generate(club("x", manager = "Ryan Harley (caretaker)"), pool))
        assertNull(ManagerQuestionGenerator().generate(club("x", manager = "Vacant"), pool))
        assertNotNull(ManagerQuestionGenerator().generate(club("x", manager = "Paul Warne"), pool))
    }

    @Test
    fun `badge question only uses the redacted badge`() {
        assertNull(BadgeQuestionGenerator().generate(club("x", badgeQuizUrl = null), pool))
        val question = BadgeQuestionGenerator().generate(club("x"), pool)!!
        assertEquals("https://example.com/x.png", question.imageUrl)
        assertEquals("Club X", question.options[question.correctOptionIndex])
    }

    @Test
    fun `leagues sort in pyramid order with unknown leagues last`() {
        val sorted = listOf("National League", "Zeta League", "Premier League", "League Two", "Championship", "League One")
            .sortedWith(Leagues.pyramidOrder)
        assertEquals(
            listOf("Premier League", "Championship", "League One", "League Two", "National League", "Zeta League"),
            sorted,
        )
    }
}
