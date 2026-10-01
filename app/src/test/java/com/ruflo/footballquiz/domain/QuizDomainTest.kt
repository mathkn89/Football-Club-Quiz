package com.ruflo.footballquiz.domain

import com.ruflo.footballquiz.data.local.entity.ClubEntity
import com.ruflo.footballquiz.domain.generator.FoundedYearQuestionGenerator
import com.ruflo.footballquiz.domain.generator.NicknameQuestionGenerator
import com.ruflo.footballquiz.domain.model.Leagues
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class QuizDomainTest {

    private fun club(id: String, nickname: String = "Nick $id", founded: Int = 1900) = ClubEntity(
        id = id, name = "Club $id", shortName = id, nickname = nickname, stadiumName = "Ground $id",
        stadiumCapacity = 1000, foundedYear = founded, city = "City", badgeDrawableName = null,
        badgeRemoteUrl = null, version = 1, manager = "Manager", league = "League One",
    )

    @Test
    fun `nickname question is skipped when target nickname is blank`() {
        val pool = listOf(club("a"), club("b"), club("c"))
        assertNull(NicknameQuestionGenerator().generate(club("x", nickname = ""), pool))
    }

    @Test
    fun `blank distractors are never offered as options`() {
        val pool = listOf(club("a"), club("b"), club("c"), club("d", nickname = ""), club("e", nickname = " "))
        repeat(20) {
            val question = NicknameQuestionGenerator().generate(club("x"), pool)
            assertNotNull(question)
            val options = question!!.options
            assertFalse(options.any { it.isBlank() })
            assertEquals(4, options.size)
        }
    }

    @Test
    fun `founded year question is skipped for unknown year and ignores zero distractors`() {
        val pool = listOf(club("a", founded = 1880), club("b", founded = 1890), club("c", founded = 0))
        assertNull(FoundedYearQuestionGenerator().generate(club("x", founded = 0), pool + club("d", founded = 1870)))
        assertNull(FoundedYearQuestionGenerator().generate(club("x", founded = 1900), pool))
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
