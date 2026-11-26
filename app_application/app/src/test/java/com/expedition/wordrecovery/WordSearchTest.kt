package com.expedition.wordrecovery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WordSearchTest {

    @Test
    fun parseDictionary_normalizesDeduplicatesAndFilters() {
        val parsed = parseDictionary("Oath, pea; EAT\noath\n123\ntoolongwordhere\nrain")
        assertEquals(listOf("oath", "pea", "eat", "rain"), parsed)
    }

    @Test
    fun findWords_basicSample_findsEatAndOath() {
        val grid = listOf(
            listOf("o", "a", "a", "n"),
            listOf("e", "t", "a", "e"),
            listOf("i", "h", "k", "r"),
            listOf("i", "f", "l", "v")
        )
        val dict = parseDictionary("oath\npea\neat\nrain")
        val results = findWords(grid, dict)
        assertEquals(listOf("eat", "oath"), results.map { it.word })
        assertTrue(results.all { it.coordinates.size == it.word.length })
    }

    @Test
    fun findWords_sharedPrefixes_findsOverlappingWords() {
        val grid = listOf(
            listOf("a", "p", "p", "l", "e"),
            listOf("x", "p", "l", "y", "z"),
            listOf("o", "a", "t", "h", "q"),
            listOf("e", "a", "t", "r", "n"),
            listOf("h", "e", "a", "r", "s")
        )
        val dict = parseDictionary("apple\napply\napt\nape\napp\napplication\noath\neat\ntea\nrain\nnear\nhear\nhare")
        val foundWords = findWords(grid, dict).map { it.word }
        assertTrue(foundWords.containsAll(listOf("app", "apple", "apply", "eat", "hear", "oath")))
    }
}
