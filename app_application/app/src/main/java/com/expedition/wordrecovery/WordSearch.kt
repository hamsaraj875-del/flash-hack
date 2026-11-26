package com.expedition.wordrecovery

data class GridPoint(val row: Int, val col: Int)

data class WordMatch(
    val word: String,
    val coordinates: List<GridPoint>
)

private class TrieNode {
    val children = HashMap<Char, TrieNode>()
    var word: String? = null
}

private fun buildTrie(words: List<String>): TrieNode {
    val root = TrieNode()
    for (word in words) {
        var node = root
        for (ch in word) {
            node = node.children.getOrPut(ch) { TrieNode() }
        }
        node.word = word
    }
    return root
}

/**
 * Finds each dictionary word once and records its first valid path.
 * Coordinates returned by this function are zero-based.
 */
fun findWords(grid: List<List<String>>, dictionary: List<String>): List<WordMatch> {
    val rows = grid.size
    val cols = grid.firstOrNull()?.size ?: 0
    if (rows == 0 || cols == 0 || dictionary.isEmpty()) return emptyList()

    val trie = buildTrie(dictionary)
    val visited = Array(rows) { BooleanArray(cols) }
    val found = LinkedHashMap<String, List<GridPoint>>()
    val path = ArrayList<GridPoint>()
    val directions = arrayOf(
        intArrayOf(-1, 0),
        intArrayOf(1, 0),
        intArrayOf(0, -1),
        intArrayOf(0, 1)
    )

    fun dfs(row: Int, col: Int, parent: TrieNode) {
        val cell = grid[row][col]
        if (cell.length != 1) return
        val ch = cell[0]
        val node = parent.children[ch] ?: return

        visited[row][col] = true
        path.add(GridPoint(row, col))

        val terminalWord = node.word
        if (terminalWord != null && !found.containsKey(terminalWord)) {
            found[terminalWord] = path.toList()
        }

        // Explore only if this prefix still has possible dictionary continuations.
        if (node.children.isNotEmpty()) {
            for (dir in directions) {
                val nr = row + dir[0]
                val nc = col + dir[1]
                if (nr in 0 until rows && nc in 0 until cols && !visited[nr][nc]) {
                    val nextCell = grid[nr][nc]
                    if (nextCell.length == 1 && node.children.containsKey(nextCell[0])) {
                        dfs(nr, nc, node)
                    }
                }
            }
        }

        path.removeAt(path.lastIndex)
        visited[row][col] = false
    }

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val cell = grid[r][c]
            if (cell.length == 1 && trie.children.containsKey(cell[0])) {
                dfs(r, c, trie)
            }
        }
    }

    return found.entries
        .map { (word, coords) -> WordMatch(word, coords) }
        .sortedBy { it.word }
}

private val VALID_WORD_REGEX = Regex("^[a-z]{1,10}$")
private val SPLIT_REGEX = Regex("[\\s,;]+")

fun parseDictionary(text: String): List<String> {
    return text.lowercase()
        .split(SPLIT_REGEX)
        .asSequence()
        .map { it.trim() }
        .filter { VALID_WORD_REGEX.matches(it) }
        .distinct()
        .take(10_000)
        .toList()
}
