package com.example.freelancerconnect.utils

/**
 * Utility object for matching freelancer skills against job requirements.
 *
 * Skill matching is case-insensitive and whitespace-tolerant.
 */
object SkillMatcher {

    /**
     * Calculates a match score (0–100) representing how well the available
     * skills satisfy the required skills.
     *
     * @param required  Skills required by a job posting.
     * @param available Skills the freelancer has listed in their profile.
     * @return A score from 0 to 100. Returns 0 if [required] is empty.
     */
    fun score(required: List<String>, available: List<String>): Int {
        if (required.isEmpty()) return 0

        val normalizedAvailable = available
            .map { it.trim().lowercase() }
            .toSet()

        val matchCount = required.count { skill ->
            skill.trim().lowercase() in normalizedAvailable
        }

        return (matchCount * 100) / required.size
    }

    /**
     * Determines whether a freelancer is recommended for a job based on
     * their skill match score.
     *
     * @param required   Skills required by a job posting.
     * @param available  Skills the freelancer has.
     * @param threshold  Minimum score (0–100) to be considered a match. Defaults to 40.
     * @return True if the score meets or exceeds the [threshold].
     */
    fun isRecommended(
        required: List<String>,
        available: List<String>,
        threshold: Int = 40
    ): Boolean {
        return score(required, available) >= threshold
    }
}