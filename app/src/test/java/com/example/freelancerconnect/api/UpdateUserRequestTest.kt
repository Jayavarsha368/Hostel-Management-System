package com.example.freelancerconnect.api

import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateUserRequestTest {
    @Test
    fun updateUserRequestShouldAcceptResumeAndPortfolioFiles() {
        val request = UpdateUserRequest(
            name = "Alex",
            resumeUrl = "content://resume.pdf",
            portfolioUrl = "content://portfolio.pdf",
            githubUrl = "https://github.com/example",
            linkedinUrl = "https://linkedin.com/in/example"
        )

        assertEquals("content://resume.pdf", request.resumeUrl)
        assertEquals("content://portfolio.pdf", request.portfolioUrl)
        assertEquals("https://github.com/example", request.githubUrl)
        assertEquals("https://linkedin.com/in/example", request.linkedinUrl)
    }
}
