# Expedition — Word Recovery System

A React + Vite implementation of the Lost Words problem statement.

## Requirements
- Node.js 18+ and npm

## Run locally
```bash
npm install
npm run dev
```
Open the local URL printed by Vite.

## Production build
```bash
npm run build
npm run preview
```

## How it works
1. Dictionary entries are normalized, validated (lowercase a-z, 1–10 letters), deduplicated, and limited to 10,000 words.
2. A Trie stores the dictionary. Words sharing prefixes share Trie nodes.
3. DFS starts from each grid cell and follows only up, down, left, and right neighbors.
4. A visited matrix prevents reusing a cell within one word.
5. When a path reaches a terminal Trie node, its word and coordinate path are saved once.
6. Backtracking removes the last path cell and marks it unvisited, so other paths and overlapping words can use it.

Coordinates shown in the interface are 1-based. The search function internally uses zero-based coordinates.

## Input
- Edit grid cells directly.
- Change row and column counts (1–10).
- Enter dictionary words separated by newlines, spaces, commas, or semicolons.
- Upload a plain `.txt` file with one word per line.

## Notes
- The app runs entirely in the browser; no backend or API key is required.
- Search time is measured with `performance.now()` and depends on the input.
- The implementation returns one valid path per discovered word.
