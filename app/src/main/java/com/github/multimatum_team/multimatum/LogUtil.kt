package com.github.multimatum_team.multimatum

import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

/**
 * Enhanced logging methods
 *
 * Among others, events logged with this method are assigned a number to identify events easier.
 * Tags are also automatically sets to name of the class containing the function in which the log
 * method is called.
 */
object LogUtil {

    /**
     * Displays the message in the logs with level 'debug', inferring the tag
     */
    fun debugLog(str: String) = safeExec {
        val currFunc = Thread.currentThread().stackTrace[STACK_IDX_FOR_ENV_FUNC]
        Log.d(createTag(currFunc), str)
    }

    /**
     * Reports that the function in which it is called has been called
     */
    fun logFunctionCall() = safeExec {
        val currFunc = Thread.currentThread().stackTrace[STACK_IDX_FOR_ENV_FUNC]
        val msg = "${currFunc.methodName} called"
        Log.d(createTag(currFunc), msg)
    }

    /**
     * Reports that the function in which it is called has been called, and displays
     * the given message
     */
    fun logFunctionCall(str: String) = safeExec {
        val currFunc = Thread.currentThread().stackTrace[STACK_IDX_FOR_ENV_FUNC]
        val msg = "${currFunc.methodName} called: $str"
        Log.d(createTag(currFunc), msg)
    }

    private fun safeExec(task: () -> Unit) {
        try {
            task()
        } catch (throwable: Throwable) {
            Log.d(TAG, "logging failed")
        }
    }

    // creates a tag with the class name of the given stack element
    private fun createTag(currFunc: StackTraceElement): String =
        "${Counter.next()} ${simpleNameOf(currFunc.className)}"

    // extracts the simple name from a complete class name
    private fun simpleNameOf(name: String): String = name.takeLastWhile { it != '.' }

    private const val TAG = "DebugUtil"

    // index in the stack to find the environment function
    private const val STACK_IDX_FOR_ENV_FUNC = 6

    // counter for the indexing of logging events
    private object Counter {
        private val atomicInteger = AtomicInteger(0)
        fun next() = atomicInteger.incrementAndGet()
    }

}