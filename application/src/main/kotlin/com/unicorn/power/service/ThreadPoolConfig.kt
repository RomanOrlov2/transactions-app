package com.unicorn.power.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime.now
import java.time.ZoneId
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


@Configuration
class ThreadPoolConfig(@Autowired val transactionService: TransactionServiceImpl) {

    @Bean
    fun threadPoolTaskExecutor(): ScheduledThreadPoolExecutor {
        val executor = ScheduledThreadPoolExecutor(1)
        val period: Long = 2
        executor.scheduleAtFixedRate({
            transactionService.removeOldRecords(now(ZoneId.of("UTC")).minusMinutes(period))
        }, 0, period, TimeUnit.MINUTES)
        return executor
    }
}