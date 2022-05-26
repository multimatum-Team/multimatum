package com.github.multimatum_team.multimatum

/**
 * Enhanced logging methods
 *
 * Among others, events logged with this method are assigned a number to identify events easier.
 * Tags are also automatically sets to name of the class containing the function in which the log
 * method is called.
 */
object LogUtil {

    private val instance = LogUtilImpl

    /*
     * To add a new method to this class:
     * 1. declare it in FunctionsProvider
     * 2. declare it in LogUtil, with an implementation that simply calls the method on `instance`
     * 3. implement it in app/src/debug/java/com/github/multimatum_team/multimatum/LogUtilImpl.kt
     * 4. implement it as a call to doNothing in app/src/release/java/com/github/multimatum_team/multimatum/LogUtilImpl.kt
     */

    /**
     * Displays the message in the logs with level 'debug', inferring the tag
     */
    fun debugLog(str: String) = instance.debugLog(str)

    /**
     * Displays the message in the logs with level 'warning', inferring the tag
     */
    fun warningLog(str: String) = instance.warningLog(str)

    /**
     * Reports that the function in which it is called has been called
     */
    fun logFunctionCall() = instance.logFunctionCall()

    /**
     * Reports that the function in which it is called has been called, and displays
     * the given message
     */
    fun logFunctionCall(str: String) = instance.logFunctionCall(str)

    interface FunctionsProvider {
        /**
         * Displays the message in the logs with level 'debug', inferring the tag
         */
        fun debugLog(str: String)

        /**
         * Displays the message in the logs with level 'warning', inferring the tag
         */
        fun warningLog(str: String)

        /**
         * Reports that the function in which it is called has been called
         */
        fun logFunctionCall()

        /**
         * Reports that the function in which it is called has been called, and displays
         * the given message
         */
        fun logFunctionCall(str: String)
    }

}