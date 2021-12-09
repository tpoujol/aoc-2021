package puzzle6

import kotlin.system.measureTimeMillis

fun main() {
    val lanternFishModeler = LanternFishModeler()
    val test = measureTimeMillis {
        println("Number of lantern fish after 80 days: ${lanternFishModeler.modelFishReproductionReallyEfficiently(80)}")
        println("Number of lantern fish after 256 days: ${lanternFishModeler.modelFishReproductionReallyEfficiently(256)}")
    }
    println(test)
}

class LanternFishModeler {
    private val input = LanternFishModeler::class.java.getResource("/input/puzzle6.txt")
        ?.readText()
        ?.split(",")
        ?.groupBy { it.toInt() }
        ?.map { it.key to it.value.size.toLong() }
        ?.toMap()
        ?: mapOf()

    fun modelFishReproductionReallyEfficiently(numberOfDays: Int): Long {

        val internalTimerFish = LongArray(9) { i -> input[i] ?:0 }

        for (d in 0 until numberOfDays){
            val resettingFishNumber = internalTimerFish[0]

            internalTimerFish[0] = internalTimerFish[1]
            internalTimerFish[1] = internalTimerFish[2]
            internalTimerFish[2] = internalTimerFish[3]
            internalTimerFish[3] = internalTimerFish[4]
            internalTimerFish[4] = internalTimerFish[5]
            internalTimerFish[5] = internalTimerFish[6]
            internalTimerFish[6] = (internalTimerFish[7]) + resettingFishNumber
            internalTimerFish[7] = internalTimerFish[8]
            internalTimerFish[8] = resettingFishNumber
        }
        return internalTimerFish.sum()
    }
}