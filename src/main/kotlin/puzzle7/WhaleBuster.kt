package puzzle7

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

fun main() {
    val whaleBuster = WhaleBuster()
    val time = measureTimeMillis {
        println("Minimal fuel consumption is: ${whaleBuster.findCrabAlignment()}")
        println("Total fuel consumption with cost increased by distance is: ${whaleBuster.findCrabAlignmentWithCostProblems()}")
    }

    println("Time: ${time}ms")
}

class WhaleBuster {
    private val input = WhaleBuster::class.java.getResource("/input/puzzle7.csv")
        ?.readText()
        ?.split(",")
        ?.map { it.toInt() }
        ?: listOf()

    fun findCrabAlignment(): Int {
        val distanceValues = (0..input.maxOf { it }).associateWith { position ->
            input.sumOf { crabPosition ->
                abs(crabPosition - position)
            }
        }
        println(distanceValues)
        val inputNumber = input.size
        val sortedInput = input.sorted()

        val median = if (inputNumber % 2 == 1) {
            sortedInput[floor(inputNumber / 2.0).toInt()]
        } else {
            (sortedInput[inputNumber / 2] + sortedInput[inputNumber / 2]) / 2
        }

        println("median is: $median")

        return sortedInput.sumOf { abs(it - median) }
    }

    fun findCrabAlignmentWithCostProblems(): Int {
        val distanceValues = (0..input.maxOf { it }).associateWith { position ->
            input.sumOf { crabPosition ->
                val distance = abs(crabPosition - position)
                (distance * (distance + 1) / 2.0).roundToInt()
            }
        }
        val optimalPosition = distanceValues.minByOrNull { it.value }
        println("Optimal Position is: $optimalPosition")
        return optimalPosition?.value ?: 0
    }
}