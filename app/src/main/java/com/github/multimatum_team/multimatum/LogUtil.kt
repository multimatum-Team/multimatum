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

    /**
     * Displays the message in the logs with level 'debug', inferring the tag
     */
    fun debugLog(str: String) = instance.debugLog(str)

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