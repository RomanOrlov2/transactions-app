package com.unicorn.power.service

import com.unicorn.power.data.Sample
import com.unicorn.power.data.Transaction
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigDecimal.valueOf
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.ZoneId
import java.util.Collections.singletonList
import java.util.concurrent.ConcurrentSkipListMap
import java.util.function.BiFunction

@Profile("inmemory")
@Service
class TransactionServiceImpl : TransactionService {
    private val transactions = ConcurrentSkipListMap<LocalDateTime, List<Transaction>>(LocalDateTime::compareTo)

    override fun saveTransaction(transaction: Transaction) {
        val transactionDateTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(transaction.timestamp), ZoneId.of("UTC"))
        // Если такой транзакции не существует, пытаемся её туда положить (Но параллельно могут начать её туда ложить два потока)
        val putIfAbsent = transactions.putIfAbsent(
            transactionDateTime,
            singletonList(transaction)
        )
        // Если не успели положить, или транзакция с таким временем уже есть, то мержим значения
        if (putIfAbsent != null) {
            transactions.computeIfPresent(transactionDateTime, BiFunction { t, u -> u + transaction })
        }
    }

    override fun getSample(): Sample {
        var sum = BigDecimal.ZERO
        var min = BigDecimal.valueOf(Double.MAX_VALUE)
        var max = BigDecimal.ZERO
        var count: Long = 0
        // tailMap возвращает не копию мапы, а просто хитро смотрит на слепок текущего состояния мапы.
        transactions.tailMap(now(ZoneId.of("UTC")).minusMinutes(1), true).asSequence()
            .map { it.value.asSequence() }
            .flatten()
            .forEach {
                sum = sum.add(it.amount)
                count++
                min = minOf(min, it.amount)
                max = maxOf(max, it.amount)
            }
        return Sample(sum = sum, avg = sum.divide(valueOf(count), RoundingMode.HALF_UP), max = max, min = min, count = count)
    }

    fun removeOldRecords(olderThen: LocalDateTime) {
        val headMap = transactions.headMap(olderThen, true).forEach { transactions.remove(it.key) }
    }

}