/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.util

import kotlinx.coroutines.*
import kotlin.coroutines.*

/**
 * Print [Job] children tree.
 */
@InternalAPI
fun Job.printDebugTree(offset: Int = 0) {
    println(" ".repeat(offset) + this)

    children.forEach {
        it.printDebugTree(offset + 2)
    }

    if (offset == 0) println()
}

@InternalAPI
@Suppress("NOTHING_TO_INLINE")
internal expect inline fun <R, A>
    (suspend R.(A) -> Unit).startCoroutineUninterceptedOrReturn3(
    receiver: R,
    arg: A,
    continuation: Continuation<Unit>
): Any?

/**
 * Supervisor with empty coroutine exception handler ignoring all exceptions.
 */
@InternalAPI
fun SilentSupervisor(parent: Job? = null): CoroutineContext = SupervisorJob() + CoroutineExceptionHandler { _, _ ->
}

/**
 * Schedule completion block to the [CoroutineScope] [CoroutineDispatcher].
 *
 * In contrast to default [Job.invokeOnCompletion] it prevents block freezing.
 */
@InternalAPI
fun CoroutineScope.scheduleCompletionBlock(block: (cause: Throwable?) -> Unit) {
    val job = coroutineContext[Job] ?: error("Can't schedule completion without job: ${coroutineContext}")

    GlobalScope.launch(coroutineContext) {
        val cause = try {
            job.join()
            null
        } catch (cause: Throwable) {
            cause
        }

        block(cause)
    }
}