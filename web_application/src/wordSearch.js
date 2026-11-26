class TrieNode {
  constructor() {
    this.children = new Map();
    this.word = null;
  }
}

function buildTrie(words) {
  const root = new TrieNode();
  for (const word of words) {
    let node = root;
    for (const char of word) {
      if (!node.children.has(char)) node.children.set(char, new TrieNode());
      node = node.children.get(char);
    }
    node.word = word;
  }
  return root;
}

/**
 * Finds each dictionary word once and records its first valid path.
 * Coordinates returned by this function are zero-based.
 */
export function findWords(grid, dictionary) {
  const rows = grid.length;
  const cols = grid[0]?.length ?? 0;
  if (!rows || !cols || !dictionary.length) return [];

  const trie = buildTrie(dictionary);
  const visited = Array.from({ length: rows }, () => Array(cols).fill(false));
  const found = new Map();
  const path = [];
  const directions = [[-1, 0], [1, 0], [0, -1], [0, 1]];

  function dfs(row, col, parent) {
    const char = grid[row][col];
    const node = parent.children.get(char);
    if (!node) return;

    visited[row][col] = true;
    path.push([row, col]);

    if (node.word && !found.has(node.word)) {
      found.set(node.word, path.map(([r, c]) => [r, c]));
    }

    // Explore only if this prefix still has possible dictionary continuations.
    if (node.children.size > 0) {
      for (const [dr, dc] of directions) {
        const nr = row + dr;
        const nc = col + dc;
        if (
          nr >= 0 && nr < rows &&
          nc >= 0 && nc < cols &&
          !visited[nr][nc] &&
          node.children.has(grid[nr][nc])
        ) {
          dfs(nr, nc, node);
        }
      }
    }

    path.pop();
    visited[row][col] = false;
  }

  for (let r = 0; r < rows; r++) {
    for (let c = 0; c < cols; c++) {
      if (trie.children.has(grid[r][c])) dfs(r, c, trie);
    }
  }

  return [...found.entries()]
    .map(([word, coordinates]) => ({ word, coordinates }))
    .sort((a, b) => a.word.localeCompare(b.word));
}

export function parseDictionary(text) {
  return [...new Set(
    text.toLowerCase()
      .split(/[\s,;]+/)
      .map(word => word.trim())
      .filter(word => /^[a-z]{1,10}$/.test(word))
  )].slice(0, 10000);
}
