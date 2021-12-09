package puzzle4

fun main() {
    println("Hello World!")
    val bingoSubsystem = BingoSubsystem()

    println("Bingo results are:\n${bingoSubsystem.winBingo()}, ${bingoSubsystem.loseBingo()}")
}

data class Position(val col: Int, val row: Int)

data class Board(
    val boardNumbers: List<List<Int>>,
    val calledPositions: List<Position> = listOf()
)

fun Board.displayBoardLayout(): String {
    val builder = StringBuilder()

    boardNumbers.forEachIndexed { i, list ->
        list.forEachIndexed { j, number ->
            val numberCalled: Boolean = this.calledPositions.any { it.col == i && it.row == j }
            if(numberCalled) {
                builder.append("[")
            } else {
                builder.append(" ")
            }
            builder.append(number.toString())
            if (numberCalled) {
                builder.append("]")
            } else {
                builder.append(" ")
            }
            builder.append(" ")
        }
        builder.append("\n")
    }

    return builder.toString()
}

fun List<Board>.displayBoardsLayouts(): String {
    return this.fold("") { acc: String, board: Board ->
        "$acc${board.displayBoardLayout()}\n"
    }
}


class BingoSubsystem {
    private val input = BingoSubsystem::class.java.getResource("/input/puzzle4.csv")
        ?.readText()
        ?.split("\n\n")
        ?: listOf()

    private val numbersToDraw = input[0]
        .split(",")
        .map { it.toInt() }

    private val boards: List<Board> = input
        .drop(1)
        .map { string ->
            val lines = string.split("\n")
            val boardLayout: MutableList<List<Int>> = lines
                .fold(MutableList(0) { emptyList() }) { acc: MutableList<List<Int>>, s: String ->
                    acc.apply { add(s
                        .trim()
                        .split(" ")
                        .filter { it.isNotEmpty() }
                        .map { it.toInt() }
                    ) }
                }
            Board(boardLayout.toList())
        }

    fun winBingo(): Int {
        var playingBoards = boards.map { it.copy() }
        var winningBoard: Board? = null
        var lastNumber: Int? = null
        for (number in numbersToDraw) {
            //println("Drawing: $number")
            playingBoards = updateBoardsWithNumber(playingBoards, number)
            //println("Updated Boards:\n${playingBoards.displayBoardsLayouts()}")

            val winningBoardChecked = searchForWinningBoard(playingBoards)
            if (winningBoardChecked != null) {
                winningBoard = winningBoardChecked
                lastNumber = number
                break
            }
        }

        if (winningBoard == null || lastNumber == null) return 0

        println("Last number $lastNumber, winning board:\n${winningBoard.displayBoardLayout()}")

        return computeWinningBoardScore(winningBoard, lastNumber)
    }

    fun loseBingo(): Int {
        var playingBoards = boards.map { it.copy() }
        var lastWinningBoard: Board? = null
        var finalNumber: Int? = null

        for (number in numbersToDraw) {
            println("Drawing: $number")
            playingBoards = updateBoardsWithNumber(playingBoards, number)
            //println("Updated Boards:\n${playingBoards.displayBoardsLayouts()}")

            val winningBoards = searchForAllWinningBoards(playingBoards)
            playingBoards = playingBoards.filter { it !in winningBoards }
            println("winningBoards:\n ${winningBoards.displayBoardsLayouts()}")
            println("playingBoards:\n ${playingBoards.displayBoardsLayouts()}")

            if(playingBoards.isEmpty() && winningBoards.isNotEmpty()) {
                lastWinningBoard = winningBoards[0]
                finalNumber = number
                break
            }
        }

        if (lastWinningBoard == null || finalNumber == null) return 0
        return computeWinningBoardScore(lastWinningBoard, finalNumber)
    }

    companion object {
        private fun computeWinningBoardScore(board: Board, winningNumber: Int): Int {
            val unmarkedNumbers = board.boardNumbers
                .flatMapIndexed { i: Int, list: List<Int> ->
                    list.mapIndexed { j, number ->
                        Pair(number, Position(i, j))
                    }
                }

            return winningNumber * unmarkedNumbers
                .filterNot { it.second in  board.calledPositions}
                .sumOf { it.first }
        }

        private fun searchForWinningBoard(boardList: List<Board>): Board? {
            return boardList.firstOrNull(winningBoardPredicate)
        }

        private fun searchForAllWinningBoards(boardList: List<Board>): List<Board> {
            return boardList.filter(winningBoardPredicate)
        }

        private fun updateBoardsWithNumber(playingBoards: List<Board>, drawnNumber: Int): List<Board> {
            val result = playingBoards.toMutableList()
            playingBoards.forEachIndexed  board@{ index, board: Board ->
                board.boardNumbers.forEachIndexed { i, list ->
                    list.forEachIndexed { j, number ->
                        if (number == drawnNumber) {
                            result[index] = board.copy(
                                calledPositions = board.calledPositions.toMutableList().apply { add(Position(i, j)) }
                            )
                            return@board
                        }
                    }
                }
            }
            return result.toList()
        }

        private val winningBoardPredicate: (Board) -> Boolean = { board ->
            val hasACompletedRow: Boolean =
                board.calledPositions
                    .map { it.row }
                    .groupBy { it }
                    .any { it.value.size == 5}

            // Check lines
            val hasACompletedColumn: Boolean = board.calledPositions
                .map { it.col }
                .groupBy { it }
                .any { it.value.size == 5 }
            hasACompletedColumn || hasACompletedRow
        }
    }
}