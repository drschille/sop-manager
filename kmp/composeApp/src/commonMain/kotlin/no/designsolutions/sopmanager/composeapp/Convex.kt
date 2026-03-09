package no.designsolutions.timetracker.composeapp

import com.kansson.kmp.convex.core.ConvexClient
import com.kansson.kmp.convex.core.ConvexResponse


object Convex {
    val client = ConvexClient("https://combative-mule-878.convex.cloud")

}

fun <T> ConvexResponse<T>.getOrNull(): T? =
    when (this) {
        is ConvexResponse.Success -> this.data
        is ConvexResponse.Failure -> null
    }
