package puzzle11

import com.github.ajalt.mordant.rendering.TextColors
import java.lang.StringBuilder
import kotlin.system.measureTimeMillis

fun main() {
    val octopusEnergizer = OctopusEnergizer()

    val time = measureTimeMillis {
        println("Number of flashes after 100 steps: ${octopusEnergizer.processFlashes()}")
        println("Number of steps to get everyone flashing: ${octopusEnergizer.findOctopusSync()}")
    }

    println("time: $time")
}

fun List<List<Octopus>>.produceLayout(): String {
    val builder = StringBuilder()

    this.forEach { row ->
        row.forEach { octopus ->
            if (octopus.hasFlashedDuringStep) {
                builder.append(TextColors.magenta(octopus.energyLevel.toString()))
            } else {
                builder.append(octopus.energyLevel)
            }
        }
        builder.append("\n")
    }

    return builder.toString()
}

data class Octopus(var energyLevel: Int, var hasFlashedDuringStep: Boolean)

class OctopusEnergizer {
    private val input = OctopusEnergizer::class.java.getResource("/input/puzzle11.txt")
        ?.readText()
        ?.split("\n")
        ?: listOf()

    fun processFlashes(): Int {
        val octopusGrid = generateOctopusGrid(input)
        var flashCounter = 0
        for (i in 1..100) {
            increaseEnergy(octopusGrid)
            flashCounter += findAndPropagateFlashes(octopusGrid)
            if (i % 10 == 0)
                println("After step $i:\n${octopusGrid.produceLayout()}")
            resetFlashStatuses(octopusGrid)
        }
        return flashCounter
    }

    fun findOctopusSync(): Int {
        val octopusGrid = generateOctopusGrid(input)
        var everyoneFlashed: Boolean
        var stepCounter = 0
        do {
            stepCounter++
            increaseEnergy(octopusGrid)
            findAndPropagateFlashes(octopusGrid)
            everyoneFlashed = octopusGrid.flatten().all { it.hasFlashedDuringStep }
            if (everyoneFlashed || stepCounter % 10 == 0) {
                println("After step $stepCounter:\n${octopusGrid.produceLayout()}")
            }
            resetFlashStatuses(octopusGrid)
        }while (!everyoneFlashed)

        return stepCounter
    }

    companion object {
        private fun findAndPropagateFlashes(grid: List<List<Octopus>>): Int {
            var someoneFlashed = true
            var flashCounter = 0
            while (someoneFlashed) {
                someoneFlashed = false
                grid.forEachIndexed { i, list ->
                    list.forEachIndexed { j, octopus ->
                        if (octopus.energyLevel > 9 && !octopus.hasFlashedDuringStep) {
                            octopus.hasFlashedDuringStep = true
                            octopus.energyLevel = 0
                            propagateFlashingEnergy(grid, i, j)
                            someoneFlashed = true
                            flashCounter++
                        }
                    }
                }
            }
            return flashCounter
        }

        private fun increaseEnergy(grid: List<List<Octopus>>) {
            grid.flatten().forEach { it.energyLevel++ }
        }

        private fun resetFlashStatuses(grid: List<List<Octopus>>) {
            grid.flatten().forEach { it.hasFlashedDuringStep = false }
        }

        private fun propagateFlashingEnergy(grid: List<List<Octopus>>, i: Int, j: Int) {
            val energyPropagation = { octopus: Octopus ->
                if (!octopus.hasFlashedDuringStep) {
                    octopus.energyLevel++
                }
            }
            grid.getOrNull(i - 1)?.getOrNull(j - 1)?.run(energyPropagation)
            grid.getOrNull(i - 1)?.getOrNull(j)?.run(energyPropagation)
            grid.getOrNull(i - 1)?.getOrNull(j + 1)?.run(energyPropagation)

            grid.getOrNull(i)?.getOrNull(j - 1)?.run(energyPropagation)
            grid.getOrNull(i)?.getOrNull(j + 1)?.run(energyPropagation)

            grid.getOrNull(i + 1)?.getOrNull(j - 1)?.run(energyPropagation)
            grid.getOrNull(i + 1)?.getOrNull(j)?.run(energyPropagation)
            grid.getOrNull(i + 1)?.getOrNull(j + 1)?.run(energyPropagation)
        }

        private fun generateOctopusGrid(layout: List<String>) = layout.map {
            it.fold(mutableListOf()) { acc: MutableList<Octopus>, c: Char ->
                acc.apply { add(Octopus(energyLevel = c.digitToInt(), hasFlashedDuringStep = false)) }
            }.toList()
        }
    }
}