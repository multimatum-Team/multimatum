package com.github.multimatum_team.multimatum

/**
 * Release implementation of LogUtil functions (do nothing in release mode) <p>
 * This file is used in the release builds. The methods simply do nothing
 * because logging is not needed in a release version of the app.
 */
object LogUtilImpl: LogUtil.FunctionsProvider {

    override fun debugLog(str: String) = doNothing()

    override fun logFunctionCall() = doNothing()

    override fun logFunctionCall(str: String) = doNothing()

    private fun doNothing() { }

}
