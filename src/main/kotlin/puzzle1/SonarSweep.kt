package puzzle1

fun main() {
    println("Hello World!")
    val sonarSweep = SonarSweep()

    // Try adding program arguments at Run/Debug configuration
    println("Result is: ${sonarSweep.getIncreases()}, ${sonarSweep.getIncreasesWindowed()}")
}


class SonarSweep {
    private val input = SonarSweep::class.java.getResource("/input/puzzle1.txt")
        ?.readText()
        ?.split("\n")
        ?.filter { it.isNotEmpty() }
        ?.map { it.toInt() } ?: emptyList()

    fun getIncreases():  Int {
        val list: List<Int> = input

        val result = list.foldIndexed(0) { index, acc, item ->
            if (index == 0) 0 else if (item > list[index - 1]) acc + 1 else acc + 0
            }
        return result

    }

    fun getIncreasesWindowed(): Int {
        val list: List<Int> = input

        val sums: List<Int> = list
            .windowed(3, 1, false)
            .map { it.sum() }

        val result = sums.foldIndexed(0) { index, acc, item ->
            if (index == 0) 0 else if (item > sums[index - 1]) acc + 1 else acc + 0
        }
        return result
    }
}