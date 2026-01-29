package com.example.dormdeli.utils

import java.text.Normalizer
import java.util.regex.Pattern

object SearchUtils {
    fun String.normalize(): String {
        val nfdNormalizedString = Normalizer.normalize(this, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfdNormalizedString)
            .replaceAll("")
            .lowercase()
            .replace("đ", "d")
    }

    /**
     * Fuzzy search logic:
     * 1. Normalize both strings (lowercase, remove accents).
     * 2. Check if query is a substring of target.
     * 3. Check if characters of query exist in target in order.
     */
    fun fuzzyMatch(query: String, target: String): Boolean {
        if (query.isBlank()) return true
        
        val normalizedQuery = query.normalize()
        val normalizedTarget = target.normalize()
        
        // Direct substring match (case-insensitive & accent-insensitive)
        if (normalizedTarget.contains(normalizedQuery)) return true
        
        // Sequence match (fuzzy)
        var queryIdx = 0
        var targetIdx = 0
        while (queryIdx < normalizedQuery.length && targetIdx < normalizedTarget.length) {
            if (normalizedQuery[queryIdx] == normalizedTarget[targetIdx]) {
                queryIdx++
            }
            targetIdx++
        }
        
        return queryIdx == normalizedQuery.length
    }
}
