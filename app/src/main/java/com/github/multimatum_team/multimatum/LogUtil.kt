package com.github.multimatum_team.multimatum

import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

object LogUtil {

    fun debugLog(str: String) = safeExec {
        val currFunc = Thread.currentThread().stackTrace[STACK_IDX_FOR_ENV_FUNC]
        Log.d(createTag(currFunc), str)
    }

    fun logFunctionCall() = safeExec {
        val currFunc = Thread.currentThread().stackTrace[STACK_IDX_FOR_ENV_FUNC]
        val msg = "${currFunc.methodName} called"
        Log.d(createTag(currFunc), msg)
    }

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

    private fun createTag(currFunc: StackTraceElement): String =
        "${Counter.next()} ${simpleNameOf(currFunc.className)}"
    private fun simpleNameOf(name: String): String = name.takeLastWhile { it != '.' }

    private const val TAG = "DebugUtil"
    private const val STACK_IDX_FOR_ENV_FUNC = 6

    private object Counter {
        private val atomicInteger = AtomicInteger(0)
        fun next() = atomicInteger.incrementAndGet()
    }

}