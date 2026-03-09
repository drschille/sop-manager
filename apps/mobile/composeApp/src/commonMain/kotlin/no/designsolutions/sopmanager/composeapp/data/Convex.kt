package no.designsolutions.sopmanager.composeapp.data

import com.kansson.kmp.convex.core.ConvexClient

object Convex {
    private const val DEFAULT_CONVEX_URL = "https://dynamic-fish-439.convex.cloud"
    val client = ConvexClient(DEFAULT_CONVEX_URL)
}