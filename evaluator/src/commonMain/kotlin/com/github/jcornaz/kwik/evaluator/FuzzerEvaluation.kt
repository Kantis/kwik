package com.github.jcornaz.kwik.evaluator

import com.github.jcornaz.kwik.fuzzer.api.Fuzzer
import com.github.jcornaz.kwik.generator.api.randomSequence
import com.github.jcornaz.kwik.simplifier.api.ExperimentalKwikFuzzer
import com.github.jcornaz.kwik.simplifier.api.findSimplestFalsification


/**
 * Call multiple times [block] with random values generated by the given [fuzzer]
 *
 * The [block] must perform assertions and throw an exception if the property is falsified.
 * Absence of exception thrown in the [block] means the property is satisfied.
 *
 * @param iterations Number of times [block] should be executed
 * @param seed Random generation seed. Random by default. Specify a value to reproduce consistent results
 * @param block Function invoked multiple times with random inputs to assess a property of the System under test.
 *                 Must return a throw an exception if the property is falsified.
 */
@ExperimentalKwikFuzzer
fun <T> forAny(
    fuzzer: Fuzzer<T>,
    iterations: Int = kwikDefaultIterations,
    seed: Long = nextSeed(),
    block: (T) -> Unit
) {
    require(iterations > 0) { "Iterations must be > 0, but was: $iterations" }

    val inputIterator = fuzzer.generator.randomSequence(seed).iterator()
    val unsatisfiedGuarantees = fuzzer.guarantees.toMutableList()
    var iterationDone = 0

    do {
        val input = inputIterator.next()

        unsatisfiedGuarantees.removeSatisfying(input)

        try {
            block(input)
        } catch (throwable: Throwable) {
            val simplerInput = fuzzer.simplifier.findSimplestFalsification(input) {
                runCatching { block(it) }.isSuccess
            }
            throw FalsifiedPropertyError(iterationDone + 1, iterations, seed, listOf(simplerInput), throwable)
        }

        ++iterationDone
    } while (iterationDone < iterations || unsatisfiedGuarantees.isNotEmpty())
}

private fun <T> MutableList<(T) -> Boolean>.removeSatisfying(input: T) {
    val iterator = listIterator()
    while (iterator.hasNext()) {
        if (iterator.next().invoke(input))
            iterator.remove()
    }
}