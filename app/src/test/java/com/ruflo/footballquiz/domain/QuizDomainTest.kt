package com.ruflo.footballquiz.domain

import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.domain.generator.CapacityQuestionGenerator
import com.ruflo.footballquiz.domain.generator.CityQuestionGenerator
import com.ruflo.footballquiz.domain.generator.FoundedYearQuestionGenerator
import com.ruflo.footballquiz.domain.generator.KitQuestionGenerator
import com.ruflo.footballquiz.domain.generator.ManagerQuestionGenerator
import com.ruflo.footballquiz.domain.generator.NicknameClubQuestionGenerator
import com.ruflo.footballquiz.domain.generator.NicknameQuestionGenerator
import com.ruflo.footballquiz.domain.generator.OldestClubQuestionGenerator
import com.ruflo.footballquiz.domain.generator.StadiumClubQuestionGenerator
import com.ruflo.footballquiz.domain.generator.StadiumQuestionGenerator
import com.ruflo.footballquiz.domain.model.Difficulty
import com.ruflo.footballquiz.domain.model.Kit
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
        kit: Array<String?>? = arrayOf("PLAIN", "RED", null, "WHITE"),
    ) = ClubEntity(
        id = id, name = "$shortName F.C.", shortName = shortName, nickname = nickname, stadiumName = stadium,
        stadiumCapacity = capacity, foundedYear = founded, city = city, badgeDrawableName = null,
        badgeRemoteUrl = null, version = 1, manager = manager, league = "League One",
        kitPattern = kit?.get(0), kitPrimary = kit?.get(1), kitSecondary = kit?.get(2), kitShorts = kit?.get(3),
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
    fun `difficulty sets how close founded-year options are`() {
        repeat(20) {
            val hard = FoundedYearQuestionGenerator().generate(club("x", founded = 1886), pool, Difficulty.HARD)!!
            assertTrue(hard.options.all { abs(it.toInt() - 1886) <= 4 })
            val easy = FoundedYearQuestionGenerator().generate(club("x", founded = 1886), pool, Difficulty.EASY)!!
            assertTrue(easy.options.filter { it != "1886" }.all { abs(it.toInt() - 1886) in 10..40 })
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
    fun `kit question never offers another club sharing a shirt colour`() {
        val arsenal = club("ars", kit = arrayOf("SLEEVES", "RED", "WHITE", "WHITE"))
        val others = listOf(
            club("liv", kit = arrayOf("PLAIN", "RED", null, "RED")),
            club("spu", kit = arrayOf("PLAIN", "WHITE", null, "NAVY")),
            club("sou", kit = arrayOf("STRIPES", "RED", "WHITE", "BLACK")),
            club("che", kit = arrayOf("PLAIN", "BLUE", null, "BLUE")),
            club("mil", kit = arrayOf("PLAIN", "NAVY", null, "WHITE")),
            club("nor", kit = arrayOf("PLAIN", "YELLOW", null, "GREEN")),
            club("mci", kit = arrayOf("PLAIN", "SKY", null, "WHITE")),
            club("avl", kit = arrayOf("SLEEVES", "CLARET", "SKY", "SKY")),
        )
        repeat(30) {
            val question = KitQuestionGenerator().generate(arsenal, others)!!
            assertEquals("Club ARS", question.options[question.correctOptionIndex])
            val wrong = question.options - "Club ARS"
            assertFalse(wrong.any { it in setOf("Club LIV", "Club SPU", "Club SOU") })
            // Navy counts as blue, so Chelsea and Millwall never both appear.
            assertFalse("Club CHE" in wrong && "Club MIL" in wrong)
        }
        assertNull(KitQuestionGenerator().generate(club("x", kit = null), others))
    }

    @Test
    fun `kit describes itself in words`() {
        val kit = Kit.from("sleeves", "red", "white", "white")!!
        assertEquals("Red with white sleeves", kit.shirtDescription)
        assertEquals("Blue and white hoops", Kit.from("hoops", "blue", "white", "white")!!.shirtDescription)
        assertNull(Kit.from("stripes", "red", null, "black"))
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
