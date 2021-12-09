package puzzle5

import java.lang.StringBuilder
import java.util.stream.IntStream.rangeClosed
import kotlin.system.measureTimeMillis

fun main() {
    val test = measureTimeMillis {
        println("Hello World!")
        val hydrothermalVentDetector = HydrothermalVentDetector()

        println("Number of horizontal intersects is: ${hydrothermalVentDetector.detectStraightVentLines()}")
        println("Total number of intersects is: ${hydrothermalVentDetector.detectAllVentLines()}")
    }

    println(test)
}

data class Position(val x: Int, val y: Int): Comparable<Position> {
    constructor(pair: Pair<Int, Int>) : this(pair.first, pair.second)
    override fun compareTo(other: Position): Int {
        return if (this.y != other.y) this.y - other.y else this.x - other.x
    }
}

data class Line(val startingPoint: Position, val finishingPoint: Position)

data class Cell(val position: Position, val ventNumbers: Int)

private fun List<List<Int>>.printLayout():String {
    val builder = StringBuilder()

    this.forEach { line: List<Int> ->
        line.forEach { builder.append("${if (it == 0) "." else it.toString()} ") }
        builder.append("\n")
    }

    return builder.toString()
}

class HydrothermalVentDetector {
    private val input: List<Line> = HydrothermalVentDetector::class.java.getResource("/input/puzzle5.csv")
        ?.readText()
        ?.split("\n")
        ?.map { lineString ->
            val (startingString, endingString) = lineString.split(" -> ")
            val (startingX, startingY) = startingString.split(",")
            val (endingX, endingY) = endingString.split(",")

            // Get the positions
            val startingPoint = Position(startingX.toInt(), startingY.toInt())
            val finishingPoint = Position(endingX.toInt(), endingY.toInt())

            if (startingPoint <= finishingPoint) Line(startingPoint,finishingPoint)
            else Line(finishingPoint, startingPoint)
        }
        ?: listOf()

    private val maxX = input.maxByOrNull { it.finishingPoint.x }?.finishingPoint?.x ?: 0
    private val maxY = input.maxByOrNull { it.finishingPoint.y }?.finishingPoint?.y ?: 0

    fun detectStraightVentLines(): Int {
        val ventsLayout: MutableList<MutableList<Int>> = MutableList(maxY + 1) { MutableList(maxX + 1) { 0 } }

        detectAndPlaceStraightLinesOnLayout(input, ventsLayout)

        return computeNumberOfIntersects(ventsLayout)
    }

    fun detectAllVentLines(): Int {
        val ventsLayout: MutableList<MutableList<Int>> = MutableList(maxY + 1) { MutableList(maxX + 1) { 0 } }

        detectAndPlaceStraightLinesOnLayout(input, ventsLayout)

        detectAndPlaceDiagonalLinesOnLayout(input, ventsLayout)

        println("My final layout is:\n${ventsLayout.printLayout()}")
        return computeNumberOfIntersects(ventsLayout)
    }

    companion object {
        private val straightLinePredicate: (Line) -> Boolean = {
            it.startingPoint.x == it.finishingPoint.x || it.startingPoint.y == it.finishingPoint.y
        }
        /**
         * This function will mutate the given layout
          */
        private fun detectAndPlaceStraightLinesOnLayout(lines: List<Line>, layout: MutableList<MutableList<Int>>) {
            val straightVents = lines
                .filter(straightLinePredicate)
                .groupBy { it.startingPoint.x == it.finishingPoint.x }

            val horizontalVents = straightVents[false] ?: listOf()
            val verticalVents = straightVents[true] ?: listOf()

            verticalVents.forEach { line ->
                for (i in rangeClosed(line.startingPoint.y, line.finishingPoint.y)) {
                    layout[i][line.startingPoint.x] += 1
                }
            }

            horizontalVents.forEach { line ->
                for (j in rangeClosed(line.startingPoint.x, line.finishingPoint.x)) {
                    layout[line.startingPoint.y][j] += 1
                }
            }
        }

        private fun detectAndPlaceDiagonalLinesOnLayout(lines: List<Line>, layout: MutableList<MutableList<Int>>) {
            val diagonalLines = lines
                .filterNot(straightLinePredicate)

            diagonalLines.forEach { line ->
                var currentX = line.startingPoint.x
                val xDirection = if (line.startingPoint.x > line.finishingPoint.x) -1 else +1
                for (i in rangeClosed(line.startingPoint.y, line.finishingPoint.y)) {
                    layout[i][currentX] += 1
                    currentX += xDirection
                }
            }
        }

        private fun computeNumberOfIntersects(ventLayout: List<List<Int>>) = ventLayout.flatten().count { it > 1 }
    }
}