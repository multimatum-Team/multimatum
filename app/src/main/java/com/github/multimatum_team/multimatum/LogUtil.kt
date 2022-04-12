package com.github.multimatum_team.multimatum

import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

object LogUtil {

    fun debugLog(str: String) = safeExec {
        val currFunc = Thread.currentThread().stackTrace[STACK_IDX_FOR_ENV_FUNC]
        Log.d(currFunc.className, str)
    }

    fun logFunctionCall() = safeExec {
        val currFunc = Thread.currentThread().stackTrace[STACK_IDX_FOR_ENV_FUNC]
        val msg = "${currFunc.methodName} called"
        Log.d("${Counter.next()} ${currFunc.className}", msg)
    }

    fun logFunctionCall(str: String) = safeExec {
        val currFunc = Thread.currentThread().stackTrace[STACK_IDX_FOR_ENV_FUNC]
        val msg = "${currFunc.methodName} called: $str"
        Log.d("${Counter.next()} ${currFunc.className}", msg)
    }

    private fun safeExec(task: () -> Unit){
        try {
            task()
        } catch (throwable: Throwable){
            Log.d(TAG, "logging failed")
        }
    }

    private const val TAG = "DebugUtil"
    private const val STACK_IDX_FOR_ENV_FUNC = 6

    private object Counter {
        private val atomicInteger = AtomicInteger(0)
        fun next() = atomicInteger.incrementAndGet()
    }

}