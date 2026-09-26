package com.riere.blockbooster

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.random.Random

private const val SIZE = 8
private typealias Cell = Pair<Int, Int>

private data class Piece(val cells: List<Cell>, val name: String)
private val PIECES = listOf(
    Piece(listOf(0 to 0), "Single"),
    Piece(listOf(0 to 0, 1 to 0), "Domino"),
    Piece(listOf(0 to 0, 0 to 1), "Vertical"),
    Piece(listOf(0 to 0, 1 to 0, 2 to 0), "Line 3"),
    Piece(listOf(0 to 0, 0 to 1, 0 to 2), "Column 3"),
    Piece(listOf(0 to 0, 1 to 0, 0 to 1, 1 to 1), "Square"),
    Piece(listOf(0 to 0, 1 to 0, 2 to 0, 1 to 1), "T"),
    Piece(listOf(0 to 0, 0 to 1, 1 to 1, 2 to 1), "L"),
    Piece(listOf(0 to 0, 0 to 1, 0 to 2, 1 to 2), "J"),
    Piece(listOf(0 to 0, 1 to 0, 1 to 1, 2 to 1), "Z"),
    Piece(listOf(1 to 0, 2 to 0, 0 to 1, 1 to 1), "S"),
    Piece(listOf(0 to 0, 1 to 0, 0 to 1), "Corner"),
    Piece(listOf(0 to 0, 1 to 0, 2 to 0, 0 to 1), "Big L"),
    Piece(listOf(0 to 0, 0 to 1, 1 to 1, 2 to 1), "Big J")
)

private fun randomPieces(): List<Piece> = List(3) { PIECES.random() }

private fun fits(board: List<Boolean>, piece: Piece, row: Int, col: Int): Boolean =
    piece.cells.all { (dr, dc) ->
        val r = row + dr
        val c = col + dc
        r in 0 until SIZE && c in 0 until SIZE && !board[r * SIZE + c]
    }

private fun placeAndClear(board: List<Boolean>, piece: Piece, row: Int, col: Int): Pair<List<Boolean>, Int> {
    val next = board.toMutableList()
    piece.cells.forEach { (dr, dc) -> next[(row + dr) * SIZE + col + dc] = true }
    val rows = (0 until SIZE).filter { r -> (0 until SIZE).all { c -> next[r * SIZE + c] } }
    val cols = (0 until SIZE).filter { c -> (0 until SIZE).all { r -> next[r * SIZE + c] } }
    rows.forEach { r -> (0 until SIZE).forEach { c -> next[r * SIZE + c] = false } }
    cols.forEach { c -> (0 until SIZE).forEach { r -> next[r * SIZE + c] = false } }
    return next to (rows.size + cols.size)
}

private fun hasMove(board: List<Boolean>, piece: Piece): Boolean =
    (0 until SIZE).any { r -> (0 until SIZE).any { c -> fits(board, piece, r, c) } }

private fun gameOver(board: List<Boolean>, pieces: List<Piece>): Boolean =
    pieces.none { hasMove(board, it) }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BlockBoosterApp() }
    }
}

@Composable
private fun BlockBoosterApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("block_booster", Context.MODE_PRIVATE) }
    var board by remember { mutableStateOf(List(SIZE * SIZE) { false }) }
    var pieces by remember { mutableStateOf(randomPieces()) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(prefs.getInt("high_score", 0)) }
    var gameOver by remember { mutableStateOf(false) }

    fun vibrate(ms: Long = 20) {
        runCatching {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= 31) {
                context.getSystemService(VibratorManager::class.java).defaultVibrator
            } else context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (android.os.Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            else @Suppress("DEPRECATION") vibrator.vibrate(ms)
        }
    }

    fun restart() {
        board = List(SIZE * SIZE) { false }
        pieces = randomPieces()
        selected = null
        score = 0
        combo = 0
        gameOver = false
    }

    fun tryPlace(row: Int, col: Int) {
        val index = selected ?: return
        val piece = pieces[index]
        if (!fits(board, piece, row, col)) {
            vibrate(45)
            return
        }
        val (next, cleared) = placeAndClear(board, piece, row, col)
        board = next
        val gained = piece.cells.size * 10 + if (cleared > 0) cleared * 100 else 0
        combo = if (cleared > 0) combo + 1 else 0
        score += gained + if (cleared > 0) combo * 50 else 0
        if (score > highScore) {
            highScore = score
            prefs.edit().putInt("high_score", highScore).apply()
        }
        vibrate(if (cleared > 0) 70 else 18)
        pieces = pieces.toMutableList().also { it[index] = PIECES.random() }
        selected = null
        gameOver = gameOver(board, pieces)
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("BLOCK BOOSTER", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                Text("Place blocks. Clear lines. Beat your best.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Stat("SCORE", score.toString())
                    Stat("BEST", highScore.toString())
                    Stat("COMBO", "x$combo")
                }
                Spacer(Modifier.height(14.dp))
                Card(Modifier.fillMaxWidth()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(SIZE),
                        modifier = Modifier.padding(8.dp).fillMaxWidth().aspectRatio(1f),
                        userScrollEnabled = false
                    ) {
                        itemsIndexed(board) { index, filled ->
                            val r = index / SIZE
                            val c = index % SIZE
                            val canPreview = selected?.let { fits(board, pieces[it], r, c) } == true
                            Box(
                                Modifier.padding(2.dp).aspectRatio(1f)
                                    .background(
                                        when {
                                            filled -> MaterialTheme.colorScheme.primary
                                            canPreview -> MaterialTheme.colorScheme.primaryContainer
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        MaterialTheme.shapes.small
                                    )
                                    .border(1.dp, MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
                                    .clickable(enabled = !gameOver) { tryPlace(r, c) }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Choose a block", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    pieces.forEachIndexed { index, piece ->
                        PieceCard(piece, selected == index) {
                            if (!gameOver) selected = if (selected == index) null else index
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                if (gameOver) {
                    Text("NO MORE MOVES", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Final score: $score")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = ::restart) { Text("PLAY AGAIN") }
                } else {
                    TextButton(onClick = ::restart) { Text("Restart game") }
                }
                Spacer(Modifier.weight(1f))
                Text("Offline • No account required • v1.0.0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PieceCard(piece: Piece, selected: Boolean, onClick: () -> Unit) {
    val maxR = piece.cells.maxOf { it.first }
    val maxC = piece.cells.maxOf { it.second }
    Column(
        Modifier.size(92.dp).border(
            2.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            MaterialTheme.shapes.medium
        ).clickable { onClick() }.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column {
            for (r in 0..maxR) {
                Row {
                    for (c in 0..maxC) {
                        Box(
                            Modifier.size(15.dp).padding(1.dp).background(
                                if ((r to c) in piece.cells) MaterialTheme.colorScheme.primary
                                else Color.Transparent,
                                MaterialTheme.shapes.extraSmall
                            )
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(if (selected) "SELECTED" else "TAP", fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}
