package com.scr.project.commons.cinema.test

import org.awaitility.Awaitility.await
import java.util.concurrent.TimeUnit.SECONDS

@JvmSynthetic
fun awaitUntil(seconds: Long = 30, assertion: () -> Unit) = await().atMost(seconds, SECONDS).untilAsserted(assertion)