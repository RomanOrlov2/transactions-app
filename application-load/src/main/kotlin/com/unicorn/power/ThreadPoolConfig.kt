package com.unicorn.power

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.lang.Thread.sleep
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong


@Configuration
class ThreadPoolConfig {
    @Bean
    fun threadPoolTaskExecutor(): ScheduledThreadPoolExecutor {
        val threadPoolSize = 10
        val executor = ScheduledThreadPoolExecutor(threadPoolSize)
        for (i in 0 until threadPoolSize) {
            executor.scheduleAtFixedRate({
                try {
                    sleep((50 * Math.random()).roundToLong())
                    val restTemplate = RestTemplate()
                    val transaction = Transaction(1000 * Math.random(), System.currentTimeMillis())
//                    println("Сгенерирована и будет отправлена сущность $transaction")
                    restTemplate.postForEntity("http://application:8080/api/transactions", transaction, Any::class.java)
                } catch (e: Exception) {
                    println(e)
                }
            }, 0, 50, TimeUnit.MILLISECONDS)
        }
        return executor
    }
}