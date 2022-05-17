package com.github.multimatum_team.multimatum.util

import android.content.Intent
import com.google.android.gms.tasks.Tasks
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.internal.DynamicLinkData
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

fun mockFirebaseDynamicLinks(): FirebaseDynamicLinks {
    val dynamicLinks = Mockito.mock(FirebaseDynamicLinks::class.java)
    `when`(dynamicLinks.getDynamicLink(any<Intent>())).then {
        val intent = it.getArgument<Intent>(0)
        var dynamicLink: String? = null
        var deepLink: String? = null
        if (intent.data != null) {
            dynamicLink = intent.data.toString()
            deepLink = intent.data!!.getQueryParameter("link")!!
        }
        val dynamicLinkData = DynamicLinkData(
            dynamicLink,
            deepLink,
            0,
            0,
            null,
            null
        )
        Tasks.forResult(PendingDynamicLinkData(dynamicLinkData))
    }
    return dynamicLinks
}