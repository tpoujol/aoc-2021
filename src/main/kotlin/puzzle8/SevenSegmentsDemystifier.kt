package puzzle8

import kotlin.math.pow
import kotlin.system.measureTimeMillis

fun main() {
    val sevenSegmentsDemystifier = SevenSegmentsDemystifier()
    val basicDigitWiring = DigitWiring()
    val time = measureTimeMillis {
        println("test: ${basicDigitWiring.findNumber("acf")}")
        println("Part 1 solution is: ${sevenSegmentsDemystifier.countEasyDigits()}")
        println("Part 2 solution is: ${sevenSegmentsDemystifier.findAllDigits()}")
    }

    println("Time: $time")
}

data class InputEntry(val patterns: List<String>, val output: List<String>)

data class DigitWiring(
    private val one: String = "a",
    private val two: String = "b",
    private val three: String = "c",
    private val four: String = "d",
    private val five: String = "e",
    private val six: String = "f",
    private val seven: String = "g"
) {
    private fun produceDisplayForInt(value: Int): List<String> {
        return when (value) {
            0 -> listOf(one, two, three, five, six, seven)
            1 -> listOf(three, six)
            2 -> listOf(one, three, four, five, seven)
            3 -> listOf(one, three, four, six, seven)
            4 -> listOf(two, three, four, six)
            5 -> listOf(one, two, four, six, seven)
            6 -> listOf(one, two, four, five, six, seven)
            7 -> listOf(one, three, six)
            8 -> listOf(one, two, three, four, five, six, seven)
            9 -> listOf(one, two, three, four, six, seven)
            else -> throw IllegalArgumentException("This is not a single digit number")
        }
    }

    fun findNumber(toFind: String): Int {
        // has to be checked in that order otherwise an 8 could be caught by a 0 for example
        val splitString = toFind.toList().map { it.toString() }
        return when {
            splitString.containsAll(produceDisplayForInt(8)) -> 8
            splitString.containsAll(produceDisplayForInt(0)) -> 0
            splitString.containsAll(produceDisplayForInt(9)) -> 9
            splitString.containsAll(produceDisplayForInt(6)) -> 6
            splitString.containsAll(produceDisplayForInt(2)) -> 2
            splitString.containsAll(produceDisplayForInt(3)) -> 3
            splitString.containsAll(produceDisplayForInt(5)) -> 5
            splitString.containsAll(produceDisplayForInt(4)) -> 4
            splitString.containsAll(produceDisplayForInt(7)) -> 7
            splitString.containsAll(produceDisplayForInt(1)) -> 1
            else -> -1
        }
    }

    fun findNumberForList(toFind: List<String>): Int {
        return toFind
            .reversed()
            .foldIndexed(0) {index: Int, acc: Int, s: String ->
                acc + findNumber(s) * 10.0.pow(index).toInt()
            }
    }
}

class SevenSegmentsDemystifier {
    private val input = SevenSegmentsDemystifier::class.java.getResource("/input/puzzle8.txt")
        ?.readText()
        ?.split("\n")
        ?.filter { it.isNotBlank() }
        ?.map { s: String ->
            val (patterns, output) = s.split("|").map { it.trim() }
            InputEntry(
                patterns.split(" ").map { it.trim() },
                output.split(" ").map { it.trim() }
            )
        }
        ?: listOf()

    fun countEasyDigits(): Int {
        var counter = 0
        input.forEach { entry: InputEntry ->
            counter += entry.output.sumOf {
                val result = when(it.length) {
                    2, 3, 4, 7 -> 1
                    else -> 0
                }
                result
            }
        }
        return counter
    }

    private fun decryptPattern(patterns: List<String>): DigitWiring {
        val dividedPatterns = patterns.groupBy { it.length }
        val segmentOne: Char
        val segmentTwo: Char
        val segmentThree: Char
        val segmentFour: Char
        val segmentFive: Char
        val segmentSix: Char
        val segmentSeven: Char
        // Extract possibilities for the obvious digits
        val patternForOne = dividedPatterns.getOrDefault(2, null)?.get(0)?.toList()

        val patternForSeven = dividedPatterns.getOrDefault(3, null)?.get(0)?.toList()

        val patternForFour = dividedPatterns.getOrDefault(4, null)?.get(0)?.toList()

        if (patternForSeven == null || patternForOne == null || patternForFour == null ) {
            return DigitWiring()
        }
        segmentOne = patternForSeven.filterNot { patternForOne.contains(it) }[0]
        val possibilitiesForSegments3And6 = patternForSeven.filter { patternForOne.contains(it) }

        val possibilitiesForSegment2And4 = patternForFour.filterNot { patternForOne.contains(it) || it == segmentOne }

        // Find a 3 in the patterns, this will be a number
        // with 5 segments,
        // with all the possibilities for 3 and 6,
        // with the segment 1,
        // and with one of the possibilities of the 2 and 4
        val patternForThree = dividedPatterns
            .getOrDefault(5, null)
            ?.map { it.toList() }
            ?.filter { it.contains(segmentOne) }
            ?.filter { it.containsAll(possibilitiesForSegments3And6) }
            ?.filter { chars ->
                possibilitiesForSegment2And4.any { chars.contains(it) }
            }
            ?.getOrNull(0)

        if (patternForThree == null) {
            println("no pattern for 3")
            return DigitWiring()
        }

        segmentFour = patternForThree.filter { possibilitiesForSegment2And4.contains(it) }[0]
        segmentTwo = possibilitiesForSegment2And4.filterNot { it == segmentFour }[0]
        segmentSeven = patternForThree.filterNot {
            it == segmentOne || it == segmentFour || patternForOne.contains(it)
        }[0]

        // Next, we find a segment representing a 5, this will be a number with
        // 5 segments
        // the segments 1, 2, 4 and 7 in it
        // one of the segments from the possibilities for 3 and 6

        val patternForFive = dividedPatterns
            .getOrDefault(5, null)
            ?.map { it.toList()}
            ?.filter{ it.containsAll(listOf(segmentOne, segmentTwo, segmentFour, segmentSeven)) }
            ?.filter { list: List<Char> ->
                possibilitiesForSegments3And6.any { list.contains(it) }
            }
            ?.getOrNull(0)

        if (patternForFive == null) {
            println("no pattern for 5")
            return DigitWiring()
        }

        segmentSix = patternForFive.filter { possibilitiesForSegments3And6.contains(it) }[0]
        segmentThree = possibilitiesForSegments3And6.filterNot { it == segmentSix }[0]

        // Finally, we deduce the last segment
        segmentFive = "abcdefg".toList().filterNot {
            listOf(segmentOne, segmentTwo, segmentThree, segmentFour, segmentSix, segmentSeven).contains(it)
        }[0]
        return DigitWiring(
            segmentOne.toString(),
            segmentTwo.toString(),
            segmentThree.toString(),
            segmentFour.toString(),
            segmentFive.toString(),
            segmentSix.toString(),
            segmentSeven.toString()
        )
    }

    fun findAllDigits(): Int {
        val display = decryptPattern(input[0].patterns)
        println("$display -> ${display.findNumberForList(input[0].output)}")

        return input.fold(0) { acc, inputEntry ->
            val digitWiring = decryptPattern(inputEntry.patterns)
            acc + digitWiring.findNumberForList(inputEntry.output)
        }
    }
}