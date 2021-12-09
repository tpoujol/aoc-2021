package puzzle3

import kotlin.math.pow
import kotlin.math.roundToInt


fun main() {
    println("Hello World!")
    val binaryDiagnostic = BinaryDiagnostic()

    println("Power consumption is: ${binaryDiagnostic.findPowerConsumption()}, ${binaryDiagnostic.findLifeSupportRating()}")
}

class BinaryDiagnostic {
    private val input: List<List<Int>> = BinaryDiagnostic::class.java.getResource("/input/puzzle3.txt")
        ?.readText()
        ?.split("\n")
        ?.filter { it.isNotEmpty() }
        ?.map { it.fold(MutableList(0) { 0 }) { acc, c -> acc.apply { this.add(c.digitToInt()) } }.toList() }
        ?: listOf()

    fun findPowerConsumption():Int {
        val sums = input.fold(MutableList(input[0].size) { 0 }) { acc: MutableList<Int>, list: List<Int> ->
            list.forEachIndexed { index, value -> acc[index] += value }
            acc
        }
        println(sums)
        val binaryGamma = sums
            .map { (it.toDouble() / input.size).roundToInt() }

        val binaryEpsilon = sums
            .map { ((input.size - it).toDouble() / input.size ).roundToInt() }

        val gamma = convertBinaryToDecimal(binaryGamma)

        val epsilon = convertBinaryToDecimal(binaryEpsilon)

        println("binaryGamma: $binaryGamma, gamma: $gamma")
        println("binaryEpsilon: $binaryEpsilon, epsilon: $epsilon")

        return epsilon * gamma
    }

    fun findLifeSupportRating(): Int{
        val oxygenRatingBinary = filterOutInputForLifeSupportValue(input, 0, true)
        val co2RatingBinary = filterOutInputForLifeSupportValue(input, 0, false)

        val oxygenRating = convertBinaryToDecimal(oxygenRatingBinary)
        val co2Rating = convertBinaryToDecimal(co2RatingBinary)

        println("oxygenRatingBinary: $oxygenRatingBinary, oxygen: $oxygenRating")
        println("co2RatingBinary: $co2RatingBinary, co2: $co2Rating")

        return oxygenRating * co2Rating
    }

    companion object {
        private fun convertBinaryToDecimal(binaryValue: List<Int>) = binaryValue
            .reversed()
            .foldIndexed(0.0) { index: Int, acc: Double, digit: Int -> acc + 2.0.pow(index) * digit }
            .toInt()

        private fun filterOutInputForLifeSupportValue(
            valuesList: List<List<Int>>,
            indexOfDiscriminant: Int,
            shouldTakeMostCommon: Boolean
        ): List<Int> {
            val sum = valuesList.fold(0) { acc, list -> acc + list[indexOfDiscriminant] }

            val discriminant: Int = if (shouldTakeMostCommon) {
                if (sum > valuesList.size / 2.0) 1 else if (sum < valuesList.size / 2.0) 0 else 1
            } else {
                if (sum > valuesList.size / 2.0) 0 else if (sum < valuesList.size / 2.0) 1 else 0
            }

            val resultList = valuesList.filter { it[indexOfDiscriminant] == discriminant }

            println("initial size: ${valuesList.size}, sum: $sum, index: $indexOfDiscriminant, discriminant: $discriminant size: ${resultList.size}, $resultList")

            return if (resultList.size == 1) resultList[0] else filterOutInputForLifeSupportValue(
                resultList, indexOfDiscriminant + 1, shouldTakeMostCommon
            )
        }
    }
}