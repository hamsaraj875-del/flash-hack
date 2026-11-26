import React, { useMemo, useRef, useState } from "react";
import {
  Activity, BookOpen, Check, ChevronRight, Clock3, FileUp,
  Grid3X3, RotateCcw, Search, Sparkles, Upload, X
} from "lucide-react";
import { findWords, parseDictionary } from "./wordSearch";

const sampleGrid = [
  ["o", "a", "a", "n"],
  ["e", "t", "a", "e"],
  ["i", "h", "k", "r"],
  ["i", "f", "l", "v"]
];
const sampleWords = "oath\npea\neat\nrain";

const sharedPrefixWords = [
  "apple", "apply", "apt", "ape", "app", "application",
  "oath", "eat", "tea", "rain", "near", "hear", "hare"
];

function makeGrid(rows, cols, current) {
  return Array.from({ length: rows }, (_, r) =>
    Array.from({ length: cols }, (_, c) => current[r]?.[c] ?? "")
  );
}

export default function App() {
  const [grid, setGrid] = useState(sampleGrid);
  const [dictionaryText, setDictionaryText] = useState(sampleWords);
  const [results, setResults] = useState([]);
  const [selectedWord, setSelectedWord] = useState("");
  const [hasSearched, setHasSearched] = useState(false);
  const [searchTime, setSearchTime] = useState(0);
  const [error, setError] = useState("");
  const fileRef = useRef(null);

  const dictionary = useMemo(() => parseDictionary(dictionaryText), [dictionaryText]);
  const selected = results.find(item => item.word === selectedWord) ?? results[0];
  const activeCells = useMemo(
    () => new Set((selected?.coordinates ?? []).map(([r, c]) => `${r}-${c}`)),
    [selected]
  );
  const pathIndex = useMemo(() => {
    const map = new Map();
    (selected?.coordinates ?? []).forEach(([r, c], index) => map.set(`${r}-${c}`, index + 1));
    return map;
  }, [selected]);

  function updateCell(row, col, value) {
    setGrid(old => old.map((line, r) =>
      line.map((cell, c) => r === row && c === col ? value.toLowerCase().replace(/[^a-z]/g, "").slice(-1) : cell)
    ));
    setHasSearched(false);
  }

  function resizeGrid(rows, cols) {
    setGrid(old => makeGrid(rows, cols, old));
    setResults([]);
    setSelectedWord("");
    setHasSearched(false);
  }

  function runSearch() {
    setError("");
    const rows = grid.length;
    const cols = grid[0]?.length ?? 0;
    if (!rows || !cols || grid.some(row => row.length !== cols)) {
      setError("Grid must be rectangular.");
      return;
    }
    if (rows > 10 || cols > 10) {
      setError("Grid dimensions cannot exceed 10 × 10.");
      return;
    }
    if (grid.some(row => row.some(cell => !/^[a-z]$/.test(cell)))) {
      setError("Fill every grid cell with one lowercase letter.");
      return;
    }
    if (!dictionary.length) {
      setError("Enter at least one valid dictionary word (1–10 lowercase letters).");
      return;
    }
    const start = performance.now();
    const found = findWords(grid, dictionary);
    setSearchTime(performance.now() - start);
    setResults(found);
    setSelectedWord(found[0]?.word ?? "");
    setHasSearched(true);
  }

  function loadSample() {
    setGrid(sampleGrid.map(row => [...row]));
    setDictionaryText(sampleWords);
    setResults([]);
    setSelectedWord("");
    setHasSearched(false);
    setError("");
  }

  function loadPrefixDemo() {
    setGrid([
      ["a", "p", "p", "l", "e"],
      ["x", "p", "l", "y", "z"],
      ["o", "a", "t", "h", "q"],
      ["e", "a", "t", "r", "n"],
      ["h", "e", "a", "r", "s"]
    ]);
    setDictionaryText(sharedPrefixWords.join("\n"));
    setResults([]);
    setSelectedWord("");
    setHasSearched(false);
    setError("");
  }

  function handleFile(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    if (file.size > 2 * 1024 * 1024) {
      setError("Dictionary file must be smaller than 2 MB.");
      event.target.value = "";
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      setDictionaryText(String(reader.result ?? ""));
      setError("");
      setHasSearched(false);
    };
    reader.onerror = () => setError("Could not read that file.");
    reader.readAsText(file);
    event.target.value = "";
  }

  const pathText = selected?.coordinates
    .map(([r, c]) => `(${r + 1}, ${c + 1})`)
    .join(" → ");

  return (
    <main className="app-shell">
      <header className="topbar">
        <a className="brand" href="#" aria-label="Expedition home">
          <span className="brand-mark"><Grid3X3 size={19} /></span>
          <span>FIELD<span className="brand-accent">/</span>NOTES</span>
        </a>
        <div className="topbar-right">
          <span className="live-dot" /> EXPEDITION TOOLKIT
          <span className="version">BUILD 01.0</span>
        </div>
      </header>

      <section className="hero">
        <div className="eyebrow"><span /> RESEARCH DIVISION <span className="eyebrow-line" /></div>
        <h1>The Expedition's<br /><em>Lost Words.</em></h1>
        <p className="hero-copy">Recover hidden language from damaged transmissions.<br className="desktop-break" /> Trace each discovery, one letter at a time.</p>
        <div className="hero-meta">
          <span><Activity size={14} /> PREFIX SEARCH ACTIVE</span>
          <span><Grid3X3 size={14} /> MAX GRID 10 × 10</span>
          <span><BookOpen size={14} /> UP TO 10,000 WORDS</span>
        </div>
      </section>

      <section className="workspace">
        <div className="section-heading">
          <div><span className="section-number">01</span><h2>Configure your search</h2></div>
          <button className="text-button" onClick={loadSample}><RotateCcw size={14} /> RESET EXAMPLE</button>
        </div>

        <div className="config-grid">
          <section className="panel grid-panel">
            <div className="panel-head">
              <div><span className="panel-kicker">TRANSMISSION DATA</span><h3>Letter grid</h3></div>
              <span className="dimension-tag">{grid.length} × {grid[0].length}</span>
            </div>
            <div className="grid-controls">
              <label>ROWS <select value={grid.length} onChange={e => resizeGrid(Number(e.target.value), grid[0].length)}>{Array.from({length:10},(_,i)=><option key={i+1} value={i+1}>{i+1}</option>)}</select></label>
              <label>COLUMNS <select value={grid[0].length} onChange={e => resizeGrid(grid.length, Number(e.target.value))}>{Array.from({length:10},(_,i)=><option key={i+1} value={i+1}>{i+1}</option>)}</select></label>
              <span className="edit-hint">SELECT A CELL TO EDIT</span>
            </div>
            <div className="letter-grid" style={{ "--cols": grid[0].length }}>
              {grid.flatMap((row, r) => row.map((letter, c) => {
                const key = `${r}-${c}`;
                const isActive = activeCells.has(key) && hasSearched;
                return (
                  <div className={`letter-cell ${isActive ? "path-cell" : ""}`} key={key}>
                    <input
                      aria-label={`Row ${r+1}, column ${c+1}`}
                      maxLength={1}
                      value={letter}
                      onChange={e => updateCell(r, c, e.target.value)}
                    />
                    {isActive && <span className="path-badge">{pathIndex.get(key)}</span>}
                  </div>
                );
              }))}
            </div>
            <div className="grid-legend"><span className="legend-square" /> SELECTED WORD PATH <span className="legend-note">ROW / COLUMN INDEX STARTS AT 1</span></div>
          </section>

          <section className="panel dictionary-panel">
            <div className="panel-head">
              <div><span className="panel-kicker">WORD DATABASE</span><h3>Candidate dictionary</h3></div>
              <span className="count-tag">{dictionary.length.toLocaleString()} WORDS</span>
            </div>
            <p className="panel-description">Enter candidate words, one per line or separated by commas. Duplicate entries are removed automatically.</p>
            <textarea
              className="dictionary-input"
              value={dictionaryText}
              onChange={e => { setDictionaryText(e.target.value); setHasSearched(false); }}
              spellCheck="false"
              placeholder={"oath\npea\neat\nrain"}
              aria-label="Dictionary words"
            />
            <div className="dictionary-actions">
              <button className="upload-button" onClick={() => fileRef.current?.click()}><Upload size={15} /> UPLOAD .TXT</button>
              <input ref={fileRef} type="file" accept=".txt,text/plain" hidden onChange={handleFile} />
              <span>MAX 10,000 WORDS · 10 LETTERS EACH</span>
            </div>
            <div className="demo-row">
              <span>LOAD TEST CASE</span>
              <button onClick={loadSample}>BASIC</button>
              <button onClick={loadPrefixDemo}>SHARED PREFIXES</button>
            </div>
          </section>
        </div>

        {error && <div className="error-banner"><X size={16} /> {error}</div>}
        <div className="search-action-row">
          <div className="algorithm-note"><Sparkles size={15} /><span>TRIE + DEPTH-FIRST SEARCH <b>·</b> BACKTRACKING</span></div>
          <button className="search-button" onClick={runSearch}><Search size={17} /> RUN WORD RECOVERY <ChevronRight size={16} /></button>
        </div>

        <section className="results-section">
          <div className="section-heading results-heading">
            <div><span className="section-number">02</span><h2>Recovery report</h2></div>
            {hasSearched && <span className="report-status"><Check size={13} /> SEARCH COMPLETE</span>}
          </div>

          {!hasSearched ? (
            <div className="empty-state">
              <div className="empty-icon"><Search size={23} /></div>
              <h3>Awaiting transmission</h3>
              <p>Configure the letter grid and dictionary, then run recovery to reveal matching words and their paths.</p>
            </div>
          ) : (
            <>
              <div className="stats-row">
                <div className="stat-card"><span>WORDS RECOVERED</span><strong>{results.length}</strong></div>
                <div className="stat-card"><span>DICTIONARY SIZE</span><strong>{dictionary.length.toLocaleString()}</strong></div>
                <div className="stat-card"><span>SEARCH TIME</span><strong>{searchTime.toFixed(2)}<small> ms</small></strong></div>
                <div className="stat-card"><span>NOT FOUND</span><strong>{dictionary.length - results.length}</strong></div>
              </div>

              {results.length === 0 ? (
                <div className="no-results"><Search size={20} /><div><strong>No words recovered</strong><p>No dictionary words could be traced in this grid. Try another grid or dictionary.</p></div></div>
              ) : (
                <div className="result-layout">
                  <div className="results-list panel">
                    <div className="list-head"><span>DISCOVERED WORDS</span><span>{results.length} MATCHES</span></div>
                    {results.map((item, index) => (
                      <button key={item.word} className={`result-item ${selected?.word === item.word ? "selected" : ""}`} onClick={() => setSelectedWord(item.word)}>
                        <span className="result-index">{String(index + 1).padStart(2, "0")}</span>
                        <span className="result-word">{item.word}</span>
                        <span className="result-length">{item.word.length} LETTERS</span>
                        <ChevronRight size={15} />
                      </button>
                    ))}
                  </div>
                  <div className="path-detail panel">
                    <div className="list-head"><span>RECOVERED PATH</span><span>SELECTED WORD</span></div>
                    <div className="path-word">{selected?.word}</div>
                    <p className="path-caption">FOLLOW THE NUMBERED CELLS IN ORDER</p>
                    <div className="coordinate-list">
                      {selected?.coordinates.map(([r,c], index) => (
                        <React.Fragment key={`${r}-${c}`}>
                          <div className="coordinate"><span>{String(index + 1).padStart(2, "0")}</span><strong>({r+1}, {c+1})</strong><small>LETTER: {grid[r][c].toUpperCase()}</small></div>
                          {index < selected.coordinates.length - 1 && <div className="coordinate-arrow"><ChevronRight size={13} /></div>}
                        </React.Fragment>
                      ))}
                    </div>
                    <div className="path-summary"><span>FULL COORDINATE PATH</span><p>{pathText}</p></div>
                  </div>
                </div>
              )}
            </>
          )}
        </section>
      </section>

      <section className="method-section">
        <div className="section-heading"><div><span className="section-number">03</span><h2>Search methodology</h2></div></div>
        <div className="method-grid">
          <article><span>01 / INDEX</span><h3>Trie construction</h3><p>Dictionary words are stored in a prefix tree. Shared prefixes use the same nodes instead of being searched repeatedly.</p></article>
          <article><span>02 / EXPLORE</span><h3>Depth-first search</h3><p>Search starts at each cell and explores only horizontal and vertical neighbors that continue a valid dictionary prefix.</p></article>
          <article><span>03 / RESTORE</span><h3>Backtracking</h3><p>Visited cells are marked during a path and unmarked when recursion returns, preventing cell reuse while allowing overlapping words.</p></article>
        </div>
      </section>

      <footer><span>FIELD/NOTES — WORD RECOVERY SYSTEM</span><span><Clock3 size={13} /> RESEARCH LOG · 2026</span></footer>
    </main>
  );
}
