package puzzle13

import kotlin.system.measureTimeMillis

fun main() {
    val origamiSolver = OrigamiSolver()

    val time = measureTimeMillis {
        println("number of dots after one fold: ${origamiSolver.countDotsAfterFirstFold()}")
        println("Final Layout is:\n${origamiSolver.foldLayoutEntirely()}")
    }

    println("time: $time")
}

fun List<List<Boolean>>.printLayout(
    fold: FoldInstruction? = null
): String = buildString {
    for ((i, line) in this@printLayout.withIndex()) {
        for ((j, b) in line.withIndex()) {
            if (b) {
                append("#")
            } else if (fold is VerticalFoldInstruction && j == fold.x) {
                append("|")
            } else if (fold is HorizontalFoldInstruction && i == fold.y)  {
                append("—")
            } else {
                append(" ")
            }
        }
        appendLine()
    }
}

data class Position(val x: Int, val y: Int)

sealed interface FoldInstruction {
    fun performFold(layout: List<List<Boolean>>): List<List<Boolean>>
}

data class VerticalFoldInstruction(val x: Int): FoldInstruction {
    override fun performFold(layout: List<List<Boolean>>): List<List<Boolean>> {
        val newLayout = MutableList(layout.size) { MutableList(x) { false } }
        for (j in 0 until x) {
            val verticallySymmetricColumn = layout[0].size - j - 1

            for (i in layout.indices) {
                newLayout[i][j] = layout[i][j] || layout[i][verticallySymmetricColumn]
            }
        }
        return newLayout
            .map { it.toList() }
            .toList()
    }
}

data class HorizontalFoldInstruction(val y: Int): FoldInstruction {
    override fun performFold(layout: List<List<Boolean>>): List<List<Boolean>> {
        val newLayout = MutableList(y) { MutableList(layout[0].size) { false } }
        for (i in y downTo 1){
            val above = y - i
            val under = y + i
            for (j in layout[0].indices) {
                newLayout[y - i][j] = layout.getOrNull(under)?.getOrNull(j)?: false || layout.getOrNull(above)?.getOrNull(j) ?: false
            }
        }

        return newLayout
            .map { it.toList() }
            .toList()
    }
}

fun List<Position>.createLayout(): List<List<Boolean>> = MutableList(this.maxOf { it.y }  + 1) {
    MutableList(this.maxOf { it.x } + 1) { false }
}
    .apply {
        this@createLayout.forEach {
            this[it.y][it.x] = true
        }
    }
    .map { it.toList() }
    .toList()

class OrigamiSolver {
    private val input = OrigamiSolver::class.java.getResource("/input/puzzle13.txt")
        ?.readText()
        ?: ""

    private val dotPosition = input
        .split("\n\n")[0]
        .split("\n")
        .map {
            val (x, y) = it.split(",")
            Position(x.toInt(), y.toInt())
        }

    private val foldingInstructions = input
        .split("\n\n")[1]
        .split("\n")
        .map { s: String ->
            val (axis, value) = s.removePrefix("fold along ").split("=")
            when (axis) {
                "y" -> HorizontalFoldInstruction(value.toInt())
                "x" -> VerticalFoldInstruction(value.toInt())
                else -> error(axis)
            }
        }

    fun countDotsAfterFirstFold(): Int = foldingInstructions[0]
        .performFold(dotPosition.createLayout())
        .flatten()
        .count { it }

    fun foldLayoutEntirely(): String = foldingInstructions
        .fold(dotPosition.createLayout()) { acc, foldInstruction -> foldInstruction.performFold(acc) }.printLayout()
}