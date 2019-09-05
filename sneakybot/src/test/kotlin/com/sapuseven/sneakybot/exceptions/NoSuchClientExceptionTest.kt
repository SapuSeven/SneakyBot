package com.sapuseven.sneakybot.exceptions

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class NoSuchClientExceptionTest {
    @Test
    fun defaultConstructor() {
        val exception = NoSuchClientException()
        Assertions.assertNotNull(exception)
    }
}