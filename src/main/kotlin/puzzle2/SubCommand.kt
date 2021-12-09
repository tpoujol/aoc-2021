package puzzle2

fun main() {
    println("Hello World!")
    val subCommand = SubCommand()

    println("Result is: ${subCommand.pilotSub()}, ${subCommand.pilotSubWithAim()}")
}

enum class Command {
    forward,
    down,
    up
}

data class Position (val horizontal: Int, val depth: Int, val aim: Int = 0)

class SubCommand {
    private val input: List<Pair<Command, Int>> = SubCommand::class.java.getResource("/input/puzzle2.txt")
        ?.readText()
        ?.split("\n")
        ?.filter { it.isNotEmpty() }
        ?.map {
            val values = it.split(" ")
            Pair(Command.valueOf(values[0]), values[1].toInt())
        }
        ?: listOf()

    fun pilotSub(): Int {
        val position = input.fold(Position(0, 0)) { currentPos, pair ->
            when (pair.first) {
                Command.forward -> currentPos.copy(horizontal = currentPos.horizontal + pair.second)
                Command.down -> currentPos.copy(depth = currentPos.depth + pair.second)
                Command.up -> currentPos.copy(depth = currentPos.depth - pair.second)
            }
        }
        return position.horizontal * position.depth
    }

    fun pilotSubWithAim(): Int {
        val position = input.fold(Position(0, 0, 0)) { currentPos, pair ->
            when (pair.first) {
                Command.forward -> currentPos.copy(
                    horizontal = currentPos.horizontal + pair.second,
                    depth = currentPos.depth + currentPos.aim * pair.second
                )
                Command.down -> currentPos.copy(aim = currentPos.aim + pair.second)
                Command.up -> currentPos.copy(aim = currentPos.aim - pair.second)
            }
        }
        return position.horizontal * position.depth
    }

}