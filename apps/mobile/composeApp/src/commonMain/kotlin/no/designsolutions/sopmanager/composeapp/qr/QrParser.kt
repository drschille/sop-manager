package no.designsolutions.sopmanager.composeapp.qr

object QrParser {
    fun extractPartNumber(raw: String): String? {
        val value = raw.trim()
        if (value.isEmpty()) return null

        if (!value.startsWith("myapp://part/")) {
            return value
        }

        val part = value.removePrefix("myapp://part/").trim()
        return if (part.isEmpty()) null else part
    }
}
