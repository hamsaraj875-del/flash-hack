package com.expedition.wordrecovery

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val BgDeep = Color(0xFF0B1015)
private val BgTopGlow = Color(0xFF17212A)
private val PanelBg = Color(0xE011181E)
private val PanelBorder = Color(0xFF2A343C)
private val DividerColor = Color(0xFF28313A)
private val CellBg = Color(0xFF1A242B)
private val CellBorder = Color(0xFF34414A)
private val PathCellBg = Color(0xFF64492F)
private val PathCellBorder = Color(0xFFE0AA70)
private val PathBadgeBg = Color(0xFFF0C18E)
private val PathBadgeText = Color(0xFF271B11)
private val AccentAmber = Color(0xFFD4A06A)
private val AccentAmberLight = Color(0xFFD7A16B)
private val TextPrimary = Color(0xFFE8E8DF)
private val TextSecondary = Color(0xFF9BA5A8)
private val TextMuted = Color(0xFF78868B)
private val InputBg = Color(0xFF0E151A)
private val StatBg = Color(0xFF141D23)
private val ErrorBg = Color(0xFF321F1E)
private val ErrorBorder = Color(0xFF7C4540)
private val ErrorText = Color(0xFFF0AAA0)
private val StatusGreen = Color(0xFF9BC49E)

private val SAMPLE_GRID = listOf(
    listOf("o", "a", "a", "n"),
    listOf("e", "t", "a", "e"),
    listOf("i", "h", "k", "r"),
    listOf("i", "f", "l", "v")
)
private const val SAMPLE_WORDS = "oath\npea\neat\nrain"

private val PREFIX_GRID = listOf(
    listOf("a", "p", "p", "l", "e"),
    listOf("x", "p", "l", "y", "z"),
    listOf("o", "a", "t", "h", "q"),
    listOf("e", "a", "t", "r", "n"),
    listOf("h", "e", "a", "r", "s")
)
private val SHARED_PREFIX_WORDS = listOf(
    "apple", "apply", "apt", "ape", "app", "application",
    "oath", "eat", "tea", "rain", "near", "hear", "hare"
)

private fun makeGrid(rows: Int, cols: Int, current: List<List<String>>): List<List<String>> {
    return List(rows) { r ->
        List(cols) { c -> current.getOrNull(r)?.getOrNull(c) ?: "" }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = BgDeep,
                    surface = PanelBg,
                    primary = AccentAmber,
                    onPrimary = Color(0xFF1A1713),
                    onBackground = TextPrimary,
                    onSurface = TextPrimary
                )
            ) {
                ExpeditionApp()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpeditionApp() {
    val context = LocalContext.current

    var grid by remember { mutableStateOf(SAMPLE_GRID) }
    var dictionaryText by remember { mutableStateOf(SAMPLE_WORDS) }
    var results by remember { mutableStateOf<List<WordMatch>>(emptyList()) }
    var selectedWord by remember { mutableStateOf("") }
    var hasSearched by remember { mutableStateOf(false) }
    var searchTimeMs by remember { mutableDoubleStateOf(0.0) }
    var errorMessage by remember { mutableStateOf("") }

    val dictionary = remember(dictionaryText) { parseDictionary(dictionaryText) }
    val selected = remember(results, selectedWord) {
        results.find { it.word == selectedWord } ?: results.firstOrNull()
    }
    val pathIndexMap = remember(selected) {
        val map = HashMap<GridPoint, Int>()
        selected?.coordinates?.forEachIndexed { index, point ->
            map[point] = index + 1
        }
        map
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            var fileSize: Long? = null
            context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (idx >= 0 && !cursor.isNull(idx)) {
                        fileSize = cursor.getLong(idx)
                    }
                }
            }
            if (fileSize != null && fileSize!! > 2L * 1024L * 1024L) {
                errorMessage = "Dictionary file must be smaller than 2 MB."
                return@rememberLauncherForActivityResult
            }
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null) {
                errorMessage = "Could not read that file."
                return@rememberLauncherForActivityResult
            }
            if (bytes.size > 2 * 1024 * 1024) {
                errorMessage = "Dictionary file must be smaller than 2 MB."
                return@rememberLauncherForActivityResult
            }
            dictionaryText = bytes.toString(Charsets.UTF_8)
            errorMessage = ""
            hasSearched = false
        } catch (_: Exception) {
            errorMessage = "Could not read that file."
        }
    }

    fun updateCell(row: Int, col: Int, rawValue: String) {
        val sanitized = rawValue.lowercase(Locale.US).replace(Regex("[^a-z]"), "").takeLast(1)
        grid = grid.mapIndexed { r, line ->
            if (r != row) line else line.mapIndexed { c, cell -> if (c == col) sanitized else cell }
        }
        hasSearched = false
    }

    fun resizeGrid(rows: Int, cols: Int) {
        grid = makeGrid(rows, cols, grid)
        results = emptyList()
        selectedWord = ""
        hasSearched = false
    }

    fun runSearch() {
        errorMessage = ""
        val rows = grid.size
        val cols = grid.firstOrNull()?.size ?: 0
        if (rows == 0 || cols == 0 || grid.any { it.size != cols }) {
            errorMessage = "Grid must be rectangular."
            return
        }
        if (rows > 10 || cols > 10) {
            errorMessage = "Grid dimensions cannot exceed 10 × 10."
            return
        }
        val letterRegex = Regex("^[a-z]$")
        if (grid.any { row -> row.any { !letterRegex.matches(it) } }) {
            errorMessage = "Fill every grid cell with one lowercase letter."
            return
        }
        if (dictionary.isEmpty()) {
            errorMessage = "Enter at least one valid dictionary word (1–10 lowercase letters)."
            return
        }
        val startNs = System.nanoTime()
        val found = findWords(grid, dictionary)
        searchTimeMs = (System.nanoTime() - startNs) / 1_000_000.0
        results = found
        selectedWord = found.firstOrNull()?.word ?: ""
        hasSearched = true
    }

    fun loadSample() {
        grid = SAMPLE_GRID
        dictionaryText = SAMPLE_WORDS
        results = emptyList()
        selectedWord = ""
        hasSearched = false
        errorMessage = ""
    }

    fun loadPrefixDemo() {
        grid = PREFIX_GRID
        dictionaryText = SHARED_PREFIX_WORDS.joinToString("\n")
        results = emptyList()
        selectedWord = ""
        hasSearched = false
        errorMessage = ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BgTopGlow, BgDeep, BgDeep)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // Topbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .border(1.dp, Color(0xFFBD8A54), RoundedCornerShape(2.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Expedition",
                            tint = AccentAmber,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("FIELD")
                            withStyle(SpanStyle(color = AccentAmber)) { append("/") }
                            append("NOTES")
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp,
                        color = TextPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AccentAmber)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = "EXPEDITION TOOLKIT",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        color = Color(0xFFAAB2B4)
                    )
                }
            }
            HorizontalDivider(color = DividerColor)

            // Hero
            Column(modifier = Modifier.padding(vertical = 28.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AccentAmber)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "RESEARCH DIVISION",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        letterSpacing = 1.5.sp,
                        color = AccentAmber
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(1.dp)
                            .background(Color(0xFF48515A))
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = buildAnnotatedString {
                        append("The Expedition's\n")
                        withStyle(
                            SpanStyle(
                                color = AccentAmberLight,
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic
                            )
                        ) {
                            append("Lost Words.")
                        }
                    },
                    fontSize = 38.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Recover hidden language from damaged transmissions. Trace each discovery, one letter at a time.",
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(18.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetaBadge(Icons.Default.ShowChart, "PREFIX SEARCH ACTIVE")
                    MetaBadge(Icons.Default.GridOn, "MAX GRID 10 × 10")
                    MetaBadge(Icons.Default.MenuBook, "UP TO 10,000 WORDS")
                }
            }

            HorizontalDivider(color = DividerColor)
            Spacer(Modifier.height(22.dp))

            // Section 01 Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "01",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = AccentAmber
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Configure your search",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                TextButton(onClick = ::loadSample) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color(0xFF8F999D),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "RESET EXAMPLE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = Color(0xFF8F999D)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Panel 1: Letter Grid
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PanelBg,
                shape = RoundedCornerShape(3.dp),
                border = BorderStroke(1.dp, PanelBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TRANSMISSION DATA",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                letterSpacing = 1.2.sp,
                                color = Color(0xFF829096)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Letter grid",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .border(1.dp, CellBorder)
                                .padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${grid.size} × ${grid.firstOrNull()?.size ?: 0}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFFC2C9C6)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        DimensionSelector(
                            label = "ROWS",
                            value = grid.size,
                            onSelect = { resizeGrid(it, grid.firstOrNull()?.size ?: 4) }
                        )
                        DimensionSelector(
                            label = "COLUMNS",
                            value = grid.firstOrNull()?.size ?: 4,
                            onSelect = { resizeGrid(grid.size, it) }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Letter Grid
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        grid.forEachIndexed { r, row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                row.forEachIndexed { c, letter ->
                                    val point = GridPoint(r, c)
                                    val stepNumber = if (hasSearched) pathIndexMap[point] else null
                                    val isActive = stepNumber != null
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .background(if (isActive) PathCellBg else CellBg)
                                            .border(
                                                1.dp,
                                                if (isActive) PathCellBorder else CellBorder
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        BasicTextField(
                                            value = letter,
                                            onValueChange = { updateCell(r, c, it) },
                                            singleLine = true,
                                            cursorBrush = SolidColor(AccentAmber),
                                            textStyle = TextStyle(
                                                color = if (isActive) Color(0xFFFFF4E6) else TextPrimary,
                                                fontSize = if (row.size >= 8) 14.sp else 18.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                textAlign = TextAlign.Center
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        if (stepNumber != null) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(2.dp)
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(PathBadgeBg),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = stepNumber.toString(),
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 9.sp,
                                                    color = PathBadgeText
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFB98450))
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = "SELECTED WORD PATH",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            color = AccentAmber
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "1-BASED INDEX",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            color = Color(0xFF68757C)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Panel 2: Candidate Dictionary
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PanelBg,
                shape = RoundedCornerShape(3.dp),
                border = BorderStroke(1.dp, PanelBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WORD DATABASE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                letterSpacing = 1.2.sp,
                                color = Color(0xFF829096)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Candidate dictionary",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .border(1.dp, CellBorder)
                                .padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${dictionary.size} WORDS",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFFC2C9C6)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Enter candidate words, one per line or separated by commas. Duplicate entries are removed automatically.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF89959A)
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = dictionaryText,
                        onValueChange = {
                            dictionaryText = it
                            hasSearched = false
                        },
                        placeholder = {
                            Text(
                                "oath\npea\neat\nrain",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color(0xFF59666C)
                            )
                        },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = Color(0xFFDFE2DB)
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputBg,
                            unfocusedContainerColor = InputBg,
                            focusedBorderColor = Color(0xFF9A754E),
                            unfocusedBorderColor = Color(0xFF303B43),
                            cursorColor = AccentAmber
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { filePicker.launch(arrayOf("text/plain", "*/*")) },
                            shape = RoundedCornerShape(2.dp),
                            border = BorderStroke(1.dp, Color(0xFF48545B)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = null,
                                tint = Color(0xFFC9D0CB),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(
                                text = "UPLOAD .TXT",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFFC9D0CB)
                            )
                        }
                        Text(
                            text = "MAX 10,000 · 10 LETTERS",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            color = Color(0xFF68767C)
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = DividerColor)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "LOAD TEST CASE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = Color(0xFF6E7B81)
                        )
                        Text(
                            text = "BASIC",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color(0xFFB7C0BD),
                            modifier = Modifier.clickable { loadSample() }
                        )
                        Text(
                            text = "SHARED PREFIXES",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color(0xFFB7C0BD),
                            modifier = Modifier.clickable { loadPrefixDemo() }
                        )
                    }
                }
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ErrorBg)
                        .border(1.dp, ErrorBorder)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = ErrorText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = errorMessage, fontSize = 12.sp, color = ErrorText)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Search Action Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AccentAmber,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = "TRIE + DEPTH-FIRST SEARCH · BACKTRACKING",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = Color(0xFF89969A)
                )
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = ::runSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentAmber,
                    contentColor = Color(0xFF1A1713)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "RUN WORD RECOVERY",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(Modifier.height(26.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(Modifier.height(22.dp))

            // Section 02: Recovery report
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "02",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = AccentAmber
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Recovery report",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                if (hasSearched) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = StatusGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "SEARCH COMPLETE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = StatusGreen
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (!hasSearched) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF303B43))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .border(1.dp, Color(0xFF35414A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = AccentAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Awaiting transmission",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Configure the letter grid and dictionary, then run recovery to reveal matching words and their paths.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF859197)
                        )
                    }
                }
            } else {
                // Stats grid (2x2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("WORDS RECOVERED", results.size.toString(), Modifier.weight(1f))
                    StatCard("DICTIONARY SIZE", dictionary.size.toString(), Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        "SEARCH TIME",
                        String.format(Locale.US, "%.2f ms", searchTimeMs),
                        Modifier.weight(1f)
                    )
                    StatCard(
                        "NOT FOUND",
                        (dictionary.size - results.size).coerceAtLeast(0).toString(),
                        Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(14.dp))

                if (results.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF303B43))
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = AccentAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "No words recovered",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "No dictionary words could be traced in this grid. Try another grid or dictionary.",
                                fontSize = 12.sp,
                                color = Color(0xFF8B969B)
                            )
                        }
                    }
                } else {
                    // Discovered words list
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = PanelBg,
                        shape = RoundedCornerShape(3.dp),
                        border = BorderStroke(1.dp, PanelBorder)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 15.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "DISCOVERED WORDS",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = Color(0xFF7E8C91)
                                )
                                Text(
                                    text = "${results.size} MATCHES",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = Color(0xFF7E8C91)
                                )
                            }
                            HorizontalDivider(color = PanelBorder)
                            results.forEachIndexed { index, item ->
                                val isSelected = selected?.word == item.word
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isSelected) Color(0xFF222D34) else Color.Transparent)
                                        .clickable { selectedWord = item.word }
                                        .padding(horizontal = 15.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "%02d", index + 1),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = AccentAmber
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = item.word,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        text = "${item.word.length} LETTERS",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = Color(0xFF758188)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = if (isSelected) AccentAmberLight else Color(0xFF78858A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                if (index < results.lastIndex) {
                                    HorizontalDivider(color = Color(0xFF263139))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Path detail panel
                    if (selected != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = PanelBg,
                            shape = RoundedCornerShape(3.dp),
                            border = BorderStroke(1.dp, PanelBorder)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 15.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "RECOVERED PATH",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = Color(0xFF7E8C91)
                                    )
                                    Text(
                                        text = "SELECTED WORD",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = Color(0xFF7E8C91)
                                    )
                                }
                                HorizontalDivider(color = PanelBorder)
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = selected.word,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 3.sp,
                                        color = Color(0xFFE3B27E)
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "FOLLOW THE NUMBERED CELLS IN ORDER",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = Color(0xFF718087)
                                    )
                                    Spacer(Modifier.height(14.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        selected.coordinates.forEachIndexed { index, point ->
                                            val letter = grid[point.row][point.col].uppercase(Locale.US)
                                            Column(
                                                modifier = Modifier
                                                    .background(Color(0xFF19232A))
                                                    .border(1.dp, Color(0xFF35424A))
                                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = String.format(Locale.US, "%02d", index + 1),
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 9.sp,
                                                    color = AccentAmber
                                                )
                                                Text(
                                                    text = "(${point.row + 1}, ${point.col + 1})",
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "LETTER: $letter",
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 9.sp,
                                                    color = TextMuted
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(14.dp))
                                    Text(
                                        text = "FULL COORDINATE PATH",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = TextMuted
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = selected.coordinates.joinToString(" → ") {
                                            "(${it.row + 1}, ${it.col + 1})"
                                        },
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(26.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(Modifier.height(22.dp))

            // Section 03: Search methodology
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "03",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = AccentAmber
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Search methodology",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Spacer(Modifier.height(14.dp))
            MethodCard(
                kicker = "01 / INDEX",
                title = "Trie construction",
                body = "Dictionary words are stored in a prefix tree. Shared prefixes use the same nodes instead of being searched repeatedly."
            )
            Spacer(Modifier.height(10.dp))
            MethodCard(
                kicker = "02 / EXPLORE",
                title = "Depth-first search",
                body = "Search starts at each cell and explores only horizontal and vertical neighbors that continue a valid dictionary prefix."
            )
            Spacer(Modifier.height(10.dp))
            MethodCard(
                kicker = "03 / RESTORE",
                title = "Backtracking",
                body = "Visited cells are marked during a path and unmarked when recursion returns, preventing cell reuse while allowing overlapping words."
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FIELD/NOTES — WORD RECOVERY SYSTEM",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = TextMuted
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "RESEARCH LOG · 2026",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MetaBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentAmber,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = Color(0xFF829096)
        )
    }
}

@Composable
private fun DimensionSelector(label: String, value: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = Color(0xFF8C999D)
        )
        Spacer(Modifier.width(8.dp))
        Box {
            Box(
                modifier = Modifier
                    .background(Color(0xFF1B252C), RoundedCornerShape(2.dp))
                    .border(1.dp, Color(0xFF39454D), RoundedCornerShape(2.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = value.toString(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextPrimary
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                (1..10).forEach { n ->
                    DropdownMenuItem(
                        text = { Text(n.toString(), fontFamily = FontFamily.Monospace) },
                        onClick = {
                            expanded = false
                            onSelect(n)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(StatBg)
            .border(1.dp, Color(0xFF2B363E))
            .padding(14.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = TextMuted
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
private fun MethodCard(kicker: String, title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelBg)
            .border(1.dp, PanelBorder)
            .padding(16.dp)
    ) {
        Text(
            text = kicker,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = AccentAmber
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = body,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = TextSecondary
        )
    }
}
