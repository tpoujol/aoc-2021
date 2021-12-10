package puzzle9

import com.github.ajalt.mordant.rendering.TextColors
import java.lang.StringBuilder
import kotlin.system.measureTimeMillis

fun main() {
    val smokeAvoider = SmokeAvoider()

    val time = measureTimeMillis {
        val localMinResult = smokeAvoider.findLocalMinimums()
        val basinsSize = smokeAvoider.findBasins()
        println("Layout is:\n${smokeAvoider.printGrid()}")
        val gridLayout = smokeAvoider.printGrid()

        println("Local low points $localMinResult")

        println("Biggest basins are: $basinsSize")

    }

    println("time: $time")
}

data class Cell(val value: Int, var isSmallerThanNeighbors: Boolean, var groupID: Int?): Comparable<Cell>{
    override fun compareTo(other: Cell): Int {
        return this.value - other.value
    }
}

fun List<List<Cell>>.printLayout(): String {
    val colors = listOf(
        TextColors.red,
        TextColors.green,
        TextColors.yellow,
        TextColors.blue,
        TextColors.magenta,
        TextColors.cyan,
        TextColors.white,
    )
    return this.fold(StringBuilder()) { acc: StringBuilder, list: List<Cell> ->
        list.fold(acc) { innerAcc: StringBuilder, cell: Cell ->
            val groupID = cell.groupID
            innerAcc.append(
                if (groupID != null) {
                    val groupColor = colors[groupID % colors.size]
                     groupColor(cell.value.toString())
                } else {
                    cell.value.toString()
                }
            )
            innerAcc
        }
        acc.append("\n")
        acc
    }.toString()
}

fun List<List<Cell>>.getNeighborsOfCellPos(i: Int, j: Int): List<Cell> {
    return listOfNotNull(
        this.getOrNull(i - 1)?.getOrNull(j),
        this.getOrNull(i + 1)?.getOrNull(j),
        this.getOrNull(i)?.getOrNull(j - 1),
        this.getOrNull(i)?.getOrNull(j + 1),
    )
}

class SmokeAvoider {
    private val grid = SmokeAvoider::class.java.getResource("/input/puzzle9.txt")
        ?.readText()
        ?.split("\n")
        ?.map { s: String ->
            s.fold(mutableListOf()) { acc: MutableList<Cell>, c: Char ->
                acc.add(Cell(
                    c.digitToInt(),
                    false,
                    null
                ))
                acc
            }
                .toList()
        }
        ?: listOf()

    fun findLocalMinimums(): Int {
        grid.forEachIndexed { i, list ->
            list.forEachIndexed { j, cell ->
                // Check neighbours
                cell.isSmallerThanNeighbors = cell.value < grid.getNeighborsOfCellPos(i, j).minOf { it.value }
            }
        }
        return grid.flatten().filter { it.isSmallerThanNeighbors }.sumOf { 1 + it.value }
    }

    fun findBasins(): Int {
        // Building group IDs top left to bottom right
        var groupCounter = 0
        val listOfGroupMatching: MutableMap<Int, MutableSet<Int>> = mutableMapOf()
        grid.forEachIndexed cols@{ i, list ->
            list.forEachIndexed rows@{ j, cell ->
                if (cell.value == 9) return@rows

                // check top and left neighbours for already existing group
                val topNeighborGroupId = grid.getOrNull(i - 1)?.getOrNull(j)?.groupID
                val leftNeighborGroupId = grid.getOrNull(i)?.getOrNull(j - 1)?.groupID

                if (topNeighborGroupId == null && leftNeighborGroupId == null ){
                    cell.groupID = groupCounter++
                } else if (topNeighborGroupId != null && leftNeighborGroupId == null) {
                    cell.groupID = topNeighborGroupId
                } else if (leftNeighborGroupId != null && topNeighborGroupId == null) {
                    cell.groupID = leftNeighborGroupId
                } else if (leftNeighborGroupId == topNeighborGroupId) {
                    cell.groupID = leftNeighborGroupId
                } else {
                    // By default, take top over left
                    cell.groupID = topNeighborGroupId

                    // record the need to regroup both basins afterwards
                    listOfGroupMatching.getOrPut(topNeighborGroupId!!) { mutableSetOf() }.add(leftNeighborGroupId!!)
                }
            }
        }
        println(listOfGroupMatching)

        // Regroup all group links into base groups
        var changesWereMade = true
        while (changesWereMade) {
            changesWereMade = false
            for ((key, values) in listOfGroupMatching) {
                var removedANode = false
                for (value in values) {
                    val link = listOfGroupMatching[value]
                    if (link != null) {
                        listOfGroupMatching[key]?.addAll(link)
                        listOfGroupMatching.remove(value)
                        removedANode = true
                        break
                    }
                }
                if (removedANode){
                    changesWereMade = true
                    break
                }
            }
        }

        val invertedMap = listOfGroupMatching
            .flatMap { mapEntry ->
                mapEntry.value.map { it to mapEntry.key }
            }
            .toMap()

        // Transform all temporary groups into "real ones"
        grid.flatten().forEach {
            it.groupID = invertedMap[it.groupID] ?: it.groupID
        }

        val biggestThreeGroups = grid
            .asSequence()
            .flatten()
            .groupBy { it.groupID }
            .filter { it.key != null }
            .map { it.key to it.value.count() }
            .sortedBy { it.second }
            .toList()
            .takeLast(3)

        println(biggestThreeGroups)

        return biggestThreeGroups.fold(1) { acc, pair -> acc * pair.second }
    }

    fun printGrid(): String = grid.printLayout()
}