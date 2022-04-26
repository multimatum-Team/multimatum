package com.github.multimatum_team.multimatum

/**
 * Release implementation of LogUtil functions (do nothing in release mode)
 */
object LogUtilImpl: LogUtil.FunctionsProvider {

    override fun debugLog(str: String) = doNothing()

    override fun logFunctionCall() = doNothing()

    override fun logFunctionCall(str: String) = doNothing()

    private fun doNothing() { }

}
