package puzzle10

import java.lang.IllegalStateException
import kotlin.system.measureTimeMillis

fun main() {

    val syntaxScorer = SyntaxScorer()
    val time = measureTimeMillis {
        println("Bad line scoring: ${syntaxScorer.fixBrokenLines()}")
        println("Incomplete line scoring: ${syntaxScorer.fixIncompleteLines()}")
    }

    println("time: $time")
}

sealed interface SyntaxCharacter{
    fun produceMatchingCharacter(): SyntaxCharacter
}

enum class OpeningSyntaxCharacter: SyntaxCharacter {
    Parenthesis,
    CurlyBracket,
    AngleBracket,
    SquareBracket;

    override fun produceMatchingCharacter(): ClosingSyntaxCharacter = when(this) {
        Parenthesis -> ClosingSyntaxCharacter.Parenthesis
        CurlyBracket -> ClosingSyntaxCharacter.CurlyBracket
        AngleBracket -> ClosingSyntaxCharacter.AngleBracket
        SquareBracket -> ClosingSyntaxCharacter.SquareBracket
    }
}

enum class ClosingSyntaxCharacter: SyntaxCharacter {
    Parenthesis,
    CurlyBracket,
    AngleBracket,
    SquareBracket;

    override fun produceMatchingCharacter(): OpeningSyntaxCharacter = when(this) {
        Parenthesis -> OpeningSyntaxCharacter.Parenthesis
        CurlyBracket -> OpeningSyntaxCharacter.CurlyBracket
        AngleBracket -> OpeningSyntaxCharacter.AngleBracket
        SquareBracket -> OpeningSyntaxCharacter.SquareBracket
    }

    fun toSyntaxErrorScore(): Int = when(this) {
        Parenthesis -> 3
        CurlyBracket -> 1197
        AngleBracket -> 25137
        SquareBracket -> 57
    }

    fun toAutocompleteScore(): Int = when(this) {
        Parenthesis -> 1
        CurlyBracket -> 3
        AngleBracket -> 4
        SquareBracket -> 2
    }
}

fun Char.toSyntaxCharacter():SyntaxCharacter = when(this){
    '(' -> OpeningSyntaxCharacter.Parenthesis
    '{' -> OpeningSyntaxCharacter.CurlyBracket
    '<' -> OpeningSyntaxCharacter.AngleBracket
    '[' -> OpeningSyntaxCharacter.SquareBracket

    ')' -> ClosingSyntaxCharacter.Parenthesis
    '}' -> ClosingSyntaxCharacter.CurlyBracket
    '>' -> ClosingSyntaxCharacter.AngleBracket
    ']' -> ClosingSyntaxCharacter.SquareBracket
    else -> { throw IllegalArgumentException("unknown opening Character") }
}


class SyntaxScorer {
    private val input = SyntaxScorer::class.java.getResource("/input/puzzle10.txt")
        ?.readText()
        ?.split("\n")
        ?.map {
            it.fold(mutableListOf()) { acc: MutableList<SyntaxCharacter>, c: Char ->
                acc.apply { add(c.toSyntaxCharacter()) }
            }.toList()
        }
        ?: listOf()

    fun fixBrokenLines(): Int {
       return input.fold(mutableListOf()) { acc: MutableList<LineProblem.SyntaxErrorProblem>, line: List<SyntaxCharacter> ->
            acc.apply {
                findProblemInLine(line)
                    .takeIf { it is LineProblem.SyntaxErrorProblem }
                    ?.also { add(it as LineProblem.SyntaxErrorProblem) }
            }
        }
           .sumOf { it.illegalClosingCharacter.toSyntaxErrorScore() }
    }

    fun fixIncompleteLines(): Long {
         val lineProblems = input.fold(mutableListOf()) { acc: MutableList<LineProblem.IncompleteLineProblem>, line: List<SyntaxCharacter> ->
            acc.apply {
                findProblemInLine(line)
                    .takeIf { it is LineProblem.IncompleteLineProblem }
                    ?.also { add(it as LineProblem.IncompleteLineProblem) }
            }
        }.toList()

        val result = lineProblems.map {
            it.orphanedOpeningCharacterList
                .reversed()
                .fold(0L) { acc: Long, openingSyntaxCharacter: OpeningSyntaxCharacter ->
                    acc * 5 + openingSyntaxCharacter.produceMatchingCharacter().toAutocompleteScore()
                }
        }
            .sortedBy { it }

        return result[result.size.floorDiv(2)]
    }

    private companion object {

        sealed class LineProblem{
            data class SyntaxErrorProblem(val illegalClosingCharacter: ClosingSyntaxCharacter): LineProblem()
            data class IncompleteLineProblem(val orphanedOpeningCharacterList: List<OpeningSyntaxCharacter>): LineProblem()
        }

        private fun findProblemInLine(line: List<SyntaxCharacter>): LineProblem {
            val openingCharacters = mutableListOf<OpeningSyntaxCharacter>()
            for (character in line){
                if (character is ClosingSyntaxCharacter) {
                    if (openingCharacters.isEmpty()){
                        return LineProblem.SyntaxErrorProblem(character)
                    } else if (openingCharacters.last().produceMatchingCharacter() != character) {
                        return LineProblem.SyntaxErrorProblem(character)
                    } else {
                        openingCharacters.removeLast()
                    }
                }
                else if (character is OpeningSyntaxCharacter) {
                    openingCharacters.add(character)
                }
                else {
                    throw IllegalStateException("character: $character is neither an opening nor a closing syntax char")
                }
            }
            return LineProblem.IncompleteLineProblem(openingCharacters.toList())
        }
    }
}